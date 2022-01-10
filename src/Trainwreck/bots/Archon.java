package Trainwreck.bots;

import Trainwreck.util.Constants;
import Trainwreck.util.Status;
import battlecode.common.*;

import java.util.Objects;

import static Trainwreck.util.Helper.isCombatUnit;


public class Archon extends Robot {

    /**
     * Dimensions of the map. Map ranges between 20x20 and 60x60.
     */
    private final int mapWidth;
    private final int mapHeight;

    /**
     * RobotID of this archon
     */
    private final int ownID;

    /**
     * Variable to keep track of which archon acts before others.
     */
    private final int turnOrder;

    /**
     * Max number of miners that one archon should make.
     */
    private static final int ARCHON_LOW_MINER_LIMIT = 10;
    private static final int ARCHON_HIGH_MINER_LIMIT = 15;

    /**
     * Minimum number of soldiers before they start charging towards an enemy
     * base.
     */
    private static final int SOLDIER_BATCH_SIZE = 15;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);

        // get our own ID
        ownID = rc.getID();

        // code specific to this robot's initialisation
        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();

        /*
         * Tell other archons we exist and, add possible enemy bases
         * based on symmetries and other friendly bases.
         */
        comms.addFriendlyArchon();

        // get when in the order we act
        turnOrder = comms.getArchonOrder(ownID);
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        // execute communication strategy
        communicationStrategy();

        // execute unit building strategy
        unitBuildingStrategy();

        /*
         * Check if we can still repair (and thus did not build)
         */
        if (rc.isActionReady()) {
            // we can still repair!
            RobotInfo[] friendlies = rc.senseNearbyRobots(actionRadiusSquared, friendly);
            for (RobotInfo bot : friendlies) {
                if (bot.health < bot.type.health) {
                    // damaged friendly bot found!
                    if (rc.canRepair(bot.location)) {
                        rc.repair(bot.location);
                    }
                }
            }
        }
    }

    /**
     * Strategy for archon's unit building distribution.
     * Current strategy is:
     * Is an enemy combat unit within vision range? Build a soldier if we can!
     * Else:
     * * do we have priority to build this turn?     (mechanism to ensure one archon does not spend all resources)
     * * OR
     * * do we currently have more than 150 lead?
     * * if so:
     * * * Build either a miner or soldier, depending on how many units we have, lead in the area and the turnCount.
     * <p>
     * <p>
     * FUTURE: depend on on how many units we have alive,map size, no. archons and turnCount for finer control.
     */
    void unitBuildingStrategy() throws GameActionException {
        /*
         * Get number of units currently alive (or at least last turn). Reset counter to be accurate for next turn.
         */
        int numberOfMiners = comms.getUnitCounter(ownID, RobotType.MINER);
////        int numberOfSages = comms.getUnitCounter(ownID, RobotType.SAGE);
        int numberOfSoldiers = comms.getUnitCounter(ownID, RobotType.SOLDIER);
////      int numberOfBuilders = comms.getUnitCounter(ownID, RobotType.BUILDER);
        comms.resetAllUnitCounters(ownID);


        // pick direction to build in
        Direction dir = buildingDirection();

        if (dir != null) { // there is room to build!
            /*
             * Check if there is an enemy combat unit in range
             */
            boolean enemyInRange = false;
            RobotInfo[] enemiesInRange = rc.senseNearbyRobots(visionRadiusSquared, enemy);
            for (RobotInfo bot : enemiesInRange) {
                if (isCombatUnit(bot.getType())) {
                    // Enemy combat unit in range!!!
                    enemyInRange = true;
                    break;
                }
            }

            if (enemyInRange) {
                // There was an enemy in range! Build a soldier if we can!
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
                return;
            }

            // No enemy combatants in range. Do we have priority to build this turn? Or do we have many resources?
            boolean priority = priorityCheck();
            if (priority || rc.getTeamLeadAmount(friendly) >= 150) { // we can build!

                rc.setIndicatorString(priority + " " + rc.getTeamLeadAmount(friendly));

                int minersNeeded = ARCHON_LOW_MINER_LIMIT;
                int nearbyLead = rc.senseNearbyLocationsWithLead(visionRadiusSquared).length;
                if (nearbyLead > minersNeeded * 4) {
                    // Lots of lead available! Get more miners!
                    minersNeeded = ARCHON_HIGH_MINER_LIMIT;
                }

                // Strategy beyond depends on how far we are in the game.
                if (turnCount < 100) {
                    /*
                     * Early game strategy, prioritise miners
                     */
                    // do we need more miners?
                    if (numberOfMiners < minersNeeded) {
                        // We need more miners! Try to build one!
                        if (rc.canBuildRobot(RobotType.MINER, dir)) {
                            rc.buildRobot(RobotType.MINER, dir);
                        }

                    } else {
                        // We have enough miners! Let's make some soldiers!
                        if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                            rc.buildRobot(RobotType.SOLDIER, dir);
                        }
                    }
                } else {
                    /*
                     * Late(r) game strategy. Mainly build soldiers, replenish miners every now and then.
                     */

                    RobotType toBuild = RobotType.SOLDIER; // default

                    // do we need more miners? If so, higher chance to make one
                    if (numberOfMiners < minersNeeded) {
                        // only sometimes build the miner
                        if (Math.random() < 0.10) {
                            toBuild = RobotType.MINER;
                        }
                    } else {
                        // we do not need miners, we might still want another one!
                        if (Math.random() < 0.01) {
                            toBuild = RobotType.MINER;
                        }
                    }

                    // build the desired robot if we can
                    if (rc.canBuildRobot(toBuild, dir)) {
                        rc.buildRobot(toBuild, dir);
                    }

                }
            }

        }

        if (numberOfSoldiers > SOLDIER_BATCH_SIZE) {
            comms.setState(Status.ATTACK_SIGNAL, true);
        }


