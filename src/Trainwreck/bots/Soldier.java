package Trainwreck.bots;

import Trainwreck.util.Constants;
import Trainwreck.util.DirectionBasedPathfinding;
import Trainwreck.util.Pathfinding;
import Trainwreck.util.RandomPreferLessRubblePathfinding;
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

        MapLocation myLocation = rc.getLocation();

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
            for (int i = 1; i < attackableEnemies.length; i++) {
                RobotInfo current = attackableEnemies[i];
                if (current.health < lowestHP) {
                    toAttack = current.location;
                    lowestHP = current.health;
                }
            }

            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }


        /*
         * Move to enemy if one is in vision range. Preferring target of attack.
         */
        Direction dir;
        if (nearbyEnemies.length > 0) {
            // move towards an enemy.
            Pathfinding pathfinder = new DirectionBasedPathfinding();
            if (attackableEnemies.length > 0) { // prefer the enemy we are attacking currently
                dir = pathfinder.getDirection(myLocation, attackableEnemies[0].location, rc);
            } else {
                dir = pathfinder.getDirection(myLocation, nearbyEnemies[0].location, rc);
            }
        } else {
            // move semi randomly
            Pathfinding pathfinder = new RandomPreferLessRubblePathfinding();
            dir = pathfinder.getDirection(myLocation, myLocation, rc);
        }

        /*
         * Move if it is possible.
         */
//        rc.setIndicatorString("Moving to " + dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
