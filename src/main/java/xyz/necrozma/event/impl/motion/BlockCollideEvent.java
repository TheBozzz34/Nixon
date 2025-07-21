package xyz.necrozma.event.impl.motion;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class BlockCollideEvent extends Event {
    private AxisAlignedBB collisionBoundingBox;
    private Block block;
    private int x, y, z;
}