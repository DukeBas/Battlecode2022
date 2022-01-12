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

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        MapLocation targetSpot = source; // where we want to go to, initialise as current spot
        int bestPenaltyScore = Integer.MAX_VALUE; // lower score is better

        /*
         * Consider 5x5 square around us
         */
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                MapLocation loc = source.translate(dx, dy);

                // is the location valid?
                if (!rc.onTheMap(loc) || // is it on the map?
                        !rc.canSenseLocation(loc) || // can we sense the rubble there?
                        !locationInArchonRange(target, loc) || // is it in range of the archon?
                        blockingArchon(target, loc) || // is it blocking the archon?
                        rc.isLocationOccupied(loc)) // is the location already occupied?
                {
                    // location is not valid!
                    continue; // skip to the next one
                }

                // is the location better?
                int rubble = rc.senseRubble(loc);
                int score = calcScore(target, loc, rubble);
                if (score < bestPenaltyScore) {
                    // new better spot found!
                    targetSpot = loc;
                    bestPenaltyScore = score;
                }
            }
        }

//        rc.setIndicatorString(targetSpot + " " + blockingArchon(target, source) + " "+
//                        !rc.canSenseLocation(loc) + " " +
//                        !locationInArchonRange(target, source) + " " +
//                !rc.onTheMap(targetSpot) + " " +
//                !rc.onTheMap(targetSpot) + " " +
//                );

        Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
        return pathfinder.getDirection(source, targetSpot, rc);
    }

    /**
     * Checks if a location is in a specified range of an archon.
     *
     * @param archon location of the archon
     * @param loc    to check
     * @return whether the location is in the specified range of the archon
     */
    private boolean locationInArchonRange(MapLocation archon, MapLocation loc) {
        return (archon.distanceSquaredTo(loc) <= 25); // in what R^2 of the archon should we stay?
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

    /**
     * Use a scoring function to determine the best tiles around the archon. Uses distance from archon and rubble.
     * Lower is better.
     * This was done as using just rubble made all units go to one direction (the first considered in dx/dy loop).
     *
     * @param archon location of the archon
     * @param loc    to calculate the score for
     * @param rubble on that location
     * @return score of the location
     */
    private int calcScore(MapLocation archon, MapLocation loc, int rubble) {
        return rubble + archon.distanceSquaredTo(loc);
    }
}
