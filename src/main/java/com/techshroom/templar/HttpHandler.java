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

public class HttpHandler extends ChannelInitializer<SocketChannel> {

    private static final Environment ENV = Environment.getInstance();

    private final EventLoopGroup appLoop = new NioEventLoopGroup(ENV.WORKER_THREAD_COUNT,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("worker-thread-%s").build());
    private final HttpRouterHandler router;

    public HttpHandler(HttpRouterHandler router) {
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
        pipe.addLast(new HttpObjectAggregator(ENV.MAX_CONTENT_LENGTH));

        // route (in application loop)
        pipe.addLast(appLoop, router);
    }

    public void shutdown() {
        appLoop.shutdownGracefully();
    }

}
