package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Interface defines several versions of getDirection(...) with different arguments.
 */
public interface Pathfinding {
    /**
     * Returns direction robot should take to find a path from source to target.
     * Does not account for terrain.
     *
     * @param source location.
     * @param target location.
     * @return direction to take.
     */
    Direction getDirection(MapLocation source, MapLocation target);

    /**
     * Returns direction robot should take to find a path from source to target.
     *
     * @param source location.
     * @param target location.
     * @param terrain precomputed array of nearby terrain.
     * @return direction to take.
     */
    Direction getDirection(MapLocation source, MapLocation target, MapLocation[] terrain);

    /**
     * Returns direction robot should take to find a path from source to target.
     *
     * @param source location.
     * @param target location.
     * @param rc RobotController object of robot that wants to travel from source to target.
     * @return direction to take.
     */
    Direction getDirection(MapLocation source, MapLocation target, RobotController rc);
}

