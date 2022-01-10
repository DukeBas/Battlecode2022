package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Defines a pathfinding option to find a 'good' spot in range of an archon,
 * without blocking tiles where archon can build.
 */
public class SpotNearArchonPathfinding implements Pathfinding {
    private final int ARCHON_RANGE = 25; // in what R^2 of the archon should we stay?

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        MapLocation targetSpot = null; // where we want to go to
        int leastRubble = Integer.MAX_VALUE;

        /*
         * Consider 5x5 square around us
         */
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                MapLocation loc = source.translate(dx, dy);

                // is the location valid?
                if (rc.onTheMap(loc))
            }
        }






        /*
         * We must move if we are blocking the archon, else only move if beneficial
         */
        if (source.distanceSquaredTo(target) <= 2) {
            // we are blocking the archon spawns!
        }


        return null;
    }

    /**
     * Checks if a location is in a specified range of an archon.
     *
     * @param archon location of the archon
     * @param loc    to check
     * @param range  to compare to
     * @return whether the location is in the specified range of the archon
     */
    private boolean locationInArchonRange(MapLocation archon, MapLocation loc, int range) {
        return (archon.distanceSquaredTo(loc) <= range);
    }

    /**
     * Consider squares around the archon and on the diagonal as blocking (so units can escape).
     *
     * @param archon location of the archon
     * @param loc    to check
     * @return whether the location is a blocking one for the archon.
     */
    private boolean blockingArchon(MapLocation archon, MapLocation loc) {
        int dist = archon.distanceSquaredTo(loc);
        return dist <= 2 || dist == 8 || dist == 18 || dist == 32; // checks for ring of at most 34
    }
}
