package xyz.necrozma.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class XboxLiveMojangAuth {

    private static final String XBOX_LIVE_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final Gson gson;

    public XboxLiveMojangAuth() {
        this.gson = new Gson();
    }

    /**
     * Complete authentication flow from Microsoft access token to Minecraft bearer token
     * @param microsoftAccessToken The Microsoft access token obtained from initial OAuth flow
     * @return AuthenticationResult containing the Minecraft bearer token and user info
     * @throws AuthenticationException if any step fails
     */
    public AuthenticationResult authenticate(String microsoftAccessToken) throws AuthenticationException {
        try {
            // Step 1: Get Xbox Live token
            XboxLiveTokenResponse xboxToken = authenticateXboxLive(microsoftAccessToken);

            // Step 2: Get XSTS token
            XSTSTokenResponse xstsToken = getXSTSToken(xboxToken.token);

            // Step 3: Get Minecraft bearer token
            MinecraftAuthResponse minecraftAuth = authenticateMinecraft(xstsToken.token, xstsToken.userHash);

            // Step 4: Get Minecraft profile (includes UUID)
            MinecraftProfile profile = getMinecraftProfile(minecraftAuth.accessToken);

            return new AuthenticationResult(
                    minecraftAuth.accessToken,
                    minecraftAuth.username,
                    minecraftAuth.expiresIn,
                    profile.uuid,
                    profile.name
            );

        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Step 1: Authenticate with Xbox Live
     */
    private XboxLiveTokenResponse authenticateXboxLive(String accessToken) throws IOException, AuthenticationException {
        JsonObject requestBody = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", accessToken); // Try without "d=" prefix first

        requestBody.add("Properties", properties);
        requestBody.addProperty("RelyingParty", "http://auth.xboxlive.com");
        requestBody.addProperty("TokenType", "JWT");

        String requestBodyStr = gson.toJson(requestBody);

        try {
            String response = sendPostRequest(XBOX_LIVE_AUTH_URL, requestBodyStr);
            return parseXboxLiveResponse(response);
        } catch (AuthenticationException e) {
            // Try with "d=" prefix if first attempt fails
            properties.addProperty("RpsTicket", "d=" + accessToken);
            requestBody.add("Properties", properties);
            requestBodyStr = gson.toJson(requestBody);

            try {
                String response = sendPostRequest(XBOX_LIVE_AUTH_URL, requestBodyStr);
                return parseXboxLiveResponse(response);
            } catch (AuthenticationException retryException) {
                throw new AuthenticationException("Xbox Live authentication failed with both token formats. Last error: " + retryException.getMessage());
            }
        }
    }

    private XboxLiveTokenResponse parseXboxLiveResponse(String response) throws AuthenticationException {
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        String token = responseJson.get("Token").getAsString();
        String userHash = responseJson.getAsJsonObject("DisplayClaims")
                .getAsJsonArray("xui")
                .get(0).getAsJsonObject()
                .get("uhs").getAsString();

        return new XboxLiveTokenResponse(token, userHash);
    }

    /**
     * Step 2: Get XSTS token
     */
    private XSTSTokenResponse getXSTSToken(String xboxToken) throws IOException, AuthenticationException {
        JsonObject requestBody = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        properties.add("UserTokens", gson.toJsonTree(new String[]{xboxToken}));

        requestBody.add("Properties", properties);
        requestBody.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        requestBody.addProperty("TokenType", "JWT");

        String requestBodyStr = gson.toJson(requestBody);

        try {
            String response = sendPostRequest(XSTS_AUTH_URL, requestBodyStr);
            return parseXSTSResponse(response);
        } catch (AuthenticationException e) {
            // Check if it's a 401 error with specific error codes
            if (e.getMessage().contains("401")) {
                throw e; // Re-throw 401 errors as they contain specific error handling
            }
            throw new AuthenticationException("XSTS authentication failed: " + e.getMessage());
        }
    }

    private XSTSTokenResponse parseXSTSResponse(String response) throws AuthenticationException {
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        String token = responseJson.get("Token").getAsString();
        String userHash = responseJson.getAsJsonObject("DisplayClaims")
                .getAsJsonArray("xui")
                .get(0).getAsJsonObject()
                .get("uhs").getAsString();

        return new XSTSTokenResponse(token, userHash);
    }

    /**
     * Step 3: Get Minecraft bearer token
     */
    private MinecraftAuthResponse authenticateMinecraft(String xstsToken, String userHash) throws IOException, AuthenticationException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        requestBody.addProperty("ensureLegacyEnabled", true);

        String requestBodyStr = gson.toJson(requestBody);
        String response = sendPostRequest(MINECRAFT_AUTH_URL, requestBodyStr);

        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        String accessToken = responseJson.get("access_token").getAsString();
        String username = responseJson.get("username").getAsString();
        int expiresIn = responseJson.get("expires_in").getAsInt();

        return new MinecraftAuthResponse(accessToken, username, expiresIn);
    }

    /**
     * Step 4: Get Minecraft profile information including UUID
     */
    private MinecraftProfile getMinecraftProfile(String accessToken) throws IOException, AuthenticationException {
        HttpURLConnection connection = (HttpURLConnection) new URL(MINECRAFT_PROFILE_URL).openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                String errorResponse = readErrorResponse(connection);
                throw new AuthenticationException("Failed to get Minecraft profile. Status: " + responseCode + ", Body: " + errorResponse);
            }

            String response = readResponse(connection);
            JsonObject profileJson = JsonParser.parseString(response).getAsJsonObject();

            String uuid = profileJson.get("id").getAsString();
            String name = profileJson.get("name").getAsString();

            return new MinecraftProfile(uuid, name);

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Send POST request using HttpURLConnection (Java 8 compatible)
     */
    private String sendPostRequest(String urlString, String requestBody) throws IOException, AuthenticationException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Set request properties
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(30000); // 30 seconds

            // Debug output
            System.out.println("Sending POST request to: " + urlString);
            System.out.println("Request body: " + requestBody);

            // Write request body
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            if (responseCode == 401) {
                // Handle 401 specifically for XSTS errors
                String errorResponse = readErrorResponse(connection);
                System.out.println("401 Error response: " + errorResponse);

                try {
                    JsonObject errorJson = JsonParser.parseString(errorResponse).getAsJsonObject();
                    if (errorJson.has("XErr")) {
                        long errorCode = errorJson.get("XErr").getAsLong();

                        if (errorCode == 2148916238L) {
                            throw new AuthenticationException("Account belongs to someone under 18 and needs to be added to a family");
                        } else if (errorCode == 2148916233L) {
                            throw new AuthenticationException("Account has no Xbox account, you must sign up for one first");
                        } else {
                            throw new AuthenticationException("Authentication failed with Xbox error code: " + errorCode);
                        }
                    }
                } catch (Exception parseEx) {
                    throw new AuthenticationException("401 Unauthorized. Error details: " + errorResponse);
                }
            }

            if (responseCode != 200) {
                String errorResponse = readErrorResponse(connection);
                System.out.println("Error response: " + errorResponse);
                throw new AuthenticationException("Request failed. Status: " + responseCode + ", Body: " + errorResponse);
            }

            // Read successful response
            String response = readResponse(connection);
            System.out.println("Success response: " + response);
            return response;

        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            throw new AuthenticationException("Network error: " + e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Read response from HttpURLConnection
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Read error response from HttpURLConnection
     */
    private String readErrorResponse(HttpURLConnection connection) throws IOException {
        // Check if error stream exists, if not, try to read from input stream
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
        } else {
            // If no error stream, try to read from input stream or return generic message
            try {
                return readResponse(connection);
            } catch (IOException e) {
                return "No error details available. Response code: " + connection.getResponseCode();
            }
        }
    }

    // Helper classes for response data
    private static class XboxLiveTokenResponse {
        final String token;
        final String userHash;

        XboxLiveTokenResponse(String token, String userHash) {
            this.token = token;
            this.userHash = userHash;
        }
    }

    private static class XSTSTokenResponse {
        final String token;
        final String userHash;

        XSTSTokenResponse(String token, String userHash) {
            this.token = token;
            this.userHash = userHash;
        }
    }

    private static class MinecraftAuthResponse {
        final String accessToken;
        final String username;
        final int expiresIn;

        MinecraftAuthResponse(String accessToken, String username, int expiresIn) {
            this.accessToken = accessToken;
            this.username = username;
            this.expiresIn = expiresIn;
        }
    }

    private static class MinecraftProfile {
        final String uuid;
        final String name;

        MinecraftProfile(String uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }

}