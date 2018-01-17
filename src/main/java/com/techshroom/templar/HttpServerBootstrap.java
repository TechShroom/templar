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
