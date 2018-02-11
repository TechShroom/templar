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
