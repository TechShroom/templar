package com.techshroom.templar;

import com.techshroom.lettar.Router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpRouterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Router<ByteBuf, Object> router;

    public HttpRouterHandler(Router<ByteBuf, Object> router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        ctx.write(router.route(FullestRequest.wrap(msg)));
    }

}
