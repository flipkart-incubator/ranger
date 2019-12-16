package com.flipkart.ranger.util;

/**
 *
 */
public class Exceptions {
    private Exceptions() {}

    public static void illegalState(String message, Throwable t) {
        throw new IllegalStateException(message, t);
    }

    public static void illegalState(Throwable t) {
        throw new IllegalStateException(t);
    }

    public static void illegalState(String message) {
        throw new IllegalStateException(message);
    }
}
