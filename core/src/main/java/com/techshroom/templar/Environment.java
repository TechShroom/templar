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

import com.techshroom.jungle.Loaders;
import com.techshroom.jungle.SysPropConfigOption;
import com.techshroom.jungle.SysPropNamespace;

public class Environment {

    private static final SysPropNamespace NS = SysPropNamespace.create("templar");
    private static final SysPropNamespace THREADS_NS = NS.subspace("threads");
    private static final SysPropNamespace CONTENT_NS = NS.subspace("content");

    /**
     * Property: {@code templar.threads.accept}
     */
    public static final SysPropConfigOption<Integer> ACCEPT_THREAD_COUNT = THREADS_NS
            .create("accept", Loaders.forIntInRange(1, Integer.MAX_VALUE), 2);
    /**
     * Property: {@code templar.threads.io}
     */
    public static final SysPropConfigOption<Integer> IO_THREAD_COUNT = THREADS_NS
            .create("io", Loaders.forIntInRange(1, Integer.MAX_VALUE), 20);
    /**
     * Property: {@code templar.threads.worker}
     */
    public static final SysPropConfigOption<Integer> WORKER_THREAD_COUNT = THREADS_NS
            .create("worker", Loaders.forIntInRange(1, Integer.MAX_VALUE), 4);

    /**
     * Property: {@code templar.content.length.max}
     */
    public static final SysPropConfigOption<Integer> MAX_CONTENT_LENGTH = CONTENT_NS
            .create("length.max", Loaders.forIntInRange(1, Integer.MAX_VALUE), 128 * 1024 * 1024);

}
