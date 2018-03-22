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

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableListMultimap;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;
import com.techshroom.lettar.util.HttpUtil;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

@AutoValue
public abstract class FullestRequest implements Request<ByteBuf> {

    public static FullestRequest wrap(FullHttpRequest nettyRequest) {
        return new AutoValue_FullestRequest(nettyRequest.retainedDuplicate());
    }

    FullestRequest() {
    }

    abstract FullHttpRequest request();

    private boolean released = false;

    public synchronized void release() {
        if (released) {
            return;
        }
        request().release();
        released = true;
    }

    @Override
    protected void finalize() throws Throwable {
        release();
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
    public HttpMultimap getQueryParts() {
        ImmutableListMultimap.Builder<String, String> builder = HttpUtil.headerMapBuilder();
        qsDecoded().parameters().forEach((k, l) -> {
            l.forEach(v -> builder.put(k, v));
        });
        return HttpMultimap.copyOfPreSorted(builder.build());
    }

    @Override
    @Memoized
    public HttpMultimap getHeaders() {
        ImmutableListMultimap.Builder<String, String> builder = HttpUtil.headerMapBuilder()
                .putAll(request().headers());
        return HttpMultimap.copyOfPreSorted(builder.build());
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
