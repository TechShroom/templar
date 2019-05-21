/*
 * This file is part of templar-parent, licensed under the MIT License (MIT).
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

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;
import com.techshroom.lettar.util.HttpUtil;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCounted;

import java.util.Map;

@AutoValue
public abstract class FullestRequest implements Request<ByteBuf>, ReferenceCounted {

    public static FullestRequest wrap(FullHttpRequest nettyRequest) {
        return new AutoValue_FullestRequest(nettyRequest.retainedDuplicate());
    }

    FullestRequest() {
    }

    abstract FullHttpRequest request();

    @Override
    public int refCnt() {
        return request().refCnt();
    }

    @Override
    public FullestRequest retain() {
        request().retain();
        return this;
    }

    @Override
    public FullestRequest retain(int increment) {
        request().retain(increment);
        return this;
    }

    @Override
    public FullestRequest touch() {
        request().touch();
        return this;
    }

    @Override
    public FullestRequest touch(Object hint) {
        request().touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return request().release();
    }

    @Override
    public boolean release(int decrement) {
        return request().release(decrement);
    }

    @Memoized
    QueryStringDecoder qsDecoded() {
        return new QueryStringDecoder(request().uri());
    }

    @Override
    public String getPath() {
        return qsDecoded().path();
    }

    @Override
    @Memoized
    public ImmutableListMultimap<String, String> getQueryParts() {
        ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
        qsDecoded().parameters().forEach(builder::putAll);
        return builder.build();
    }

    @Override
    @Memoized
    public HttpMultimap getHeaders() {
        return HttpMultimap.copyOf(HttpUtil.headerMultimapBuilder()
            .putAll(request().headers())
            .build());
    }

    @Override
    @Memoized
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request().method().name());
    }

    @Override
    @Memoized
    public ByteBuf getBody() {
        return request().content();
    }

}
