package com.techshroom.templar;

import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LineChunkProvider extends BaseChunkProvider {

    private final int MAX_LINES_PER_CHUNK = 512;

    private final ByteBuf outputBuffer = Unpooled.buffer();
    private boolean sentEofChunk = false;

    public LineChunkProvider(InputStream stream) {
        super(stream);
    }

    @Override
    protected ByteBuf computeNextSimple() {
        int available = available();
        int lastLine = -1;
        int lines = 0;
        while ((lastLine == -1 || available > 0) && lines <= MAX_LINES_PER_CHUNK) {
            int next = read();
            if (next == -1) {
                // EOF

                // don't send empty chunks
                if (sentEofChunk || outputBuffer.readableBytes() == 0) {
                    return endOfData();
                }
                sentEofChunk = true;
                break;
            }

            // decrement available bytes we think we have
            available--;

            outputBuffer.writeByte(next);

            if (next == '\n') {
                // mark as "done", check available bytes for more lines
                lastLine = outputBuffer.readableBytes();
                available = available();
                lines++;
            }
        }
        ByteBuf copy = outputBuffer.copy(0, lastLine);
        // move data after the last line to the new position
        int numBytes = outputBuffer.readableBytes() - lastLine;
        outputBuffer.writerIndex(0);
        outputBuffer.writeBytes(outputBuffer, lastLine, numBytes);
        return copy;
    }

}
