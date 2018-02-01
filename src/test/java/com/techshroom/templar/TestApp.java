package com.techshroom.templar;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.techshroom.lettar.Response;
import com.techshroom.lettar.Router;
import com.techshroom.lettar.SimpleResponse;
import com.techshroom.lettar.annotation.NotFoundHandler;
import com.techshroom.lettar.annotation.ServerErrorHandler;
import com.techshroom.lettar.pipe.PipelineRouterInitializer;
import com.techshroom.lettar.pipe.builtins.path.Path;

import io.netty.buffer.ByteBuf;

public class TestApp {

    public static void main(String[] args) {
        Router<ByteBuf, ByteBuf> router = new PipelineRouterInitializer()
                .newRouter(ImmutableList.of(
                        new Api()));
        HttpServerBootstrap bootstrap = new HttpServerBootstrap("localhost", 57005, () -> {
            return new HttpInitializer(new HttpRouterHandler(router));
        });

        bootstrap.start();
    }

    @JavaSerializationBodyCodec
    public static final class Api {

        @Path("/")
        public Response<Object> index() {
            return SimpleResponse.of(200, "Anything goes!");
        }

        @NotFoundHandler
        public Response<Object> notFound() {
            return SimpleResponse.of(404, "I don't know where that is!");
        }

        @ServerErrorHandler
        public Response<Object> error(Throwable t) {
            return SimpleResponse.of(500, "Now look at what you've done!\n" + Throwables.getStackTraceAsString(t));
        }
    }

}
