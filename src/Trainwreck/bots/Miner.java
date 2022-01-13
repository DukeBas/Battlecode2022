package Trainwreck.bots;

import Trainwreck.util.*;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Miner extends Robot {
    final int MAX_RESOURCE_LOCATIONS = 15; // at least 8

    public Miner(RobotController rc) {
        super(rc);

        /*
         * Generate semi-random initial starting location based on own location, map and archon.
         */
        // TODO.. something depending on archon, edges, layout, and symmetry?
//        targetPathfindingLocation = new MapLocation(11, 11);
        determineTargetLocation(rc.getLocation()); // temporarily just call regular set location function
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
         * Get nearby enemies.
         */
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(visionRadiusSquared, enemy);
        RobotInfo[] nearbyEnemyCombatants = getCombatants(nearbyEnemies);

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
        // TODO if close to enemy archon, do mine last 1 lead of a deposit!
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
         * If there is a resource deposit nearby, travel towards the best one (but not if there are too many enemies).
         * Else, go towards target location.
         */
        determineTargetLocation(myLocation);
        Direction dir;
        if (ResourceLocations.size() > 0) { // there are resource deposits nearby
            LocationWithResources targetResource = ResourceLocations.get(0); // get the best one

            RobotInfo[] combatantsNearResource = getCombatants(rc.senseNearbyRobots(targetResource.loc,
                    RobotType.SOLDIER.actionRadiusSquared, enemy));
            if (combatantsNearResource.length > 0){
                // there are enemy combatants nearby the resource!
                dir = new BestOppositePathfinding().getDirection(myLocation, combatantsNearResource[0].location, rc);
                rc.setIndicatorString("enemies near resource!");
            } else {
                rc.setIndicatorString(targetResource.loc + " ");
                dir = new WeightedRandomDirectionBasedPathfinding().getDirection(myLocation, targetResource.loc, rc);
            }

        } else { // no resource nearby!
            if (nearbyEnemyCombatants.length > 0) { // there are enemy combatants nearby!
                dir = new BestOppositePathfinding().getDirection(myLocation, nearbyEnemyCombatants[0].location, rc);
                rc.setIndicatorString("enemies nearby!");
                // force reset target location, so we do not keep going towards enemy
                targetPathfindingLocation = null;

            } else { // coast is clear
                rc.setIndicatorString("Going toward " + targetPathfindingLocation);
                dir = new WeightedRandomDirectionBasedPathfinding().getDirection(myLocation, targetPathfindingLocation, rc);
            }
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
    }


    /**
     * Checks if we are close to target location and sets a new one.
     */
    private void determineTargetLocation(MapLocation myLoc) {
        // are we close to target?
        if (targetPathfindingLocation == null || (myLoc.distanceSquaredTo(targetPathfindingLocation) <= 8)) {
            /*
             * We are close to target location, or we do not have one. Generate a new one.
             */
            //TODO... develop a more sophisticated target generation algorithm
            int x = (int) (rc.getMapWidth() * Math.random());
            int y = (int) (rc.getMapHeight() * Math.random());
            targetPathfindingLocation = new MapLocation(x, y);
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
