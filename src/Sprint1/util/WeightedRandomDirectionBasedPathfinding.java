package Sprint1.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets intended direction from direction based. Then looks at that and the two adjacent directions,
 * picks output direction based on a weighted random choice based on rubble.
 */
public class WeightedRandomDirectionBasedPathfinding implements Pathfinding {
    private double preferenceStrength = 20; // Higher means less rubble is preferred even more.

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc)
            throws GameActionException {
        /*
         * Prepare weighted directions
         */
        List<RandomPreferLessRubblePathfinding.WeightedDirection> availableSpots = new ArrayList<>();
        double totalWeight = 0;

        /*
         * Get direct direction based pathfinding intended direction, see if that spot is available,
         * if so, add it.
         */
        Pathfinding pathfinder = new DirectionBasedPathfinding();
        Direction intended = pathfinder.getDirection(source, target, rc);
        totalWeight += addOption(source, rc, availableSpots, intended);
        Direction leftOfIntended = intended.rotateLeft();
        totalWeight += addOption(source, rc, availableSpots, leftOfIntended);
        Direction rightOfIntended = intended.rotateRight();
        totalWeight += addOption(source, rc, availableSpots, rightOfIntended);

        if (availableSpots.size() > 0) {
            // there are options
            return RandomPreferLessRubblePathfinding.pickRandomWeightedDirection(availableSpots, totalWeight);
        } else {
            // No options!
            return Direction.CENTER;
        }
    }

    private double addOption(MapLocation source, RobotController rc,
                             List<RandomPreferLessRubblePathfinding.WeightedDirection> availableSpots,
                             Direction dir) throws GameActionException {
        MapLocation loc = new MapLocation(source.x, source.y).add(dir);
        double weight = 0;

        // check if location is valid and open
        if (rc.onTheMap(loc) && !rc.isLocationOccupied(loc)) {
            // Location is open! Add it as an option! How much rubble is there?
            int rubble = rc.senseRubble(loc);

            // Determine weight based on action/movement cooldown formula inverse
            weight = 10.0 / (rubble * preferenceStrength + 10);
            availableSpots.add(new RandomPreferLessRubblePathfinding.WeightedDirection(dir, weight));
        }

        return weight;
    }
}
