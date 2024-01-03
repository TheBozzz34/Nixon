package xyz.necrozma.command;

import lombok.Getter;
import org.reflections.Reflections;
import tv.twitch.chat.Chat;
import xyz.necrozma.Client;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.util.ChatUtil;

import java.util.*;


@Getter
public final class CommandManager {
    public static final String COMMAND_PREFIX = Client.INSTANCE.getCommandPrefix();

    private Map<String, Command> commands;

    public CommandManager() {
        this.init();
    }

    private void init() {
        this.commands = new HashMap<>();
        try {
            this.register();
        } catch (CommandException e) {
            throw new CommandException("Failed to register commands: " + e.getMessage());
        }
    }

    public boolean handleCommand(final String message) {
        if(message.isEmpty()) {
            return false;
        }
        final String[] args = message.substring(COMMAND_PREFIX.length()).split(" ");

        if(message.equalsIgnoreCase(COMMAND_PREFIX)) {
            ChatUtil.sendMessage("Usage: " + COMMAND_PREFIX + "help", true);
            return true;
        }

        try {
            this.getCommand(args[0]).orElseThrow(() -> new CommandException("Command " + args[0] + " not found")).execute(Arrays.copyOfRange(args, 1, args.length));
        } catch (CommandException ignored) {
        }

        return true;

    }

    private void register() throws CommandException {
        final Reflections reflections = new Reflections("xyz.necrozma.command.impl");
        final Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        for(final Class<? extends Command> command : classes) {
            try {
                final Command comm = command.newInstance();
                this.commands.put(comm.getName(), comm);
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("Failed to register command " + command.getName() + ": " + e.getMessage());
            }
        }
    }

    public Optional<Command> getCommand(final String commandName) {
        final Command command = this.commands.get(commandName);
        if(command != null) {
            return Optional.of(command);
        } else {
            return Optional.ofNullable(this.commands.values()
                    .stream()
                    .filter(cmd -> cmd.isAlias(commandName))
                    .findFirst()
                    .orElseThrow(() ->
                            new CommandException("Command " + commandName + " not found")

            ));
        }
    }
}
