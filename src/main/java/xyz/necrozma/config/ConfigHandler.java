package xyz.necrozma.config;


import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import tv.twitch.chat.Chat;
import xyz.necrozma.Client;
import xyz.necrozma.gui.guitheme.Theme;
import xyz.necrozma.gui.guitheme.ThemeUtil;
import xyz.necrozma.module.Module;
import xyz.necrozma.notification.NotificationType;
import xyz.necrozma.settings.Settings;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.ChatUtil;
import xyz.necrozma.util.FileUtil;

@UtilityClass
public final class ConfigHandler {

    private final String s = File.separator;

    public void save(final String name) {
        final StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("Rise_Version_").append(Client.INSTANCE.getVersion()).append("\r\n");
        configBuilder.append("MainMenuTheme_").append(Client.INSTANCE.getGuiTheme().getCurrentTheme()).append("\r\n");
        configBuilder.append("ClientName_").append(ThemeUtil.getCustomClientName()).append("\r\n");

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            final String moduleName = m.getName();
            configBuilder.append("Toggle_").append(moduleName).append("_").append(m.isToggled()).append("\r\n");

            for (final Settings setting : m.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    configBuilder.append("BooleanSetting_").append(moduleName).append("_").append(setting.name).append("_").append(((BooleanSetting) setting).enabled).append("\r\n");
                }
                if (setting instanceof NumberSetting) {
                    configBuilder.append("NumberSetting_").append(moduleName).append("_").append(setting.name).append("_").append(((NumberSetting) setting).value).append("\r\n");
                }
                if (setting instanceof ModeSetting) {
                    configBuilder.append("ModeSetting_").append(moduleName).append("_").append(setting.name).append("_").append(((ModeSetting) setting).getMode()).append("\r\n");
                }
            }
        }

        FileUtil.saveFile("Config" + s + name + ".txt", true, configBuilder.toString());
        Client.INSTANCE.getNM().registerNotification("Config saved " + name + ".");
    }

    public void load(final String name) {
        final String config = FileUtil.loadFile("Config" + s + name + ".txt");
        if (config == null) {
            Client.INSTANCE.getNM().registerNotification("Config does not exist.");
            return;
        }

        final String[] configLines = config.split("\r\n");

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            if (m.isToggled()) {
                m.toggle();
            }
        }

        boolean gotConfigVersion = false;
        for (final String line : configLines) {

            final String[] split = line.split("_");
            if (split[0].contains("Rise")) {
                if (split[1].contains("Version")) {
                    gotConfigVersion = true;

                    final String clientVersion = Client.INSTANCE.getVersion();
                    final String configVersion = split[2];

                    if (!clientVersion.equalsIgnoreCase(configVersion)) {
                        ChatUtil.sendMessage("This config was made in a different version of Rise! Incompatibilities are expected.");
                        Client.INSTANCE.getNM().registerNotification(
                                "This config was made in a different version of Rise! Incompatibilities are expected.", NotificationType.WARNING
                        );
                    }
                }
            }

            if (split[0].contains("MainMenuTheme")) {
                Client.INSTANCE.getGuiTheme().setCurrentTheme(Theme.valueOf(split[1]));
                continue;
            }

            if (split[0].contains("ClientName")) {
                ThemeUtil.setCustomClientName(split.length > 1 ? split[1] : "");
                continue;
            }

            if (split[0].contains("ChatMSG")) {
                ChatUtil.sendMessage(split[1]);
            }

            if (split.length < 3) continue;

            if (split[0].contains("Toggle")) {
                if (split[2].contains("true")) {
                    if (Client.INSTANCE.getMM().getModuleFromString(split[1]) != null) {
                        final Module module = Objects.requireNonNull(Client.INSTANCE.getMM().getModuleFromString(split[1]));

                        if (!module.isToggled()) {
                            module.toggle();
                        }
                    }
                }
            }

            final Settings setting = Client.INSTANCE.getMM().getSetting(split[1], split[2]);

            if (Client.INSTANCE.getMM().getModuleFromString(split[1]) == null)
                continue;

            if (split[0].contains("BooleanSetting") && setting instanceof BooleanSetting) {
                if (split[3].contains("true")) {
                    ((BooleanSetting) setting).enabled = true;
                }

                if (split[3].contains("false")) {
                    ((BooleanSetting) setting).enabled = false;
                }
            }

            if (split[0].contains("NumberSetting") && setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(Double.parseDouble(split[3]));
            }

            if (split[0].contains("ModeSetting") && setting instanceof ModeSetting) {
                ((ModeSetting) setting).set(split[3]);
            }
        }
        if (!gotConfigVersion) {
            ChatUtil.sendMessage("This config was made in a different version of Rise! Incompatibilities are expected.");
            Client.INSTANCE.getNM().registerNotification(
                    "This config was made in a different version of Rise! Incompatibilities are expected.", NotificationType.WARNING
            );
        }


        //Notification
        Client.INSTANCE.getNM().registerNotification("Config loaded " + name + ".");
        Client.amountOfConfigsLoaded++;
    }

    public void loadFromList(final List<String> list) {
        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            if (m.isToggled() && !m.getName().toLowerCase().contains("clickgui")) {
                m.toggle();
            }
        }

        for (final String line : list) {
            if (line == null) return;

            final String[] split = line.split("_");

            if (Client.INSTANCE.getMM().getModuleFromString(split[1]) != null) {
                final Module module = Objects.requireNonNull(Client.INSTANCE.getMM().getModuleFromString(split[1]));

                if (module.getName().toLowerCase().contains("clickgui")) {
                    continue;
                }
            }

            if (split[0].contains("MainMenuTheme")) {
                Client.INSTANCE.getGuiTheme().setCurrentTheme(Theme.valueOf(split[1]));
                continue;
            }

            if (split[0].contains("ClientName")) {
                ThemeUtil.setCustomClientName(split[1]);
                continue;
            }

            if (split[0].contains("Toggle")) {
                if (split[2].contains("true")) {
                    if (Client.INSTANCE.getMM().getModuleFromString(split[1]) != null) {
                        final Module module = Objects.requireNonNull(Client.INSTANCE.getMM().getModuleFromString(split[1]));

                        if (!module.isToggled()) {
                            module.toggle();
                        }
                    }
                }
            }

            final Settings setting = Client.INSTANCE.getMM().getSetting(split[1], split[2]);

            if (split[0].contains("BooleanSetting") && setting instanceof BooleanSetting) {
                if (split[3].contains("true")) {
                    ((BooleanSetting) setting).enabled = true;
                }

                if (split[3].contains("false")) {
                    ((BooleanSetting) setting).enabled = false;
                }
            }

            if (split[0].contains("NumberSetting") && setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(Double.parseDouble(split[3]));
            }

            if (split[0].contains("ModeSetting") && setting instanceof ModeSetting) {
                ((ModeSetting) setting).set(split[3]);
            }
        }

        Client.amountOfConfigsLoaded++;
    }

    public void list() {
        if (!FileUtil.exists("Config\\"))
            Client.INSTANCE.getNM().registerNotification("No configs created.");
        final File configFolder = FileUtil.getFileOrPath("Config\\");

        if (configFolder.listFiles() == null || Objects.requireNonNull(configFolder.listFiles()).length < 1) {
            Client.INSTANCE.getNM().registerNotification("No configs created.");
        } else {
            ChatUtil.sendMessage("&7[&bNixon&7]&r List of configuration files: ");

            for (final File file : Objects.requireNonNull(configFolder.listFiles())) {
                ChatUtil.sendMessage("&7[&bNixon&7]&r " + file.getName().replace(".txt", ""));
            }
        }
    }

    public void delete(final String name) {
        if (FileUtil.exists("Config\\" + name + ".txt")) {
            Client.INSTANCE.getNM().registerNotification("Config does not exist.");
            return;
        }

        FileUtil.delete("Config\\" + name + ".txt");
        Client.INSTANCE.getNM().registerNotification("Config " + name + " has been deleted.");
    }

    public void loadFromRes(final String name) {
        final URL defaultImage = ConfigHandler.class.getResource(s + "assets" + s + "minecraft" + s + "rise" + s + "defaultcfg" + s + name + ".txt");
        final File loadFile;
        try {
            loadFile = new File(defaultImage.toURI());
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            Client.INSTANCE.getNM().registerNotification("Error while loading config");
            return;
        }

        Client.INSTANCE.getNM().registerNotification(loadFile.getAbsolutePath());

        final boolean exists = loadFile.exists();

        if (!exists) {
            Client.INSTANCE.getNM().registerNotification("Error while loading config");
            return;
        }

        Scanner scan = null;

        try {
            scan = new Scanner(loadFile);
        } catch (final IOException e1) {
            Client.INSTANCE.getNM().registerNotification("Error while loading config");
            e1.printStackTrace();
        }

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            if (m.isToggled()) {
                m.toggle();
            }
        }

        while (true) {
            assert scan != null;
            if (!scan.hasNextLine()) break;

            final String line = scan.nextLine();

            if (line == null) return;

            final String[] spit = line.split("_");

            if (spit[0].contains("Toggle")) {
                if (spit[2].contains("true")) {
                    if (Client.INSTANCE.getMM().getModuleFromString(spit[1]) != null) {
                        final Module module = Objects.requireNonNull(Client.INSTANCE.getMM().getModuleFromString(spit[1]));

                        if (!module.isToggled()) {
                            module.toggle();
                        }
                    }
                }
            }

            final Settings setting = Client.INSTANCE.getMM().getSetting(spit[1], spit[2]);

            if (spit[0].contains("BooleanSetting") && setting instanceof BooleanSetting) {
                if (spit[3].contains("true")) {
                    ((BooleanSetting) setting).enabled = true;
                }

                if (spit[3].contains("false")) {
                    ((BooleanSetting) setting).enabled = false;
                }
            }

            if (spit[0].contains("NumberSetting") && setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(Double.parseDouble(spit[3]));
            }

            if (spit[0].contains("ModeSetting") && setting instanceof ModeSetting) {
                ((ModeSetting) setting).set(spit[3]);
            }
        }

        //Notification
        Client.INSTANCE.getNM().registerNotification("Config loaded " + name + ".");
        Client.amountOfConfigsLoaded++;
    }
}