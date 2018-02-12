/*
 * This file is part of templar, licensed under the MIT License (MIT).
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

import static com.google.common.base.Preconditions.checkState;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;

@Sharable
public class InputStreamChunker extends ChannelOutboundHandlerAdapter {

    private static final InputStreamChunker INSTANCE = new InputStreamChunker();

    public static InputStreamChunker getInstance() {
        return INSTANCE;
    }

    private InputStreamChunker() {
        checkState(INSTANCE == null, "no u");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof LettarChunkRequest) {
            chunk(ctx, ((LettarChunkRequest) msg).chunkProvider, promise);
            return;
        }
        super.write(ctx, msg, promise);
    }

    private void chunk(ChannelHandlerContext ctx, ChunkProvider chunks, ChannelPromise promise) {
        // one initial flush for headers
        ctx.flush();
        while (chunks.hasNext()) {
            ByteBuf chunk = chunks.next();
            HttpContent content;
            if (!chunks.hasNext()) {
                content = new DefaultLastHttpContent(chunk);
            } else {
                content = new DefaultHttpContent(chunk);
            }
            ctx.writeAndFlush(content)
                    .addListener((ChannelFuture future) -> {
                        if (!future.isSuccess()) {
                            chunks.close();
                            future.channel().pipeline().fireExceptionCaught(future.cause());
                        }
                    });
        }
        promise.setSuccess();
    }

}
