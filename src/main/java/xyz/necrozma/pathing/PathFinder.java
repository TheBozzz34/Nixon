package xyz.necrozma.pathing;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import xyz.necrozma.util.PlayerUtil;


import java.util.*;

/**
 * A* pathfinding implementation for Minecraft
 * Finds the optimal path between two BlockPos positions
 */
public class PathFinder
{
    private final PathValidator validator;
    private final int maxSearchDistance;
    private final int maxIterations;

    /**
     * Interface for validating whether a position is walkable
     */
    public interface PathValidator
    {
        /**
         * Check if a position is walkable
         * @param pos The position to check
         * @return true if the position can be walked on
         */
        boolean isWalkable(BlockPos pos);

        /**
         * Get the movement cost for moving to this position
         * @param from The starting position
         * @param to The target position
         * @return The cost of moving (higher = more expensive)
         */
        default double getMovementCost(BlockPos from, BlockPos to)
        {
            // Default cost based on distance
            int dx = Math.abs(to.getX() - from.getX());
            int dy = Math.abs(to.getY() - from.getY());
            int dz = Math.abs(to.getZ() - from.getZ());

            // Diagonal movement costs more
            if (dx > 0 && dz > 0) return 1.414; // sqrt(2)
            if (dy > 0) return 1.2; // Slight penalty for vertical movement
            return 1.0;
        }
    }

    /**
     * Internal node class for A* algorithm
     */
    private static class Node implements Comparable<Node>
    {
        public final BlockPos pos;
        public final Node parent;
        public final double gCost; // Cost from start
        public final double hCost; // Heuristic cost to end
        public final double fCost; // Total cost

