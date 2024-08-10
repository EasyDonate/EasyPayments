package ru.easydonate.easypayments.core.util;

import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@UtilityClass
public class ChecksumSupplier {

    private static final char[] HEX_CODE = "0123456789abcdef".toCharArray();

    public byte[] computeChecksum(Path path) throws Exception {
        try (InputStream dataStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int numRead;
            do {
                numRead = dataStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return digest.digest();
        }
    }

    public String getChecksumAsString(Path path) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(Files.readAllBytes(path));

        byte[] digest = messageDigest.digest();
        return printHexBinary(digest).toUpperCase();
    }

    private String printHexBinary(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);
        for (byte b : data)
            builder.append(HEX_CODE[(b >> 4) & 0xF]).append(HEX_CODE[(b & 0xF)]);

        return builder.toString();
    }

}
