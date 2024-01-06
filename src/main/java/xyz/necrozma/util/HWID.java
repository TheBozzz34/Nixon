package xyz.necrozma.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HWID {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] generateHWID() {
        try {
            MessageDigest hash = MessageDigest.getInstance("MD5");

            String s = System.getProperty("os.name") +
                    System.getProperty("os.arch") +
                    System.getProperty("os.version") +
                    Runtime.getRuntime().availableProcessors() +
                    System.getenv("PROCESSOR_IDENTIFIER") +
                    System.getenv("PROCESSOR_ARCHITECTURE") +
                    System.getenv("NUMBER_OF_PROCESSORS") +
                    System.getenv("user.name");
            return hash.digest(s.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new Error("Algorithm wasn't found.", e);
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean checkHWID() {
        try {
            final HttpsURLConnection connection =
                    (HttpsURLConnection) new URL("https://pi.necrozma.xyz/whitelist?hwid=" + bytesToHex(generateHWID()))
                            .openConnection();

            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String currentln;
            final ArrayList<String> response = new ArrayList<>();

            while ((currentln = in.readLine()) != null) {
                response.add(currentln);
            }

            if (!response.contains("true") || response.contains("false")) {
                // Runtime.getRuntime().exit(0);
                return false;
            }
        } catch (final Exception e) {
            Runtime.getRuntime().exit(0);
        }
        return true;
    }
}