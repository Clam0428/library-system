package com.yx.utils;

import java.util.Random;

/**
 * 工具类 - 验证码生成、随机等
 */
public class CommonUtils {
    private static final Random RANDOM = new Random();

    /**
     * 生成数字验证码
     */
    public static String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成字母+数字验证码
     */
    public static String generateAlphanumericCode(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 生成唯一的读者号
     */
    public static String generateReaderNumber() {
        long timestamp = System.currentTimeMillis();
        int random = 100 + RANDOM.nextInt(900);  // 生成 100-999
        return "R" + timestamp + random;
    }

    /**
     * 生成唯一的订单号
     */
    public static String generateOrderNumber() {
        long timestamp = System.currentTimeMillis();
        int random = 100 + RANDOM.nextInt(900);  // 生成 100-999
        return "O" + timestamp + random;
    }

    /**
     * MD5加密密码（简单实现，生产环境建议使用BCrypt）
     */
    public static String encodPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword) {
        return encodPassword(rawPassword).equals(encodedPassword);
    }

    /**
     * 格式化日期
     */
    public static String formatDate(java.util.Date date) {
        if (date == null) return "-";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
