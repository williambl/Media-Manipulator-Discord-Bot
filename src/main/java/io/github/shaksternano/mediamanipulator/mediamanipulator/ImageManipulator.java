package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaderRegistry;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.image.util.ImageMediaBuilder;
import io.github.shaksternano.mediamanipulator.image.writer.util.ImageWriterRegistry;
import io.github.shaksternano.mediamanipulator.image.writer.util.ImageWriters;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.CollectionUtil;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A manipulator that works with image based media.
 */
public class ImageManipulator implements MediaManipulator {

    private static final Set<String> ANIMATED_IMAGE_FORMATS = ImmutableSet.of(
        "gif"
    );

    private static File animatedOnlyOperation(File media, String fileFormat, Function<ImageMedia, ImageMedia> operation, String operationName, String staticImageErrorMessage) throws IOException {
        if (ANIMATED_IMAGE_FORMATS.contains(fileFormat.toLowerCase())) {
            ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);

            if (imageMedia.isAnimated()) {
                ImageMedia result = operation.apply(imageMedia);
                File output = FileUtil.getUniqueTempFile(operationName + "." + fileFormat);
                ImageWriters.write(result, output, fileFormat);
                return output;
            } else {
                throw new UnsupportedFileFormatException(staticImageErrorMessage);
            }
        } else {
            throw new UnsupportedFileFormatException(staticImageErrorMessage);
        }
    }

    @Override
    public File resize(File media, String fileFormat, float resizeMultiplier, boolean raw, boolean rename) throws IOException {
        return applyToEachFrame(media, fileFormat, null, image -> ImageUtil.resize(image, resizeMultiplier, raw), rename ? "resized" : null);
    }

    @Override
    public File reduceFps(File media, String fileFormat, int fpsReductionRatio, boolean rename) throws IOException {
        return animatedOnlyOperation(
            media,
            fileFormat,
            imageMedia -> MediaCompression.removeFrames(imageMedia, fpsReductionRatio),
            rename ? "reduced_fps" : null,
            "Cannot reduce the FPS of a static image."
        );
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public File spin(File media, String fileFormat, float speed, @Nullable Color backgroundColor) throws IOException {
        ImageMedia image = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> keptImages = image.toNormalisedImages();

        image = null;

        BufferedImage firstFrame = keptImages.get(0);

        int maxDimension = Math.max(firstFrame.getWidth(), firstFrame.getHeight());
        float absoluteSpeed = Math.abs(speed);

        int framesPerRotation = 150;
        if (absoluteSpeed >= 1) {
            framesPerRotation = Math.max((int) (framesPerRotation / absoluteSpeed), 1);
        }

        int size = framesPerRotation * ((keptImages.size() + (framesPerRotation - 1)) / framesPerRotation);
        Map<Integer, Frame> indexedFrames = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            int duration = Frame.GIF_MINIMUM_FRAME_DURATION;
            if (absoluteSpeed < 1) {
                duration /= absoluteSpeed;
            }

            indexedFrames.put(i, new AwtFrame(keptImages.get(i % keptImages.size()), duration));
        }

        keptImages = null;

        final int finalFramesPerRotation = framesPerRotation;
        List<Frame> rotatedFrames = indexedFrames.entrySet().parallelStream().map(frameEntry -> {
            float index = frameEntry.getKey();
            Frame originalFrame = frameEntry.getValue();
            BufferedImage originalImage = originalFrame.getImage();
            float angle = 360 * (index / finalFramesPerRotation);

            if (speed < 0) {
                angle = -angle;
            }

            BufferedImage rotatedImage = ImageUtil.rotate(originalImage, angle, maxDimension, maxDimension, backgroundColor, BufferedImage.TYPE_INT_ARGB);
            Frame frame = new AwtFrame(rotatedImage, originalFrame.getDuration());
            originalFrame.flush();
            return frame;
        }).collect(ImmutableList.toImmutableList());
        ImageMedia rotatedImage = ImageMediaBuilder.fromCollection(rotatedFrames);

        File outputFile = FileUtil.getUniqueTempFile("spun.gif");

        ImageWriters.write(rotatedImage, outputFile, "gif");

        return outputFile;
    }

    @Override
    public File makeGif(File media, String fileFormat, boolean justRenameFile) throws IOException {
        if (com.google.common.io.Files.getFileExtension(media.getName()).equalsIgnoreCase("gif")) {
            throw new UnsupportedFileFormatException("This file is already a GIF file!");
        } else {
            File gifFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "gif"));

            if (justRenameFile) {
                Files.move(media.toPath(), gifFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                ImageMedia nonGifImage = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
                ImageWriters.write(nonGifImage, gifFile, "gif");
            }

            return gifFile;
        }
    }

    @Override
    public File makePngAndTransparent(File media, String fileFormat) throws IOException {
        File pngFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "png"));
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
        ImageWriters.write(imageMedia, pngFile, "png");
        return pngFile;
    }

    @Override
    public File makeIco(File media, String fileFormat) throws IOException {
        File icoFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "ico"));
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        ImageWriters.write(imageMedia, icoFile, "ico");
        return icoFile;
    }

    @Override
    public File compress(File media, String fileFormat, @Nullable Guild guild) throws IOException {
        if (media.length() > DiscordUtil.getMaxUploadSize(guild)) {
            boolean reduceResolution = true;
            while (media.length() > DiscordUtil.getMaxUploadSize(guild)) {
                if (reduceResolution || !ANIMATED_IMAGE_FORMATS.contains(fileFormat)) {
                    media = resize(media, fileFormat, 0.75F, false, false);
                } else {
                    media = reduceFps(media, fileFormat, 2, false);
                }

                BufferedImage image = ImageReaders.read(media, fileFormat, null).getFirstImage();
                if (image.getWidth() <= DiscordUtil.DISCORD_MAX_DISPLAY_WIDTH || image.getHeight() <= DiscordUtil.DISCORD_MAX_DISPLAY_HEIGHT) {
                    reduceResolution = !reduceResolution;
                }
                image.flush();
            }

        }

        return media;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> readerFormats = ImageReaderRegistry.getSupportedFormats();
        Set<String> writerFormats = ImageWriterRegistry.getSupportedFormats();
        return CollectionUtil.intersection(readerFormats, writerFormats);
    }

    /**
     * Applies the given operation to the given image based file.
     *
     * @param media                The image based file to apply the operation to.
     * @param inputFormat          The image format of the file being edited.
     * @param outputFormatFunction A bi-function that takes the input format and the and image media and returns the output format.
     *                             If null, the input format will be used as the output format.
     * @param operation            The operation to apply.
     * @param operationName        The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    private static File applyToEachFrame(File media, String inputFormat, @Nullable BiFunction<String, ImageMedia, String> outputFormatFunction, Function<BufferedImage, BufferedImage> operation, @Nullable String operationName) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, inputFormat, null);

        ImageMedia outputImage = ImageMediaBuilder.fromCollection(imageMedia.parallelStream().map(frame -> {
            BufferedImage unmodifiedImage = frame.getImage();
            BufferedImage modifiedImage = operation.apply(unmodifiedImage);
            int duration = frame.getDuration();
            frame.flush();
            return new AwtFrame(modifiedImage, duration);
        }).collect(ImmutableList.toImmutableList()));

        String outputExtension = com.google.common.io.Files.getFileExtension(media.getName());
        String outputName;
        if (operationName == null) {
            outputName = FileUtil.changeExtension(media.getName(), outputExtension);
        } else {
            outputName = operationName + '.' + outputExtension;
        }

        String outputFormat = outputFormatFunction == null ? inputFormat : outputFormatFunction.apply(inputFormat, imageMedia);
        File output = FileUtil.getUniqueTempFile(outputName);
        ImageWriters.write(outputImage, output, outputFormat);

        return output;
    }
}
