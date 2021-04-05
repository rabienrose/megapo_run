package com.chamo.megapo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String string2MD5(String key) {
        if (digest == null) {
            return String.valueOf(key.hashCode());
        }
        //更新字节
        digest.update(key.getBytes());
        //获取最终的摘要
        return convert2HexString(digest.digest());
    }

    /**
     * 转为16进制字符串
     *
     * @param bytes
     * @return
     */
    private static String convert2HexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            //->8->08
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
