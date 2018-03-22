/*
 * This file is part of templar-core, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.templar;

import java.io.InputStream;

import com.techshroom.templar.util.Pooled;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

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
        return Pooled.copiedBuffer(PooledByteBufAllocator.DEFAULT, buffer, 0, index);
    }

}
