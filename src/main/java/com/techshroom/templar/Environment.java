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

public class Environment {

    private static final Environment INSTANCE = new Environment();

    public static Environment getInstance() {
        return INSTANCE;
    }

    private static String prop(String name) {
        return "templar." + name;
    }

    public final int ACCEPT_THREAD_COUNT = Integer.getInteger(prop("threads.accept"), 2);
    public final int IO_THREAD_COUNT = Integer.getInteger(prop("threads.io"), 20);
    public final int WORKER_THREAD_COUNT = Integer.getInteger(prop("threads.worker"), 4);

    public final int MAX_CONTENT_LENGTH = Integer.getInteger(prop("content.length.max"), 128 * 1024 * 1024);

}
