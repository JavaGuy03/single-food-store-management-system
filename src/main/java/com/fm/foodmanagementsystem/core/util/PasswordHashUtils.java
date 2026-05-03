package com.fm.foodmanagementsystem.core.util;

/** Tiện ích nhận diện hash BCrypt (Spring Security mặc định). */
public final class PasswordHashUtils {

    private PasswordHashUtils() {
    }

    public static boolean looksLikeBcrypt(String encodedPassword) {
        return encodedPassword != null
                && encodedPassword.length() >= 4
                && (encodedPassword.startsWith("$2a$")
                        || encodedPassword.startsWith("$2b$")
                        || encodedPassword.startsWith("$2y$"));
    }
}
