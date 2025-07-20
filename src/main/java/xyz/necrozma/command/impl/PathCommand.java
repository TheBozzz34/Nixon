package xyz.necrozma.command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import tv.twitch.chat.Chat;
import xyz.necrozma.Client;
import xyz.necrozma.command.Command;
import xyz.necrozma.command.CommandInfo;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.pathing.Path;
import xyz.necrozma.pathing.PathFinder;
import xyz.necrozma.util.ChatUtil;

import java.awt.*;
import java.util.ConcurrentModificationException;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;


@CommandInfo(name = "path", description = "Calculates the path to the specified point", usage = "path", aliases = {"p"})
public class PathCommand extends Command {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final World world = mc.theWorld;

    @Override
    public void execute(String... args) throws CommandException {

        if (!verifyInput(args)) {
            ChatUtil.sendMessage("Invalid arguments", true);
            return;
        }

       BlockPos start = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
       BlockPos end = new BlockPos(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
       ChatUtil.sendMessage("Calculating path from " + start + " to " + end, true);

        PathFinder.PathValidator validator = new PathFinder.BasicValidator(world);
        PathFinder pathFinder = new PathFinder(validator);
        Path path = pathFinder.findPath(start, end);

        if (path == null) {
            ChatUtil.sendMessage("No path found", true);
            return;
        }
    }

    boolean verifyInput(String... args) {
        if (args.length != 3) {
            ChatUtil.sendMessage("Invalid arguments", true);
            return false;
        }
        try {
            Double.parseDouble(args[0]);
            Double.parseDouble(args[1]);
            Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            ChatUtil.sendMessage("Invalid arguments", true);
            return false;
        }
        return true;

    }
}
