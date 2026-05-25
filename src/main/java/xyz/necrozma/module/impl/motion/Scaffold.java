package xyz.necrozma.module.impl.motion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.input.MoveButtonEvent;
import xyz.necrozma.event.impl.motion.CanPlaceBlockEvent;
import xyz.necrozma.event.impl.motion.PostMotionEvent;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.motion.StrafeEvent;
import xyz.necrozma.event.impl.packet.EventPacket;
import xyz.necrozma.event.impl.packet.PacketReceiveEvent;
import xyz.necrozma.event.impl.render.BlurEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NoteSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.*;
import de.florianmichael.viamcp.ViaMCP;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ModuleInfo(name = "Scaffold", description = "Bridges automatically for you", category = Category.MOVEMENT)
public class Scaffold extends Module {


    private static final Minecraft minecraft = Client.INSTANCE.getMC();

    private final NoteSetting modeSettings = new NoteSetting("Mode Settings", this);
    private final ModeSetting rotations = new ModeSetting("Rotations", this, "Normal", "None", "Normal", "Simple", "Down", "Snap", "Bruteforce", "Pitch Abuse");
    private final ModeSetting tower = new ModeSetting("Tower", this, "None", "None", "Vanilla", "Slow", "Verus", "Intave", "Hypixel");
    private final ModeSetting movementFix = new ModeSetting("Movement Fix", this, "None", "None", "Yaw", "Hidden");
    private final ModeSetting sprint = new ModeSetting("Sprint", this, "Normal", "Normal", "Disabled", "Bypass", "Legit");
    private final ModeSetting blockCounter = new ModeSetting("Block Counter", this, "Normal", "Normal", "MC");

    private final NoteSetting generalSettings = new NoteSetting("General Settings", this);
    private final NumberSetting timer = new NumberSetting("Timer", this, 1, 0.1, 10, 0.1);
    private final NumberSetting towerTimer = new NumberSetting("Tower Timer", this, 1, 0.1, 10, 0.1);
    private final BooleanSetting downwards = new BooleanSetting("Downwards", this, true);
    private final BooleanSetting safewalk = new BooleanSetting("Safe Walk", this, true);
    private final BooleanSetting strafe = new BooleanSetting("Strafe", this, false);
    private final BooleanSetting sameY = new BooleanSetting("Same Y", this, false);
    private final BooleanSetting swing = new BooleanSetting("Swing", this, true);

    private final NoteSetting bypassSettings = new NoteSetting("Bypass Settings", this);
    private final NumberSetting range = new NumberSetting("Range", this, 3, 1, 6, 0.5);
    private final BooleanSetting randomiseRotationSpeedOnEnable = new BooleanSetting("Randomise Rotation Speed On Enable", this, false);
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", this, 50, 5, 360, 5);
    private final NumberSetting randomisation = new NumberSetting("Randomisation", this, 1, 0, 6, 0.1);
    private final NumberSetting placeDelay = new NumberSetting("Place Delay", this, 0, 0, 5, 0.1);
    private final BooleanSetting randomisePlaceDelay = new BooleanSetting("Randomise Place Delay", this, false);
    private final NumberSetting speedMultiplier = new NumberSetting("Speed Multiplier", this, 1, 0, 2, 0.05);
    private final NumberSetting eagle = new NumberSetting("Eagle", this, 4, 0, 15, 1, "15-Never", "0-Packet");
    private final BooleanSetting ignoreSpeed = new BooleanSetting("Ignore Speed", this, false);
    private final BooleanSetting towerMove = new BooleanSetting("Tower Move", this, true);
    private final ModeSetting rayCast = new ModeSetting("Ray Cast", this, "Normal", "Normal", "Strict", "Off");
    private final ModeSetting placeOn = new ModeSetting("Place on", this, "Legit", "Legit", "Post");
    private final BooleanSetting dragClick = new BooleanSetting("Drag Click", this, false);
    private final BooleanSetting jitter = new BooleanSetting("Jitter", this, false);
    private final BooleanSetting telly = new BooleanSetting("Telly", this, false);
    private final BooleanSetting hideJumps = new BooleanSetting("Hide Jumps", this, false);

    private Vec3 targetBlock;
    private List<Vec3> placePossibilities = new ArrayList<>();
    private EnumFacingOffset enumFacing;
    private BlockPos blockFace;

