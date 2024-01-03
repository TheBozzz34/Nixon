package xyz.necrozma.command;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.Validate;
import xyz.necrozma.Client;
import xyz.necrozma.exception.CommandException;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
    protected static final Minecraft mc = Client.INSTANCE.getMC();

    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;

    public Command() {
        final CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);
        Validate.notNull(commandInfo, "Bad annotation!");
        this.name = commandInfo.name();
        this.description = commandInfo.description();
        this.usage = commandInfo.usage();
        this.aliases = Arrays.asList(commandInfo.aliases());
    }

    public boolean isAlias(String alias) {
        return aliases.stream().anyMatch(alias::equalsIgnoreCase);
    }

    public abstract void execute(String... args) throws CommandException;
}
