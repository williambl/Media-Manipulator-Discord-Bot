package io.github.shaksternano.borgar.media;

import com.google.common.io.Files;
import io.github.shaksternano.borgar.exception.FailedOperationException;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.media.io.MediaReaders;
import io.github.shaksternano.borgar.media.io.MediaWriters;
import io.github.shaksternano.borgar.media.io.imageprocessor.BasicImageProcessor;
import io.github.shaksternano.borgar.media.io.imageprocessor.DualImageProcessor;
import io.github.shaksternano.borgar.media.io.imageprocessor.IdentityProcessor;
import io.github.shaksternano.borgar.media.io.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.borgar.media.io.reader.MediaReader;
import io.github.shaksternano.borgar.media.io.reader.ZippedMediaReader;
import io.github.shaksternano.borgar.util.CompletableFutureUtil;
import io.github.shaksternano.borgar.util.collect.MappedList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MediaUtil {
    public static ExecutorService MEDIA_PROCESSING_EXECUTOR = Executors.newCachedThreadPool();

    public static File processMedia(
        File input,
        String outputFormat,
        String resultName,
        UnaryOperator<BufferedImage> imageMapper,
        long maxFileSize
    ) throws IOException {
        return processMedia(
            input,
            outputFormat,
            resultName,
            new BasicImageProcessor(imageMapper),
            maxFileSize
        );
    }

    public static File processMedia(
        File input,
        String outputFormat,
        String resultName,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(input, output, outputFormat, processor, maxFileSize);
    }

    public static File processMedia(
        File input,
        File output,
        String outputFormat,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        return processMedia(
            input,
            outputFormat,
            output,
            outputFormat,
            processor,
            maxFileSize
        );
    }

    public static File processMedia(
        File input,
        String inputFormat,
        File output,
        String outputFormat,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var imageReader = MediaReaders.createImageReader(input, inputFormat);
        var audioReader = MediaReaders.createAudioReader(input, inputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor, maxFileSize);
    }

    public static File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        String outputFormat,
        String resultName,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor, maxFileSize);
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        File output,
        String outputFormat,
        SingleImageProcessor<T> processor,
        long maxFileSize
    ) throws IOException {
        if (processor.speed() < 0) {
            imageReader = imageReader.reversed();
            audioReader = audioReader.reversed();
        }
        try (
            var finalImageReader = imageReader;
            var finalAudioReader = audioReader;
            processor
        ) {
            var outputSize = Long.MAX_VALUE;
            var resizeRatio = 1F;
            var maxResizeAttempts = 3;
            var attempts = 0;
            do {
                try (
                    var imageIterator = finalImageReader.iterator();
                    var audioIterator = finalAudioReader.iterator();
                    var writer = MediaWriters.createWriter(
                        output,
                        outputFormat,
                        finalImageReader.loopCount(),
                        finalAudioReader.audioChannels(),
                        finalAudioReader.audioSampleRate(),
                        finalAudioReader.audioBitrate(),
                        maxFileSize,
                        finalImageReader.duration()
                    )
                ) {
                    T constantFrameDataValue = null;
                    List<CompletableFuture<ImageFrame>> imageFrames = new ArrayList<>();
                    while (imageIterator.hasNext()) {
                        var imageFrame = imageIterator.next();
                        if (constantFrameDataValue == null) {
                            constantFrameDataValue = processor.constantData(imageFrame.content());
                        }
                        T finalConstantFrameDataValue = constantFrameDataValue;
                        float finalResizeRatio = resizeRatio;
                        imageFrames.add(CompletableFutureUtil.supplyAsyncIO(() -> imageFrame.transform(
                            ImageUtil.resize(
                                    processor.transformImage(imageFrame, finalConstantFrameDataValue),
                                    finalResizeRatio
                            ),
                            processor.absoluteSpeed()
                        ), MEDIA_PROCESSING_EXECUTOR));
                        if (writer.isStatic()) {
                            break;
                        }
                    }

                    for (var iter = CompletableFutureUtil.joinAll(imageFrames).iterator(); iter.hasNext();) {
                        writer.writeImageFrame(iter.next());
                    }

                    if (writer.supportsAudio()) {
                        List<CompletableFuture<AudioFrame>> audioFrames = new ArrayList<>();
                        while (audioIterator.hasNext()) {
                            var audioFrame = audioIterator.next();
                            audioFrames.add(CompletableFuture.supplyAsync(() -> audioFrame.transform(processor.absoluteSpeed()), MEDIA_PROCESSING_EXECUTOR));
                        }

                        for (var iter = CompletableFutureUtil.joinAll(audioFrames).iterator(); iter.hasNext();) {
                            writer.writeAudioFrame(iter.next());
                        }
                    }
                }
                outputSize = output.length();
                resizeRatio = Math.min((float) maxFileSize / outputSize, 0.9F);
                attempts++;
            } while (maxFileSize > 0 && outputSize > maxFileSize && attempts < maxResizeAttempts);
            return output;
        }
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader1,
        MediaReader<AudioFrame> audioReader1,
        MediaReader<ImageFrame> imageReader2,
        String outputFormat,
        String resultName,
        DualImageProcessor<T> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        var zippedImageReader = new ZippedMediaReader<>(imageReader1, imageReader2);
        if (processor.speed() < 0) {
            zippedImageReader = zippedImageReader.reversed();
            audioReader1 = audioReader1.reversed();
        }
        try (
            var finalZippedImageReader = zippedImageReader;
            var finalAudioReader = audioReader1;
            processor
        ) {
            var outputSize = Long.MAX_VALUE;
            var resizeRatio = 1F;
            var maxResizeAttempts = 3;
            var attempts = 0;
            do {
                try (
                    var zippedImageIterator = finalZippedImageReader.iterator();
                    var audioIterator = finalAudioReader.iterator();
                    var writer = MediaWriters.createWriter(
                        output,
                        outputFormat,
                        finalZippedImageReader.loopCount(),
                        finalAudioReader.audioChannels(),
                        finalAudioReader.audioSampleRate(),
                        finalAudioReader.audioBitrate(),
                        maxFileSize,
                        finalZippedImageReader.duration())
                ) {
                    List<CompletableFuture<ImageFrame>> imageFrames = new ArrayList<>();
                    T constantFrameDataValue = null;
                    while (zippedImageIterator.hasNext()) {
                        var framePair = zippedImageIterator.next();
                        var firstFrame = framePair.first();
                        var secondFrame = framePair.second();
                        if (constantFrameDataValue == null) {
                            constantFrameDataValue = processor.constantData(firstFrame.content(), secondFrame.content());
                        }
                        var toTransform = finalZippedImageReader.isFirstControlling()
                            ? firstFrame
                            : secondFrame;
                        float finalResizeRatio = resizeRatio;
                        T finalConstantFrameDataValue = constantFrameDataValue;
                        imageFrames.add(CompletableFutureUtil.supplyAsyncIO(() -> toTransform.transform(
                            ImageUtil.resize(
                                    processor.transformImage(firstFrame, secondFrame, finalConstantFrameDataValue),
                                    finalResizeRatio
                            ),
                            processor.absoluteSpeed()
                        ), MEDIA_PROCESSING_EXECUTOR));
                        if (writer.isStatic()) {
                            break;
                        }
                    }

                    for (var iter = CompletableFutureUtil.joinAll(imageFrames).iterator(); iter.hasNext();) {
                        writer.writeImageFrame(iter.next());
                    }

                    if (writer.supportsAudio()) {
                        List<CompletableFuture<AudioFrame>> audioFrames = new ArrayList<>();
                        while (audioIterator.hasNext()) {
                            var audioFrame = audioIterator.next();
                            audioFrames.add(CompletableFuture.supplyAsync(() -> audioFrame.transform(processor.absoluteSpeed()), MEDIA_PROCESSING_EXECUTOR));
                        }

                        for (var iter = CompletableFutureUtil.joinAll(audioFrames).iterator(); iter.hasNext();) {
                            writer.writeAudioFrame(iter.next());
                        }
                    }
                }
                outputSize = output.length();
                resizeRatio = Math.min((float) maxFileSize / outputSize, 0.9F);
                attempts++;
            } while (maxFileSize > 0 && outputSize > maxFileSize && attempts < maxResizeAttempts);
            return output;
        }
    }

    public static File cropMedia(
        File media,
        String outputFormat,
        String resultName,
        Function<BufferedImage, Rectangle> cropKeepAreaFinder,
        long maxFileSize,
        String failureMessage
    ) throws IOException {
        try (
            var reader = MediaReaders.createImageReader(media, outputFormat);
            var iterator = reader.iterator()
        ) {
            var width = -1;
            var height = -1;
            List<CompletableFuture<Rectangle>> toKeepAreas = new ArrayList<>();
            while (iterator.hasNext()) {
                var frame = iterator.next();
                var image = frame.content();
                if (width < 0) {
                    width = image.getWidth();
                    height = image.getHeight();
                }

                int finalWidth = width;
                int finalHeight = height;
                toKeepAreas.add(CompletableFuture.supplyAsync(() -> {
                    var mayKeepArea = cropKeepAreaFinder.apply(image);
                    if ((mayKeepArea.getX() != 0
                            || mayKeepArea.getY() != 0
                            || mayKeepArea.getWidth() != finalWidth
                            || mayKeepArea.getHeight() != finalHeight)
                            && mayKeepArea.getWidth() > 0
                            && mayKeepArea.getHeight() > 0
                    ) {
                        return mayKeepArea;
                    }
                    return null;
                }, MEDIA_PROCESSING_EXECUTOR));
            }

            CompletableFuture.allOf(toKeepAreas.toArray(CompletableFuture[]::new)).join();
            var toKeep = toKeepAreas.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .reduce(Rectangle::union)
                    .orElse(null);

            if (toKeep == null
                || (toKeep.getX() == 0
                && toKeep.getY() == 0
                && toKeep.getWidth() == width
                && toKeep.getHeight() == height
            )) {
                throw new FailedOperationException(failureMessage);
            } else {
                return processMedia(
                    media,
                    outputFormat,
                    resultName,
                    image -> image.getSubimage(
                        toKeep.x,
                        toKeep.y,
                        toKeep.width,
                        toKeep.height
                    ),
                    maxFileSize
                );
            }
        }
    }

    public static File transcode(
        File input,
        String fileName,
        String inputFormat,
        String outputFormat,
        long maxFileSize
    ) throws IOException {
        var nameWithoutExtension = Files.getNameWithoutExtension(fileName);
        var output = FileUtil.createTempFile(nameWithoutExtension, outputFormat);
        return processMedia(
            input,
            inputFormat,
            output,
            outputFormat,
            IdentityProcessor.INSTANCE,
            maxFileSize
        );
    }

    public static String equivalentTransparentFormat(String format) {
        if (isJpg(format)) {
            return "png";
        } else {
            return format;
        }
    }

    public static int supportedTransparentImageType(BufferedImage image, String format) {
        return supportsTransparency(format) ? BufferedImage.TYPE_INT_ARGB : ImageUtil.getType(image);
    }

    public static boolean supportsTransparency(String format) {
        return equalsIgnoreCaseAny(format,
            "bmp",
            "png",
            "gif",
            "tif",
            "tiff"
        );
    }

    private static boolean isJpg(String format) {
        return equalsIgnoreCaseAny(format,
            "jpg",
            "jpeg"
        );
    }

    public static boolean isStaticOnly(String format) {
        return equalsIgnoreCaseAny(format,
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "tif",
            "tiff"
        );
    }

    private static boolean equalsIgnoreCaseAny(String string, String... toCompare) {
        for (var compare : toCompare) {
            if (string.equalsIgnoreCase(compare)) {
                return true;
            }
        }
        return false;
    }

    public static <E extends VideoFrame<?, E>> E frameAtTime(long timestamp, List<E> frames, long duration) {
        var circularTimestamp = timestamp % Math.max(duration, 1);
        var index = findIndex(circularTimestamp, new MappedList<>(frames, VideoFrame::timestamp));
        return frames.get(index);
    }

    /**
     * Finds the index of the frame with the given timestamp.
     * If there is no frame with the given timestamp, the index of the frame
     * with the highest timestamp smaller than the given timestamp is returned.
     *
     * @param timeStamp  The timestamp in microseconds.
     * @param timestamps The frame timestamps.
     * @return The index of the frame with the given timestamp.
     */
    public static int findIndex(long timeStamp, List<? extends Number> timestamps) {
        if (timestamps.isEmpty()) {
            throw new IllegalArgumentException("Timestamp list is empty");
        } else if (timeStamp < 0) {
            throw new IllegalArgumentException("Timestamp must not be negative");
        } else if (timeStamp < timestamps.get(0).doubleValue()) {
            throw new IllegalArgumentException("Timestamp must not be smaller than the first timestamp");
        } else if (timeStamp == timestamps.get(0).doubleValue()) {
            return 0;
        } else if (timeStamp < timestamps.get(timestamps.size() - 1).doubleValue()) {
            return findIndexBinarySearch(timeStamp, timestamps);
        } else {
            // If the timestamp is equal to or greater than the last timestamp.
            return timestamps.size() - 1;
        }
    }

    private static int findIndexBinarySearch(long timeStamp, List<? extends Number> timestamps) {
        var low = 0;
        var high = timestamps.size() - 1;
        while (low <= high) {
            var mid = low + ((high - low) / 2);
            if (timestamps.get(mid).doubleValue() == timeStamp
                || (timestamps.get(mid).doubleValue() < timeStamp
                && timestamps.get(mid + 1).doubleValue() > timeStamp)) {
                return mid;
            } else if (timestamps.get(mid).doubleValue() < timeStamp) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        throw new IllegalStateException("This should never be reached. Timestamp: " + timeStamp + ", all timestamps: " + timestamps);
    }
}
