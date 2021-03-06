package Sprint2.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Simple pathfinding going straight to target, ignoring terrain and other robots.
 */
public class DirectionBasedPathfinding implements Pathfinding {
    @Override
    public Direction getDirection(final MapLocation source, final MapLocation target, RobotController rc) {
        return source.directionTo(target);
    }
}
