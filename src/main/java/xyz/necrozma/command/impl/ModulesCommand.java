package xyz.necrozma.command.impl;

import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.command.Command;
import xyz.necrozma.command.CommandInfo;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.util.ChatUtil;

@CommandInfo(name = "modules", description = "Displays all modules", usage = "modules", aliases = {"mods"})
public class ModulesCommand extends Command {
    @Override
    public void execute(String... args) throws CommandException {
        ChatUtil.sendMessage("--------------------", true);
        Client.INSTANCE.getMM().getModules().values().forEach(module -> ChatUtil.sendMessage("&7" + module.getName() + " &8- &7" + module.getDescription() + " &8[&7" + getPrettyKeyName(module.getKey()) + "&8]", true));
        ChatUtil.sendMessage("--------------------", true);
    }

    private String getPrettyKeyName(int key) {
        return Keyboard.getKeyName(key).toUpperCase();
    }
}
