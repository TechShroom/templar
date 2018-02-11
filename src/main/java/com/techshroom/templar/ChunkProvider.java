package com.techshroom.templar;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import io.netty.buffer.ByteBuf;

public interface ChunkProvider extends Iterator<ByteBuf>, Closeable {

    @Override
    void close() throws IOException;

}
