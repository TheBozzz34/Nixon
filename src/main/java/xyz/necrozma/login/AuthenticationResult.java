package xyz.necrozma.login;

public class AuthenticationResult {
    private final String accessToken;
    private final String username;
    private final int expiresIn;
    private final String uuid;
    private final String minecraftUsername;

    public AuthenticationResult(String accessToken, String username, int expiresIn, String uuid, String minecraftUsername) {
        this.accessToken = accessToken;
        this.username = username;
        this.expiresIn = expiresIn;
        this.uuid = uuid;
        this.minecraftUsername = minecraftUsername;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    @Override
    public String toString() {
        return "AuthenticationResult{" +
                "username='" + username + '\'' +
                ", minecraftUsername='" + minecraftUsername + '\'' +
                ", uuid='" + uuid + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}