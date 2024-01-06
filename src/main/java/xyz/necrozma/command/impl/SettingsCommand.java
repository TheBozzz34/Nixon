package xyz.necrozma.command.impl;

import xyz.necrozma.Client;
import xyz.necrozma.command.Command;
import xyz.necrozma.command.CommandInfo;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.settings.Settings;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.ChatUtil;
import xyz.necrozma.module.Module;

import java.util.Objects;

@CommandInfo(name = "settings", description = "Changes module settings", usage = "settings <module> (option) (value)", aliases = {"s"})
public class SettingsCommand extends Command {
    @Override
    public void execute(String... args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Invalid number of arguments! Usage: " + getUsage());
        }
        // check if module exists
        Module module = Client.INSTANCE.getMM().getModuleFromString(args[0]);
        if (module == null) {
            throw new CommandException("Module " + args[0] + " not found!");
        }

        if (args.length == 1) {
            // List settings for the specified module
            ChatUtil.sendMessage("Settings for module " + args[0] + ":", true);
            for (Settings setting : module.settings) {

                if(setting instanceof BooleanSetting) {
                    ChatUtil.sendMessage(setting.getName() + ": " + ((BooleanSetting) setting).isEnabled(), true);
                } else if(setting instanceof NumberSetting) {
                    ChatUtil.sendMessage(setting.getName() + ": " + ((NumberSetting) setting).getValue(), true);
                }

            }
        } else if (args.length == 3) {
            // Assuming args[1] is the setting name and args[2] is the value
            Settings setting = module.getSetting(args[1]);
            if (setting == null) {
                throw new CommandException("Setting " + args[1] + " not found for module " + args[0]);
            }

            if(setting instanceof BooleanSetting) {
                ((BooleanSetting) setting).setEnabled(Boolean.parseBoolean(args[2]));
            } else if(setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(Double.parseDouble(args[2]));
            }
            ChatUtil.sendMessage("Set " + args[1] + " to " + args[2] + " for module " + args[0], true);
        } else {
            throw new CommandException("Invalid number of arguments! Usage: " + getUsage());
        }
    }
}
