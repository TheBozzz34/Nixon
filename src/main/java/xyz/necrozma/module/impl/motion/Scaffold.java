package xyz.necrozma.module.impl.motion;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.input.MoveButtonEvent;
import xyz.necrozma.event.impl.motion.CanPlaceBlockEvent;
import xyz.necrozma.event.impl.motion.PostMotionEvent;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.motion.StrafeEvent;
import xyz.necrozma.event.impl.packet.EventPacket;
import xyz.necrozma.event.impl.render.BlurEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NoteSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.BlockUtil;
import xyz.necrozma.util.MoveUtil;
import xyz.necrozma.util.PacketUtil;
import xyz.necrozma.util.RotationUtil;
import xyz.necrozma.util.SafeWalk;
import de.florianmichael.viamcp.ViaMCP;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@ModuleInfo(name = "Scaffold", description = "Bridges automatically for you", category = Category.MOVEMENT)
public class Scaffold extends Module {

    private static final double MOTION_PREDICTION = 1.35D;


    private static final double[] LOOK_AHEAD = {0.35D, 0.7D};

    private static final Minecraft minecraft = Client.INSTANCE.getMC();
    private static final Client client = Client.INSTANCE;

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
    private final NumberSetting range = new NumberSetting("Range", this, 4.5, 1, 6, 0.5);
    private final BooleanSetting randomiseRotationSpeedOnEnable = new BooleanSetting("Randomise Rotation Speed On Enable", this, false);
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", this, 180, 5, 360, 5);
    private final NumberSetting randomisation = new NumberSetting("Randomisation", this, 1, 0, 6, 0.1);
    private final NumberSetting placeDelay = new NumberSetting("Place Delay", this, 0, 0, 5, 0.1);
    private final BooleanSetting randomisePlaceDelay = new BooleanSetting("Randomise Place Delay", this, false);
    private final NumberSetting speedMultiplier = new NumberSetting("Speed Multiplier", this, 1, 0, 2, 0.05);
    private final NumberSetting eagle = new NumberSetting("Eagle", this, 15, 0, 15, 1, "15-Never", "0-Packet");
    private final BooleanSetting ignoreSpeed = new BooleanSetting("Ignore Speed", this, false);
    private final BooleanSetting towerMove = new BooleanSetting("Tower Move", this, true);
    private final ModeSetting rayCast = new ModeSetting("Ray Cast", this, "Normal", "Normal", "Strict", "Off");
    private final ModeSetting placeOn = new ModeSetting("Place on", this, "Legit", "Legit", "Post");
    private final BooleanSetting dragClick = new BooleanSetting("Drag Click", this, false);
    private final BooleanSetting jitter = new BooleanSetting("Jitter", this, false);
    private final BooleanSetting telly = new BooleanSetting("Telly", this, false);
    private final BooleanSetting hideJumps = new BooleanSetting("Hide Jumps", this, false);

    /** The block we are currently committed to placing. */
    private Placement placement;

    /** Rotations reported to the server. Only ever written from {@link #onPreMotion}. */
    private float yaw, pitch;
    private float targetYaw, targetPitch;
    private float lastYaw, lastPitch;

    /** Per-target aim jitter, rerolled when the target changes rather than every call. */
    private float aimJitterYaw, aimJitterPitch;

    /** Hotbar slot the player had selected before the module took over. */
    private int playerSlot = -1;
    private int heldSlot = -1;

    private int blockCount;
    private int ticksOnAir;
    private int offGroundTicks;
    private int ticksSincePlace;
    private double requiredPlaceDelay;
    private int blocksPlaced;
    private double startY;
    private boolean eagleSneaking;
    private boolean sneakPacketSent;

    /** An air position we want to fill, plus the block face we click to fill it. */
    private static final class Placement {
        private final BlockPos target;
        private final BlockPos against;
        private final EnumFacing face;

        private Placement(final BlockPos target, final BlockPos against, final EnumFacing face) {
            this.target = target;
            this.against = against;
            this.face = face;
        }

