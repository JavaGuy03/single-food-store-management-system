package com.fm.foodmanagementsystem.core.util;

import java.util.Locale;

public final class EmailUtils {

    private EmailUtils() {
    }

    /** Chuẩn hóa email đăng nhập/đăng ký (trim + lowercase) để tránh lệch kiểu Gmail. */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
