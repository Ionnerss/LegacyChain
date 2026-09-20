package LegacyChain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    static String sha256(String input) {
        StringBuilder sb = new StringBuilder(); //less space usage
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            byte[] digested = md.digest(bytes);

            for (byte b : digested) {
                int i = b & 255;

                String hex = Integer.toHexString(i);
                if (i < 16) hex = 0 + hex;

                sb.append(hex);
            }

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        return sb.toString();
    }
}