//        // TESTING PURPOSES:
//        rc.setIndicatorString("Miners: " + numberOfMiners +
//                ", Sages: " + numberOfSages +
//                ", Soldiers:" + numberOfSoldiers +
//                ", Builders:" + numberOfBuilders);
    }

    /**
     * Determines a spot to build in. Prefers lighter terrain.
     * Returns null if no spot is free.
     *
     * @return direction to build in
     */
    private Direction buildingDirection() throws GameActionException {
        Direction dir = null;
        int lowestRubble = Integer.MAX_VALUE; // initialise with really high value
        for (Direction d : Constants.directions) {
            MapLocation loc = rc.getLocation().add(d); // add direction to current position

            // if location is not on map, skip
            if (!rc.onTheMap(loc)) continue;

            if (!rc.isLocationOccupied(loc)) {
                // we can build here!
                int rubbleHere = rc.senseRubble(loc);
                if (rubbleHere < lowestRubble) {
                    // we found a spot with less rubble!
                    lowestRubble = rubbleHere;
                    dir = d;
                }
            }
        }

        return dir;
    }


    /**
     * Checks whether this archon has priority this turn.
     *
     * @return whether this archon has priority now.
     */
    private boolean priorityCheck() throws GameActionException {
        final int totalFriendlyArchons = comms.getNumberFriendlyArchons();
        return turnCount % totalFriendlyArchons == turnOrder; // priority rotates evenly
    }


    /**
     * Communications strategy used by the archons.
     */
    @Override
    void communicationStrategy() throws GameActionException {
        super.communicationStrategy();

        rc.setIndicatorString(turnCount + "");
        if (turnCount == 1) {
            commsFirstRound();
        }
    }

    /**
     * Checks a location generated by using symmetry if it could contain an enemy archon, if so, add it.
     */
    private void commsFirstRound() throws GameActionException {
        /*
         * Add a potential enemy archon location for every symmetry.
         * Check for every location if it is already occupied by a friendly archon,
         * and if it is range, if so look if it is there.
         */

        MapLocation myLoc = rc.getLocation();

        // Clockwise rotations only happen on square maps!
        if (mapWidth == mapHeight) {
            // Rotation 90 degrees
            // Not necessary?

            // Rotation 180 degrees
            addSymmetricLocation(new MapLocation(mapWidth - myLoc.x - 1, mapHeight - myLoc.y - 1));

            // Rotation 270 degrees
            // Not necessary?
        }
        // Reflection horizontal
        addSymmetricLocation(new MapLocation(myLoc.x, mapHeight - myLoc.y - 1));

        // Reflection vertical
        addSymmetricLocation(new MapLocation(mapWidth - myLoc.x - 1, myLoc.y));

        MapLocation[] potentialEnemyBases = comms.getLocationsPotentialEnemyArchons();

        StringBuilder out = new StringBuilder();


        // DEBUG
        for (MapLocation loc : potentialEnemyBases) {
            out.append(loc.toString()).append(" ");
        }

        rc.setIndicatorString("pot. en.: " + out);
    }

    /**
     * @param target location of potential enemy archon
     */
    private void addSymmetricLocation(MapLocation target) throws GameActionException {
        /*
         * Check if location is in range, if so check if there is an archon there.
         */
        if (rc.canSenseLocation(target)) {
            // it is in range!
            RobotInfo robotAtLocation = rc.senseRobotAtLocation(target);
            if (robotAtLocation == null) {
                // No robot here! Disproven!
                return;
            }
            // There's a robot here!
            if (robotAtLocation.team.equals(enemy)) {
                // enemy spotted! Record it!
                comms.addEnemyArchon(target, robotAtLocation.ID);
            }
            // Either enemy location recorded or it was friendly! Disproven!
            return;
        }

        /*
         * Check if location is covered by a friendly archon already.
         */
        MapLocation[] friendlyArchons = comms.getLocationsFriendlyArchons();
        for (
                MapLocation loc : friendlyArchons) { // check all known friendly archons
            if (target.equals(loc)) {
                // We found a friendly archon on that position! Disproven!
                return;
            }
        }

        /*
         * If we got here, it means we could not disprove an archon could be on the location,
         * add it to the list of potential enemy archon locations!
         */
        comms.addPotentialEnemyArchonLocation(target);
    }
}