    private float targetYaw, targetPitch, yaw, lastYaw, lastPitch;
    public static float pitch;

    private boolean lastGround;
    private int blockCount;
    private int ticksOnAir;
    private double startY;
    private int slot;
    private int offGroundTicks;
    private int blocksPlaced;
    private boolean sneaking;
    private boolean shiftPressed;
    private int failedPlacementTicks;
    TimeUtil timer3 = new TimeUtil();

    private static final Client client = Client.INSTANCE;

    @Override
    public void onUpdateAlwaysInGui() {
        towerMove.hidden = tower.is("None") || tower.is("Slow") || tower.is("Hypixel");

        towerTimer.hidden = tower.is("None");
    }

    @Override
    public void onEnable() {
        slot = mc.thePlayer.inventory.currentItem;

        yaw = lastYaw = mc.thePlayer.rotationYaw;
        pitch = lastPitch = mc.thePlayer.rotationPitch;

        if (rotations.is("Pitch Abuse")) {
            targetYaw = mc.thePlayer.rotationYaw;
            targetPitch = 94;
        } else {
            targetYaw = mc.thePlayer.rotationYaw - 180;
            targetPitch = 90;
        }

        startY = mc.thePlayer.posY;
        resetPlacementTarget();
        failedPlacementTicks = 0;
        shiftPressed = false;

        if (randomiseRotationSpeedOnEnable.isEnabled()) {
            rotationSpeed.setValue(50 + (85 - 50) * Math.random());
        }
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final Packet<?> p = event.getPacket();

        if (p instanceof S2FPacketSetSlot)
            event.setCancelled(true);
    }

