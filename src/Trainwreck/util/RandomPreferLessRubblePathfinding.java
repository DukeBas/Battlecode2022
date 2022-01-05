package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;

public class RandomPreferLessRubblePathfinding implements Pathfinding {
    double preferenceStrength = 5; // Higher means less rubble is preferred even more.

    public RandomPreferLessRubblePathfinding() {
        // do not change default strength
    }

    public RandomPreferLessRubblePathfinding(double pref) {
        this.preferenceStrength = pref;
    }

    @Override
    public Direction getDirection(final MapLocation source, final MapLocation target, RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        Direction dir;
        ArrayList<WeightedDirection> options = new ArrayList<>();
        double totalWeight = 0;

        try { // rc.onTheMap throws a game exception if checked location is outside of vision, should never be the case
            for (Direction d : Constants.directions) {
                MapLocation loc = new MapLocation(myLocation.x, myLocation.y).add(d); // from a new location object
                if (rc.onTheMap(loc)) {
                    // location is on the map! How much rubble is there?
                    int rubble = rc.senseRubble(loc);

                    // Determine weight based on action/movement cooldown formula inverse
                    double weight = 10.0 / (rubble * preferenceStrength + 10);
                    options.add(new WeightedDirection(d, weight));
                    totalWeight += weight;
                }
            }
        } catch (Exception e) {
            System.out.println("RandomPreferLessRubblePathfinding: checked location not in vision! " + e);
        }

        if (options.size() == 0) {
            // no options were added, don't move (should never happen)
            dir = Direction.CENTER;
        } else {
            // pick a random direction based on the weight of the items
            int index = 0;
            for (double r = Math.random() * totalWeight; index < options.size() - 1; index++) {
                r -= options.get(index).weight;
                if (r <= 0.0) {
                    break;
                }
            }
            dir = options.get(index).dir;
        }

        return dir;
    }

    /**
     * Record type for attaching a weight to a direction.
     */
    private class WeightedDirection {
        public Direction dir;
        public double weight;

        WeightedDirection(Direction dir, double weight) {
            this.dir = dir;
            this.weight = weight;
        }
    }
}

