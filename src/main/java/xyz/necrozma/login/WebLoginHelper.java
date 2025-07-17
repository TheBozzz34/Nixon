package xyz.necrozma.login;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.litarvan.openauth.microsoft.AuthTokens;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class WebLoginHelper {
    private static final String CLIENT_ID = "17b88a38-2d92-4552-80b3-cd98b5b66cea";
    private static final String CLIENT_SECRET = "uuP8Q~usIA-7b6tb_TvmBwKmHi51oJXnhQ79wa~Y"; // Add your client secret here
    private static final String REDIRECT_URI = "http://localhost:59848/callback";
    private static final String AUTH_ENDPOINT = "https://login.live.com/oauth20_authorize.srf";
    private static final String TOKEN_ENDPOINT = "https://login.live.com/oauth20_token.srf";
    private static final String SCOPE = "XboxLive.signin XboxLive.offline_access";
    private static final int PORT = 59848;

    public static AuthTokens getTokensFromWebLogin() throws IOException {
        String authUrl = AUTH_ENDPOINT + "?" + buildQueryParams(new HashMap<String, String>() {{
            put("client_id", CLIENT_ID);
            put("response_type", "code"); // Changed from "token" to "code"
            put("redirect_uri", REDIRECT_URI);
            put("scope", SCOPE);
        }});

        // Open the browser
        Desktop.getDesktop().browse(URI.create(authUrl));

        // Set up local HTTP server to catch the redirect
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        TokenReceiver receiver = new TokenReceiver();

        // Set up the callback endpoint
        server.createContext("/callback", receiver);

        server.setExecutor(null);
        server.start();

        // Wait for redirect with authorization code
        while (!receiver.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }

        server.stop(1);
        return receiver.getTokens();
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

    private static AuthTokens exchangeCodeForTokens(String code) throws IOException {
        String postData = buildQueryParams(new HashMap<String, String>() {{
            put("client_id", CLIENT_ID);
            put("client_secret", CLIENT_SECRET); // Add client secret to token request
            put("grant_type", "authorization_code");
            put("code", code);
            put("redirect_uri", REDIRECT_URI);
            put("scope", SCOPE);
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
        if (responseCode != 200) {
            throw new IOException("Token exchange failed with response code: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response (simple parsing for access_token and refresh_token)
            String responseStr = response.toString();
            //System.out.println("Token response: " + responseStr); // Debug output

            String accessToken = extractJsonValue(responseStr, "access_token");
            // d= fix
            //accessToken = "d=" + accessToken;
            String refreshToken = extractJsonValue(responseStr, "refresh_token");

            //System.out.println("Access token: " + (accessToken != null ? accessToken.substring(0, Math.min(50, accessToken.length())) + "..." : "null"));
            //System.out.println("Refresh token: " + (refreshToken != null ? refreshToken.substring(0, Math.min(50, refreshToken.length())) + "..." : "null"));

            if (accessToken == null) {
                throw new IOException("Failed to extract access token from response: " + responseStr);
            }

            return new AuthTokens(accessToken, refreshToken);
        }
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;

        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;

        return json.substring(start, end);
    }

    private static class TokenReceiver implements HttpHandler {
        private volatile boolean done = false;
        private AuthTokens tokens;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            //System.out.println("Callback received!");

            URI uri = exchange.getRequestURI();
            String query = uri.getRawQuery();

            //System.out.println("Query string: " + query);

            Map<String, String> params = parseQuery(query);

            if (params.containsKey("error")) {
                // Handle error case
                String error = params.get("error");
                String errorDescription = params.get("error_description");
                System.out.println("OAuth error: " + error + " - " + errorDescription);
                done = true;

                String body = "<html><body><h2>Login Error</h2><p>" + error + ": " + errorDescription + "</p></body></html>";
                exchange.sendResponseHeaders(400, body.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes());
                }
                return;
            }

            String code = params.get("code");
            //System.out.println("Authorization code received: " + (code != null ? "yes" : "no"));

            if (code != null) {
                try {
                    // Exchange authorization code for tokens
                    tokens = exchangeCodeForTokens(code);
                    done = true;

                    String body = "<html><body><h2>Login Successful</h2><p>You can close this window.</p></body></html>";
                    exchange.sendResponseHeaders(200, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes());
                    }
                } catch (IOException e) {
                    System.err.println("Token exchange failed: " + e.getMessage());
                    e.printStackTrace();
                    done = true;
                    String body = "<html><body><h2>Token Exchange Failed</h2><p>" + e.getMessage() + "</p></body></html>";
                    exchange.sendResponseHeaders(500, body.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(body.getBytes());
                    }
                }
            } else {
                done = true;
                String body = "<html><body><h2>No Authorization Code</h2><p>Login failed - no code received.</p></body></html>";
                exchange.sendResponseHeaders(400, body.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body.getBytes());
                }
            }
        }

        public boolean isDone() {
            return done;
        }

        public AuthTokens getTokens() {
            return tokens;
        }

        private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] parts = pair.split("=");
                    if (parts.length == 2) {
                        String key = URLDecoder.decode(parts[0], "UTF-8");
                        String value = URLDecoder.decode(parts[1], "UTF-8");
                        params.put(key, value);
                    }
                }
            }
            return params;
        }
    }
}