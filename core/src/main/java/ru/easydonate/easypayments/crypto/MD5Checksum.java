package ru.easydonate.easypayments.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public final class MD5Checksum {

    private static final char[] HEX_CODE = "0123456789abcdef".toCharArray();

    public static byte[] createChecksum(File file) throws Exception {
        InputStream fileInputStream = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest digest = MessageDigest.getInstance("MD5");

        int numRead;
        do {
            numRead = fileInputStream.read(buffer);
            if(numRead > 0)
                digest.update(buffer, 0, numRead);
        } while (numRead != -1);

        fileInputStream.close();
        return digest.digest();
    }

    public static String getMD5Checksum(File file) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(Files.readAllBytes(file.toPath()));

        byte[] digest = messageDigest.digest();
        return printHexBinary(digest).toUpperCase();
    }

    private static String printHexBinary(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);
        for(byte b : data) {
            builder.append(HEX_CODE[(b >> 4) & 0xF]);
            builder.append(HEX_CODE[(b & 0xF)]);
        }
        return builder.toString();
    }

}
