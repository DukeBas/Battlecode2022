package Trainwreck.util;

import battlecode.common.*;

import static Trainwreck.util.Helper.isCombatUnit;

/**
 * Makes miners avoid enemy combat units if possible, spread out from other each other
 * and prefer to stay away from edges if possible.
 */
public class MinerSpreadingPathfinding implements Pathfinding {
    private static final int RANGE_TO_CONSIDER_FRIENDLIES = 10;
    private static final int RANGE_CLOSE_TO_CENTER = 15;

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) {
        MapLocation myLoc = rc.getLocation();

        /*
         * Avoid enemies. To conserve bytecode, only run from the first enemy spotted.
         * Most often enemies are coming from one general direction so running from the first,
         *  should be good enough for most situations.
         */
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, rc.getTeam().opponent());
        RobotInfo enemyCombatant = null;
        for (RobotInfo bot : nearbyEnemies) {
            if (isCombatUnit(bot.type)) { // Enemy combat unit spotted!
                enemyCombatant = bot;
                break;
            }
        }

        // are enemies nearby?
        try {
            if (enemyCombatant != null) {
                /*
                 * Enemies in sight! Run in the opposite direction, picking the lowest possible rubble.
                 */
                return bestOpposite(rc, myLoc.directionTo(enemyCombatant.location));
            }
        } catch (Exception e) {
            e.printStackTrace();
            rc.setIndicatorString(e.toString());
        }

        /*
         * No enemies near! Let's spread out from friendly miners and try not to go to the edge.
         */
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(RANGE_TO_CONSIDER_FRIENDLIES, rc.getTeam());
        RobotInfo closestMiner = null;
        int distClosestMiner = Integer.MAX_VALUE;
        for (RobotInfo bot : nearbyFriendlies) {
            if (bot.type == RobotType.MINER) {
                int dist = myLoc.distanceSquaredTo(bot.location);
                if (dist < distClosestMiner) { // we found a new closest miner!
                    closestMiner = bot;
                    distClosestMiner = dist;
                }
            }
        }

        if (closestMiner != null) { // there is another miner close!
            try {
                // get opposite direction
                Direction dir = bestOpposite(rc, myLoc.directionTo(closestMiner.location));

                // check if moving to this position puts us close to the edge (2 closest tiles to edge)
                MapLocation toMoveTo = myLoc.add(dir);
                if (!rc.onTheMap(toMoveTo.translate(2, 2)) ||
                        !rc.onTheMap(toMoveTo.translate(-2, 2)) ||
                        !rc.onTheMap(toMoveTo.translate(2, -2)) ||
                        !rc.onTheMap(toMoveTo.translate(-2, -2))) { // Location is close to the edge!
                    // Go toward friendly unit so they hopefully give us more space!
                    return bestOpposite(rc, dir);
                } else {
                    return dir;
                }
            } catch (Exception e) {
                e.printStackTrace();
                rc.setIndicatorString(e.toString());
            }
        }

        /*
         * No other friendly units in range, use other pathfinding technique.
         * Tend towards center of the map, to hopefully find resources along the way
         */
        MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        int distanceToCenter = myLoc.distanceSquaredTo(center);
        if (distanceToCenter > 18 && Math.random() < 0.2) {// Let's go towards the center!
            Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
            return pathfinder.getDirection(myLoc, center, rc);
        } else {
            Pathfinding pathfinder = new RandomPreferLessRubblePathfinding();
            return pathfinder.getDirection(myLoc, myLoc, rc);
        }
    }

    /**
     * Returns the best way to run away from target direction. Considers rubble.
     *
     * @param toTarget direction to target to get the best opposite of.
     */
    private Direction bestOpposite(RobotController rc, Direction toTarget) throws GameActionException {

        MapLocation myLoc = rc.getLocation();

        Direction opposite = toTarget.opposite();
        Direction left = opposite.rotateLeft();
        Direction right = opposite.rotateRight();

        // have direct opposite as default
        Direction best = opposite;
        int lowestRubble = rc.senseRubble(myLoc.add(opposite));

        // is slightly to the left better?
        MapLocation leftLoc = myLoc.add(left);
        if (!rc.isLocationOccupied(leftLoc)) { // check if tile is free
            int leftRubble = rc.senseRubble(leftLoc);
            if (leftRubble < lowestRubble) {
                // left is better!
                lowestRubble = leftRubble;
                best = left;
            }
        }

        // is slightly to the right better?
        MapLocation rightLoc = myLoc.add(right);
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
