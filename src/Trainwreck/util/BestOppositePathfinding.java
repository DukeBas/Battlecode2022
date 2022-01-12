package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Defines getDirection as best way to run away from a direction, only considering local rubble.
 */
public class BestOppositePathfinding implements Pathfinding {

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        Direction opposite = source.directionTo(target).opposite();
        Direction left = opposite.rotateLeft();
        Direction right = opposite.rotateRight();

        // have direct opposite as default
        Direction best = opposite;
        int lowestRubble = rc.senseRubble(source.add(opposite));

        // is slightly to the left better?
        MapLocation leftLoc = source.add(left);
        if (!rc.isLocationOccupied(leftLoc)) { // check if tile is free
            int leftRubble = rc.senseRubble(leftLoc);
            if (leftRubble < lowestRubble) {
                // left is better!
                lowestRubble = leftRubble;
                best = left;
            }
        }

        // is slightly to the right better?
        MapLocation rightLoc = source.add(right);
        if (!rc.isLocationOccupied(rightLoc)) { // check if tile is free
            int rightRubble = rc.senseRubble(rightLoc);
            if (rightRubble < lowestRubble) {
                // right is better!
                // no need to assign rubble value, since this is the last option
                best = right;
            }
        }

        return best;
    }
}
