package Trainwreck.bots;

import Trainwreck.util.Constants;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


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
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }

    /**
     * Communications strategy used by
     */
    private void communicationStrategy(){
        // first round add own location to know friendly archons
    }

    /**
     * First round add self to known friendly archons list.
     */
    private void commsFirstRound(){

    }

    /**
     * Add possible enemy bases based on symmetries and other friendly bases.
     */
    private void commsSecondRound(){

    }
}
