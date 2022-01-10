package Trainwreck.bots;

import Trainwreck.util.*;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Miner extends Robot {
    final int MAX_RESOURCE_LOCATIONS = 15; // at least 8

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
         * Communicate!
         */
        communicationStrategy();

        List<LocationWithResources> ResourceLocations = new ArrayList<>();
        List<LocationWithResources> MineableLocations = new ArrayList<>();

        /*
         * Fills list with locations with resources first with locations with gold,
         * then with tiles with lead if there are no locations with gold.
         * Only adds up to MAX_RESOURCE_LOCATIONS number of locations to list.
         * Starts for lead with locations withing actions radius. Only considers lead with more than 1 remaining.
         * Keep track if we can mine the resource
         */
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(this.visionRadiusSquared);

        if (nearbyGold.length > 0) {
            for (MapLocation loc : nearbyGold) {
                LocationWithResources lwr = new LocationWithResources(loc, rc.senseLead(loc), rc.senseGold(loc));
                // can't go over maximum number yet, so unchecked
                ResourceLocations.add(lwr);
                if (myLocation.distanceSquaredTo(loc) <= this.actionRadiusSquared) { // check if we can mine it
                    MineableLocations.add(lwr);
                }
            }
        } else {
            // start by looking at locations with more than 1 lead in our action radius
            MapLocation[] mineableLead = rc.senseNearbyLocationsWithLead(this.actionRadiusSquared, 2);
            for (MapLocation loc : mineableLead) {
                int lead = rc.senseLead(loc);
                // can't go over maximum number yet, so unchecked
                LocationWithResources lwr = new LocationWithResources(loc, lead, 0);
                ResourceLocations.add(lwr);
                // we do not need to check if we can mine the resource, we always can
                MineableLocations.add(lwr);
            }

            // no lead currently mineable, look beyond action range. Only consider tiles with more than 1 lead.
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(this.visionRadiusSquared, 2);
            for (MapLocation loc : nearbyLead) {
                if (ResourceLocations.size() >= MAX_RESOURCE_LOCATIONS) {
                    break; // prevent any more locations from being added.
                }
                int lead = rc.senseLead(loc);
                ResourceLocations.add(new LocationWithResources(loc, lead, 0));
                // we do not need to check if we can mine the resource, always impossible

            }
        }
        /*
         * Sort the found locations with resources and go to the best, prioritising gold over lead.
         * Note: does not actually sort if there are more than 8 items in queue, since then none are in range
         * (see above), so only the best actually matters, so we remove everything else.
         *
         */
        if (ResourceLocations.size() <= 8) {
            Collections.sort(ResourceLocations);
        } else {
            LocationWithResources best = Collections.max(ResourceLocations);
            ResourceLocations = new ArrayList<>();
            ResourceLocations.add(best);
        }

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
         * Do not go to lead deposits with only 1 left. (see earlier comment for what locations we consider.)
         * Spread out, maximising coverage of map.
         */
        Direction dir;
        if (ResourceLocations.size() > 0) { // there are targets
            LocationWithResources target = ResourceLocations.get(0);
            if (target.gold == 0 && target.lead <= 1) { // No viable resources in range
                // move randomly, spreading out
                Pathfinding randomPathfinder = new MinerSpreadingPathfinding();
                dir = randomPathfinder.getDirection(myLocation, myLocation, rc);
            } else { // find direction to target
                Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
                dir = pathfinder.getDirection(myLocation, target.loc, rc);
                if (myLocation.equals(target.loc)) { // Stand still if at target
                    dir = Direction.CENTER;
                }
            }
        } else {
            // move randomly, spreading out
            Pathfinding randomPathfinder = new MinerSpreadingPathfinding();
            dir = randomPathfinder.getDirection(myLocation, myLocation, rc);

        }

        // try to move toward target, if not already there
//        rc.setIndicatorString("canMove(" + dir + ") = " + rc.canMove(dir));
        if (!dir.equals(Direction.CENTER) && rc.canMove(dir)) { // do not move if already at target
            rc.move(dir);
        }
    }


    @Override
    void communicationStrategy() throws GameActionException {
        super.communicationStrategy(); // execute commands in super class

        /*
         * Update target, if one is available
         */
        targetArchonLocation = comms.getClosestPotentialEnemyArchonLocation();
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
