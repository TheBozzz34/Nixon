package xyz.necrozma.auth;

import fr.litarvan.openauth.microsoft.AuthTokens;
import xyz.necrozma.util.ConfigManager;
import xyz.necrozma.login.XboxLiveMojangAuth;
import xyz.necrozma.login.AuthenticationResult;
import xyz.necrozma.login.AuthenticationException;
import xyz.necrozma.login.WebLoginHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling Microsoft/Xbox Live authentication with token refresh capability.
 * Manages the complete authentication flow and token persistence.
 */
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    // Microsoft OAuth endpoints
    private static final String MS_TOKEN_REFRESH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String CLIENT_ID = "00000000402b5328"; // Default Minecraft client ID
    private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL XboxLive.signin offline_access";

    private final ConfigManager configManager;
    private final XboxLiveMojangAuth xboxAuth;
    private final Gson gson;

    private AuthenticationResult lastAuthResult;

    public AuthenticationService(final ConfigManager configManager) {
        this.configManager = configManager;
        this.xboxAuth = new XboxLiveMojangAuth();
        this.gson = new Gson();
    }

    /**
     * Authenticates the user, using stored tokens if available or prompting for new login.
     *
     * @return true if authentication was successful
     */
    public boolean authenticate() {
        try {
            LOGGER.info("Starting authentication process...");

            // Try to use stored tokens first
            if (tryStoredTokens()) {
                LOGGER.info("Successfully authenticated using stored tokens");
                return true;
            }

            // Try to refresh tokens if available
            if (tryRefreshTokens()) {
                LOGGER.info("Successfully authenticated using refreshed tokens");
                return true;
            }

            // Fall back to web login
            return performWebLogin();

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Authentication failed", e);
            return false;
        }
    }

    /**
     * Tries to authenticate using stored tokens.
     */
    private boolean tryStoredTokens() {
        try {
            if (!configManager.hasValidTokens()) {
                LOGGER.info("No valid stored tokens found");
                return false;
            }

            final ConfigManager.AuthTokenData tokenData = configManager.loadAuthTokens();
            if (tokenData == null) {
                LOGGER.info("Failed to load stored tokens");
                return false;
            }

            LOGGER.info("Attempting authentication with stored access token");
            return authenticateWithAccessToken(tokenData.getAccessToken());

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to authenticate with stored tokens", e);
            return false;
        }
    }

    /**
     * Tries to refresh the stored tokens.
     */
    private boolean tryRefreshTokens() {
        try {
            final ConfigManager.AuthTokenData tokenData = configManager.loadAuthTokens();
            if (tokenData == null || tokenData.getRefreshToken() == null) {
                LOGGER.info("No refresh token available");
                return false;
            }

            LOGGER.info("Attempting to refresh Microsoft tokens");
            final RefreshTokenResponse refreshResponse = refreshMicrosoftToken(tokenData.getRefreshToken());

            if (refreshResponse == null) {
                LOGGER.warning("Failed to refresh Microsoft tokens");
                configManager.clearAuthTokens();
                return false;
            }

            // Save the new tokens
            configManager.saveAuthTokens(
                    refreshResponse.accessToken,
                    refreshResponse.refreshToken,
                    refreshResponse.expiresIn
            );

            LOGGER.info("Successfully refreshed Microsoft tokens");
            return authenticateWithAccessToken(refreshResponse.accessToken);

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh tokens", e);
            configManager.clearAuthTokens();
            return false;
        }
    }

    /**
     * Performs web login authentication flow.
     */
    private boolean performWebLogin() {
        try {
            LOGGER.info("Performing web login authentication");

            final AuthTokens tokens = WebLoginHelper.getTokensFromWebLogin();

            if (tokens.getAccessToken() == null || tokens.getAccessToken().trim().isEmpty()) {
                LOGGER.severe("Access token is null or empty");
                return false;
            }

            // Save the tokens for future use
            if (tokens.getRefreshToken() != null) {
                configManager.saveAuthTokens(
                        tokens.getAccessToken(),
                        tokens.getRefreshToken(),
                        3600 // Default 1 hour expiration
                );
                LOGGER.info("Saved authentication tokens for future use");
            }

            return authenticateWithAccessToken(tokens.getAccessToken());

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Web login authentication failed", e);
            return false;
        }
    }

    /**
     * Authenticates with Xbox Live using the provided access token.
     */
    private boolean authenticateWithAccessToken(final String accessToken) {
        try {
            LOGGER.info("Starting Xbox Live authentication");

            final AuthenticationResult result = xboxAuth.authenticate(accessToken);
            this.lastAuthResult = result;

            LOGGER.info("Authentication successful!");
            LOGGER.info("Xbox Username: " + result.getUsername());
            LOGGER.info("Minecraft Username: " + result.getMinecraftUsername());
            LOGGER.info("Minecraft UUID: " + result.getUuid());
            LOGGER.info("Token expires in: " + result.getExpiresIn() + " seconds");

            // Validate result
            if (result.getMinecraftUsername() == null || result.getUuid() == null || result.getAccessToken() == null) {
                LOGGER.severe("Authentication result contains null values");
                return false;
            }

            // Create Minecraft session
            LOGGER.info("Creating Minecraft session");
            final Session session = new Session(
                    result.getMinecraftUsername(),
                    result.getUuid(),
                    result.getAccessToken(),
                    "legacy"
            );

            Minecraft.getMinecraft().setSession(session);
            LOGGER.info("Minecraft session created successfully");

            return true;

        } catch (final AuthenticationException e) {
            LOGGER.log(Level.SEVERE, "Xbox Live authentication failed", e);

            // Clear stored tokens if authentication fails
            configManager.clearAuthTokens();

            return false;
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during authentication", e);
            return false;
        }
    }

    /**
     * Refreshes Microsoft access token using refresh token.
     */
    private RefreshTokenResponse refreshMicrosoftToken(final String refreshToken) {
        try {
            final String requestBody = String.format(
                    "client_id=%s&scope=%s&refresh_token=%s&grant_type=refresh_token",
                    CLIENT_ID,
                    SCOPE,
                    refreshToken
            );

            final String response = sendPostRequest(MS_TOKEN_REFRESH_URL, requestBody, true);

            if (response == null) {
                return null;
            }

            final JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();

            final String accessToken = responseJson.get("access_token").getAsString();
            final String newRefreshToken = responseJson.has("refresh_token") ?
                    responseJson.get("refresh_token").getAsString() : refreshToken;
            final int expiresIn = responseJson.get("expires_in").getAsInt();

            return new RefreshTokenResponse(accessToken, newRefreshToken, expiresIn);

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh Microsoft token", e);
            return null;
        }
    }

    /**
     * Sends a POST request for token refresh.
     */
    private String sendPostRequest(final String urlString, final String requestBody, final boolean isFormData) {
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    isFormData ? "application/x-www-form-urlencoded" : "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // Write request body
            try (final OutputStream outputStream = connection.getOutputStream()) {
                final byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            final int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return readResponse(connection);
            } else {
                final String errorResponse = readErrorResponse(connection);
                LOGGER.warning("Token refresh failed. Status: " + responseCode + ", Body: " + errorResponse);
                return null;
            }

        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Network error during token refresh", e);
            return null;
        }
    }

    /**
     * Reads response from HttpURLConnection.
     */
    private String readResponse(final HttpURLConnection connection) throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            final StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Reads error response from HttpURLConnection.
     */
    private String readErrorResponse(final HttpURLConnection connection) throws IOException {
        if (connection.getErrorStream() != null) {
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {

                final StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
        return "No error details available";
    }

    /**
     * Logs out the current user and clears stored tokens.
     */
    public void logout() {
        try {
            configManager.clearAuthTokens();
            this.lastAuthResult = null;

            // Reset Minecraft session to default
            Minecraft.getMinecraft().setSession(new Session("Player", "", "", "legacy"));

            LOGGER.info("User logged out successfully");
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Error during logout", e);
        }
    }

    /**
     * Gets the last authentication result.
     */
    public AuthenticationResult getLastAuthResult() {
        return lastAuthResult;
    }

    /**
     * Checks if user is currently authenticated.
     */
    public boolean isAuthenticated() {
        return lastAuthResult != null && configManager.hasValidTokens();
    }

    /**
     * Forces a refresh of the current authentication.
     */
    public boolean forceRefresh() {
        configManager.clearAuthTokens();
        return authenticate();
    }

    /**
     * Response class for refresh token operations.
     */
    private static class RefreshTokenResponse {
        final String accessToken;
        final String refreshToken;
        final int expiresIn;

        RefreshTokenResponse(final String accessToken, final String refreshToken, final int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }
    }
}