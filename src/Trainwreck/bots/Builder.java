package Trainwreck.bots;

import Trainwreck.util.Pathfinding;
import Trainwreck.util.RandomPreferLessRubblePathfinding;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Builder extends Robot {
    /**
     * At least for now, the builder is destined to build one thing and then
     * just repair things. This is so resources are not hogged by the builders
     * only, and there's only a limited number of them.
     */
    private boolean built = false;
    private final RobotType robotType;

    public Builder(RobotController rc) {
        super(rc);

        // robot selection algorithm should probably still be changed:
        switch (Robot.rng.nextInt() % 2) {
        case 0:
            this.robotType = RobotType.LABORATORY;
            break;

        case 1:
            this.robotType = RobotType.WATCHTOWER;
            break;

        default:
            throw new IllegalStateException("Random cannot be outside of [0, 2)");
        }
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    public void run() throws GameActionException {
        // first 50 rounds: moves a bit further away, then builds one thing
        if (!this.built && this.turnCount > 50) {
            if (this.attemptBuild(this.robotType, 1)) {
                this.built = true;
            }
        }
        
        // repairs anything it can find
        boolean repairing = false;
        RobotInfo[] robots = this.rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            MapLocation location = robot.getLocation();
            if (this.rc.canRepair(location) && robot.health < robot.type.health) {
                this.rc.repair(location);
                repairing = true;
            }
        }

        // it's a good idea to stay still if the builder is repairing something
        if (!repairing) {
            MapLocation loc = this.rc.getLocation();
            Pathfinding pathfinding = new RandomPreferLessRubblePathfinding();
            Direction direction = pathfinding.getDirection(loc, loc, this.rc); 
            if (this.rc.canMove(direction)) {
                this.rc.move(direction);
            }
        }
    }
}
