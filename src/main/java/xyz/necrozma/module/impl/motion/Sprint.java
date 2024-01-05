package xyz.necrozma.module.impl.motion;

import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.util.ChatUtil;

@ModuleInfo(name = "Sprint", description = "Automatically sprints", category = Category.MOVEMENT)
public final class Sprint extends Module {
    public Sprint() {
        setKey(Keyboard.KEY_R);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    private final Listener<EventUpdate> listener = new Listener<>(e -> {
        if (mc.thePlayer != null) {
            if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isUsingItem() ) {
                mc.thePlayer.setSprinting(true);
            }
        }
    });
}