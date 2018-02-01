package com.techshroom.templar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.techshroom.lettar.body.SimpleCodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

public class JavaSerializationCodec implements SimpleCodec<ByteBuf, Object> {

    @Override
    public Object decode(ByteBuf input) {
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
