package xyz.necrozma.pathing;

import net.minecraft.util.BlockPos;

public class PathFinder {

    public Path calculatePath(BlockPos start, BlockPos end) {
        Path path = new Path();
        path.setStart(start);
        path.setEnd(end);

        int index = path.getIndex();

        while (index < 10) {
            path.addPoint(getTopBlock(new BlockPos(start.getX() + index, start.getY(), start.getZ())));
            index++;
        }

        return path;
    }


    public BlockPos getTopBlock(BlockPos pos) {
        BlockPos topBlock = pos;
        while (topBlock.getY() < 256 && !topBlock.up().equals(new BlockPos(0, 256, 0))) {
            topBlock = topBlock.up();
        }
        return topBlock;
    }
}
