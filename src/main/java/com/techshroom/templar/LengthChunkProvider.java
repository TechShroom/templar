package com.techshroom.templar;

import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LengthChunkProvider extends BaseChunkProvider {

    private static final int DEFAULT_CHUNK_SIZE = 8192;

    private final byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
    private boolean sentEofChunk = false;

    public LengthChunkProvider(InputStream stream) {
        super(stream);
    }

    @Override
    protected ByteBuf computeNextSimple() {
        int index = 0;
        while (index < buffer.length) {
            int read = read(buffer, index, buffer.length - index);
            if (read == -1) {
                // EOF

                // don't send empty chunks
                if (sentEofChunk || index == 0) {
                    return endOfData();
                }
                sentEofChunk = true;
                break;
            }
            index += read;
        }
        return Unpooled.copiedBuffer(buffer, 0, index);
    }

}
