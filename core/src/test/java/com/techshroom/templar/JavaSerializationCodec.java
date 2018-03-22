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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import com.techshroom.lettar.body.SimpleCodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

public class JavaSerializationCodec implements SimpleCodec<ByteBuf, Object> {

    @Override
    public Object decode(Type bodyType, ByteBuf input) {
        if (input.readableBytes() == 0) {
            return null;
        }
        try (ObjectInputStream stream = new ObjectInputStream(new ByteBufInputStream(input))) {
            return stream.readObject();
        } catch (IOException e) {
            // improbable!
            throw new AssertionError(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuf encode(Object input) {
        ByteArrayOutputStream cap = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(cap)) {
            stream.writeObject(input);
        } catch (IOException e) {
            // impossible!
            throw new AssertionError(e);
        }
        return Unpooled.wrappedBuffer(cap.toByteArray());
    }

    @Override
    public String toString() {
        return "Java Serialization";
    }

}
