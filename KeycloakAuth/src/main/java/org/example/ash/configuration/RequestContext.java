package org.example.ash.configuration;

public class RequestContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void set(String userName) {
        CURRENT_USER.set(userName);
    }

    public static String get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
