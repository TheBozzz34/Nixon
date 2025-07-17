package xyz.necrozma.login;


import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CodeLoginHelper {
    private static final String CLIENT_ID = "17b88a38-2d92-4552-80b3-cd98b5b66cea";
    private static final String DEVICE_CODE_ENDPOINT = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String SCOPE = "XboxLive.signin XboxLive.offline_access";

    public static AuthTokens getTokensFromWebLogin() throws IOException {
        // Step 1: Get device code
        DeviceCodeResponse deviceCode = getDeviceCode();

        // Step 2: Display user code and open browser
        System.out.println("Please visit: " + deviceCode.verificationUri);
        System.out.println("Enter code: " + deviceCode.userCode);
        System.out.println("Opening browser...");

        try {
            Desktop.getDesktop().browse(URI.create(deviceCode.verificationUri));
        } catch (Exception e) {
            System.err.println("Could not open browser automatically. Please manually visit: " + deviceCode.verificationUri);
        }

        // Step 3: Poll for tokens
        return pollForTokens(deviceCode);
    }

    private static DeviceCodeResponse getDeviceCode() throws IOException {
        String postData = buildQueryParams(new HashMap<String, String>() {{
            put("client_id", CLIENT_ID);
            put("scope", SCOPE);
        }});

        URL url = new URL(DEVICE_CODE_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            String errorResponse = readErrorResponse(connection);
            throw new IOException("Device code request failed with response code: " + responseCode +
                                "\nError details: " + errorResponse);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String responseStr = response.toString();
            System.out.println("Device code response: " + responseStr); // Debug output

            DeviceCodeResponse deviceCodeResponse = new DeviceCodeResponse();
            deviceCodeResponse.deviceCode = extractJsonValue(responseStr, "device_code");
            deviceCodeResponse.userCode = extractJsonValue(responseStr, "user_code");
            deviceCodeResponse.verificationUri = extractJsonValue(responseStr, "verification_uri");
            deviceCodeResponse.expiresIn = Integer.parseInt(extractJsonValue(responseStr, "expires_in"));
            deviceCodeResponse.interval = Integer.parseInt(extractJsonValue(responseStr, "interval"));

            if (deviceCodeResponse.deviceCode == null || deviceCodeResponse.userCode == null) {
                throw new IOException("Failed to get device code from response: " + responseStr);
            }

            return deviceCodeResponse;
        }
    }

    private static AuthTokens pollForTokens(DeviceCodeResponse deviceCode) throws IOException {
        long startTime = System.currentTimeMillis();
        long timeoutMs = deviceCode.expiresIn * 1000L;
        int pollInterval = deviceCode.interval * 1000; // Convert to milliseconds

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Polling interrupted");
            }

            try {
                return requestTokens(deviceCode.deviceCode);
            } catch (AuthorizationPendingException e) {
                // User hasn't completed authorization yet, continue polling
                System.out.println("Waiting for user to complete authorization...");
                continue;
            } catch (IOException e) {
                // Other errors should be thrown
                throw e;
            }
        }

        throw new IOException("Device code expired. Please try again.");
    }

    private static AuthTokens requestTokens(String deviceCode) throws IOException {
        String postData = buildQueryParams(new HashMap<String, String>() {{
            put("client_id", CLIENT_ID);
            put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            put("device_code", deviceCode);
        }});

        URL url = new URL(TOKEN_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes("UTF-8"));
        }

        int responseCode = connection.getResponseCode();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String responseStr = response.toString();

            if (responseCode == 400) {
                String error = extractJsonValue(responseStr, "error");
                if ("authorization_pending".equals(error)) {
                    throw new AuthorizationPendingException("Authorization pending");
                } else if ("slow_down".equals(error)) {
                    // Should increase polling interval, but for simplicity we'll just continue
                    throw new AuthorizationPendingException("Slow down");
                } else if ("expired_token".equals(error)) {
                    throw new IOException("Device code expired");
                } else if ("access_denied".equals(error)) {
                    throw new IOException("User denied access");
                }
                throw new IOException("Token request failed: " + error);
            }

            if (responseCode != 200) {
                throw new IOException("Token request failed with response code: " + responseCode + ", response: " + responseStr);
            }

            String accessToken = extractJsonValue(responseStr, "access_token");
            String refreshToken = extractJsonValue(responseStr, "refresh_token");

            if (accessToken == null) {
                throw new IOException("Failed to extract access token from response: " + responseStr);
            }

            System.out.println("Successfully obtained tokens!");
            return new AuthTokens(accessToken, refreshToken);
        }
    }

    private static String buildQueryParams(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) query.append("&");
            query.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            query.append("=");
            query.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return query.toString();
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            // Try without quotes for numeric values
            searchKey = "\"" + key + "\":";
            start = json.indexOf(searchKey);
            if (start == -1) return null;
            start += searchKey.length();

            // Find the end of the numeric value
            int end = start;
            while (end < json.length() && Character.isDigit(json.charAt(end))) {
                end++;
            }
            return json.substring(start, end);
        }

        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;

        return json.substring(start, end);
    }

    private static class DeviceCodeResponse {
        String deviceCode;
        String userCode;
        String verificationUri;
        int expiresIn;
        int interval;
    }

    private static class AuthorizationPendingException extends IOException {
        public AuthorizationPendingException(String message) {
            super(message);
        }
    }

    private static String readErrorResponse(HttpURLConnection connection) throws IOException {
        if (connection.getErrorStream() != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
        return "No error details available";
    }
}