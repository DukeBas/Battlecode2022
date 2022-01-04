package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Simple pathfinding going straight to target, ignoring terrain and other robots.
 */
public class DirectionBasedPathfinding implements Pathfinding {
    @Override
    public Direction getDirection(MapLocation source, MapLocation target) {
        return source.directionTo(target);
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, MapLocation[] terrain) {
        return getDirection(source, target);
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) {
        return getDirection(source, target);
    }
}
