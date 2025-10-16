package com.moa.moa_backend.KakaoAuth.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    public static Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("LOGIN_USER_ID");
        return (id instanceof Long) ? (Long) id : null;
    }
}
