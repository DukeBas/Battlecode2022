package Trainwreck.bots;

import Trainwreck.util.Constants;
import battlecode.common.*;

import java.util.Collections;

public class Soldier extends Robot {

    public Soldier(RobotController rc) {
        super(rc);
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        // Get the enemy team
        Team opponent = rc.getTeam().opponent();

        /*
         * Get nearby enemies.
         */
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(visionRadiusSquared, opponent);
        RobotInfo[] attackableEnemies = rc.senseNearbyRobots(actionRadiusSquared, opponent);

        /*
         * Attack lowest HP enemy, if there are any in range.
         */
        if (attackableEnemies.length > 0) {
            MapLocation toAttack = attackableEnemies[0].location;
            int lowestHP = attackableEnemies[0].health;
            for (int i = 1; i < attackableEnemies.length; i++){
                RobotInfo current = attackableEnemies[i];
                if (current.health < lowestHP){
                    toAttack = current.location;
                    lowestHP = current.health;
                }
            }

            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }


        // Also try to move randomly.
        Direction dir = Constants.directions[rng.nextInt(Constants.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }
}
