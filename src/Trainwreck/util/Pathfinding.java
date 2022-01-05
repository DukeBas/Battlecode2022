package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Interface defines getDirection function for pathfinding.
 */
public interface Pathfinding {
    /**
     * Returns direction robot should take to find a path from source to target.
     *
     * @param source location
     * @param target location
     * @param rc     RobotController object of robot that wants to travel from source to target
     * @return direction to take
     */
    Direction getDirection(MapLocation source, MapLocation target, RobotController rc);
}

