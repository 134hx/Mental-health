package com.example.xinli.common;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    // token -> userId
    private static final Map<String, Long> TOKEN_MAP = new ConcurrentHashMap<>();

    public static String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_MAP.put(token, userId);
        return token;
    }

    // 增加判空保护
    public static Long getUserId(String token) {
        if(token == null){
            return null;
        }
        return TOKEN_MAP.get(token);
    }

    public static void remove(String token) {
        if(token != null){
            TOKEN_MAP.remove(token);
        }
    }
}