        private boolean sameAs(final Placement other) {
            return other != null && target.equals(other.target) && against.equals(other.against) && face == other.face;
        }
    }

    @Override
    public void onUpdateAlwaysInGui() {
        towerMove.hidden = tower.is("None") || tower.is("Slow") || tower.is("Hypixel");
        towerTimer.hidden = tower.is("None");
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;

        playerSlot = mc.thePlayer.inventory.currentItem;
        heldSlot = playerSlot;

        yaw = lastYaw = targetYaw = mc.thePlayer.rotationYaw;
        pitch = lastPitch = targetPitch = mc.thePlayer.rotationPitch;

        startY = mc.thePlayer.posY;
        placement = null;
        blocksPlaced = 0;
        ticksSincePlace = Integer.MAX_VALUE;
        requiredPlaceDelay = placeDelay.getValue();
        eagleSneaking = false;

        SafeWalk.setActive(safewalk.isEnabled());

        if (randomiseRotationSpeedOnEnable.isEnabled()) {
            rotationSpeed.setValue(120 + random() * 180);
        }
    }

    @Override
    public void onDisable() {
        SafeWalk.setActive(false);

        if (mc.thePlayer == null) return;

        restorePlayerSlot();

        EntityPlayer.movementYaw = null;
        EntityPlayer.enableCameraYOffset = false;
        mc.timer.timerSpeed = 1;
        mc.gameSettings.keyBindSneak.setKeyPressed(false);

        if (sneakPacketSent) {
            sneakPacketSent = false;
            PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        }

        eagleSneaking = false;
        placement = null;
    }

    // ------------------------------------------------------------------
    // per tick
    // ------------------------------------------------------------------

    @Override
    public void onPreMotion(final PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        updateCounters();
        updateBlockCount();
        updateCameraOffset();

        SafeWalk.setActive(safewalk.isEnabled());
        updateEagle();

        if (strafe.isEnabled()) MoveUtil.strafe();

        applySprintMode();

        // Keep the committed target if it is still usable, otherwise pick a new one.
        if (!isPlacementUsable(placement)) {
            setPlacement(findPlacement());
        }

        if (rotations.is("None") || placement == null) {
            // Nothing to aim at, so track the player's own rotation. Otherwise the next
            // target would start smoothing from a stale value and take longer to line up.
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        } else {
            stepRotations();

            event.setYaw(yaw);
            event.setPitch(pitch);

            mc.thePlayer.renderYawOffset = yaw;
            mc.thePlayer.rotationYawHead = yaw;
        }

        lastYaw = yaw;
        lastPitch = pitch;

        // Switch a tick ahead of placing so the server sees the held item change first.
        // Only while we actually have something to place, so the player keeps their item
        // when there is nothing to bridge.
        if (placement != null) selectBlockSlot();

        mc.timer.timerSpeed = (float) timer.getValue();
        applyTower();
        applySpeedMultiplier();
    }

