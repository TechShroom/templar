package com.techshroom.templar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.google.common.collect.AbstractIterator;

import io.netty.buffer.ByteBuf;

public abstract class BaseChunkProvider extends AbstractIterator<ByteBuf> implements ChunkProvider {

    protected final InputStream stream;
    protected volatile boolean closed;

    protected BaseChunkProvider(InputStream stream) {
        this.stream = new BufferedInputStream(stream);
    }

    @Override
    protected final ByteBuf computeNext() {
        if (closed) {
            return endOfData();
        }
        return computeNextSimple();
    }

    protected abstract ByteBuf computeNextSimple();

    @Override
    public void close() throws IOException {
        closed = true;
        stream.close();
    }

    protected final int available() {
        try {
            return stream.available();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected final int read() {
        try {
            return stream.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected final int read(byte[] b, int off, int len) {
        try {
            return stream.read(b, off, len);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