    @Override
    public void onDisable() {
        if (slot != mc.thePlayer.inventory.currentItem)
            PacketUtil.sendPacketWithoutEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

        EntityPlayer.movementYaw = null;
        mc.timer.timerSpeed = 1;
        mc.gameSettings.keyBindSneak.setKeyPressed(false);
        EntityPlayer.enableCameraYOffset = false;

        if (sneaking) {
            sneaking = false;
            PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public void onStrafe(final StrafeEvent event) {
        if (movementFix.is("Hidden")) {
            event.setCancelled(true);
            silentRotationStrafe(event, yaw);
        }
    }

    @Override
    public void onRender3DEvent(final Render3DEvent event) {
        if (!rotations.is("None")) {
            if (enumFacing == null || blockFace == null)
                return;

            this.calculateRotations();
        }
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {

        if (PlayerUtil.getBlockRelativeToPlayer(0, -1, 0) instanceof BlockAir)
            ticksOnAir++;
        else
            ticksOnAir = 0;

        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else
            offGroundTicks++;

        EntityPlayer.enableCameraYOffset = false;

        if (mc.thePlayer.posY > startY && hideJumps.isEnabled() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            EntityPlayer.enableCameraYOffset = true;
            EntityPlayer.cameraYPosition = startY;
        }

        int blocks = 0;

        if (strafe.isEnabled()) MoveUtil.strafe();

        for (int i = 36; i < 45; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                final Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                if (block.isFullCube() && !BlockUtil.BLOCK_BLACKLIST.contains(block))
                    blocks += itemStack.getStackSize();
            }
        }

        blockCount = blocks;

        if (mc.thePlayer.onGround || (mc.gameSettings.keyBindJump.isKeyDown() && !sameY.isEnabled()))
            startY = mc.thePlayer.posY;

        final int blockSlot = BlockUtil.findBlock() - 36;

        if (blockSlot < 0 || blockSlot > 9)
            return;

        switch (sprint.getMode()) {
            case "Disabled": {
                mc.gameSettings.keyBindSprint.setKeyPressed(false);
                mc.thePlayer.setSprinting(false);
                break;
            }

            case "Bypass": {
                mc.thePlayer.setSprinting(false);
                break;
            }

            case "Legit": {
                if (Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(yaw)) > 90) {
                    mc.gameSettings.keyBindSprint.setKeyPressed(false);
                    mc.thePlayer.setSprinting(false);
                }
                break;
            }
        }

        resetPlacementTarget();
        placePossibilities = getPlacePossibilities();

        if (placePossibilities.isEmpty()) {
            placePossibilities = getPlacePossibilitiesPredicted();
        }


        if (placePossibilities.isEmpty())
            return;

        placePossibilities.sort(Comparator.comparingDouble(this::getPlacementScore));

        if (!selectBestPlacementTarget())
            return;

        if (downwards.isEnabled() && mc.gameSettings.keyBindSneak.isKeyDown() && mc.thePlayer.onGround && enumFacing != null && enumFacing.getEnumFacing() != null)
            enumFacing.enumFacing = EnumFacing.DOWN;

        if (sameY.isEnabled() && mc.thePlayer.posY < startY) startY = mc.thePlayer.posY;

        updateBridgeSneakState();

        if (blocksPlaced > eagle.getValue()) blocksPlaced = 1;

        if (!rotations.is("None")) {
            event.setYaw(yaw);
            event.setPitch(pitch);

            mc.thePlayer.renderYawOffset = yaw;
            mc.thePlayer.rotationYawHead = yaw;

//            mc.thePlayer.rotationYaw = yaw;
//            mc.thePlayer.rotationPitch = pitch;

            lastYaw = yaw;
            lastPitch = pitch;
        } else {
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        }

        if (placePossibilities.isEmpty() || targetBlock == null || enumFacing == null || blockFace == null || slot < 0 || slot > 9)
            return;

        mc.timer.timerSpeed = (float) timer.getValue();

        if (mc.gameSettings.keyBindJump.isKeyDown() && (towerMove.isEnabled() && !(tower.is("Slow") || tower.is("Hypixel")) || !MoveUtil.isMoving()) && (!(PlayerUtil.getBlockRelativeToPlayer(0, -1, 0) instanceof BlockAir) || tower.is("Intave") || tower.is("Slow")) && !client.getMM().getModule(Speed.class).isToggled()) {
            mc.timer.timerSpeed = (float) towerTimer.getValue();

            switch (tower.getMode()) {
                case "Vanilla": {
                    mc.thePlayer.motionY = 0.42F;
                    break;
                }

                case "Hypixel":
                    if (mc.thePlayer.onGround)
                        mc.thePlayer.motionY = 0.4F;

                    if (offGroundTicks == 3) mc.thePlayer.motionY -= 0.02;
                    break;

                case "Slow": {
                    if (mc.thePlayer.onGround)
                        mc.thePlayer.motionY = 0.4F;
                    else if (PlayerUtil.getBlockRelativeToPlayer(0, -1, 0) instanceof BlockAir)
                        mc.thePlayer.motionY -= 0.4F;

                    MoveUtil.stop();
                    break;
                }

                case "Verus": {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        mc.thePlayer.motionY = 0.42F;
                    }
                    break;
                }

                case "Intave": {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.40444491418477924;
                    }

                    if (offGroundTicks == 5) {
                        mc.thePlayer.motionY = MoveUtil.getPredictedMotionY(mc.thePlayer.motionY);
                    }
                    break;
                }
            }
        }

        final double baseSpeed = getBaseSpeed();
        final double speedMultiplier = this.speedMultiplier.getValue();
        if (Math.abs(speedMultiplier - 1.0) > 1E-4 && mc.thePlayer.onGround && !(mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() > 2 - 1) && baseSpeed != 0) {
            MoveUtil.strafe(baseSpeed * speedMultiplier);
        }

