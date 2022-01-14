package Trainwreck.bots;

import Trainwreck.util.Communication;
import Trainwreck.util.Constants;
import Trainwreck.util.FirstCommunication;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

import static Trainwreck.util.Helper.isCombatUnit;
import static Trainwreck.util.Helper.isDroid;

public abstract class Robot {

    /**
     * This is the RobotController singleton. You use it to perform actions from this robot,
     * and to get information on its current status.
     */
    protected final RobotController rc;

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     */
    protected int turnCount = 0;

    /**
     * Helper field to easily access a robots action range.
     */
    protected final int actionRadiusSquared;

    /**
     * Helper field to easily access a robots vision range.
     */
    protected final int visionRadiusSquared;

    /**
     * Interface to interact with the shared memory
     */
    protected final Communication comms;

    /**
     * Variable to hold a location as a target enemy archon.
     */
    protected MapLocation targetArchonLocation = null;

    /**
     * Variable to hold a location to path to.
     */
    protected MapLocation targetPathfindingLocation = null;

    /**
     * Variable to store ID of enemy team
     */
    protected final Team enemy;

    /**
     * Variable to store ID of own team
     */
    protected final Team friendly;

    /**
     * Variable to store own robot type, so compiler can (hopefully) optimize.
     */
    protected final RobotType ownType;

    /**
     * Variable to hold the ID of the archon/builder who originally built this robot.
     * -1 for archons, as they are only generated at the start of the game.
     * -2 if creator was not found. Could only happen if creator dies (Moves out of range?) before initialisation.
     */
    protected final int builtByID;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    public Robot(RobotController rc) {
        this.rc = rc;
        this.ownType = rc.getType();
        friendly = rc.getTeam();
        enemy = friendly.opponent();

        /*
         * Get the ID of the original robot that built this robot.
         */
        int creatorID = -2; // default, signalling failure to find creator. We do this because builtByID is final.
        if (ownType == RobotType.ARCHON) {
            // Archons are not built by other robots!
            creatorID = -1;
        } else {
            // Not an archon!
            RobotInfo[] closeFriendlies = rc.senseNearbyRobots(2, friendly);
            if (isDroid(ownType)) {
                // Droid, always built by an archon!

                /*
                 * Go through nearby friendly units until an archon is found.
                 * even if this is not the exact correct one, it means that the archons are very close
                 * together, so it should not matter in the grand scheme of things.
                 */
                for (RobotInfo bot : closeFriendlies) {
                    if (bot.getType() == RobotType.ARCHON) {
                        creatorID = bot.getID();
                        break;
                    }
                }
            } else {
                // Not a droid, so must be either a laboratory or watchtower.
                // Droid, always built by an archon!

                /*
                 * Go through nearby friendly units until a builder is found.
                 * There might be multiple close, so this is currently not accurate.
                 */
                for (RobotInfo bot : closeFriendlies) {
                    if (bot.getType() == RobotType.BUILDER) {
                        creatorID = bot.getID();
                        break;
                    }
                }
            }
        }
        builtByID = creatorID;

        /*
         * Initialise communications object
         */
        comms = new FirstCommunication(rc);

        actionRadiusSquared = ownType.actionRadiusSquared; // Need to check how this works out for Lab as it has no action range
        visionRadiusSquared = ownType.visionRadiusSquared;
    }

    /**
     * Runs the game loop for the rest of the game and does not return.
     */
    public void runGameLoop() {
        /* This code runs during the entire lifespan of the robot, which is why it is in an infinite
         * loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
         * loop, we call Clock.yield(), signifying that we've done everything we want to do.
         */

        while (true) {
            try {
                // Execute 1 round of actions for this robot.
                this._run();

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(ownType + " GameAction-Exception");
                e.printStackTrace();

                rc.setIndicatorString("G-E: " + e);
            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(ownType + " Generic-Exception");
                e.printStackTrace();

                rc.setIndicatorString(e.getStackTrace()[0].getMethodName() + " : " + e);
            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Execution should never reach here. (unless intentional) Self-destruction imminent!
    }

    /**
     * Method to hold communication actions.
     */
    void communicationStrategy() throws GameActionException {
        comms.checkForEnemyArchons();

        /*
         * If we have a target location and can see it, then the archon was added before in comms.checkForEnemyArchons();
         * So only clear location if in vision range
         */
        if (targetArchonLocation != null && rc.canSenseLocation(targetArchonLocation)) {
            // if location does not contain an enemy archon, invalidate it
            RobotInfo robotAtLocation = rc.senseRobotAtLocation(targetArchonLocation);
            if (!(robotAtLocation != null && robotAtLocation.team == enemy)) {
                // nothing here! clear target location!
                rc.setIndicatorString("NOTHING HERE, CLEARING");
                comms.invalidateLocationEnemyArchon(targetArchonLocation);
                targetArchonLocation = null;
            }
        }

        /*
         * Increase unit counters, so archons know how many units of each type are alive at all times.
         */
        if (isDroid(ownType)) {
            increaseUnitCounter();
        }
    }

    /**
     * Determines a spot to build in. Prefers lighter terrain.
     * Returns null if no spot is free.
     *
     * @return direction to build in
     */
    protected final Direction buildingDirection() throws GameActionException {
        Direction dir = null;
        int lowestRubble = Integer.MAX_VALUE; // initialise with really high value
        for (Direction d : Constants.directions) {
            MapLocation loc = rc.getLocation().add(d); // add direction to current position

            // if location is not on map, skip
            if (!rc.onTheMap(loc)) continue;

            // TODO: isLocationOccupied() costs more than canBuildRobot(),
            // could optimize
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
     * Increases the unit counter for this unit type in the communications.
     */
    private void increaseUnitCounter() throws GameActionException {
        comms.increaseUnitCounter(builtByID, ownType);
    }

    /**
     * Takes an array of robots and returns a copy with all non-combat units filtered out.
     *
     * @param robots to filter
     * @return filtered array to combatants
     */
    protected final RobotInfo[] getCombatants(RobotInfo[] robots) {
        // could use Java 8 stream API here instead
        ArrayList<RobotInfo> combatants = new ArrayList<>();

        for (RobotInfo bot : robots) {
            if (isCombatUnit(bot.type)) {
                combatants.add(bot);
            }
        }

        return combatants.toArray(new RobotInfo[0]);
    }

    /**
     * Run 1 round of this robot, including the actions beformed before and after by this super class.
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    private void _run() throws GameActionException {
        turnCount++;
        this.run();
    }

    /**
     * Run 1 round of this robot.
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    abstract void run() throws GameActionException;
}
