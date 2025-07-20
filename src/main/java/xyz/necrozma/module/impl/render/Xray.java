package xyz.necrozma.module.impl.render;

import lombok.Getter;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.util.ChatUtil;

import javax.swing.text.html.BlockView;
import java.util.ArrayList;
import java.util.List;


@ModuleInfo(name = "Xray", description = "Makes ores visible", category = Category.RENDER)
public class Xray extends Module {

    public static final List<Block> BLOCKS = new ArrayList<>();

    public Xray() {
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onToggle() {
        mc.renderGlobal.loadRenderers();
        super.onToggle();
    }

    public void addBlocks() {
        BLOCKS.add(Block.getBlockById(16));
        BLOCKS.add(Block.getBlockById(15));
        BLOCKS.add(Block.getBlockById(14));
        BLOCKS.add(Block.getBlockById(56));
        BLOCKS.add(Block.getBlockById(73));
        BLOCKS.add(Block.getBlockById(129));
        BLOCKS.add(Block.getBlockById(21));
        BLOCKS.add(Block.getBlockById(73));
        BLOCKS.add(Block.getBlockById(10));
        BLOCKS.add(Block.getBlockById(11));
    }
}
