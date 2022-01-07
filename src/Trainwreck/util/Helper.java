package Trainwreck.util;

import battlecode.common.RobotType;

public class Helper {
    /**
     * Returns whether a robot type is considered a combat unit (Sage, Soldier, Watchtower)
     */
    public static boolean isCombatUnit(RobotType type) {
        return type == RobotType.SAGE
                || type == RobotType.SOLDIER
                || type == RobotType.WATCHTOWER;
    }
}
