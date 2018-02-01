package com.techshroom.templar;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.techshroom.lettar.annotation.BodyCodec;
import com.techshroom.lettar.pipe.PipeCompatible;
import com.techshroom.lettar.pipe.builtins.accept.Produces;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@PipeCompatible(metaAnnotation = true)
@BodyCodec(JavaSerializationCodec.class)
@Produces("application/java-serialized-object")
public @interface JavaSerializationBodyCodec {

}
