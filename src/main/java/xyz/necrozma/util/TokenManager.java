package xyz.necrozma.util;


import lombok.Getter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auth Token management and configuration handling for the client
 * @author Enhanced by Assistant
 */
public class TokenManager {

    private static final Logger LOGGER = Logger.getLogger(TokenManager.class.getName());
    private static final String AUTH_CONFIG_FILE = "auth.dat";
    private static final String KEY_FILE = "key.dat";
    private static final String CONFIG_VERSION_PREFIX = "Nixon_Version_";

    private final String version;
    private final Gson gson;
    private final SecretKey encryptionKey;

    public TokenManager(final String version) {
        this.version = version;
        this.gson = new Gson();
        this.encryptionKey = loadOrCreateEncryptionKey();
    }


    /**
     * Saves authentication tokens securely (encrypted).
     *
     * @param accessToken  the Microsoft access token
     * @param refreshToken the Microsoft refresh token
     * @param expiresIn    expiration time in seconds
     */
    public void saveAuthTokens(final String accessToken, final String refreshToken, final long expiresIn) {
        try {
            final JsonObject authData = new JsonObject();
            authData.addProperty("version", version);
            authData.addProperty("accessToken", accessToken);
            authData.addProperty("refreshToken", refreshToken);
            authData.addProperty("expiresIn", expiresIn);
            authData.addProperty("savedAt", System.currentTimeMillis());

            final String jsonString = gson.toJson(authData);
            final String encryptedData = encrypt(jsonString);

            final boolean success = FileUtil.saveFile(AUTH_CONFIG_FILE, true, encryptedData);

            if (success) {
                LOGGER.info("Authentication tokens saved successfully");
            } else {
                LOGGER.warning("Failed to save authentication tokens");
            }

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving authentication tokens", e);
        }
    }

    /**
     * Loads authentication tokens securely (decrypted).
     *
     * @return AuthTokenData containing the tokens, or null if not found/invalid
     */
    public AuthTokenData loadAuthTokens() {
        try {
            final String encryptedData = FileUtil.loadFile(AUTH_CONFIG_FILE);

            if (encryptedData == null) {
                LOGGER.info("No authentication tokens found");
                return null;
            }

            final String decryptedData = decrypt(encryptedData);
            final JsonObject authData = JsonParser.parseString(decryptedData).getAsJsonObject();

            final String tokenVersion = authData.get("version").getAsString();
            if (!isVersionCompatible(tokenVersion)) {
                LOGGER.warning("Authentication tokens version mismatch, clearing tokens");
                clearAuthTokens();
                return null;
            }

            final String accessToken = authData.get("accessToken").getAsString();
            final String refreshToken = authData.get("refreshToken").getAsString();
            final long expiresIn = authData.get("expiresIn").getAsLong();
            final long savedAt = authData.get("savedAt").getAsLong();

            final AuthTokenData tokenData = new AuthTokenData(accessToken, refreshToken, expiresIn, savedAt);

            LOGGER.info("Authentication tokens loaded successfully");
            return tokenData;

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Error loading authentication tokens, clearing stored tokens", e);
            clearAuthTokens();
            return null;
        }
    }

    /**
     * Clears stored authentication tokens.
     */
    public void clearAuthTokens() {
        try {
            FileUtil.delete(AUTH_CONFIG_FILE);
            LOGGER.info("Authentication tokens cleared");
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Error clearing authentication tokens", e);
        }
    }

    /**
     * Checks if stored tokens are still valid (not expired).
     *
     * @return true if tokens exist and are not expired
     */
    public boolean hasValidTokens() {
        final AuthTokenData tokenData = loadAuthTokens();
        if (tokenData == null) {
            return false;
        }

        final long currentTime = System.currentTimeMillis();
        final long tokenAge = (currentTime - tokenData.getSavedAt()) / 1000; // Convert to seconds
        final long expirationBuffer = 300; // 5 minutes buffer

        return tokenAge < (tokenData.getExpiresIn() - expirationBuffer);
    }

    /**
     * Loads or creates an encryption key for secure token storage.
     */
    private SecretKey loadOrCreateEncryptionKey() {
        try {
            final String keyData = FileUtil.loadFile(KEY_FILE);

            if (keyData != null) {
                final byte[] keyBytes = Base64.getDecoder().decode(keyData);
                return new SecretKeySpec(keyBytes, "AES");
            } else {
                // Generate new key
                final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                final SecretKey secretKey = keyGen.generateKey();

                // Save key
                final String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                FileUtil.saveFile(KEY_FILE, true, encodedKey);

                LOGGER.info("Generated new encryption key");
                return secretKey;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading/creating encryption key", e);
            throw new IllegalStateException("Failed to initialize encryption", e);
        }
    }

    /**
     * Encrypts data using AES encryption.
     */
    private String encrypt(final String data) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

        final byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypts data using AES encryption.
     */
    private String decrypt(final String encryptedData) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey);

        final byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        final byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }


    /**
     * Checks if the config version is compatible with the current version.
     */
    private boolean isVersionCompatible(final String configVersion) {
        return version.equals(configVersion);
    }





    /**
     * Data class for authentication token information.
     */
    @Getter
    public static class AuthTokenData {
        private final String accessToken;
        private final String refreshToken;
        private final long expiresIn;
        private final long savedAt;

        public AuthTokenData(final String accessToken, final String refreshToken, final long expiresIn, final long savedAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.savedAt = savedAt;
        }

        /**
         * Checks if the token is expired (with 5-minute buffer).
         */
        public boolean isExpired() {
            final long currentTime = System.currentTimeMillis();
            final long tokenAge = (currentTime - savedAt) / 1000; // Convert to seconds
            final long expirationBuffer = 300; // 5 minutes buffer

            return tokenAge >= (expiresIn - expirationBuffer);
        }
    }
}