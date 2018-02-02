package com.techshroom.templar;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles exceptions for
 * {@link #exceptionCaught(ChannelHandlerContext, Throwable)}. Some are
 * filtered, the rest are logged.
 */
@Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

    private static final ExceptionHandler INSTANCE = new ExceptionHandler();

    public static ExceptionHandler getInstance() {
        return INSTANCE;
    }

    private ExceptionHandler() {
        checkState(INSTANCE == null, "no u");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            return;
        }
        LOGGER.warn("Inbound error", cause);
    }
}
