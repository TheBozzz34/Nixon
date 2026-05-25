package xyz.necrozma.module.impl.render;

import net.minecraft.block.Block;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@ModuleInfo(name = "Xray", description = "Makes ores visible", category = Category.RENDER)
public class Xray extends Module {

    public static final List<Block> BLOCKS = new CopyOnWriteArrayList<>();

    private final BooleanSetting coal = new BooleanSetting("Coal", this, true);
    private final BooleanSetting iron = new BooleanSetting("Iron", this, true);
    private final BooleanSetting gold = new BooleanSetting("Gold", this, true);
    private final BooleanSetting diamond = new BooleanSetting("Diamond", this, true);
    private final BooleanSetting redstone = new BooleanSetting("Redstone", this, true);
    private final BooleanSetting emerald = new BooleanSetting("Emerald", this, true);
    private final BooleanSetting lapis = new BooleanSetting("Lapis", this, true);
    private final BooleanSetting lava = new BooleanSetting("Lava", this, true);

    private int lastSelectionMask = Integer.MIN_VALUE;

    public Xray() {
        refreshVisibleBlocks();
    }

    @Override
    public void onEnable() {
        refreshVisibleBlocks();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onToggle() {
        refreshVisibleBlocks();
        mc.renderGlobal.loadRenderers();
        super.onToggle();
    }

    @Override
    public void onUpdate(final EventUpdate event) {
        final int currentMask = getSelectionMask();

        if (currentMask != lastSelectionMask) {
            lastSelectionMask = currentMask;
            refreshVisibleBlocks();

            if (mc.theWorld != null) {
                mc.renderGlobal.loadRenderers();
            }
        }
    }

    public void addBlocks() {
        refreshVisibleBlocks();
    }

    private void refreshVisibleBlocks() {
        BLOCKS.clear();

        addBlockIfEnabled(coal.isEnabled(), 16);
        addBlockIfEnabled(iron.isEnabled(), 15);
        addBlockIfEnabled(gold.isEnabled(), 14);
        addBlockIfEnabled(diamond.isEnabled(), 56);
        addBlockIfEnabled(redstone.isEnabled(), 73);
        addBlockIfEnabled(redstone.isEnabled(), 74);
        addBlockIfEnabled(emerald.isEnabled(), 129);
        addBlockIfEnabled(lapis.isEnabled(), 21);

        if (lava.isEnabled()) {
            addBlockIfEnabled(true, 10);
            addBlockIfEnabled(true, 11);
        }
    }

    private static void addBlockIfEnabled(final boolean enabled, final int blockId) {
        if (!enabled) return;

        final Block block = Block.getBlockById(blockId);
        if (block != null && !BLOCKS.contains(block)) {
            BLOCKS.add(block);
        }
    }

    public static boolean isXrayBlock(final Block block) {
        return BLOCKS.contains(block);
    }

    private int getSelectionMask() {
        int mask = 0;
        mask |= coal.isEnabled() ? 1 : 0;
        mask |= iron.isEnabled() ? 1 << 1 : 0;
        mask |= gold.isEnabled() ? 1 << 2 : 0;
        mask |= diamond.isEnabled() ? 1 << 3 : 0;
        mask |= redstone.isEnabled() ? 1 << 4 : 0;
        mask |= emerald.isEnabled() ? 1 << 5 : 0;
        mask |= lapis.isEnabled() ? 1 << 6 : 0;
        mask |= lava.isEnabled() ? 1 << 7 : 0;
        return mask;
    }
}
