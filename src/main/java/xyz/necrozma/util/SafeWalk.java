package xyz.necrozma.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

/**
 * Client side safe walk.
 * <p>
 * Vanilla only clamps horizontal movement at a ledge while the player is sneaking
 * ({@code Entity#moveEntity}). Holding sneak to get that clamp also applies the 0.3x
 * sneak movement multiplier and sends sneak packets, which is why sneak based bridging
 * feels slow. This flag lets the edge clamp run without the player actually sneaking,
 * so movement speed is untouched and no sneak packets are sent.
 *
 * @see Entity#moveEntity(double, double, double)
 */
public final class SafeWalk {

    private static boolean active;

    private SafeWalk() {
    }

    public static void setActive(final boolean active) {
        SafeWalk.active = active;
    }

    public static boolean isActive() {
        return active;
    }

    /**
     * Whether the vanilla ledge clamp should be applied to the given entity even though
     * it is not sneaking. Only ever true for the local player.
     *
     * @param entity the entity being moved
     * @return true if movement off a ledge should be clamped
     */
    public static boolean shouldClamp(final Entity entity) {
        return active && entity == Minecraft.getMinecraft().thePlayer;
    }
}
