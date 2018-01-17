package com.techshroom.templar;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableListMultimap;
import com.techshroom.lettar.HttpUtil;
import com.techshroom.lettar.Request;
import com.techshroom.lettar.SimpleRequest;
import com.techshroom.lettar.collections.HttpMultimap;
import com.techshroom.lettar.routing.HttpMethod;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

@AutoValue
public abstract class FullestRequest implements Request<ByteBuf> {

    public static FullestRequest wrap(FullHttpRequest nettyRequest) {
        return new AutoValue_FullestRequest(nettyRequest);
    }

    FullestRequest() {
    }

    abstract FullHttpRequest request();

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

    @Override
    public <U> Request<U> withBody(U body) {
        return SimpleRequest.copyOfWithBody(this, body);
    }

}
