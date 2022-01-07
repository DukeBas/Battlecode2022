package Trainwreck.bots;

import Trainwreck.util.*;
import battlecode.common.*;

import java.util.Collections;
import java.util.Objects;

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
        MapLocation myLocation = rc.getLocation();

        /*
         * Communicate!
         */
        communicationStrategy();


        /*
         * Get nearby enemies.
         */
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(visionRadiusSquared, enemy);
        RobotInfo[] attackableEnemies = rc.senseNearbyRobots(actionRadiusSquared, enemy);


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
            Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
            if (attackableEnemies.length > 0) { // prefer the enemy we are attacking currently
                dir = pathfinder.getDirection(myLocation, attackableEnemies[0].location, rc);
            } else {
                dir = pathfinder.getDirection(myLocation, nearbyEnemies[0].location, rc);
            }
        } else {
            /*
             * If we have a target location, travel towards it
             */
            if (targetArchonLocation != null) {
                Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
                dir = pathfinder.getDirection(myLocation, targetArchonLocation, rc);
            } else {
                // move semi randomly
                Pathfinding pathfinder = new RandomPreferLessRubblePathfinding();
                dir = pathfinder.getDirection(myLocation, myLocation, rc);
            }
        }

        /*
         * Move if it is possible.
         */
//        rc.setIndicatorString("Moving to " + dir);
        if (!dir.equals(Direction.CENTER) && rc.canMove(dir)) {
            rc.move(dir);
        }
    }



    @Override
    void communicationStrategy() throws GameActionException {
        super.communicationStrategy(); // execute commands in super class

        /*
         * If we do not have a target yet, look for one in shared communications.
         */
        if (targetArchonLocation == null) {
            /*
             * If there are known enemy archons, set closest as target
             */
//            rc.setIndicatorString("BEFORE getLocationClosestEnemyArchon");
            MapLocation closestEnemyArchon = comms.getLocationClosestEnemyArchon();
            if (closestEnemyArchon != null) {
                // there is a known enemy archon location!
                targetArchonLocation = closestEnemyArchon;
                return; // prevent looking further
            }
//            rc.setIndicatorString("AFTER getLocationClosestEnemyArchon");

            /*
             * If there are suspected locations which have an enemy archon.
             */
            MapLocation closestPotentialEnemyArchon = comms.getClosestPotentialEnemyArchonLocation();
            if (closestPotentialEnemyArchon != null) {
                // there is a known enemy archon location!
                targetArchonLocation = closestPotentialEnemyArchon;
            }
        }
    }
}
