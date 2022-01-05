package Trainwreck.bots;

import battlecode.common.*;

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
        int radius = rc.getType().actionRadiusSquared;
        int best_enemy = 0;
        int i = 0;
        int value_best_enemy = 0;
        int new_enemy_score = 0;
        //RobotInfo best_enemy;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            while (i < enemies.length) {
                new_enemy_score = 0;
                if (new_enemy_score > value_best_enemy){
                    best_enemy = i;
                }
                i++;
            }

            MapLocation toAttack = enemies[best_enemy].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }
}
