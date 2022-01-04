package Trainwreck.bots;

import Trainwreck.util.Constants;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        //TODO pathfind to gold sources if they exist, else go to lead sources with more than 1


        // Also try to move randomly.
        Direction dir = Constants.directions[rng.nextInt(Constants.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
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