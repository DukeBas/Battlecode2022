package Trainwreck.bots;

import Trainwreck.util.Communication;
import Trainwreck.util.FirstCommunication;
import battlecode.common.*;

import java.util.Objects;
import java.util.Random;

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
            if (isDroid(ownType)) {
                // Droid, always built by an archon!
                RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(2, friendly);

                /*
                 * Go through nearby friendly units until an archon is found.
                 * even if this is not the exact correct one, it means that the archons are very close
                 * together, so it should not matter in the grand scheme of things.
                 */
                for (RobotInfo bot : nearbyFriendlies) {
                    if (bot.getType() == RobotType.ARCHON) {
                        creatorID = bot.getID();
                        break;
                    }
                }
            } else {
                // Not a droid, so must be either a laboratory or watchtower.
                // NOTE: WE ASSUME HERE BUILD RANGE IS 5 (ACTION RADIUS), BUT IT COULD BE 2.
                // Droid, always built by an archon!
                RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(5, friendly);

                /*
                 * Go through nearby friendly units until a builder is found.
                 * There might be multiple close, so this is currently not accurate.
                 */
                for (RobotInfo bot : nearbyFriendlies) {
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
                throw e;
                System.out.println(ownType + " GameAction-Exception");
                e.printStackTrace();

                rc.setIndicatorString("GameActionException: " + e);
            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(ownType + " Generic-Exception");
                e.printStackTrace();

                rc.setIndicatorString("Exception: " + e);
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

    private void increaseUnitCounter() throws GameActionException {
        comms.increaseUnitCounter(builtByID, ownType);
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
