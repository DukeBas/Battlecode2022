package Trainwreck.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;


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
 * <p>
 * <p>
 * We use the first 6 bits for x, next 6 for y, and last 4 for extra information for encoding locations.
 */
public class FirstCommunication implements Communication {
    final int INDEX_START_FRIENDLY_ARCHON = 8; // 8,9,10,11
    final int INDEX_START_ENEMY_ARCHON = 4; // 8,9,10,11

    final int NUMBER_MAX_ARCHONS = 4;

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


    // DO NOT USE: FOR TESTING ONLY
    public FirstCommunication(RobotController rc) {
        this.rc = rc;

        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();

        System.out.println("WRONG CONSTRUCTOR USED");
    }

    public FirstCommunication() {
        mapHeight = 1;
        mapWidth = 1;
        rc = null;
    }

    @Override
    public boolean locationIsValid(MapLocation loc) {
        return loc.x >= 0 && loc.y >= 0 && loc.x < mapWidth && loc.y < mapHeight;
    }

    @Override
    public MapLocation[] getLocationsFriendlyArchons() throws GameActionException {
        return getMapLocationsArchons(INDEX_START_FRIENDLY_ARCHON);
    }

    @Override
    public MapLocation[] getLocationsEnemyArchons() throws GameActionException {
        return getMapLocationsArchons(INDEX_START_ENEMY_ARCHON);
    }

    private MapLocation[] getMapLocationsArchons(int index_start_archons) throws GameActionException {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = index_start_archons; i < i + NUMBER_MAX_ARCHONS; i++) {
            if (rc.readSharedArray(i) != 0) {
                indices.add(i);
            }
        }

        MapLocation[] output = new MapLocation[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            output[i] = locationDecoder(indices.get(i));
        }

        return output;
    }

    @Override
    public MapLocation getLocationsClosestFriendlyArchon(MapLocation loc) throws GameActionException {
        return getClosestArchon(loc, INDEX_START_FRIENDLY_ARCHON);
    }

    private MapLocation getClosestArchon(MapLocation loc, int index_start_archons) throws GameActionException {
        MapLocation[] archonLocations = getMapLocationsArchons(index_start_archons);
        MapLocation closestArchon = archonLocations[0];
        int closestDistance = loc.distanceSquaredTo(closestArchon);
        for (int i = 1; i < archonLocations.length; i++) {
            int dist = loc.distanceSquaredTo(closestArchon);
            if (dist < closestDistance) {
                closestArchon = archonLocations[i];
                closestDistance = dist;
            }
        }

        return closestArchon;
    }

    @Override
    public MapLocation getLocationsClosestEnemyArchon(MapLocation loc) throws GameActionException {
        return getClosestArchon(loc, INDEX_START_ENEMY_ARCHON);
    }


    private MapLocation getLocationArchonWithIDAtIndex(int index_start_archons, int RobotID) throws GameActionException {
        for (int i = index_start_archons; i < index_start_archons + NUMBER_MAX_ARCHONS; i++) {
            if (locationExtraDecoder(rc.readSharedArray(i)) == encodeID(RobotID)) {
                return locationDecoder(rc.readSharedArray(i));
            }
        }
        return null;
    }

    @Override
    public MapLocation getLocationFriendlyArchonWithID(int RobotID) throws GameActionException {
        return getLocationArchonWithIDAtIndex(INDEX_START_FRIENDLY_ARCHON, RobotID);
    }

    @Override
    public MapLocation getLocationEnemyArchonWithID(int RobotID) throws GameActionException {
        return getLocationArchonWithIDAtIndex(INDEX_START_ENEMY_ARCHON, RobotID);
    }

    @Override
    public void invalidateLocationEnemyArchon(int RobotID) throws GameActionException {
        for (int i = INDEX_START_ENEMY_ARCHON; i < INDEX_START_ENEMY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (locationExtraDecoder(rc.readSharedArray(i)) == encodeID(RobotID)) {
                rc.writeSharedArray(i, encodeID(RobotID));
                break;
            }
        }
    }

    @Override
    public void invalidateLocationEnemyArchon(MapLocation loc) throws GameActionException {
        for (int i = INDEX_START_ENEMY_ARCHON; i < INDEX_START_ENEMY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (loc.equals(locationDecoder(rc.readSharedArray(i)))) {
                rc.writeSharedArray(i, locationExtraDecoder(rc.readSharedArray(i)));
                break;
            }
        }
    }

    @Override
    public void updateLocationFriendlyArchon(int RobotID, MapLocation loc) throws GameActionException {
        for (int i = INDEX_START_FRIENDLY_ARCHON; i < INDEX_START_FRIENDLY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (locationExtraDecoder(rc.readSharedArray(i)) == encodeID(RobotID)) {
                rc.writeSharedArray(i, locationEncoder(loc, RobotID));
            }
        }
    }

    @Override
    public void updateLocationEnemyArchon(int RobotID, MapLocation loc) throws GameActionException {
        for (int i = INDEX_START_ENEMY_ARCHON; i < INDEX_START_ENEMY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (locationExtraDecoder(rc.readSharedArray(i)) == encodeID(RobotID)) {
                rc.writeSharedArray(i, locationEncoder(loc, RobotID));
            }
        }
    }

    @Override
    public void increaseUnitCounter(int ArchonID, RobotType type) throws GameActionException {
        //TODO/
    }

    @Override
    public void addFriendlyArchon(int RobotID, MapLocation loc) throws GameActionException {
        /*
         * Read from the start of the range until a spot is found (max 3 filled slots before, not checked).
         */
        for (int i = INDEX_START_FRIENDLY_ARCHON; true; i++) { // looks really weird
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, locationEncoder(loc, encodeID(RobotID)));
                break;
            }

        }
    }

    @Override
    public void addPotentialEnemyArchonLocation(MapLocation loc) {
        //TODO
    }

    @Override
    public int locationEncoder(MapLocation loc) {
        return locationEncoder(loc, 0);
    }

    /**
     * Encodes a location.
     *
     * @param loc   MapLocation to encode.
     * @param extra information to include, between 0 and 15 (inclusive).
     */
    @Override
    public int locationEncoder(MapLocation loc, int extra) {
        // Encode extra information
        int output = extra;

        // Encode x of location
        output += 1024 * loc.x;

        // Encode y of location
        output += 16 * loc.y;

        return output;
    }

    @Override
    public MapLocation locationDecoder(int input) {
        return new MapLocation((input & 0b1111110000000000) / 1024, (input & 0b0000001111110000) / 16);
    }

    @Override
    public int locationExtraDecoder(int input) {
        return input & 0b0000000000001111;
    }

    private int encodeID(int RobotID) {
        return RobotID % 15 + 1; //TODO: better mapping than modulo, use a space in the array
    }
}
