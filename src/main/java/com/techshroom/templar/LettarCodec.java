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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.techshroom.lettar.Response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@Sharable
public class LettarCodec extends MessageToMessageCodec<FullHttpRequest, Response<Object>> {

    private static final LettarCodec INSTANCE = new LettarCodec();

    public static LettarCodec getInstance() {
        return INSTANCE;
    }

    private LettarCodec() {
        checkState(INSTANCE == null, "no u");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Response<Object> msg, List<Object> out) throws Exception {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        msg.getHeaders().getMultimap().forEach(headers::add);
        // the response could have any junk
        // under most circumstances it will be InputStream or ByteBuf
        // but some errors come as Object or String
        ByteBuf body;
        if (msg.getBody() instanceof InputStream) {
            handleStreaming(msg, out, headers);
            return;
        } else if (msg.getBody() instanceof ByteBuf) {
            body = (ByteBuf) msg.getBody();
        } else {
            body = Unpooled.copiedBuffer(String.valueOf(msg.getBody()), StandardCharsets.UTF_8);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(msg.getStatusCode()),
                body,
                headers,
                EmptyHttpHeaders.INSTANCE);
        out.add(response);
    }

    private void handleStreaming(Response<Object> msg, List<Object> out, DefaultHttpHeaders headers) {
        // write out headers, etc. immediately
        DefaultHttpResponse httpMessage = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(msg.getStatusCode()),
                headers);
        // ensure chunked (but do NOT kill length headers!)
        httpMessage.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
        out.add(httpMessage);

        InputStream stream = (InputStream) msg.getBody();
        ChunkProvider chunkProvider;
        if (msg.getHeaders().getSingleValueOrDefault("content-type", "text/plain")
                .equals("application/octet-stream")) {
            // binary data -- normal chunking
            chunkProvider = new LengthChunkProvider(stream);
        } else {
            // likely text data -- line-based chunking
            chunkProvider = new LineChunkProvider(stream);
        }

        out.add(new LettarChunkRequest(chunkProvider));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        out.add(FullestRequest.wrap(msg));
    }

}
