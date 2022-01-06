package Trainwreck.bots;

import Trainwreck.util.Communication;
import Trainwreck.util.FirstCommunication;
import battlecode.common.*;
import Trainwreck.util.Constants;

import java.util.Objects;
import java.util.Random;

public abstract class Robot {

    /**
     * This is the RobotController singleton. You use it to perform actions from this robot,
     * and to get information on its current status.
     */
    final RobotController rc;

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
    final Communication comms;

    /**
     * Variable to hold a location as a target enemy archon.
     */
    MapLocation targetArchonLocation = null;

    /**
     * Variable to store ID of enemy team
     */
    final Team enemy;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    public Robot(RobotController rc) {
        this.rc = rc;
        System.out.println("Hello world! I am a " + this.rc.getType());

        /*
         * Initialise communications object
         */
        comms = new FirstCommunication(rc);

        actionRadiusSquared = rc.getType().actionRadiusSquared; // Need to check how this works out for Lab as it has no action range
        visionRadiusSquared = rc.getType().visionRadiusSquared;

        enemy = rc.getTeam().opponent();
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
                System.out.println(rc.getType() + " GameAction-Exception");
                this.rc.setIndicatorString(e.getClass().toString() + ": " + e.getMessage());
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Generic-Exception");
                this.rc.setIndicatorString(e.getClass().toString() + ": " + e.getMessage());
                e.printStackTrace();

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
        rc.setIndicatorString("start comms turn " + turnCount);
        comms.checkForEnemyArchons();

        /*
         * If we have a target location and can see it, then the archon was added before in comms.checkForEnemyArchons();
         * So only clear location if in vision range
         */
        if (Objects.nonNull(targetArchonLocation) && rc.canSenseLocation(targetArchonLocation)){
            // if location does not contain an enemy archon, invalidate it
            RobotInfo robotAtLocation = rc.senseRobotAtLocation(targetArchonLocation);
            if (Objects.nonNull(robotAtLocation) && robotAtLocation.team == enemy){
                // actually found an enemy archon!
            } else {
                // nothing here! clear target location!
                rc.setIndicatorString("NOTHING HERE, CLEARING");
                comms.invalidateLocationEnemyArchon(targetArchonLocation);
                targetArchonLocation = null;
            }
        }
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

    /**
     * Tries to build a specific robot type in any direction.
     */
    protected final boolean attemptBuild(RobotType robotType, int odds) throws GameActionException {
        // don't take all the resources yourself, but give other
        // archons/builders a chance to build; there's at most 4 archons, so
        // this is *slightly* more fair than first-come-first-serve
        if (Robot.rng.nextInt() % odds != 0) {
            return false;
        }

        for (Direction direction : Constants.cardinalDirections) {
            if (this.rc.canBuildRobot(robotType, direction)) {
                this.rc.buildRobot(robotType, direction);
                return true;
            }
        }

        return false;
    }
}
