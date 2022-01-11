package Sprint1.util;

import battlecode.common.RobotType;

public class Helper {
    /**
     * Returns whether a robot type is considered a combat unit (Sage, Soldier, Watchtower).
     *
     * @param type RobotType of the unit
     */
    public static boolean isCombatUnit(RobotType type) {
        return type == RobotType.SAGE
                || type == RobotType.SOLDIER
                || type == RobotType.WATCHTOWER;
    }

    /**
     * Returns whether a robot type is considered a droid.
     *
     * @param type RobotType of the unit.
     */
    public static boolean isDroid(RobotType type) {
        return type == RobotType.MINER
                || type == RobotType.SAGE
                || type == RobotType.SOLDIER
                || type == RobotType.BUILDER;
    }
}
