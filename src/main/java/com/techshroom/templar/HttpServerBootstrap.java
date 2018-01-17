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

import java.util.function.Supplier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServerBootstrap {

    private static final Environment ENV = Environment.getInstance();

    private final String bindAddress;
    private final int port;
    private final Supplier<HttpHandler> httpHandler;

    public HttpServerBootstrap(String bindAddress, int port, Supplier<HttpHandler> httpHandler) {
        this.bindAddress = bindAddress;
        this.port = port;
        this.httpHandler = httpHandler;
    }

    public void start() {
        EventLoopGroup accLoop = new NioEventLoopGroup(ENV.ACCEPT_THREAD_COUNT,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("accept-thread-%s").build());
        EventLoopGroup ioLoop = new NioEventLoopGroup(ENV.IO_THREAD_COUNT,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("io-thread-%s").build());
        HttpHandler handler = httpHandler.get();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .localAddress(bindAddress, port)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(handler)
                .group(accLoop, ioLoop)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .validate();

        ChannelFuture serverFuture = null;
        try {
            serverFuture = bootstrap.bind().sync();
            serverFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            if (serverFuture != null) {
                serverFuture.channel().close();
            }
            Thread.currentThread().interrupt();
        } finally {
            accLoop.shutdownGracefully();
            ioLoop.shutdownGracefully();
            handler.shutdown();
        }
    }

}
