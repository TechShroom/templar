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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    private final EventLoopGroup appLoop = new NioEventLoopGroup(Environment.WORKER_THREAD_COUNT.get(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("worker-thread-%s").build());
    private final HttpRouterHandler router;

    public HttpInitializer(HttpRouterHandler router) {
        this.router = router;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();

        // http at the top
        pipe.addLast(new HttpServerCodec());

        // GZIP
        pipe.addLast(new HttpContentCompressor());
        pipe.addLast(new HttpContentDecompressor());

        // collect into a single object
        pipe.addLast(new HttpObjectAggregator(Environment.MAX_CONTENT_LENGTH.get()));

        // prime Content-length if not yet done
        pipe.addLast(HttpContentLengthFiller.getInstance());

        // split streaming bodies into chunks
        pipe.addLast(InputStreamChunker.getInstance());

        pipe.addLast(new LoggingHandler("pre-codec-logger", LogLevel.DEBUG));

        // encode/decode to lettar classes
        pipe.addLast(LettarCodec.getInstance());

        pipe.addLast(new LoggingHandler("pre-router-logger", LogLevel.DEBUG));

        // weird error handler
        pipe.addLast(new LastDitchErrorLogger());

        // route (in application loop)
        pipe.addLast(appLoop, router);

        // errors (if any)
        pipe.addLast(ExceptionHandler.getInstance());
    }

    public void shutdown() {
        appLoop.shutdownGracefully();
    }

}
