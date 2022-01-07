package Trainwreck.bots;

import Trainwreck.util.Constants;
import battlecode.common.*;


public class Archon extends Robot {

    public Archon(RobotController rc) {
        super(rc);

        // code specific to this robot's initialisation
        // ...
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        // execture communication strategy
        communicationStrategy();

        //Pick a direction to build in.
        Direction dir = Constants.directions[rng.nextInt(Constants.directions.length)];
        if (rng.nextBoolean()) {
            // Let's try to build a miner.
//            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            // Let's try to build a soldier.
//            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }

    /**
     * Communications strategy used by the archons.
     */
    private void communicationStrategy() throws GameActionException {
        rc.setIndicatorString(turnCount + "");
        if (turnCount == 1) {
            commsFirstRound();
        } else if (turnCount == 2) {
            commsSecondRound();
        }
    }

    /**
     * First round add self to known friendly archons list.
     */
    private void commsFirstRound() throws GameActionException {
        comms.addFriendlyArchon();
    }

    /**
     * Add possible enemy bases based on symmetries and other friendly bases.
     */
    private void commsSecondRound() throws GameActionException {
        rc.setIndicatorString("start second");
        comms.addPotentialEnemyArchonLocation(new MapLocation(7, 9));
        comms.addPotentialEnemyArchonLocation(new MapLocation(1, 1));
        comms.addPotentialEnemyArchonLocation(new MapLocation(1, 1));
        MapLocation close = comms.getClosestPotentialEnemyArchonLocation();
        rc.setIndicatorString(close.toString() + " and there were " + comms.getNumberPotentialEnemyArchonLocations());

        comms.invalidateLocationEnemyArchon(new MapLocation(1, 1));
        MapLocation close2 = comms.getClosestPotentialEnemyArchonLocation();

//        rc.setIndicatorString("2: " + close2.toString() + " and there were " + comms.getNumberPotentialEnemyArchonLocations());

        rc.setIndicatorString("TESTEST");

        comms.addEnemyArchon(new MapLocation(7, 9), 5);

//        MapLocation close3 = comms.getClosestPotentialEnemyArchonLocation();
//        rc.setIndicatorString("3: " + close3.toString() + " and there were " + comms.getNumberPotentialEnemyArchonLocations());
        rc.setIndicatorString("Closest enemy base is " + comms.getLocationClosestEnemyArchon() + " and there are " + comms.getNumberPotentialEnemyArchonLocations());

        //TODO add potential enemy bases based on symmetries
    }
}
