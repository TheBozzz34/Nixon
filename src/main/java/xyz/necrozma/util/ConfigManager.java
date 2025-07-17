package xyz.necrozma.util;

import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced configuration manager for Nixon client.
 * Provides robust config saving/loading with validation, error handling, and secure token storage.
 *
 * @author Enhanced by Assistant
 */
public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE = "settings.txt";
    private static final String AUTH_CONFIG_FILE = "auth.dat";
    private static final String KEY_FILE = "key.dat";
    private static final String CONFIG_VERSION_PREFIX = "Nixon_Version_";
    private static final String TOGGLE_PREFIX = "Toggle_";

    private final String version;
    private final ModuleManager moduleManager;
    private final Gson gson;
    private SecretKey encryptionKey;

    public ConfigManager(final String version, final ModuleManager moduleManager) {
        this.version = version;
        this.moduleManager = moduleManager;
        this.gson = new Gson();
        this.encryptionKey = loadOrCreateEncryptionKey();
    }

    /**
     * Saves the current configuration to file.
     * Creates a backup of the existing config before saving.
     *
     * @return true if config was saved successfully
     */
    public boolean saveConfig() {
        try {
            // Create backup of existing config
            if (FileUtil.exists(CONFIG_FILE)) {
                FileUtil.createBackup(CONFIG_FILE);
            }

            final StringBuilder configBuilder = new StringBuilder();

            // Write version header
            configBuilder.append(CONFIG_VERSION_PREFIX).append(version).append(System.lineSeparator());

            // Write module toggle states
            for (final Module module : moduleManager.getModules().values()) {
                configBuilder.append(TOGGLE_PREFIX)
                        .append(module.getName())
                        .append("_")
                        .append(module.isToggled())
                        .append(System.lineSeparator());
            }

            final boolean success = FileUtil.saveFile(CONFIG_FILE, true, configBuilder.toString());

            if (success) {
                LOGGER.info("Configuration saved successfully");
            } else {
                LOGGER.warning("Failed to save configuration");
            }

            return success;

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving configuration", e);
            return false;
        }
    }

    /**
     * Loads configuration from file.
     * Validates version compatibility and applies module states.
     *
     * @return true if config was loaded successfully
     */
    public boolean loadConfig() {
        try {
            final List<String> lines = FileUtil.loadFileLines(CONFIG_FILE);

            if (lines == null || lines.isEmpty()) {
                LOGGER.info("No configuration file found, using defaults");
                return false;
            }

            // Parse and validate version
            final String versionLine = lines.get(0);
            if (!versionLine.startsWith(CONFIG_VERSION_PREFIX)) {
                LOGGER.warning("Invalid configuration format - missing version header");
                return false;
            }

            final String configVersion = versionLine.substring(CONFIG_VERSION_PREFIX.length());
            if (!isVersionCompatible(configVersion)) {
                LOGGER.warning("Configuration version mismatch: " + configVersion + " vs " + version);
                // Continue loading but log the warning
            }

            // Parse module states
            final Map<String, Boolean> moduleStates = new HashMap<>();
            for (int i = 1; i < lines.size(); i++) {
                final String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                if (line.startsWith(TOGGLE_PREFIX)) {
                    parseModuleToggle(line, moduleStates);
                } else {
                    LOGGER.warning("Unknown configuration line: " + line);
                }
            }

            // Apply module states
            applyModuleStates(moduleStates);

            LOGGER.info("Configuration loaded successfully");
            return true;

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading configuration", e);
            return false;
        }
    }

    /**
     * Saves authentication tokens securely (encrypted).
     *
     * @param accessToken the Microsoft access token
     * @param refreshToken the Microsoft refresh token
     * @param expiresIn expiration time in seconds
     * @return true if tokens were saved successfully
     */
    public boolean saveAuthTokens(final String accessToken, final String refreshToken, final long expiresIn) {
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

            return success;

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving authentication tokens", e);
            return false;
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
     * Parses a module toggle line and adds it to the states map.
     */
    private void parseModuleToggle(final String line, final Map<String, Boolean> moduleStates) {
        try {
            // Remove prefix and split by last underscore
            final String content = line.substring(TOGGLE_PREFIX.length());
            final int lastUnderscoreIndex = content.lastIndexOf("_");

            if (lastUnderscoreIndex == -1) {
                LOGGER.warning("Invalid toggle format: " + line);
                return;
            }

            final String moduleName = content.substring(0, lastUnderscoreIndex);
            final String toggleValue = content.substring(lastUnderscoreIndex + 1);

            final boolean isToggled = Boolean.parseBoolean(toggleValue);
            moduleStates.put(moduleName, isToggled);

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse toggle line: " + line, e);
        }
    }

    /**
     * Applies the loaded module states to the actual modules.
     */
    private void applyModuleStates(final Map<String, Boolean> moduleStates) {
        int appliedCount = 0;
        int skippedCount = 0;

        for (final Map.Entry<String, Boolean> entry : moduleStates.entrySet()) {
            final String moduleName = entry.getKey();
            final boolean shouldToggle = entry.getValue();

            final Module module = moduleManager.getModuleFromString(moduleName);
            if (module != null) {
                if (module.isToggled() != shouldToggle) {
                    module.setToggled(shouldToggle);
                    appliedCount++;
                }
            } else {
                LOGGER.warning("Module not found: " + moduleName);
                skippedCount++;
            }
        }

        LOGGER.info("Applied " + appliedCount + " module states, skipped " + skippedCount + " unknown modules");
    }

    /**
     * Checks if the config version is compatible with the current version.
     */
    private boolean isVersionCompatible(final String configVersion) {
        // You can implement version compatibility logic here
        // For now, just check if versions match exactly
        return version.equals(configVersion);
    }

    /**
     * Resets configuration to defaults.
     */
    public boolean resetConfig() {
        try {
            // Turn off all modules
            for (final Module module : moduleManager.getModules().values()) {
                module.setToggled(false);
            }

            // Save the reset state
            final boolean success = saveConfig();

            if (success) {
                LOGGER.info("Configuration reset to defaults");
            }

            return success;

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting configuration", e);
            return false;
        }
    }

    /**
     * Validates the current configuration file.
     */
    public boolean validateConfig() {
        try {
            final List<String> lines = FileUtil.loadFileLines(CONFIG_FILE);

            if (lines == null || lines.isEmpty()) {
                return false;
            }

            // Check version header
            if (!lines.get(0).startsWith(CONFIG_VERSION_PREFIX)) {
                return false;
            }

            // Check each toggle line
            for (int i = 1; i < lines.size(); i++) {
                final String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                if (!line.startsWith(TOGGLE_PREFIX)) {
                    return false;
                }

                // Validate toggle format
                final String content = line.substring(TOGGLE_PREFIX.length());
                final int lastUnderscoreIndex = content.lastIndexOf("_");

                if (lastUnderscoreIndex == -1) {
                    return false;
                }

                final String toggleValue = content.substring(lastUnderscoreIndex + 1);
                if (!toggleValue.equals("true") && !toggleValue.equals("false")) {
                    return false;
                }
            }

            return true;

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Error validating configuration", e);
            return false;
        }
    }

    /**
     * Gets the configuration file size.
     */
    public long getConfigSize() {
        return FileUtil.getFileSize(CONFIG_FILE);
    }

    /**
     * Checks if a configuration file exists.
     */
    public boolean configExists() {
        return FileUtil.exists(CONFIG_FILE);
    }

    /**
     * Data class for authentication token information.
     */
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

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public long getSavedAt() {
            return savedAt;
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