package com.techshroom.templar;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;

@Sharable
public class HttpContentLengthFiller extends MessageToMessageEncoder<FullHttpResponse> {

    private static final HttpContentLengthFiller INSTANCE = new HttpContentLengthFiller();

    public static HttpContentLengthFiller getInstance() {
        return INSTANCE;
    }

    private HttpContentLengthFiller() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, FullHttpResponse msg, List<Object> out) throws Exception {
        msg.retain();
        if (HttpUtil.isContentLengthSet(msg)) {
            out.add(msg);
            return;
        }
        msg.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.content().readableBytes());
        out.add(msg);
    }

}
