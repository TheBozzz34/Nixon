package xyz.necrozma.command.impl;

import xyz.necrozma.Client;
import xyz.necrozma.command.Command;
import xyz.necrozma.command.CommandInfo;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.util.ChatUtil;

@CommandInfo(name = "help", description = "Displays all commands", usage = "help <command>", aliases = {"?"})
public class HelpCommand extends Command {
    @Override
    public void execute(String... args) throws CommandException {
        if(args.length > 0) {
            ChatUtil.sendMessage("--------------------", true);
            final Command command = Client.INSTANCE.getCM().getCommand(args[0]).orElseThrow(() -> new CommandException("Command " + args[0] + " not found"));
            ChatUtil.sendMessage("&7" + command.getName() + " &8- &7" + command.getDescription(), true);
            ChatUtil.sendMessage("--------------------", true);
            return;
        }
        ChatUtil.sendMessage("--------------------", true);
        Client.INSTANCE.getCM().getCommands().values().stream().filter(command -> !(command instanceof HelpCommand)).forEach(command -> ChatUtil.sendMessage("&7" + command.getName() + " &8- &7" + command.getDescription(), true));

        ChatUtil.sendMessage("--------------------", true);
    }
}
