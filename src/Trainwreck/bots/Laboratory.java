package Trainwreck.bots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Laboratory extends Robot {
    /**
     * The gold advantage that we try to maintain in comparison with the enemy
     * team.
     */
    private static final int MIN_GOLD_AHEAD = 20;

    public Laboratory(RobotController rc) {
        super(rc);
    }

    /**
     * This code is run once per turn (assuming we do not go over bytecode limits.)
     *
     * @throws GameActionException if an illegal game action is performed.
     */
    @Override
    void run() throws GameActionException {
        int ownGold = rc.getTeamGoldAmount(friendly);
        int enemyGold = rc.getTeamGoldAmount(enemy);
        if (ownGold < enemyGold + MIN_GOLD_AHEAD && rc.canTransmute()) {
            rc.transmute();
        }
    }
}
