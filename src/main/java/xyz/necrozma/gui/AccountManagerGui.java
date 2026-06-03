package xyz.necrozma.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import xyz.necrozma.Client;
import xyz.necrozma.auth.AuthenticationService;
import xyz.necrozma.login.AuthenticationResult;
import xyz.necrozma.login.CodeLoginHelper;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public final class AccountManagerGui extends GuiScreen {

    private final GuiScreen parent;
    private volatile boolean authInProgress;
    private volatile String statusMessage = "Manage your account from here.";

    public AccountManagerGui(final GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        final int centerX = this.width / 2;
        int y = this.height / 2 - 42;

        this.buttonList.add(new GuiButton(1, centerX - 100, y, 200, 20, "Sign In / Refresh"));
        y += 24;
        this.buttonList.add(new GuiButton(2, centerX - 100, y, 200, 20, "Force Device Login"));
        y += 24;
        this.buttonList.add(new GuiButton(3, centerX - 100, y, 200, 20, "Logout"));
        y += 24;
        this.buttonList.add(new GuiButton(4, centerX - 100, y, 200, 20, "Open Sign-In Page"));
        y += 30;
        this.buttonList.add(new GuiButton(0, centerX - 100, y, 200, 20, "Back"));
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(parent);
                break;
            case 1:
                beginAuthentication(false);
                break;
            case 2:
                beginAuthentication(true);
                break;
            case 3:
                logout();
                break;
            case 4:
                openVerificationUrl();
                break;
            default:
                break;
        }
    }

    private void beginAuthentication(final boolean forceRefresh) {
        if (authInProgress) {
            return;
        }

        final AuthenticationService authService = Client.INSTANCE.getAuthService();
        if (authService == null) {
            statusMessage = "Authentication service is unavailable.";
            return;
        }

        CodeLoginHelper.clearLoginState();
        authInProgress = true;
        statusMessage = forceRefresh ? "Starting forced device login..." : "Starting authentication...";

        final Thread authThread = new Thread(() -> {
            try {
                final boolean success = forceRefresh ? authService.forceRefresh() : authService.authenticate();
                statusMessage = success ? "Authentication successful." : "Authentication failed.";
            } catch (final Exception e) {
                statusMessage = "Authentication error: " + e.getMessage();
            } finally {
                authInProgress = false;
            }
        }, "nixon-account-manager-auth");

        authThread.setDaemon(true);
        authThread.start();
    }

    private void logout() {
        final AuthenticationService authService = Client.INSTANCE.getAuthService();
        if (authService == null) {
            statusMessage = "Authentication service is unavailable.";
            return;
        }

        authService.logout();
        CodeLoginHelper.clearLoginState();
        statusMessage = "Logged out.";
    }

    private void openVerificationUrl() {
        final String url = CodeLoginHelper.getPendingVerificationUrl();
        if (url == null || url.trim().isEmpty()) {
            statusMessage = "No active sign-in URL yet.";
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create(url));
            statusMessage = "Opened sign-in page in your browser.";
        } catch (final Exception e) {
            statusMessage = "Failed to open browser. Copy URL from this screen.";
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();

        final AuthenticationService authService = Client.INSTANCE.getAuthService();
        final AuthenticationResult authResult = authService != null ? authService.getLastAuthResult() : null;
        final boolean isAuthenticated = authService != null && authService.isAuthenticated();
        final String sessionUser = Minecraft.getMinecraft().getSession() != null
                ? Minecraft.getMinecraft().getSession().getUsername()
                : "Unknown";

        int y = 20;
        this.drawCenteredString(this.fontRendererObj, "Account Manager", this.width / 2, y, Color.WHITE.getRGB());
        y += 18;
        this.drawCenteredString(this.fontRendererObj, "Status: " + (isAuthenticated ? "Authenticated" : "Not authenticated"), this.width / 2, y, Color.LIGHT_GRAY.getRGB());
        y += 12;
        this.drawCenteredString(this.fontRendererObj, "Current Session: " + sessionUser, this.width / 2, y, Color.LIGHT_GRAY.getRGB());
        y += 12;

        if (authResult != null) {
            this.drawCenteredString(this.fontRendererObj, "Minecraft Account: " + authResult.getMinecraftUsername(), this.width / 2, y, Color.LIGHT_GRAY.getRGB());
            y += 12;
            this.drawCenteredString(this.fontRendererObj, "UUID: " + authResult.getUuid(), this.width / 2, y, Color.GRAY.getRGB());
            y += 12;
        }

        final String userCode = CodeLoginHelper.getPendingUserCode();
        final String verificationUrl = CodeLoginHelper.getPendingVerificationUrl();
        final String loginStatus = CodeLoginHelper.getLoginStatus();

        this.drawCenteredString(this.fontRendererObj, "Auth Flow: " + loginStatus, this.width / 2, y, Color.GRAY.getRGB());
        y += 14;

        if (userCode != null && !userCode.trim().isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "Device Code: " + userCode, this.width / 2, y, Color.WHITE.getRGB());
            y += 12;
        }

        if (verificationUrl != null && !verificationUrl.trim().isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "Sign-In URL: " + verificationUrl, this.width / 2, y, Color.GRAY.getRGB());
        }

        this.drawCenteredString(this.fontRendererObj, statusMessage, this.width / 2, this.height - 18, authInProgress ? Color.YELLOW.getRGB() : Color.LIGHT_GRAY.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
