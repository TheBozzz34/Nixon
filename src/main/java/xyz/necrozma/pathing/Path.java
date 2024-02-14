package xyz.necrozma.pathing;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

@Setter
@Getter
public class Path {
    private ArrayList<BlockPos> path;
    private BlockPos start;
    private BlockPos end;
    private int index;
    private boolean isFinished;

    public Path() {
        this.path = new ArrayList<BlockPos>();
        this.index = 0;
        this.isFinished = false;
    }

    public void addPoint(BlockPos point) {
        this.path.add(point);
    }

    public BlockPos getNextPoint() {
        if (this.index < this.path.size()) {
            return this.path.get(this.index++);
        }
        this.isFinished = true;
        return null;
    }

    public void reset() {
        this.index = 0;
        this.isFinished = false;
    }
    public BlockPos getEnd() {
        return this.path.get(this.path.size() - 1);
    }

    public BlockPos getStart() {
        return this.path.get(0);
    }
}
