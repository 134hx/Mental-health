package com.example.xinli.common;

import java.security.MessageDigest;

public class Md5Util {
    public static String encrypt(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
}
