package io.github.shaksternano.mediamanipulator.io.mediareader;

import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public class ScrimageGifReader extends BaseMediaReader<BufferedImage> {

    private final List<Frame> frames = new ArrayList<>();
    private int currentIndex = 0;
    private long currentTimestamp = 0;

    public ScrimageGifReader(File input) throws IOException {
        this(AnimatedGifReader.read(ImageSource.of(input)));
    }

    public ScrimageGifReader(InputStream input) throws IOException {
        this(AnimatedGifReader.read(ImageSource.of(input)));
    }

    private ScrimageGifReader(AnimatedGif gif) throws IOException {
        if (gif.getFrameCount() <= 0) {
            throw new IOException("Could not read any frames!");
        }
        long shortestDuration = Long.MAX_VALUE;
        long totalDuration = 0;
        Set<Long> timestamps = new HashSet<>();
        for (int i = 0; i < gif.getFrameCount(); i++) {
            if (!timestamps.add(totalDuration)) {
                throw new IllegalStateException("Duplicate timestamp found: " + totalDuration);
            }
            BufferedImage image = gif.getFrame(i).awt();
            frames.add(new Frame(image, totalDuration));
            long duration = gif.getDelay(i).toMillis() * 1000;
            shortestDuration = Math.min(shortestDuration, duration);
            totalDuration += duration;
        }
        frameRate = 1000.0 / shortestDuration;
        frameCount = gif.getFrameCount();
        duration = totalDuration;
        frameDuration = shortestDuration;
        Dimension dimension = gif.getDimensions();
        width = dimension.width;
        height = dimension.height;
    }

    @Nullable
    @Override
    public BufferedImage getNextFrame() {
        if (currentIndex >= frames.size()) {
            return null;
        } else {
            Frame frame = frames.get(currentIndex);
            BufferedImage image = frame.image();
            currentIndex++;
            currentTimestamp = frame.timestamp();
            return image;
        }
    }

    @Override
    public long getTimestamp() {
        return currentTimestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        if (timestamp > duration) {
            throw new IllegalArgumentException("Timestamp must not be greater than the duration");
        }
        currentTimestamp = timestamp;
        currentIndex = findIndex(timestamp, frames);
    }

    /**
     * Finds the index of the frame with the given timestamp.
     * If there is no frame with the given timestamp, the index of the frame
     * with the highest timestamp smaller than the given timestamp is returned.
     *
     * @param timeStamp The timestamp in microseconds.
     * @param frames    The frames.
     * @return The index of the frame with the given timestamp.
     */
    private static int findIndex(long timeStamp, List<Frame> frames) {
        if (frames.size() == 0) {
            throw new IllegalArgumentException("Frames list is empty");
        } else if (timeStamp < 0) {
            throw new IllegalArgumentException("Timestamp must not be negative");
        } else if (timeStamp < frames.get(0).timestamp()) {
            throw new IllegalArgumentException("Timestamp must not be smaller than the first timestamp");
        } else if (timeStamp == frames.get(0).timestamp()) {
            return 0;
        } else if (timeStamp < frames.get(frames.size() - 1).timestamp()) {
            return findIndexBinarySearch(timeStamp, frames);
        } else {
            // If the timestamp is equal to or greater than the last timestamp.
            return frames.size() - 1;
        }
    }

    private static int findIndexBinarySearch(long timeStamp, List<Frame> frames) {
        int low = 0;
        int high = frames.size() - 1;
        while (low <= high) {
            int mid = low + ((high - low) / 2);
            if (frames.get(mid).timestamp() == timeStamp
                || (frames.get(mid).timestamp() < timeStamp
                && frames.get(mid + 1).timestamp() > timeStamp)) {
                return mid;
            } else if (frames.get(mid).timestamp() < timeStamp) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        throw new IllegalStateException("This should never be reached");
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Iterator<BufferedImage> iterator() {
        return frames.stream()
            .map(Frame::image)
            .iterator();
    }

    private record Frame(BufferedImage image, long timestamp) {
    }

    public enum Factory implements MediaReaderFactory<BufferedImage> {

        INSTANCE;

        @Override
        public MediaReader<BufferedImage> createReader(File media) throws IOException {
            return new ScrimageGifReader(media);
        }

        @Override
        public MediaReader<BufferedImage> createReader(InputStream media) throws IOException {
            return new ScrimageGifReader(media);
        }
    }
}