        public Node(BlockPos pos, Node parent, double gCost, double hCost)
        {
            this.pos = pos;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public int compareTo(Node other)
        {
            int result = Double.compare(this.fCost, other.fCost);
            if (result == 0)
            {
                result = Double.compare(this.hCost, other.hCost);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return pos.equals(other.pos);
        }

        @Override
        public int hashCode()
        {
            return pos.hashCode();
        }
    }

    /**
     * Create a new PathFinder with default settings
     */
    public PathFinder(PathValidator validator)
    {
        this(validator, 256, 10000);
    }

    /**
     * Create a new PathFinder with custom limits
     * @param validator The path validation interface
     * @param maxSearchDistance Maximum distance to search
     * @param maxIterations Maximum iterations before giving up
     */
    public PathFinder(PathValidator validator, int maxSearchDistance, int maxIterations)
    {
        this.validator = validator;
        this.maxSearchDistance = maxSearchDistance;
        this.maxIterations = maxIterations;
    }

    /**
     * Find a path from start to end using A*
     * @param start Starting position
     * @param end Ending position
     * @return Path object containing the route, or null if no path found
     */
    public Path findPath(BlockPos start, BlockPos end)
    {
        if (start.equals(end))
        {
            return new Path(start, end);
        }

        // Check if start and end are valid
        if (!validator.isWalkable(start) || !validator.isWalkable(end))
        {
            return null;
        }

        // Check if end is too far away
        if (start.distanceSq(end.getX(), end.getY(), end.getZ()) > maxSearchDistance * maxSearchDistance)
        {
            return null;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, Node> allNodes = new HashMap<>();

        // Initialize start node
        Node startNode = new Node(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations)
        {
            iterations++;
            Node current = openSet.poll();

            // Check if we reached the goal
            if (current.pos.equals(end))
            {
                return reconstructPath(current);
            }

            closedSet.add(current.pos);

            // Check all neighbors
            for (BlockPos neighbor : getNeighbors(current.pos))
            {
                if (closedSet.contains(neighbor) || !validator.isWalkable(neighbor))
                {
                    continue;
                }

                double movementCost = validator.getMovementCost(current.pos, neighbor);
                double tentativeGCost = current.gCost + movementCost;

                Node neighborNode = allNodes.get(neighbor);

                if (neighborNode == null)
                {
                    // New node
                    neighborNode = new Node(neighbor, current, tentativeGCost, heuristic(neighbor, end));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                }
                else if (tentativeGCost < neighborNode.gCost)
                {
                    // Found better path to this node
                    openSet.remove(neighborNode);
                    neighborNode = new Node(neighbor, current, tentativeGCost, heuristic(neighbor, end));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        // No path found
        return null;
    }

    /**
     * Get all valid neighboring positions
     */
    private List<BlockPos> getNeighbors(BlockPos pos)
    {
        List<BlockPos> neighbors = new ArrayList<>();

        // 8 horizontal directions + up/down
        int[][] directions = {
                {-1, 0, 0}, {1, 0, 0},   // West, East
                {0, 0, -1}, {0, 0, 1},   // North, South
                {-1, 0, -1}, {1, 0, -1}, // NW, NE
                {-1, 0, 1}, {1, 0, 1},   // SW, SE
                {0, 1, 0}, {0, -1, 0}    // Up, Down
        };

        for (int[] dir : directions)
        {
            BlockPos neighbor = pos.add(dir[0], dir[1], dir[2]);
            neighbors.add(neighbor);
        }

        return neighbors;
    }

    /**
     * Calculate heuristic distance (Manhattan distance with diagonal allowance)
     */
    private double heuristic(BlockPos from, BlockPos to)
    {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());

        // Use 3D Euclidean distance as heuristic
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Reconstruct the path from the goal node back to start
     */
    private Path reconstructPath(Node goalNode)
    {
        List<BlockPos> waypoints = new ArrayList<>();
        Node current = goalNode;

        while (current != null)
        {
            waypoints.add(current.pos);
            current = current.parent;
        }

        // Reverse to get path from start to end
        Collections.reverse(waypoints);

        return new Path(waypoints);
    }

    /**
     * Simple path validator that checks basic movement rules
     */
    public static class BasicValidator implements PathValidator
    {
        private final World world;

        public BasicValidator(World world)
        {
            this.world = world;
        }

        @Override
        public boolean isWalkable(BlockPos pos)
        {
            try
            {
                // Check if the block at pos is solid (can stand on)
                BlockPos ground = pos.down();
                IBlockState groundState = world.getBlockState(ground);
                if (groundState == null || groundState.getBlock() == null)
                {
                    return false;
                }

                // Check if ground block is solid
                Block groundBlock = groundState.getBlock();
                if (!groundBlock.isFullBlock() /*&& !groundBlock.isSolidFullCube()*/)
                {
                    return false;
                }

                // Check if there's space to walk (2 blocks high)
                IBlockState currentState = world.getBlockState(pos);
                IBlockState aboveState = world.getBlockState(pos.up());

                if (currentState == null || aboveState == null)
                {
                    return false;
                }

                Block currentBlock = currentState.getBlock();
                Block aboveBlock = aboveState.getBlock();

                if (currentBlock == null || aboveBlock == null)
                {
                    return false;
                }

                // Check if both blocks are passable (not solid)
                if ((currentBlock.isFullBlock() /*&& currentBlock.isSolidFullCube()*/) ||
                        (aboveBlock.isFullBlock() /*&& aboveBlock.isSolidFullCube()*/))
                {
                    return false;
                }

                return true;
            }
            catch (Exception e)
            {
                // If any exception occurs, assume position is not walkable
                return false;
            }
        }

        @Override
        public double getMovementCost(BlockPos from, BlockPos to)
        {
            int dx = Math.abs(to.getX() - from.getX());
            int dy = Math.abs(to.getY() - from.getY());
            int dz = Math.abs(to.getZ() - from.getZ());

            // Higher cost for vertical movement
            if (dy > 0) return 1.5;

            // Higher cost for diagonal movement
            if (dx > 0 && dz > 0) return 1.414;

            return 1.0;
        }
    }
}