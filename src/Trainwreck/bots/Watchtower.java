package Trainwreck.bots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Watchtower extends Robot {

    public Watchtower(RobotController rc) {
        super(rc);
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getTeam().equals(enemy)) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                    break;
                }
            }
        }
    }
}
