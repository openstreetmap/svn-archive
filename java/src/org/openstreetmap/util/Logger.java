package org.openstreetmap.util;

public class Logger {

    public static void log(String s) {
        System.err.println(s);
    }

    public static void log(Throwable t) {
        log(t.getMessage());
        t.printStackTrace(System.err);
    }
}
