package xyz.necrozma.command.impl;

import xyz.necrozma.Client;
import xyz.necrozma.command.Command;
import xyz.necrozma.command.CommandInfo;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.util.ChatUtil;

@CommandInfo(name = "ping", description = "Responds with Pong!", usage = "ping", aliases = {"p"})
public class PingCommand extends Command {
    @Override
    public void execute(String... args) throws CommandException {
        ChatUtil.sendMessage("Pong!", true);
    }
}
