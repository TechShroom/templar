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
        
        @Path("/error")
        public Response<Object> error() {
            throw new AssertionError("Error. This the error path.");
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
