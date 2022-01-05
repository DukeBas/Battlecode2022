package Trainwreck.bots;

import Trainwreck.util.Constants;
import Trainwreck.util.DirectionBasedPathfinding;
import Trainwreck.util.Pathfinding;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Miner extends Robot {

    public Miner(RobotController rc) {
        super(rc);
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        /*
         * find all resources in range, start by getting all map locations in sight.
         */
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(myLocation, this.visionRadiusSquared);

        /*
         * Filter out locations without resources, add the number of resources in a combined object.
         * List sorted first on gold then lead.
         */
        List<LocationWithResources> ResourceLocations = new ArrayList<>();
        for (MapLocation loc : nearbyLocations) {
            int lead = rc.senseLead(loc);
            int gold = rc.senseGold(loc);
            if (lead > 0 || gold > 0) {
                ResourceLocations.add(new LocationWithResources(loc, lead, gold));
            }
        }
        Collections.sort(ResourceLocations);

        /*
         * Get the resources that are immediately mineable to us.
         * Note: could be optimized by not creating a new array, filtering ResourceLocations and rearranging code.
         */
        List<LocationWithResources> MineableLocations = new ArrayList<>();
        for (LocationWithResources lwr : ResourceLocations) {
            if (myLocation.distanceSquaredTo(lwr.loc) <= this.actionRadiusSquared) {
                MineableLocations.add(lwr);
            }
        }
        // Depending on list implementation it should still be sorted, but to be sure.
        Collections.sort(MineableLocations); // TODO find out if this really is unnecessary

        /*
         * Mine highest valued tiles around us first. Mining gold first.
         */
        for (LocationWithResources lwr : MineableLocations) {
            if (!rc.isActionReady()) {
                // if no more action can be taken, break out of loop to save bytecode.
                break;
            }
            // mine gold as long as it's available
            while (rc.canMineGold(lwr.loc)) {
                rc.mineGold(lwr.loc);
            }
        }
        for (LocationWithResources lwr : MineableLocations) {
            if (!rc.isActionReady()) {
                // if no more action can be taken, break out of loop to save bytecode.
                break;
            }
            // mines until there is one lead left.
            int leadLeft = rc.senseLead(lwr.loc);
            while (leadLeft > 1 && rc.canMineLead(lwr.loc)) {
                rc.mineLead(lwr.loc);
                leadLeft--;
            }
        }

        /*
         * If there is a gold resource in sight, travel towards it. Otherwise, go to the largest lead deposit nearby.
         * Since array is sorted, if the first place does not contain gold, none will. If the first place does
         * contain gold, then it automatically also contains the most amount available nearby.
         * Do not go to lead deposits with only 1 left.
         */
        Pathfinding pathfinder = new DirectionBasedPathfinding();
        Direction dir;
        if (ResourceLocations.size() > 0) { // there are targets
            LocationWithResources target = ResourceLocations.get(0);
            if (myLocation.equals(target.loc)) {
                dir = Direction.CENTER;
            } else if (target.gold == 0 && target.lead <= 1) { // No viable resources in range
                // move randomly
                dir = Constants.directions[rng.nextInt(Constants.directions.length)];
            } else {
                dir = pathfinder.getDirection(myLocation, target.loc, rc);
            }
        } else {
            // move randomly
            dir = Constants.directions[rng.nextInt(Constants.directions.length)];
        }

        // try to move toward target, if not already there
        rc.setIndicatorString("canMove("+dir+") = " + rc.canMove(dir));
        if (rc.canMove(dir)) { // do not move if already at target
            rc.move(dir);
        }


    }
}

/**
 * Record type of MapLocation and the amount of lead and gold on that position.
 */
class LocationWithResources implements Comparable<LocationWithResources> {
    public MapLocation loc;
    public int lead;
    public int gold;

    LocationWithResources(MapLocation loc, int lead, int gold) {
        this.loc = loc;
        this.lead = lead;
        this.gold = gold;
    }

    /**
     * First compare based on gold, then on lead.
     *
     * @param o LocationWithResources to compare to.
     * @return positive if this is greater, negative if other is greater, 0 if they have equal values.
     */
    @Override
    public int compareTo(LocationWithResources o) {
        // first compare based on gold
        int goldCompare = Integer.compare(this.gold, o.gold);
        if (goldCompare != 0) {
            return goldCompare;
        } else {
            // equal gold, compare on lead now
            // both lead and gold the same.
            return Integer.compare(this.lead, o.lead);
        }
    }
}