        lastGround = mc.thePlayer.onGround;
    }

    public double getBaseSpeed() {
        if (mc.gameSettings.keyBindSprint.isKeyDown()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && !ignoreSpeed.isEnabled()) {
                if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 == 1) {
                    return 0.18386012061481244;
                } else {
                    return 0.21450346015841276;
                }
            } else {
                return 0.15321676228437875;
            }
        } else {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && !ignoreSpeed.isEnabled()) {
                if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 == 1) {
                    return 0.14143085686761;
                } else {
                    return 0.16500264553372018;
                }
            } else {
                return 0.11785905094607611;
            }
        }
    }

    public float[] calculateRotations() {
        if (((ticksOnAir >= placeDelay.getValue()) || rotations.is("Down") || rotations.is("Snap")) || movementFix.is("Yaw")) {
            final float[] rotations = BlockUtil.getDirectionToBlock(blockFace.getX(), blockFace.getY(), blockFace.getZ(), enumFacing.getEnumFacing());

            if (movementFix.is("Yaw")) {
                targetYaw = (float) (MoveUtil.getDirectionWrappedTo90() * (180 / Math.PI) - 180);
                targetPitch = rotations[1];

                switch (this.rotations.getMode()) {
                    case "Snap": {
                        if (ticksOnAir >= placeDelay.getValue()) {
                            targetYaw = (float) (MoveUtil.getDirectionWrappedTo90() * (180 / Math.PI) - 180);
                            targetPitch = rotations[1];
                        } else {
                            targetYaw = (float) (mc.thePlayer.rotationYaw + Math.random());
                            targetPitch = mc.thePlayer.rotationPitch;
                        }
                        break;
                    }
                }
            } else {
                switch (this.rotations.getMode()) {
                    case "Normal": {
                        targetYaw = rotations[0];
                        targetPitch = rotations[1];
                        break;
                    }

                    case "Simple": {
                        float yaw = 0;

                        switch (enumFacing.getEnumFacing()) {
                            case SOUTH: {
                                yaw = 180;
                                break;
                            }

                            case EAST: {
                                yaw = 90;
                                break;
                            }

                            case WEST: {
                                yaw = -90;
                                break;
                            }
                        }

                        targetYaw = yaw;
                        targetPitch = 90;
                        break;
                    }

                    case "Down": {

                        float rotationYaw = mc.thePlayer.rotationYaw;

                        if (mc.thePlayer.moveForward < 0 && mc.thePlayer.moveStrafing == 0) {
                            rotationYaw += 180;
                        }

                        if (mc.thePlayer.moveStrafing > 0) {
                            rotationYaw -= 90;
                        }

                        if (mc.thePlayer.moveStrafing < 0) {
                            rotationYaw += 90;
                        }

                        this.yaw = (float) (Math.toRadians(rotationYaw) * (180 / Math.PI) - 180 + Math.random());
                        this.pitch = (float) (87 + Math.random());

                        break;
                    }

                    case "Snap": {
                        targetYaw = rotations[0];
                        targetPitch = rotations[1];

                        if (ticksOnAir <= placeDelay.getValue()) {
                            targetYaw = (float) (mc.thePlayer.rotationYaw + Math.random());
                            targetPitch = mc.thePlayer.rotationPitch;
                        }
                        break;
                    }

                    case "Bruteforce": {
                        boolean found = false;
                        for (float yaw = mc.thePlayer.rotationYaw - 180; yaw <= mc.thePlayer.rotationYaw + 360 - 180 && !found; yaw += 45) {
                            for (float pitch = 90; pitch > 30 && !found; pitch -= 1) {
                                if (BlockUtil.lookingAtBlock(blockFace, yaw, pitch, enumFacing.getEnumFacing(), rayCast.is("Strict"))) {
                                    targetYaw = yaw;
                                    targetPitch = pitch;
                                    found = true;
                                }
                            }
                        }

                        if (!found) {
                            targetYaw = (float) (rotations[0] + (Math.random() - 0.5) * 4);
                            targetPitch = (float) (rotations[1] + (Math.random() - 0.5) * 4);
                        }
                        break;
                    }

                    case "Pitch Abuse":
                        boolean found = false;

                        for (float yaw = mc.thePlayer.rotationYaw; yaw <= mc.thePlayer.rotationYaw + 360 && !found; yaw += 45) {
                            for (float pitch = 90; pitch < 180 && !found; pitch += 1) {
                                if (BlockUtil.lookingAtBlock(blockFace, yaw, pitch, enumFacing.getEnumFacing(), rayCast.is("Strict"))) {
                                    targetYaw = yaw;
                                    targetPitch = pitch;
                                    found = true;
                                }
                            }
                        }

                        for (yaw = mc.thePlayer.rotationYaw - 180; yaw <= mc.thePlayer.rotationYaw + 360 - 180 && !found; yaw += 45) {
                            for (float pitch = 90; pitch > 30 && !found; pitch -= 1) {
                                if (BlockUtil.lookingAtBlock(blockFace, yaw, pitch, enumFacing.getEnumFacing(), rayCast.is("Strict"))) {
                                    targetYaw = yaw;
                                    targetPitch = pitch;
                                    found = true;
                                }
                            }
                        }

                        if (!found) {
                            targetYaw = mc.thePlayer.rotationYaw;
                            targetPitch = 94;
                        }
                        break;
                }
            }
        }

        final int fps = (int) (Minecraft.getDebugFPS() / 20.0F);

        final float rotationSpeed = (float) (this.rotationSpeed.getValue() + Math.random() * 20) * 6 / fps;

        final float deltaYaw = (((targetYaw - lastYaw) + 540) % 360) - 180;
        final float deltaPitch = targetPitch - lastPitch;

        final float distanceYaw = MathHelper.clamp_float(deltaYaw, -rotationSpeed, rotationSpeed);
        final float distancePitch = MathHelper.clamp_float(deltaPitch, -rotationSpeed, rotationSpeed);

        yaw = lastYaw + distanceYaw;
        pitch = lastPitch + distancePitch;

        targetPitch += (float) (this.randomisation.getValue() * (Math.random() - 0.5) * 3);
        targetYaw += (float) (this.randomisation.getValue() * (Math.random() - 0.5) * 3);

        if (rotationSpeed >= 355) {
            yaw = targetYaw;
            pitch = targetPitch;
        }

        final float[] currentRotations = new float[]{yaw, pitch};
        final float[] lastRotations = new float[]{lastYaw, lastPitch};

        final float[] fixedRotations = RotationUtil.getFixedRotation(currentRotations, lastRotations);

        yaw = fixedRotations[0];
        pitch = fixedRotations[1];

        if (!rotations.is("Pitch Abuse")) pitch = MathHelper.clamp_float(pitch, -90, 90);

        return new float[]{yaw, pitch};
    }

    @Override
    public void onRender2DEvent(final Render2DEvent event) {
        if (blockCount == 0)
            return;

        final ScaledResolution sr = event.getScaledResolution();
        final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(slot);
        Color color;

        if (blockCount <= 63) {
            color = Color.RED;
        } else color = Color.GREEN;


        if (blockCounter.is("MC")) {
            final int height = sr.getScaledHeight() / 2;

            mc.fontRendererObj.drawStringWithShadow(String.valueOf(blockCount), sr.getScaledWidth() / 2F + 1F, height + 9, color.getRGB());

            if (itemStack != null) {
                GlStateManager.pushMatrix();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int) (sr.getScaledWidth() / 2F - 17F), height + 4);
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            } else
                CustomFont.drawCenteredString("?", sr.getScaledWidth() / 2F + 0.5F, height + 6, -1);
        }


        if (blockCounter.is("Normal")) {
            final int height = sr.getScaledHeight() - 90;
            RenderUtil.roundedRect((sr.getScaledWidth() / 2F) - 15, height, 30, 30, 6, new Color(0, 0, 0, 80));

            CustomFont.drawCenteredString(String.valueOf(blockCount), sr.getScaledWidth() / 2F, height + 19, -1);

            if (itemStack != null) {
                GlStateManager.pushMatrix();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int) (sr.getScaledWidth() / 2F - 8F), height + 2);
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            } else
                CustomFont.drawCenteredString("?", sr.getScaledWidth() / 2F + 0.5F, height + 6, -1);
        }
    }

    @Override
    public void onBlur(final BlurEvent event) {
        if (blockCounter.is("Normal")) {
            final ScaledResolution sr = new ScaledResolution(mc);
            final int height = sr.getScaledHeight() - 90;

            RenderUtil.roundedRect((sr.getScaledWidth() / 2F) - 15, height, 30, 30, 6, new Color(0, 0, 0, 80));
        }
    }

    @Override
    public void onMoveButton(final MoveButtonEvent event) {
        if (jitter.isEnabled()) {
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) {
                if (mc.thePlayer.ticksExisted % 2 == 0)
                    event.setLeft(true);
                else
                    event.setRight(true);
            }

            if (mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                if (mc.thePlayer.ticksExisted % 2 == 0)
                    event.setForward(true);
                else
                    event.setBackward(true);
            }
        }

        if (telly.isEnabled() && MoveUtil.isMoving() && mc.thePlayer.onGround) {
            event.setJump(true);
        }

        event.setSneak(shiftPressed || event.isSneak());
    }

    @Override
    public void onPacketSend(final EventPacket event) {
        final Packet<?> p = event.getPacket();

        if (p instanceof C09PacketHeldItemChange) {
            event.setCancelled(true);
        }

        if (p instanceof C08PacketPlayerBlockPlacement && !MoveUtil.isMoving() && mc.gameSettings.keyBindJump.isKeyDown()) {
            switch (tower.getMode()) {
                case "Hypixel":

                    mc.thePlayer.motionY = MoveUtil.getPredictedMotionY(mc.thePlayer.motionY);

                    break;
            }
        }
    }

    @Override
    public void onCanPlaceBlockEvent(final CanPlaceBlockEvent event) {
        if (placeOn.is("Legit")) {
            this.placeBlock();
        }
    }

    @Override
    public void onPostMotion(final PostMotionEvent event) {
        if (placeOn.is("Post") || (placeOn.is("Legit") && failedPlacementTicks > 0)) {
            this.placeBlock();
        }
    }

    public void placeBlock() {
        final int blockSlot = BlockUtil.findBlock() - 36;

        if (blockSlot < 0 || blockSlot > 9)
            return;

        if (slot != blockSlot) {
            slot = blockSlot;
            PacketUtil.sendPacketWithoutEvent(new C09PacketHeldItemChange(slot));
            mc.thePlayer.inventory.currentItem = slot;
        }

        if (placePossibilities.isEmpty() || targetBlock == null || enumFacing == null || blockFace == null || slot < 0 || slot > 9)
            return;

        final boolean sameY = (this.sameY.isEnabled() || (client.getMM().getModule(Speed.class).isToggled() && !mc.gameSettings.keyBindJump.isKeyDown())) && MoveUtil.isMoving();
        if (sameY &&
                !(PlayerUtil.getBlockRelativeToPlayer(0, -1, 0) instanceof BlockAir) &&
                (int) (startY - 1) != (int) targetBlock.yCoord
        )
            return;

        final MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTraceCustom(mc.playerController.getBlockReachDistance(), mc.timer.renderPartialTicks, yaw, pitch);
        final boolean lookingAtBlock = BlockUtil.lookingAtBlock(blockFace, yaw, pitch, enumFacing.getEnumFacing(), rayCast.is("Strict"));
        Vec3 hitVec = movingObjectPosition != null && movingObjectPosition.hitVec != null
                ? movingObjectPosition.hitVec
                : createHitVec(blockFace, enumFacing.getEnumFacing(), false);

        if (hitVec == null)
            return;

        final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack();
        if (item == null)
            return;

        final boolean edgeRisk = isBridgeEdgeRisk();
        final double randomisedDelay = randomisePlaceDelay.isEnabled() && !mc.gameSettings.keyBindJump.isKeyDown() ? Math.random() * 3 : 0;
        final double requiredDelay = Math.max(0, placeDelay.getValue() + randomisedDelay - (edgeRisk ? 1.0 : 0.0));
        final boolean timingReady = ticksOnAir >= requiredDelay;
        final boolean movementStateReady = mc.thePlayer.onGround || offGroundTicks <= 5 || mc.thePlayer.fallDistance > 0 || edgeRisk;

        boolean placed = false;
        if (movementStateReady && timingReady && (lookingAtBlock || this.rayCast.is("Off"))) {
            if (!lookingAtBlock) {
                hitVec = createHitVec(blockFace, enumFacing.getEnumFacing(), true);
            }

            // TODO: fix 1.8.9 check
            if (ViaMCP.getInstance().getVersion() > 47) {
                if (swing.isEnabled())
                    mc.thePlayer.swingItem();
                else
                    PacketUtil.sendPacket(new C0APacketAnimation());
            }

            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, item, blockFace, enumFacing.getEnumFacing(), hitVec);

            if (ViaMCP.getInstance().getVersion() <= 47) {
                if (swing.isEnabled())
                    mc.thePlayer.swingItem();
                else
                    PacketUtil.sendPacket(new C0APacketAnimation());
            }

            blocksPlaced++;
            placed = true;
        } else if (dragClick.isEnabled() && Math.random() > 0.5)
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(item));

        if (placed) {
            failedPlacementTicks = 0;
        } else if (edgeRisk) {
            failedPlacementTicks = Math.min(failedPlacementTicks + 1, 20);
        } else {
            failedPlacementTicks = 0;
        }
    }

    public static void silentRotationStrafe(final StrafeEvent event, final float yaw) {
        final int dif = (int) ((MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw - yaw - 23.5F - 135.0F) + 180.0F) / 45.0F);
        final float strafe = event.getStrafe();
        final float forward = event.getForward();
        final float friction = event.getFriction();
        float calcForward = 0.0F;
        float calcStrafe = 0.0F;
        switch (dif) {
            case 0: {
                calcForward = forward;
                calcStrafe = strafe;
                break;
            }

            case 1: {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
                break;
            }

            case 2: {
                calcForward = strafe;
                calcStrafe = -forward;
                break;
            }

            case 3: {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
                break;
            }

            case 4: {
                calcForward = -forward;
                calcStrafe = -strafe;
                break;
            }

            case 5: {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
                break;
            }

            case 6: {
                calcForward = -strafe;
                calcStrafe = forward;
                break;
            }

            case 7: {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
                break;
            }
        }

        if (calcForward > 1.0F || (calcForward < 0.9F && calcForward > 0.3F) || calcForward < -1.0F || (calcForward > -0.9F && calcForward < -0.3F))
            calcForward *= 0.5F;

        if (calcStrafe > 1.0F || (calcStrafe < 0.9F && calcStrafe > 0.3F) || calcStrafe < -1.0F || (calcStrafe > -0.9F && calcStrafe < -0.3F))
            calcStrafe *= 0.5F;

        float d;
        if ((d = calcStrafe * calcStrafe + calcForward * calcForward) >= 1.0E-4F) {
            if ((d = MathHelper.sqrt_float(d)) < 1.0F) {
                d = 1.0F;
            }
            d = friction / d;
            final float yawSin = MathHelper.sin((float) (yaw * Math.PI / 180.0));
            final float yawCos = MathHelper.cos((float) (yaw * Math.PI / 180.0));
            minecraft.thePlayer.motionX += (calcStrafe *= d) * yawCos - (calcForward *= d) * yawSin;
            minecraft.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin;
        }
    }

    private void resetPlacementTarget() {
        targetBlock = null;
        enumFacing = null;
        blockFace = null;
    }

    private boolean selectBestPlacementTarget() {
        for (final Vec3 candidate : placePossibilities) {
            final EnumFacingOffset candidateFacing = getEnumFacing(candidate);
            if (candidateFacing == null || candidateFacing.getEnumFacing() == null) {
                continue;
            }

            final BlockPos candidatePos = new BlockPos(candidate.xCoord, candidate.yCoord, candidate.zCoord);
            final BlockPos candidateFace = candidatePos.add(candidateFacing.getOffset().xCoord, candidateFacing.getOffset().yCoord, candidateFacing.getOffset().zCoord);

            if (mc.theWorld.getBlockState(candidateFace).getBlock() instanceof BlockAir) {
                continue;
            }

            targetBlock = candidate;
            enumFacing = candidateFacing;
            blockFace = candidateFace;
            return true;
        }

        resetPlacementTarget();
        return false;
    }

    private Vec3 createHitVec(final BlockPos clickedBlock, final EnumFacing face, final boolean randomise) {
        if (clickedBlock == null || face == null) {
            return null;
        }

        final Vec3i direction = face.getDirectionVec();
        double x = clickedBlock.getX() + 0.5 + direction.getX() * 0.5;
        double y = clickedBlock.getY() + 0.5 + direction.getY() * 0.5;
        double z = clickedBlock.getZ() + 0.5 + direction.getZ() * 0.5;

        if (randomise) {
            x += (Math.random() - 0.5) * 0.3;
            y += (Math.random() - 0.5) * 0.3;
            z += (Math.random() - 0.5) * 0.3;
        }

        return new Vec3(x, y, z);
    }

    private EnumFacingOffset getEnumFacing(final Vec3 position) {
        final BlockPos pos = new BlockPos(position.xCoord, position.yCoord, position.zCoord);

        if (!(mc.theWorld.getBlockState(pos.down()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, -1, 0));
        }

        if (!(mc.theWorld.getBlockState(pos.east()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.WEST, new Vec3(1, 0, 0));
        }

        if (!(mc.theWorld.getBlockState(pos.west()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.EAST, new Vec3(-1, 0, 0));
        }

        if (!(mc.theWorld.getBlockState(pos.north()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, -1));
        }

        if (!(mc.theWorld.getBlockState(pos.south()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, 1));
        }

        if (!(mc.theWorld.getBlockState(pos.up()).getBlock() instanceof BlockAir)) {
            return new EnumFacingOffset(EnumFacing.DOWN, new Vec3(0, 1, 0));
        }

        return null;
    }

    private List<Vec3> getPlacePossibilitiesPredicted() {
        return collectPlacePossibilities(
                mc.thePlayer.posX + mc.thePlayer.motionX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ + mc.thePlayer.motionZ
        );
    }

    private List<Vec3> getPlacePossibilities() {
        return collectPlacePossibilities(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private List<Vec3> collectPlacePossibilities(final double centerX, final double centerY, final double centerZ) {
        final Set<BlockPos> possibilities = new LinkedHashSet<>();
        final int searchRange = (int) Math.ceil(this.range.getValue());
        final int playerFeetY = MathHelper.floor_double(mc.thePlayer.posY - 1.0);
        final BlockPos playerFeet = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        final BlockPos playerHead = playerFeet.up();

        for (int x = -searchRange; x <= searchRange; ++x) {
            for (int y = -searchRange; y <= searchRange; ++y) {
                for (int z = -searchRange; z <= searchRange; ++z) {
                    final BlockPos source = new BlockPos(centerX + x, centerY + y, centerZ + z);
                    if (mc.theWorld.getBlockState(source).getBlock() instanceof BlockAir) {
                        continue;
                    }

                    for (final EnumFacing side : EnumFacing.values()) {
                        final BlockPos candidate = source.offset(side);

                        if (!(mc.theWorld.getBlockState(candidate).getBlock() instanceof BlockAir)) {
                            continue;
                        }

                        if (candidate.equals(playerFeet) || candidate.equals(playerHead)) {
                            continue;
                        }

                        if (Math.abs(candidate.getY() - playerFeetY) > 1) {
                            continue;
                        }

                        possibilities.add(candidate);
                    }
                }
            }
        }

        final List<Vec3> result = new ArrayList<>(possibilities.size());
        for (final BlockPos candidate : possibilities) {
            result.add(new Vec3(candidate.getX(), candidate.getY(), candidate.getZ()));
        }

        return result;
    }

    private double getPlacementScore(final Vec3 candidate) {
        final Vec3 reference = getPlacementReference();
        final double dx = candidate.xCoord + 0.5 - reference.xCoord;
        final double dz = candidate.zCoord + 0.5 - reference.zCoord;
        final double dy = Math.abs(candidate.yCoord - reference.yCoord) * 0.5;
        return dx * dx + dz * dz + dy * dy;
    }

    private Vec3 getPlacementReference() {
        final double targetY = mc.thePlayer.posY - 1;
        if (!MoveUtil.isMoving()) {
            return new Vec3(mc.thePlayer.posX, targetY, mc.thePlayer.posZ);
        }

        final double direction = MoveUtil.getDirection();
        final double lookAhead = 0.9;
        final double x = mc.thePlayer.posX - Math.sin(direction) * lookAhead;
        final double z = mc.thePlayer.posZ + Math.cos(direction) * lookAhead;
        return new Vec3(x, targetY, z);
    }

    private boolean isBridgeEdgeRisk() {
        if (isAirBelow(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) {
            return true;
        }

        if (!MoveUtil.isMoving()) {
            return false;
        }

        final Vec3 reference = getPlacementReference();
        return isAirBelow(reference.xCoord, mc.thePlayer.posY, reference.zCoord);
    }

    private boolean isAirBelow(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y - 1.0, z)).getBlock() instanceof BlockAir;
    }

    private void updateBridgeSneakState() {
        final boolean blockBelowAir = isAirBelow(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        final boolean edgeRisk = safewalk.isEnabled() && isBridgeEdgeRisk();
        final boolean eagleSneak = blockBelowAir && mc.thePlayer.onGround && blocksPlaced == eagle.getValue() && eagle.getValue() != 15 && eagle.getValue() != 0;
        shiftPressed = eagleSneak || edgeRisk;

        final boolean manualSneak = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
        mc.gameSettings.keyBindSneak.setKeyPressed(shiftPressed || manualSneak);

        final boolean packetSneak = (blockBelowAir && eagle.getValue() == 0) || edgeRisk;
        if (packetSneak && !sneaking) {
            sneaking = true;
            PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
        } else if (!packetSneak && sneaking) {
            sneaking = false;
            PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }
    }


}