    private void updateCounters() {
        if (isAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ))) {
            ticksOnAir++;
        } else {
            ticksOnAir = 0;
        }

        offGroundTicks = mc.thePlayer.onGround ? 0 : offGroundTicks + 1;

        if (ticksSincePlace != Integer.MAX_VALUE) ticksSincePlace++;

        if (mc.thePlayer.onGround || (mc.gameSettings.keyBindJump.isKeyDown() && !sameY.isEnabled())) {
            startY = mc.thePlayer.posY;
        }

        if (sameY.isEnabled() && mc.thePlayer.posY < startY) startY = mc.thePlayer.posY;

        if (blocksPlaced > eagle.getValue()) blocksPlaced = 1;
    }

    private void updateBlockCount() {
        int blocks = 0;

        for (int i = 36; i < 45; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                final Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                if (block.isFullCube() && !BlockUtil.BLOCK_BLACKLIST.contains(block)) {
                    blocks += itemStack.stackSize;
                }
            }
        }

        blockCount = blocks;
    }

    private void updateCameraOffset() {
        EntityPlayer.enableCameraYOffset = false;

        if (hideJumps.isEnabled() && mc.thePlayer.posY > startY && !mc.gameSettings.keyBindJump.isKeyDown()) {
            EntityPlayer.enableCameraYOffset = true;
            EntityPlayer.cameraYPosition = startY;
        }
    }

    private void applySprintMode() {
        switch (sprint.getMode()) {
            case "Disabled":
                mc.gameSettings.keyBindSprint.setKeyPressed(false);
                mc.thePlayer.setSprinting(false);
                break;

            case "Bypass":
                mc.thePlayer.setSprinting(false);
                break;

            case "Legit":
                if (Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(yaw)) > 90) {
                    mc.gameSettings.keyBindSprint.setKeyPressed(false);
                    mc.thePlayer.setSprinting(false);
                }
                break;

            default:
                break;
        }
    }

    private void applyTower() {
        if (!mc.gameSettings.keyBindJump.isKeyDown() || tower.is("None")) return;

        final boolean moveAllowed = (towerMove.isEnabled() && !(tower.is("Slow") || tower.is("Hypixel"))) || !MoveUtil.isMoving();
        final boolean groundAllowed = !isAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ))
                || tower.is("Intave") || tower.is("Slow");

        if (!moveAllowed || !groundAllowed || client.getMM().getModule(Speed.class).isToggled()) return;

        mc.timer.timerSpeed = (float) towerTimer.getValue();

        switch (tower.getMode()) {
            case "Vanilla":
                mc.thePlayer.motionY = 0.42F;
                break;

            case "Hypixel":
                if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.4F;
                if (offGroundTicks == 3) mc.thePlayer.motionY -= 0.02;
                break;

            case "Slow":
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.4F;
                } else if (isAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ))) {
                    mc.thePlayer.motionY -= 0.4F;
                }
                MoveUtil.stop();
                break;

            case "Verus":
                if (mc.thePlayer.ticksExisted % 2 == 0) mc.thePlayer.motionY = 0.42F;
                break;

            case "Intave":
                if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.40444491418477924;
                if (offGroundTicks == 5) mc.thePlayer.motionY = MoveUtil.getPredictedMotionY(mc.thePlayer.motionY);
                break;

            default:
                break;
        }
    }

    private void applySpeedMultiplier() {
        final double multiplier = speedMultiplier.getValue();
        if (Math.abs(multiplier - 1.0) <= 1E-4 || !mc.thePlayer.onGround) return;

        final boolean stronglySpeedBuffed = mc.thePlayer.isPotionActive(Potion.moveSpeed)
                && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() > 0;
        if (stronglySpeedBuffed) return;

        final double baseSpeed = getBaseSpeed();
        if (baseSpeed != 0) MoveUtil.strafe(baseSpeed * multiplier);
    }

    public double getBaseSpeed() {
        final boolean sprinting = mc.gameSettings.keyBindSprint.isKeyDown();
        final boolean speedPotion = mc.thePlayer.isPotionActive(Potion.moveSpeed) && !ignoreSpeed.isEnabled();

        if (!speedPotion) return sprinting ? 0.15321676228437875 : 0.11785905094607611;

        final boolean tierOne = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 == 1;
        if (sprinting) return tierOne ? 0.18386012061481244 : 0.21450346015841276;
        return tierOne ? 0.14143085686761 : 0.16500264553372018;
    }

    // ------------------------------------------------------------------
    // target selection
    // ------------------------------------------------------------------

    private void setPlacement(final Placement next) {
        if (next != null && next.sameAs(placement)) return;

        placement = next;

        // Reroll the aim jitter only when the target actually changes. Rerolling every
        // call is what made the old aim wander instead of settling on the block.
        final double amount = randomisation.getValue();
        aimJitterYaw = (float) ((random() - 0.5) * amount * 2);
        aimJitterPitch = (float) ((random() - 0.5) * amount * 2);
    }

    /**
     * @return true if the committed target is still worth aiming at
     */
    private boolean isPlacementUsable(final Placement candidate) {
        if (candidate == null) return false;
        if (!isReplaceable(candidate.target)) return false;
        if (!isSolid(candidate.against)) return false;
        if (intersectsPlayer(candidate.target)) return false;
        return withinReach(candidate.against, candidate.face);
    }

    private Placement findPlacement() {
        Placement best = null;
        double bestScore = Double.MAX_VALUE;

        final List<BlockPos> targets = collectTargets();

        for (int i = 0; i < targets.size(); i++) {
            final BlockPos target = targets.get(i);

            if (!isReplaceable(target) || intersectsPlayer(target)) continue;

            final Placement candidate = bestFaceFor(target);
            if (candidate == null) continue;

            // Earlier candidates are the ones directly under the player and along the
            // movement direction, so weight them heavily.
            final double score = faceScore(candidate) + i * 1.5;
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Positions the player actually needs to stand on, most important first.
     * <p>
     * Deliberately narrow. Anything reachable and empty that gets listed here will
     * eventually be filled, because a target is only skipped once it is solid — so
     * listing the surrounding ring paves a three wide path, and listing far look-ahead
     * distances paves a strip out in front. Only the block underfoot and the one we are
     * about to step onto belong here.
     */
    private List<BlockPos> collectTargets() {
        final Set<BlockPos> targets = new LinkedHashSet<>();

        final double px = mc.thePlayer.posX;
        final double pz = mc.thePlayer.posZ;
        final int y = getBridgeY();

        // Underfoot: we are stood on nothing, or about to be.
        targets.add(new BlockPos(px, y, pz));

        if (MoveUtil.isMoving()) {
            // Just far enough ahead to get the block down before we step onto it. Sprinting
            // covers roughly 0.28 blocks a tick, so this is about two ticks of travel.
            // getDirection() already folds in strafing, so diagonals are covered.
            final double direction = MoveUtil.getDirection();
            final double dx = -Math.sin(direction);
            final double dz = Math.cos(direction);

            for (final double ahead : LOOK_AHEAD) {
                targets.add(new BlockPos(px + dx * ahead, y, pz + dz * ahead));
            }

            // Momentum can disagree with input just after a turn.
            targets.add(new BlockPos(px + mc.thePlayer.motionX * MOTION_PREDICTION, y, pz + mc.thePlayer.motionZ * MOTION_PREDICTION));
        }

        // Manual sneak with Downwards on means the player wants to descend.
        if (downwards.isEnabled() && isManuallySneaking()) {
            targets.add(new BlockPos(px, y, pz).down());
        }

        return new ArrayList<>(targets);
    }

    private int getBridgeY() {
        final double reference = sameY.isEnabled() ? startY : mc.thePlayer.posY;
        return MathHelper.floor_double(reference) - 1;
    }

    /**
     * Picks the block face to click in order to fill {@code target}.
     */
    private Placement bestFaceFor(final BlockPos target) {
        Placement best = null;
        double bestScore = Double.MAX_VALUE;

        for (final EnumFacing direction : EnumFacing.values()) {
            final BlockPos against = target.offset(direction);

            if (!isSolid(against)) continue;

            // The face of `against` that points back at `target`.
            final EnumFacing face = direction.getOpposite();

            if (!withinReach(against, face)) continue;

            final Placement candidate = new Placement(target, against, face);
            final double score = faceScore(candidate);

            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private double faceScore(final Placement candidate) {
        double score = 0;

        // Clicking the top of a block is the most reliable placement there is.
        if (candidate.face == EnumFacing.UP) score -= 1.0;
        else if (candidate.face == EnumFacing.DOWN) score += 0.75;

        final Vec3 hit = faceCentre(candidate.against, candidate.face);

        // Prefer targets that need the least turning.
        score += Math.abs(MathHelper.wrapAngleTo180_float(rotationsToward(hit)[0] - yaw)) * 0.01;

        // Prefer targets closer to the player.
        score += eyes().distanceTo(hit) * 0.25;

        return score;
    }

    /**
     * Yaw/pitch that aims the player's eyes at {@code point}.
     * <p>
     * Equivalent to {@link BlockUtil#getDirectionToBlock} for a face centre, but without
     * allocating a throwaway entity per call — this runs for every candidate face.
     */
    private float[] rotationsToward(final Vec3 point) {
        final double dx = point.xCoord - mc.thePlayer.posX;
        final double dy = point.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double dz = point.zCoord - mc.thePlayer.posZ;
        final double horizontal = Math.sqrt(dx * dx + dz * dz);

        return new float[]{
                (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0),
                (float) -Math.toDegrees(Math.atan2(dy, horizontal))
        };
    }

    // ------------------------------------------------------------------
    // rotations
    // ------------------------------------------------------------------

    /**
     * Advances {@link #yaw}/{@link #pitch} one tick toward the locked target. Called
     * exactly once per tick.
     */
    private void stepRotations() {
        final float[] wanted = computeTargetRotation();
        targetYaw = wanted[0] + aimJitterYaw;
        targetPitch = wanted[1] + aimJitterPitch;

        // Degrees per tick. The old code divided this by (fps / 20), which made turn
        // speed depend on frame rate and divided by zero below 20 fps.
        final float step = (float) rotationSpeed.getValue();

        if (step >= 355) {
            yaw = targetYaw;
            pitch = targetPitch;
        } else {
            final float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - lastYaw);
            final float deltaPitch = targetPitch - lastPitch;

            yaw = lastYaw + MathHelper.clamp_float(deltaYaw, -step, step);
            pitch = lastPitch + MathHelper.clamp_float(deltaPitch, -step, step);
        }

        final float[] fixed = RotationUtil.getFixedRotation(new float[]{yaw, pitch}, new float[]{lastYaw, lastPitch});
        yaw = fixed[0];
        pitch = fixed[1];

        if (!rotations.is("Pitch Abuse")) pitch = MathHelper.clamp_float(pitch, -90, 90);
    }

    private float[] computeTargetRotation() {
        final float[] direct = rotationsToward(faceCentre(placement.against, placement.face));

        if (movementFix.is("Yaw")) {
            final float movementYaw = (float) (Math.toDegrees(MoveUtil.getDirectionWrappedTo90()) - 180);

            if (rotations.is("Snap") && !isSnapReady()) {
                return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
            }

            return new float[]{movementYaw, direct[1]};
        }

        switch (rotations.getMode()) {
            case "Simple": {
                float simpleYaw = 0;

                switch (placement.face) {
                    case SOUTH: simpleYaw = 180; break;
                    case EAST: simpleYaw = 90; break;
                    case WEST: simpleYaw = -90; break;
                    default: break;
                }

                return new float[]{simpleYaw, 90};
            }

            case "Down": {
                float rotationYaw = mc.thePlayer.rotationYaw;

                if (mc.thePlayer.moveForward < 0 && mc.thePlayer.moveStrafing == 0) rotationYaw += 180;
                if (mc.thePlayer.moveStrafing > 0) rotationYaw -= 90;
                if (mc.thePlayer.moveStrafing < 0) rotationYaw += 90;

                return new float[]{rotationYaw - 180, 87};
            }

            case "Snap":
                if (!isSnapReady()) {
                    return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                }
                return direct;

            case "Bruteforce": {
                final float[] found = searchRotation(90, 30, -2);
                return found != null ? found : direct;
            }

            case "Pitch Abuse": {
                final float[] up = searchRotation(90, 180, 2);
                if (up != null) return up;

                final float[] down = searchRotation(90, 30, -2);
                if (down != null) return down;

                return new float[]{mc.thePlayer.rotationYaw, 94};
            }

            default:
                return direct;
        }
    }

    private boolean isSnapReady() {
        return ticksOnAir >= placeDelay.getValue();
    }

    /**
     * Scans for any rotation that puts the placement face under the crosshair.
     */
    private float[] searchRotation(final float pitchFrom, final float pitchTo, final float pitchStep) {
        final float baseYaw = mc.thePlayer.rotationYaw - 180;
        final boolean strict = rayCast.is("Strict");

        for (float candidateYaw = baseYaw; candidateYaw <= baseYaw + 360; candidateYaw += 45) {
            for (float candidatePitch = pitchFrom;
                 pitchStep > 0 ? candidatePitch < pitchTo : candidatePitch > pitchTo;
                 candidatePitch += pitchStep) {

                if (BlockUtil.lookingAtBlock(placement.against, candidateYaw, candidatePitch, placement.face, strict)) {
                    return new float[]{candidateYaw, candidatePitch};
                }
            }
        }

        return null;
    }

    @Override
    public void onStrafe(final StrafeEvent event) {
        if (movementFix.is("Hidden")) {
            event.setCancelled(true);
            silentRotationStrafe(event, yaw);
        }
    }

    // ------------------------------------------------------------------
    // placing
    // ------------------------------------------------------------------

    @Override
    public void onCanPlaceBlockEvent(final CanPlaceBlockEvent event) {
        if (placeOn.is("Legit")) tryPlace();
    }

    @Override
    public void onPostMotion(final PostMotionEvent event) {
        if (placeOn.is("Post")) tryPlace();
    }

    private void tryPlace() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isPlacementUsable(placement)) return;
        if (ticksSincePlace < requiredPlaceDelay) return;

        final int blockSlot = selectBlockSlot();
        if (blockSlot < 0) return;

        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(blockSlot + 36).getStack();
        if (stack == null) return;

        if (sameY.isEnabled() && placement.target.getY() != getBridgeY()) return;

        final boolean aimed = rayCast.is("Off")
                || BlockUtil.lookingAtBlock(placement.against, yaw, pitch, placement.face, rayCast.is("Strict"));

        if (!aimed) {
            // Still turning toward the locked target. Safe Walk keeps us on the block in
            // the meantime, so the next tick or two will line up.
            if (dragClick.isEnabled() && random() > 0.5) {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(stack));
            }
            return;
        }

        final Vec3 hitVec = resolveHitVec();

        if (ViaMCP.getInstance().getVersion() > 47) swingArm();

        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, placement.against, placement.face, hitVec);

        if (ViaMCP.getInstance().getVersion() <= 47) swingArm();

        blocksPlaced++;
        ticksSincePlace = 0;
        requiredPlaceDelay = placeDelay.getValue()
                + (randomisePlaceDelay.isEnabled() && !mc.gameSettings.keyBindJump.isKeyDown() ? random() * 2 : 0);

        // The block is down; commit to the next one immediately instead of waiting a tick.
        placement = null;
    }

    private void swingArm() {
        if (swing.isEnabled()) {
            mc.thePlayer.swingItem();
        } else {
            PacketUtil.sendPacket(new C0APacketAnimation());
        }
    }

    /**
     * Prefers the real trace hit so the click matches what the crosshair is on, and falls
     * back to the centre of the face.
     */
    private Vec3 resolveHitVec() {
        final MovingObjectPosition trace = mc.thePlayer.rayTraceCustom(
                mc.playerController.getBlockReachDistance(), mc.timer.renderPartialTicks, yaw, pitch);

        if (trace != null && trace.hitVec != null && placement.against.equals(trace.getBlockPos())) {
            return trace.hitVec;
        }

        return faceCentre(placement.against, placement.face);
    }

    // ------------------------------------------------------------------
    // hotbar
    // ------------------------------------------------------------------

    /**
     * Makes sure a placeable block is selected, keeping client and server in agreement.
     *
     * @return the hotbar slot holding blocks, or -1 if the player has none
     */
    private int selectBlockSlot() {
        final int found = BlockUtil.findBlock();
        if (found < 36 || found > 44) return -1;

        final int slot = found - 36;

        if (mc.thePlayer.inventory.currentItem != slot) {
            if (playerSlot < 0) playerSlot = mc.thePlayer.inventory.currentItem;

            mc.thePlayer.inventory.currentItem = slot;
        }

        if (heldSlot != slot) {
            heldSlot = slot;
            PacketUtil.sendPacketWithoutEvent(new C09PacketHeldItemChange(slot));
        }

        return slot;
    }

    private void restorePlayerSlot() {
        if (playerSlot < 0 || playerSlot > 8) return;

        mc.thePlayer.inventory.currentItem = playerSlot;
        PacketUtil.sendPacketWithoutEvent(new C09PacketHeldItemChange(playerSlot));

        playerSlot = -1;
        heldSlot = -1;
    }

    // ------------------------------------------------------------------
    // eagle
    // ------------------------------------------------------------------

    /**
     * Eagle is the only thing here that still uses real sneak, because dropping off the
     * edge between placements is the point of it. Staying on the block is Safe Walk's job.
     */
    private void updateEagle() {
        final int mode = (int) eagle.getValue();

        if (mode == 15) {
            stopEagle();
            return;
        }

        final boolean overAir = isAir(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));

        if (mode == 0) {
            // Packet only: no key press, so no movement penalty client side.
            eagleSneaking = false;
            setSneakPacket(overAir);
            return;
        }

        eagleSneaking = overAir && mc.thePlayer.onGround && blocksPlaced >= mode;
        mc.gameSettings.keyBindSneak.setKeyPressed(eagleSneaking || isManuallySneaking());
        setSneakPacket(eagleSneaking);
    }

    private void stopEagle() {
        eagleSneaking = false;
        mc.gameSettings.keyBindSneak.setKeyPressed(isManuallySneaking());
        setSneakPacket(false);
    }

    private void setSneakPacket(final boolean sneak) {
        if (sneak == sneakPacketSent) return;

        sneakPacketSent = sneak;
        PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer,
                sneak ? C0BPacketEntityAction.Action.START_SNEAKING : C0BPacketEntityAction.Action.STOP_SNEAKING));
    }

    /**
     * Reads the physical sneak key rather than the keybind, since we write to the keybind
     * ourselves. Mouse bound keys have a negative code and would throw here.
     */
    private boolean isManuallySneaking() {
        final int keyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        return keyCode >= 0 && Keyboard.isKeyDown(keyCode);
    }

    @Override
    public void onMoveButton(final MoveButtonEvent event) {
        if (jitter.isEnabled()) {
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()) {
                if (mc.thePlayer.ticksExisted % 2 == 0) event.setLeft(true);
                else event.setRight(true);
            }

            if (mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                if (mc.thePlayer.ticksExisted % 2 == 0) event.setForward(true);
                else event.setBackward(true);
            }
        }

        if (telly.isEnabled() && MoveUtil.isMoving() && mc.thePlayer.onGround) {
            event.setJump(true);
        }

        event.setSneak(eagleSneaking || event.isSneak());
    }

    @Override
    public void onPacketSend(final EventPacket event) {
        final Packet<?> p = event.getPacket();

        if (p instanceof C08PacketPlayerBlockPlacement && tower.is("Hypixel")
                && !MoveUtil.isMoving() && mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.motionY = MoveUtil.getPredictedMotionY(mc.thePlayer.motionY);
        }
    }

    // ------------------------------------------------------------------
    // world queries
    // ------------------------------------------------------------------

    private boolean isAir(final BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().getMaterial() == Material.air;
    }

    /** A position we are allowed to place a block into. */
    private boolean isReplaceable(final BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().getMaterial().isReplaceable();
    }

    /** A block solid enough to click a face of. */
    private boolean isSolid(final BlockPos pos) {
        final IBlockState state = mc.theWorld.getBlockState(pos);
        final Block block = state.getBlock();

        return block.getMaterial().isSolid()
                && block.getCollisionBoundingBox(mc.theWorld, pos, state) != null;
    }

    private boolean intersectsPlayer(final BlockPos pos) {
        return mc.thePlayer.getEntityBoundingBox().intersectsWith(
                AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
    }

    private boolean withinReach(final BlockPos against, final EnumFacing face) {
        final double limit = Math.min(range.getValue(), mc.playerController.getBlockReachDistance());
        return eyes().distanceTo(faceCentre(against, face)) <= limit;
    }

    private Vec3 eyes() {
        return new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    private static Vec3 faceCentre(final BlockPos pos, final EnumFacing face) {
        final Vec3i direction = face.getDirectionVec();
        return new Vec3(
                pos.getX() + 0.5 + direction.getX() * 0.5,
                pos.getY() + 0.5 + direction.getY() * 0.5,
                pos.getZ() + 0.5 + direction.getZ() * 0.5);
    }

    // ------------------------------------------------------------------
    // hud
    // ------------------------------------------------------------------

    @Override
    public void onRender2DEvent(final Render2DEvent event) {
        if (blockCount == 0) return;

        final ScaledResolution sr = event.getScaledResolution();
        final ItemStack itemStack = heldSlot >= 0 && heldSlot <= 8 ? mc.thePlayer.inventory.getStackInSlot(heldSlot) : null;
        final Color color = blockCount <= 63 ? Color.RED : Color.GREEN;

        if (blockCounter.is("MC")) {
            final int height = sr.getScaledHeight() / 2;

            mc.fontRendererObj.drawStringWithShadow(String.valueOf(blockCount), sr.getScaledWidth() / 2F + 1F, height + 9, color.getRGB());

            if (itemStack != null) {
                drawItem(itemStack, (int) (sr.getScaledWidth() / 2F - 17F), height + 4);
            } else {
                CustomFont.drawCenteredString("?", sr.getScaledWidth() / 2F + 0.5F, height + 6, -1);
            }
        }

        if (blockCounter.is("Normal")) {
            final int height = sr.getScaledHeight() - 90;
            RenderUtil.roundedRect((sr.getScaledWidth() / 2F) - 15, height, 30, 30, 6, new Color(0, 0, 0, 80));

            CustomFont.drawCenteredString(String.valueOf(blockCount), sr.getScaledWidth() / 2F, height + 19, -1);

            if (itemStack != null) {
                drawItem(itemStack, (int) (sr.getScaledWidth() / 2F - 8F), height + 2);
            } else {
                CustomFont.drawCenteredString("?", sr.getScaledWidth() / 2F + 0.5F, height + 6, -1);
            }
        }
    }

    private void drawItem(final ItemStack itemStack, final int x, final int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void onBlur(final BlurEvent event) {
        if (!blockCounter.is("Normal")) return;

        final ScaledResolution sr = new ScaledResolution(mc);
        final int height = sr.getScaledHeight() - 90;

        RenderUtil.roundedRect((sr.getScaledWidth() / 2F) - 15, height, 30, 30, 6, new Color(0, 0, 0, 80));
    }

    // ------------------------------------------------------------------
    // movement fix: hidden
    // ------------------------------------------------------------------

    public static void silentRotationStrafe(final StrafeEvent event, final float yaw) {
        final int dif = (int) ((MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw - yaw - 23.5F - 135.0F) + 180.0F) / 45.0F);
        final float strafe = event.getStrafe();
        final float forward = event.getForward();
        final float friction = event.getFriction();
        float calcForward = 0.0F;
        float calcStrafe = 0.0F;

        switch (dif) {
            case 0:
                calcForward = forward;
                calcStrafe = strafe;
                break;

            case 1:
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
                break;

            case 2:
                calcForward = strafe;
                calcStrafe = -forward;
                break;

            case 3:
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
                break;

            case 4:
                calcForward = -forward;
                calcStrafe = -strafe;
                break;

            case 5:
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
                break;

            case 6:
                calcForward = -strafe;
                calcStrafe = forward;
                break;

            case 7:
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
                break;

            default:
                break;
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
}
