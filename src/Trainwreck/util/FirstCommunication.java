package Trainwreck.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


/**
 * The following division of the shared memory is used
 * +────────────────────────────────────+────────────────────────────────────+────────────────────────────────────+────────────────────────────────────+
 * | Index 0                            | Index 1                            | Index 2                            | Index 3                            |
 * | Mapping?                           | Status booleans                    | .                                  | .                                  |
 * |                                    |                                    |                                    |                                    |
 * | Index 4                            | Index 5                            | Index 6                            | Index 7                            |
 * | EnemyArchon1 ID+Loc                | EnemyArchon2 ID+Loc                | EnemyArchon3 ID+Loc                | EnemyArchon4 ID+Loc                |
 * |                                    |                                    |                                    |                                    |
 * | Index 8                            | Index 9                            | Index 10                           | Index 11                           |
 * | FriendlyArchon1 ID+Loc             | FriendlyArchon2 ID+Loc             | FriendlyArchon3 ID+Loc             | FriendlyArchon4 ID+Loc             |
 * |                                    |                                    |                                    |                                    |
 * | Index 12                           | Index 13                           | Index 14                           | Index 15                           |
 * | Archon1 Counter (Miner+Sage)       | Archon2 Counter (Miner+Sage)       | Archon3  Counter (Miner+Sage)      | Archon4 Counter (Miner+Sage)       |
 * |                                    |                                    |                                    |                                    |
 * | Index 16                           | Index 17                           | Index 18                           | Index 19                           |
 * | Archon1 Counter (Soldier+Builder)  | Archon1 Counter (Soldier+Builder)  | Archon1 Counter (Soldier+Builder)  | Archon1 Counter (Soldier+Builder)  |
 * |                                    |                                    |                                    |                                    |
 * | 20                                 | 21                                 | 22                                 | 23                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 24                                 | 25                                 | 26                                 | 27                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 28                                 | 29                                 | 30                                 | 31                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 32                                 | 33                                 | 34                                 | 35                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 36                                 | 37                                 | 38                                 | 39                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 40                                 | 41                                 | 42                                 | 43                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 44                                 | 45                                 | 46                                 | 47                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 48                                 | 49                                 | 50                                 | 51                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 52                                 | 53                                 | 54                                 | 55                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 56                                 | 57                                 | 58                                 | 59                                 |
 * | ;                                  | ;                                  |                                    |                                    |
 * | 60                                 | 61                                 | 62                                 | 63                                 |
 * | ;                                  | ;                                  | ;                                  |                                    |
 * +────────────────────────────────────+────────────────────────────────────+────────────────────────────────────+────────────────────────────────────+
 */
public class FirstCommunication implements Communication {
    final int INDEX_START_FRIENDLY_ARCHON = 8; // 8,9,10,11

    /**
     * RobotController of the unit this object belongs to.
     * Our interface with interacting with the game.
     */
    final RobotController rc;

    /**
     * Dimensions of the map. Map ranges between 20x20 and 60x60.
     */
    final int mapWidth;
    final int mapHeight;


    public FirstCommunication(RobotController rc) {
        this.rc = rc;

        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();
    }

    @Override
    public boolean locationIsValid(MapLocation loc) {
        return loc.x >= 0 && loc.y >= 0 && loc.x < mapWidth && loc.y < mapHeight;
    }

    @Override
    public MapLocation[] getLocationsFriendlyArchons() {
        return new MapLocation[0];
    }

    @Override
    public MapLocation[] getLocationsEnemyArchons() {
        return new MapLocation[0];
    }

    @Override
    public MapLocation getLocationsClosestFriendlyArchon() {
        return null;
    }

    @Override
    public MapLocation getLocationsClosestEnemyArchon() {
        return null;
    }

    @Override
    public MapLocation getLocationFriendlyArchonWithID(int RobotID) {
        return null;
    }

    @Override
    public MapLocation getLocationEnemyArchonWithID(int RobotID) {
        return null;
    }

    @Override
    public void invalidateLocationEnemyArchon(int RobotID) {

    }

    @Override
    public void invalidateLocationEnemyArchon(MapLocation loc) {

    }

    @Override
    public void updateLocationFriendlyArchon(int RobotID, MapLocation loc) {

    }

    @Override
    public void updateLocationEnemyArchon(int RobotID, MapLocation loc) {

    }

    @Override
    public void increaseUnitCounter(int ArchonID, RobotType type) {

    }

    @Override
    public void addFriendlyArchon(int RobotID, MapLocation loc) throws GameActionException {
        /*
         * Read from the start of the range until a spot is found (max 3 filled slots before, not checked).
         */
        for (int i = INDEX_START_FRIENDLY_ARCHON; rc.readSharedArray(i) == 0; i++){
//            rc.writeSharedArray();
        }
    }

    @Override
    public void addPotentialEnemyArchonLocation(MapLocation loc) {

    }
}
