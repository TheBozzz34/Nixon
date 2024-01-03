package xyz.necrozma.exception;

import net.minecraft.util.EnumChatFormatting;
import xyz.necrozma.util.ChatUtil;

public class CommandException extends IllegalArgumentException {
    public CommandException(String message) {
        super(message);
        ChatUtil.sendMessage(EnumChatFormatting.RED + message, true);
    }
}
