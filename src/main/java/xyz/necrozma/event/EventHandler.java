package xyz.necrozma.event;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.event.impl.input.MoveButtonEvent;
import xyz.necrozma.event.impl.motion.*;
import xyz.necrozma.event.impl.packet.EventPacket;
import xyz.necrozma.event.impl.packet.PacketReceiveEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.event.impl.update.WorldChangedEvent;
import xyz.necrozma.gui.ClickGuiNG.ClickGUI;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.gui.strikeless.StrikeGUI;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.impl.motion.Fly;
import xyz.necrozma.util.PlayerUtil;

import java.awt.*;
import java.util.Objects;

public final class EventHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static EntityPlayer target = null;
    public static boolean canUpdateDeaths;

    public static void handle(final Event e) {
        final Module[] modules = Client.INSTANCE.getMM().getModules().values().toArray(new Module[0]);

        if (e instanceof Render2DEvent) {
            final Render2DEvent event = ((Render2DEvent) e);

            RenderUtil.delta2DFrameTime = (System.currentTimeMillis() - RenderUtil.last2DFrame) / 10F;
            RenderUtil.last2DFrame = System.currentTimeMillis();

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onRender2DEvent(event);
                }
            }
        } else if (e instanceof EventUpdate) {
            final EventUpdate event = ((EventUpdate) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onUpdate(event);
                }
            }
        }else if (e instanceof Render3DEvent) {
            final Render3DEvent event = ((Render3DEvent) e);

            RenderUtil.delta3DFrameTime = (System.currentTimeMillis() - RenderUtil.last3DFrame) / 10F;
            RenderUtil.last3DFrame = System.currentTimeMillis();

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onRender3DEvent(event);
                }
            }
        } else if (e instanceof PacketReceiveEvent) {
            final PacketReceiveEvent event = ((PacketReceiveEvent) e);
            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onPacketReceive(event);
                }
            }
        } else if (e instanceof EventPacket) {
            final EventPacket event = ((EventPacket) e);
            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onPacketSend(event);
                }
            }
        }  else if (e instanceof PostMotionEvent) {
            final PostMotionEvent event = ((PostMotionEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onPostMotion(event);
                }
            }
        } else if (e instanceof AttackEvent) {
            final AttackEvent event = ((AttackEvent) e);

            //Statistics
            final Entity entity = event.getTarget();
            if (entity instanceof EntityPlayer) {
                target = (EntityPlayer) entity;
            }

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onAttackEvent(event);
                }
            }

        } else if (e instanceof PreMotionEvent) {
            final PreMotionEvent event = ((PreMotionEvent) e);

            /* Used to reset PlayerUtil.isOnServer() */
            if (mc.thePlayer.ticksExisted == 1) {
                PlayerUtil.serverResponses.clear();
                PlayerUtil.sentEmail = false;
            }

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onPreMotion(event);
                }

                /* Calls events that are always used called whether the module is on or not*/
                if (mc.currentScreen instanceof ClickGUI || mc.currentScreen instanceof StrikeGUI) {
                    module.onUpdateAlwaysInGui();
                }
                module.onUpdateAlways();
            }
        } else if (e instanceof EventKey) {
            final EventKey event = ((EventKey) e);

            for (final Module module : modules) {
                if (module.getKey() == event.getKey()) {
                    module.toggle();
                }
            }
        } else if (e instanceof StrafeEvent) {
            final StrafeEvent event = ((StrafeEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onStrafe(event);
                }
            }
        } else if (e instanceof MoveButtonEvent) {
            final MoveButtonEvent event = ((MoveButtonEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onMoveButton(event);
                }
            }
        } else if (e instanceof MoveEvent) {
            final MoveEvent event = ((MoveEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onMove(event);
                }
            }
        } else if (e instanceof BlockCollideEvent) {
            final BlockCollideEvent event = ((BlockCollideEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onBlockCollide(event);
                }
            }
        } else if (e instanceof WorldChangedEvent) {
            final WorldChangedEvent event = ((WorldChangedEvent) e);

            for (final Module module : modules) {
                if (module.isToggled()) {
                    module.onWorldChanged(event);
                }
            }
        }
    }
}