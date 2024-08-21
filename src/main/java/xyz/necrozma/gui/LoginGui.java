package xyz.necrozma.gui;

import com.microsoft.aad.msal4j.*;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.client.gui.*;

import net.minecraft.util.ResourceLocation;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Module;
import xyz.necrozma.util.FileUtil;

import java.awt.*;
import java.net.URI;
import java.util.Set;


public final class LoginGui extends GuiScreen {

    private static final TTFFontRenderer fontRenderer = CustomFont.FONT_MANAGER.getFont("Dreamscape 96");

    //Positions
    private ScaledResolution sr;

    private float x;
    private float y;

    private int cocks;
    private static boolean rolled;

    private float screenWidth;
    private float screenHeight;

    private float buttonWidth = 50;
    private float buttonHeight = 20;
    private float gap = 4;
    public static float smoothedX, smoothedY;
    public static float xOffSet;
    public static float yOffSet;

    private static String authority = "https://login.microsoftonline.com/common";
    private static Set<String> scope;
    private static String clientId = "11b2bc64-013f-45a8-b336-e1f79a8d3d86";
    private static String username = "Ethan James";



    public void initGui() {
        System.out.println("Opened login Menu");
        scope.add("user.read");
    }

    @Override
    public void onGuiClosed() {
        mc.timer.timerSpeed = 1;
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {

        if (mc.mouseHelper != null) mc.mouseHelper.mouseGrab(false);

        mc.timer.timerSpeed = 3f;


        RenderUtil.color(new Color(159, 24, 242));
        mc.getTextureManager().bindTexture(new ResourceLocation("nixon/bg/bg.png"));

        final float scale = 1.66f;
        final float amount = height;

        smoothedX = (smoothedX * 250 + xOffSet) / 259;
        smoothedY = (smoothedY * 250 + yOffSet) / 259;

        drawModalRectWithCustomSizedTexture(0, 0, width / scale + smoothedX - 150, height / scale + smoothedY - 100, width, height, width * scale, height * scale);

        // Render the rise text
        screenWidth = fontRenderer.getWidth(Client.INSTANCE.getName());
        screenHeight = fontRenderer.getHeight(Client.INSTANCE.getName());

        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        UIUtil.logoPosition = /*MathUtil.lerp(UIUtil.logoPosition, */sr.getScaledHeight() / 2.0F - (screenHeight / 2.0F) - 6/*, 0.2f)*/;

        x = (sr.getScaledWidth() / 2.0F) - (screenWidth / 2.0F);
        y = (sr.getScaledHeight() / 2.0F) - (screenHeight / 2.0F) - 6;

        // Box
        RenderUtil.roundedRect(x - 10, y + fontRenderer.getHeight() + buttonHeight * 2 + gap * 2 + 2 - 108, 170, 145, 10, new Color(0, 0, 0, 35));

        buttonWidth = 70;
        buttonHeight = 20;
        gap = 4;


        // login
        RenderUtil.roundedRect(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Login with web", x + buttonWidth - 58, y + fontRenderer.getHeight() + 1 + 6, new Color(255, 255, 255, 240).hashCode());

        //Exit
        RenderUtil.roundedRect(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("close", x + gap + 10, y + fontRenderer.getHeight() + buttonHeight + 10 + 3, new Color(255, 255, 255, 240).hashCode());

        if (mouseOver(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        if (mouseOver(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        String version = "Nixon " + Client.INSTANCE.getVersion();

        CustomFont.drawString(version, 2, sr.getScaledHeight() - 12.5, new Color(255, 255, 255, 180).hashCode());


        //Note
        final String message = "Made with <3 by Necrozma";

        CustomFont.drawString(message, sr.getScaledWidth() - CustomFont.getWidth(message) - 2, sr.getScaledHeight() - 12.5, new Color(255, 255, 255, 180).hashCode());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }



    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        // login
        if (mouseOver(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {

            PublicClientApplication pca = null;

            try {
                pca = PublicClientApplication.builder(clientId)
                        .authority(authority)
                        .build();
            } catch (Exception ex) {
                System.out.println("==PublicClientApplication builder failed: " + ex.getMessage());
            }


            Set<IAccount> accountsInCache = pca.getAccounts().join();
            IAccount account = getAccountByUsername(accountsInCache, username);

            IAuthenticationResult result = null;
            try {
                //Attempt to acquire token when user's account is not in the application's token cache
                result = acquireTokenIntegratedWindowsAuth(pca, scope, account, username);
                System.out.println("Account username: " + result.account().username());
                System.out.println("Access token:     " + result.accessToken());
                System.out.println("Id token:         " + result.idToken());
                System.out.println();
            } catch (Exception ex) {
                System.out.println("==Acquiring token failed: " + ex.getMessage());
            }


            //Get list of accounts from the application's token cache, and search them for the configured username
            //getAccounts() will be empty on this first call, as accounts are added to the cache when acquiring a token
            accountsInCache = pca.getAccounts().join();
            account = getAccountByUsername(accountsInCache, username);

            try {
                //Attempt to acquire token again, now that the user's account and a token are in the application's token cache
                result = acquireTokenIntegratedWindowsAuth(pca, scope, account, username);
                System.out.println("Account username: " + result.account().username());
                System.out.println("Access token:     " + result.accessToken());
                System.out.println("Id token:         " + result.idToken());
            } catch (Exception ex) {
                System.out.println("==Acquiring token failed: " + ex.getMessage());
            }




            //Minecraft.getMinecraft().setSession(new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "legacy"));

            //final StringBuilder configBuilder = new StringBuilder();
            //configBuilder.append("Account_Token_").append(result.getRefreshToken()).append("\r\n");

            //FileUtil.saveFile("token.txt", true, configBuilder.toString());


        }

        // close
        if (mouseOver(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            mc.displayGuiScreen(new MainMenu());
        }
    }

    public boolean mouseOver(final float posX, final float posY, final float width, final float height, final float mouseX, final float mouseY) {
        if (mouseX > posX && mouseX < posX + width) {
            return mouseY > posY && mouseY < posY + height;
        }
        return false;
    }
    private static IAuthenticationResult acquireTokenIntegratedWindowsAuth(PublicClientApplication pca,
                                                                           Set<String> scope,
                                                                           IAccount account,
                                                                           String username) throws Exception {

        IAuthenticationResult result;
        try {
            SilentParameters silentParameters =
                    SilentParameters
                            .builder(scope)
                            .account(account)
                            .build();
            // Try to acquire token silently. This will fail on the first acquireTokenIntegratedWindowsAuth() call
            // because the token cache does not have any data for the user you are trying to acquire a token for
            result = pca.acquireTokenSilently(silentParameters).join();
            System.out.println("==acquireTokenSilently call succeeded");
        } catch (Exception ex) {
            if (ex.getCause() instanceof MsalException) {
                System.out.println("==acquireTokenSilently call failed: " + ex.getCause());
                IntegratedWindowsAuthenticationParameters parameters =
                        IntegratedWindowsAuthenticationParameters
                                .builder(scope, username)
                                .build();

                // Try to acquire a token using Integrated Windows Authentication (IWA). You will need to generate a Kerberos ticket.
                // If successful, you should see the token and account information printed out to console
                result = pca.acquireToken(parameters).join();
                System.out.println("==Integrated Windows Authentication flow succeeded");
            } else {
                // Handle other exceptions accordingly
                throw ex;
            }
        }
        return result;
    }

    /**
     * Helper function to return an account from a given set of accounts based on the given username,
     * or return null if no accounts in the set match
     */
    private static IAccount getAccountByUsername(Set<IAccount> accounts, String username) {
        if (accounts.isEmpty()) {
            System.out.println("==No accounts in cache");
        } else {
            System.out.println("==Accounts in cache: " + accounts.size());
            for (IAccount account : accounts) {
                if (account.username().equals(username)) {
                    return account;
                }
            }
        }
        return null;
    }
}


