package Trainwreck.bots;

import Trainwreck.util.Constants;
import Trainwreck.util.Pathfinding;
import Trainwreck.util.SpotNearArchonPathfinding;
import Trainwreck.util.Status;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Builder extends Robot {
    /**
     * At least for now, the builder is destined to build watchtowers and build
     * 1 lab when the builder gets priority and there is enough lead, until 0
     * is reached.
     */
    private int buildCount = 5;

    /**
     * What archon this robot belongs to, which is used in the resource
     * distribution to the builders and archons.
     */
    private int turnOrder = 0;

    public Builder(RobotController rc) throws GameActionException {
        super(rc);

        // upon creation, find nearest archon and set this builder's turn order
        // to the same as the archon's
        int order = 0;
        Team team = rc.getTeam();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo nearbyRobot : nearbyRobots) {
            if (nearbyRobot.team.equals(team) && nearbyRobot.type == RobotType.ARCHON) {
                order = comms.getArchonOrder(nearbyRobot.getID());
            }
        }
        this.turnOrder = order;
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    public void run() throws GameActionException {
        communicationStrategy();

        // build something if it should, in a grid pattern
        if (buildCount >= 0) {
            for (Direction dir : Constants.directions) {
                MapLocation loc = rc.getLocation().add(dir);
                if (loc.x % 2 == 0 && loc.y % 2 == 0) {
                    RobotType type = buildCount == 2 ? RobotType.LABORATORY : RobotType.WATCHTOWER;
                    if (checkBuildPriority() && rc.canBuildRobot(type, dir)) {
                        rc.buildRobot(type, dir);
                        buildCount--;
                    }
                }
            }
        }
        
        // repairs anything it can find
        boolean repairing = false;
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            MapLocation location = robot.getLocation();
            if (rc.canRepair(location) && robot.health < robot.type.health) {
                rc.repair(location);
                repairing = true;
            }
        }

        // move around the team's archons;
        // it's a good idea to stay still if the builder is repairing something
        if (!repairing) {
            MapLocation start = rc.getLocation();
            MapLocation end = comms.getLocationClosestFriendlyArchon()
                .translate(rng.nextInt(15) - 7, rng.nextInt(15) - 7);
            Pathfinding pathfinding = new SpotNearArchonPathfinding();
            Direction direction = pathfinding.getDirection(start, end, rc);
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }

    private boolean checkBuildPriority() throws GameActionException {
        return rc.getRoundNum() % comms.getNumberFriendlyArchons() == turnOrder &&
            comms.getState(Status.DEFENSIVE_BUILD_SIGNAL);
    }
}
