package Sprint2.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Defines getDirection as getting a suitable spot nearby that does not go too much towards target location.
 * Specifically, goes to best unoccupied tile nearby that is not directly, or left/right of that, towards target dir.
 */
public class FightingGroundPathfinding implements Pathfinding {
    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc)
            throws GameActionException {

        Direction out = Direction.CENTER;
        Direction towardTarget = source.directionTo(target);

        int leastRubble = rc.senseRubble(source); // default to current position
        Direction toConsider = towardTarget.rotateLeft(); // 45 deg. away from target direction, 90 deg at start loop
        // rotate away from target direction until 270 deg
        for (int i = 0; i < 5; i++){
            toConsider = toConsider.rotateLeft();
            MapLocation loc = source.add(toConsider);

            if (rc.onTheMap(loc) && !rc.isLocationOccupied(loc)){ // location is valid and unoccupied!
                int rubble = rc.senseRubble(loc);
                if (rubble < leastRubble){ // direction is better!
                    out = toConsider;
                    leastRubble = rubble;
                }
            }
        }

//        rc.setIndicatorString(out + "");

        return out;
    }
}
