package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class JavaxImageReader extends BaseMediaReader<ImageFrame> {

    private final ImageFrame image;

    public JavaxImageReader(File input, String format) throws IOException {
        this(ImageIO.read(input), format);
    }

    public JavaxImageReader(InputStream input, String format) throws IOException {
        this(ImageIO.read(input), format);
        input.close();
    }

    private JavaxImageReader(BufferedImage image, String format) {
        super(format);
        this.image = new ImageFrame(image, 0, 0);
        frameCount = 1;
        duration = 1;
        frameRate = 1;
        frameDuration = 1;
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public ImageFrame frame(long timestamp) {
        return image;
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Iterator<ImageFrame> iterator() {
        return List.of(image).iterator();
    }

    @Override
    public void forEach(Consumer<? super ImageFrame> action) {
        action.accept(image);
    }

    @Override
    public Spliterator<ImageFrame> spliterator() {
        return List.of(image).spliterator();
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }
    }
}
