package Trainwreck.bots;

import Trainwreck.util.*;
import battlecode.common.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static Trainwreck.util.Helper.isCombatUnit;

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
         * Attack lowest HP enemy, if there are any in range. Preferring combat units.
         */
        MapLocation toAttack = null;
        if (attackableEnemies.length > 0) {
            toAttack = attackableEnemies[0].location;
            // target soldiers, sages, and watchtowers over other units
            boolean foundCombatUnit = isCombatUnit(attackableEnemies[0].getType());
            int lowestHP = attackableEnemies[0].health;
            for (int i = 1; i < attackableEnemies.length; i++) {
                RobotInfo current = attackableEnemies[i];

                // once we find a combat unit, do not consider non-combat units anymore
                if (foundCombatUnit) {
                    // combat unit found before!
                    if (current.health < lowestHP && isCombatUnit(current.getType())) {
                        toAttack = current.location;
                        lowestHP = current.health;
                    }
                } else {
                    // only non-combat unit(s) found before!
                    // Add it regardless of HP if it is a combat unit!
                    if (isCombatUnit(current.getType())) {
                        toAttack = current.location;
                        lowestHP = current.health;
                        foundCombatUnit = true;
                    }

                    // No combat units! We can consider non-combat units!
                    if (current.health < lowestHP) {
                        toAttack = current.location;
                        lowestHP = current.health;
                    }
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
        Pathfinding pathfinder = new AStarPathfinding();
        dir = pathfinder.getDirection(myLocation, new MapLocation(myLocation.x + 2, myLocation.y - 2), rc);
//        if (nearbyEnemies.length > 0) {
//            // move towards an enemy.
//            Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
//            if (toAttack != null) { // prefer the enemy we are attacking currently
//                dir = pathfinder.getDirection(myLocation, toAttack, rc);
//            } else {
//                dir = pathfinder.getDirection(myLocation, nearbyEnemies[0].location, rc);
//            }
//        } else {
//            /*
//             * If we have a target location, travel towards it
//             */
//            if (targetArchonLocation != null) {
//                Pathfinding pathfinder = new WeightedRandomDirectionBasedPathfinding();
//                dir = pathfinder.getDirection(myLocation, targetArchonLocation, rc);
//            } else {
//                // move semi randomly
//                Pathfinding pathfinder = new RandomPreferLessRubblePathfinding();
//                dir = pathfinder.getDirection(myLocation, myLocation, rc);
//            }
//        }

//        rc.setIndicatorString("cur: " + targetArchonLocation + " close " + comms.getLocationClosestEnemyArchon());

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
         * Update target. If there are known enemy archons, set closest as target.
         */
        MapLocation closestEnemyArchon = comms.getLocationClosestEnemyArchon();
        if (closestEnemyArchon != null) {
            // there is a known enemy archon location!
            targetArchonLocation = closestEnemyArchon;
            return; // prevent looking further
        }

        /*
         * Set suspected location which have an enemy archon as target, if we do not have on yet
         */
        targetArchonLocation = comms.getClosestPotentialEnemyArchonLocation();

    }
}

