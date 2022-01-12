package Trainwreck.bots;

import Trainwreck.util.*;
import battlecode.common.*;

import static Trainwreck.util.Helper.isCombatUnit;

public class Soldier extends Robot {
    private final static int NUM_ENEMIES_MANY = 5; // when do we say there are many enemies nearby?
    private final static double ENEMY_COUNTING_FACTOR = 0.8; // when comparing friendly to enemy numbers
    private final static double FRIENDLY_OVERWHELMING_FACTOR = 3; // when are our forces overwhelming?

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
         * Get nearby robots.
         */
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(visionRadiusSquared, enemy);
        RobotInfo[] attackableEnemies = rc.senseNearbyRobots(actionRadiusSquared, enemy);
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(visionRadiusSquared, friendly);


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
         * Movement strategy:
         * First, either wait at base or move towards enemy archons depending on attack signal.
         *
         * When an enemy is spotted, stand still or move to nearby more favorable tiles.
         *      This is in the hope that more allies will reinforce us, so soldiers stick together.
         * If there are a lot more enemies in vision range than friendlies, retreat (unless near friendly archon).
         * If number of nearby friendlies is much greater than enemies in vision range, move closer to overwhelm.
         */
        Direction dir;
        Pathfinding pathfinder;
        rc.setIndicatorString("start");
        if (comms.getState(Status.ATTACK_SIGNAL)) { // go to attack!!!

            // get number of enemies and friendlies to decide what to do
            int numFriendlies = nearbyFriendlies.length;
            int numAttackableEnemies = attackableEnemies.length;
            int numEnemies = nearbyEnemies.length;

            if (numEnemies > 0) { // enemies withing vision range!

                if (numAttackableEnemies > 0) { // enemies in attackable range!

                    boolean archonNearby = myLocation.distanceSquaredTo(comms.getLocationClosestFriendlyArchon())
                            <= actionRadiusSquared;

                    /*
                     * If there are too many enemy units compared to the number of friendly units, retreat.
                     * But if there is a friendly archon nearby, do not do this! Repel the enemy or die trying!
                     */
                    if (!archonNearby && numAttackableEnemies > NUM_ENEMIES_MANY &&
                            numEnemies * ENEMY_COUNTING_FACTOR > numFriendlies) { // too many enemies nearby! Retreat!
                        rc.setIndicatorString("Retreating!");
                        pathfinder = new BestOppositePathfinding();
                        dir = pathfinder.getDirection(myLocation,
                                toAttack, // ideally we would run from closest enemy, but this is good enough
                                rc);
                    }

                    /*
                     * If there are comparable numbers of enemies and friendlies, try to stay
                     * out of range of too many enemies while preferring lighter tiles so we can attack more often.
                     */
                    else if (numFriendlies < numEnemies * FRIENDLY_OVERWHELMING_FACTOR) { // stand and fight!
                        rc.setIndicatorString("Standing to fight!");
                        // find a good nearby spot to fight from!
                        pathfinder = new FightingGroundPathfinding();
                        dir = pathfinder.getDirection(myLocation, toAttack, rc);

                    } else {
                        rc.setIndicatorString("Moving to overwhelm!");
                        /*
                         * We are in far greater numbers compared to the enemy, move in towards them to overwhelm!
                         */
                        pathfinder = new WeightedRandomDirectionBasedPathfinding();
                        // prefer the enemy we are attacking currently
                        dir = pathfinder.getDirection(myLocation, toAttack, rc);
                    }

                } else { // enemies in vision range, but none are attackable currently.
                    rc.setIndicatorString("Enemy not attackable, but in vision");
                    /*
                     * Move to closer favorable tile, if the number of enemy combatants is not too much
                     * compared to friendly forces, else retreat
                     */
                    pathfinder = new WeightedRandomDirectionBasedPathfinding();
                    dir = pathfinder.getDirection(myLocation, nearbyEnemies[0].location, rc);

                    // TODO... better than just travelling towards enemy!!
                }

            } else { // no enemies spotted!
                rc.setIndicatorString("No enemies spotted! Travelling towards " + targetArchonLocation);
                /*
                 * If we have a target location, travel towards it
                 */
                if (targetArchonLocation != null) {
                    pathfinder = new WeightedRandomDirectionBasedPathfinding();
                    dir = pathfinder.getDirection(myLocation, targetArchonLocation, rc);

                } else { // only happens if an enemy archon has moved, and we have not spotted it yet.
                    // move semi randomly
                    pathfinder = new RandomPreferLessRubblePathfinding();
                    dir = pathfinder.getDirection(myLocation, myLocation, rc);
                }
            }

        } else { // Wait for attack signal!
            rc.setIndicatorString("Waiting for attack signal!");
            pathfinder = new SpotNearArchonPathfinding();
            dir = pathfinder.getDirection(myLocation, comms.getLocationClosestFriendlyArchon(), rc);
        }


//        rc.setIndicatorString(comms.getState(Status.ATTACK_SIGNAL) + " " + dir);
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

