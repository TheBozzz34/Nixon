package xyz.necrozma.pathing;


import lombok.Getter;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple path storage class for pathfinding algorithms
 * Stores a series of BlockPos waypoints from start to destination
 */
public class Path
{
    private final List<BlockPos> waypoints;
    /**
     * -- GETTER --
     *  Get the starting position
     */
    @Getter
    private final BlockPos start;
    /**
     * -- GETTER --
     *  Get the ending position
     */
    @Getter
    private final BlockPos end;
    private int currentIndex;

    /**
     * Creates a new path with just start and end points
     */
    public Path(BlockPos start, BlockPos end)
    {
        this.start = start;
        this.end = end;
        this.waypoints = new ArrayList<>();
        this.waypoints.add(start);
        this.waypoints.add(end);
        this.currentIndex = 0;
    }

    /**
     * Creates a new path with a list of waypoints
     * First waypoint should be start, last should be end
     */
    public Path(List<BlockPos> waypoints)
    {
        if (waypoints == null || waypoints.size() < 2)
        {
            throw new IllegalArgumentException("Path must have at least start and end points");
        }

        this.waypoints = new ArrayList<>(waypoints);
        this.start = waypoints.get(0);
        this.end = waypoints.get(waypoints.size() - 1);
        this.currentIndex = 0;
    }

    /**
     * Get all waypoints in the path
     */
    public List<BlockPos> getWaypoints()
    {
        return new ArrayList<>(waypoints);
    }

    /**
     * Get the current target waypoint
     */
    public BlockPos getCurrentWaypoint()
    {
        if (currentIndex < waypoints.size())
        {
            return waypoints.get(currentIndex);
        }
        return null;
    }

    /**
     * Get the next waypoint in the path
     */
    public BlockPos getNextWaypoint()
    {
        if (currentIndex + 1 < waypoints.size())
        {
            return waypoints.get(currentIndex + 1);
        }
        return null;
    }

    /**
     * Advance to the next waypoint
     * Returns true if there was a next waypoint to advance to
     */
    public boolean advanceWaypoint()
    {
        if (currentIndex + 1 < waypoints.size())
        {
            currentIndex++;
            return true;
        }
        return false;
    }

    /**
     * Check if we've reached the end of the path
     */
    public boolean isComplete()
    {
        return currentIndex >= waypoints.size() - 1;
    }

    /**
     * Reset path progress to the beginning
     */
    public void reset()
    {
        currentIndex = 0;
    }

    /**
     * Get the total length of the path in blocks
     */
    public double getLength()
    {
        double totalLength = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++)
        {
            BlockPos current = waypoints.get(i);
            BlockPos next = waypoints.get(i + 1);
            totalLength += current.distanceSq(next.getX(), next.getY(), next.getZ());
        }
        return Math.sqrt(totalLength);
    }

    /**
     * Get the number of waypoints in the path
     */
    public int getWaypointCount()
    {
        return waypoints.size();
    }

    /**
     * Check if a position is close enough to the current waypoint to advance
     * Uses a distance threshold of 1.5 blocks
     */
    public boolean shouldAdvanceWaypoint(BlockPos currentPos)
    {
        return shouldAdvanceWaypoint(currentPos, 1.5);
    }

    /**
     * Check if a position is close enough to the current waypoint to advance
     */
    public boolean shouldAdvanceWaypoint(BlockPos currentPos, double threshold)
    {
        BlockPos currentWaypoint = getCurrentWaypoint();
        if (currentWaypoint == null) return false;

        double distanceSq = currentPos.distanceSq(currentWaypoint.getX(), currentWaypoint.getY(), currentWaypoint.getZ());
        return distanceSq <= threshold * threshold;
    }

    /**
     * Add a waypoint to the path (before the end point)
     */
    public void addWaypoint(BlockPos waypoint)
    {
        waypoints.add(waypoints.size() - 1, waypoint);
    }

    /**
     * Insert a waypoint at a specific index
     */
    public void insertWaypoint(int index, BlockPos waypoint)
    {
        if (index < 0 || index > waypoints.size())
        {
            throw new IndexOutOfBoundsException("Invalid waypoint index: " + index);
        }
        waypoints.add(index, waypoint);
    }

    @Override
    public String toString()
    {
        return "Path{" +
                "waypoints=" + waypoints.size() +
                ", start=" + start +
                ", end=" + end +
                ", currentIndex=" + currentIndex +
                '}';
    }
}