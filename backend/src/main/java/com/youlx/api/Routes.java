package com.youlx.api;

public final class Routes {
    private static final String prefix = "/api";

    public static final class Auth {
        public static final String LOGIN = prefix + "/login";
        public static final String LOGOUT = prefix + "/logout";
        public static final String REGISTER = prefix + "/register";
    }

    public static final class Offer {
        public static final String OFFERS = prefix + "/offers";
    }

    public static final class User {
        public static final String ME = prefix + "/me";
        public static final String USER = prefix + "/users";
    }
}
