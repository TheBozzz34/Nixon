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

@CommandInfo(name = "toggle", description = "Toggle a module", usage = "toggle <module>", aliases = {"t"})
public class ToggleCommand extends Command {
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
            module.toggle();
            ChatUtil.sendMessage("Toggled " + args[0], true);
        } else {
            throw new CommandException("Invalid number of arguments! Usage: " + getUsage());
        }
    }
}
