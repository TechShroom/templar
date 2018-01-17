package com.techshroom.templar;

public class Environment {

    private static final Environment INSTANCE = new Environment();

    public static Environment getInstance() {
        return INSTANCE;
    }

    private static String prop(String name) {
        return "templar." + name;
    }

    public final int ACCEPT_THREAD_COUNT = Integer.getInteger(prop("threads.accept"), 1);
    public final int IO_THREAD_COUNT = Integer.getInteger(prop("threads.io"), 4);
    public final int WORKER_THREAD_COUNT = Integer.getInteger(prop("threads.worker"), 4);

    public final int MAX_CONTENT_LENGTH = Integer.getInteger(prop("content.length.max"), 128 * 1024 * 1024);

}
