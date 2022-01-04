package Trainwreck.bots;

import Trainwreck.util.Constants;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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
         * find all resources in range.
         */
        // get all map locations in sight
        MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(myLocation, 1000); // uses 1000 instead of vision range as it will only go up to robot vision range anyways
        // Filter out locations without resources, add the number of resources in a combined object.


        //TODO filter resources to mining range (2 r^2)

        //TODO mine gold around us first

        //TODO mine lead around us

        //TODO do not mine if it is the last lead from the pile?

        //TODO pathfind to gold sources if they exist, else go to lead sources with more than 1

        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }


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
class LocationWithResources {
    public MapLocation loc;
    public int lead;
    public int gold;

    LocationWithResources(MapLocation loc, int lead, int gold) {
        this.loc = loc;
        this.lead = lead;
        this.gold = gold;
    }
}