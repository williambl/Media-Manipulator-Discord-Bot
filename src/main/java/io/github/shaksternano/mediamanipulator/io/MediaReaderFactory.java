package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MediaReaderFactory<T> {

    MediaReader<T> createReader(File media) throws IOException;

    MediaReader<T> createReader(InputStream media) throws IOException;
}
