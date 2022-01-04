package Trainwreck;

import Trainwreck.bots.*;
import battlecode.common.*;

import java.util.Random;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Depending on the type of the bot, create a robot of that type
        switch (rc.getType()) {
            case ARCHON:
                new Archon(rc);
                break;
            case BUILDER:
                new Builder(rc);
                break;
            case LABORATORY:
                new Laboratory(rc);
                break;
            case MINER:
                new Miner(rc);
                break;
            case SAGE:
                new Sage(rc);
                break;
            case SOLDIER:
                new Soldier(rc);
                break;
            case WATCHTOWER:
                new Watchtower(rc);
                break;
        }
    }
}
