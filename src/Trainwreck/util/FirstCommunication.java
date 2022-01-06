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
 * | Mapping?                           | Status booleans first 8 bits,      | .                                  | .                                  |
 * |                                    | last 8 bits for counting no.       |                                    |                                    |
 * |                                    | potential enemy archon locations   |                                    |                                    |
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
 * | Archon1 Counter (Soldier+Builder)  | Archon2 Counter (Soldier+Builder)  | Archon3 Counter (Soldier+Builder)  | Archon4 Counter (Soldier+Builder)  |
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
    final int INDEX_START_ENEMY_ARCHON = 4; // 4,5,6,7
    final int INDEX_START_COUNTERS = 12; // uses 2 per archon, so 12,13,14,15,16,17,18,19
    final int INDEX_STATUS_BOOLS_ENEMY_COUNTER = 1;
    final int INDEX_START_POTENTIAL_ENEMY_ARRAY = 20;

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
        return getMapLocationsArchons(INDEX_START_FRIENDLY_ARCHON, NUMBER_MAX_ARCHONS);
    }

    @Override
    public MapLocation[] getLocationsEnemyArchons() throws GameActionException {
        return getMapLocationsArchons(INDEX_START_ENEMY_ARCHON, NUMBER_MAX_ARCHONS);
    }

    private MapLocation[] getMapLocationsArchons(int index_start_archons, int length) throws GameActionException {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = index_start_archons; i < index_start_archons + length; i++) {
            if (rc.readSharedArray(i) != 0) {
                indices.add(i);
            }
        }

        MapLocation[] output = new MapLocation[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            output[i] = locationDecoder(rc.readSharedArray(indices.get(i)));
        }

        return output;
    }

    @Override
    public MapLocation getLocationsClosestFriendlyArchon(MapLocation loc) throws GameActionException {
        return getClosestArchon(loc, INDEX_START_FRIENDLY_ARCHON);
    }

    private MapLocation getClosestArchon(MapLocation loc, int index_start_archons) throws GameActionException {
        MapLocation[] archonLocations = getMapLocationsArchons(index_start_archons, NUMBER_MAX_ARCHONS);
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
                return;
            }
        }
    }

    @Override
    public void invalidateLocationEnemyArchon(MapLocation loc) throws GameActionException {
        // check enemy archon locations
        for (int i = INDEX_START_ENEMY_ARCHON; i < INDEX_START_ENEMY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (loc.equals(locationDecoder(rc.readSharedArray(i)))) {
                rc.writeSharedArray(i, locationExtraDecoder(rc.readSharedArray(i)));
                return;
            }
        }

        /*
         * check list of potential enemy archon locations, if not found before.
         * If it is found, remove it from potential list, and if there are more move last item to the gap in the array.
         */
        int potentialEnemyCounter = getPotentialEnemyArchonCounter();
        for (int i = INDEX_START_POTENTIAL_ENEMY_ARRAY; i < potentialEnemyCounter; i++) {
            if (loc.equals(locationDecoder(rc.readSharedArray(i)))) {
                decreasePotentialEnemyArchonCounter();
                potentialEnemyCounter--;
                if (i < potentialEnemyCounter) { // check if we created a gap in array
                    // we created a gap, move the last one to the gap to have a filled array part
                    int indexLast = INDEX_START_POTENTIAL_ENEMY_ARRAY + potentialEnemyCounter + 1;
                    int valueToMove = rc.readSharedArray(indexLast);
                    rc.writeSharedArray(indexLast, 0); // clear it
                    rc.writeSharedArray(i, valueToMove); // move the value to the created gap to fill it
                    return;
                }
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
        /*
         * Find index in shared array of archon with request ID
         */
        int archonIndex = INDEX_START_FRIENDLY_ARCHON;
        for (int i = INDEX_START_FRIENDLY_ARCHON; i < INDEX_START_FRIENDLY_ARCHON + NUMBER_MAX_ARCHONS; i++) {
            if (locationExtraDecoder(rc.readSharedArray(i)) == encodeID(ArchonID)) {
                archonIndex = i;
            }
        }

        /*
         * Pre-calculate the appropriate indices to use.
         */
        int indexFirstSlot = INDEX_START_COUNTERS + 2 * (archonIndex - INDEX_START_FRIENDLY_ARCHON);
        int indexSecondSlot = indexFirstSlot + 1;

        /*
         * Add counter, depending on type of unit
         */
        int previous;
        switch (type) {
            case MINER:
                previous = rc.readSharedArray(indexFirstSlot);
                rc.writeSharedArray(indexFirstSlot, previous + 1);
                break;
            case SAGE:
                previous = rc.readSharedArray(indexFirstSlot);
                rc.writeSharedArray(indexFirstSlot, previous + 1024); // use left 8 bits
                break;
            case SOLDIER:
                previous = rc.readSharedArray(indexSecondSlot);
                rc.writeSharedArray(indexSecondSlot, previous + 1);
                break;
            case BUILDER:
                previous = rc.readSharedArray(indexSecondSlot);
                rc.writeSharedArray(indexSecondSlot, previous + 1024); // use left 8 bits
                break;
        }
    }

    @Override
    public void addFriendlyArchon() throws GameActionException {
        /*
         * Read from the start of the range until a spot is found (max 3 filled slots before, not checked).
         */
        for (int i = INDEX_START_FRIENDLY_ARCHON; true; i++) { // looks really weird
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, locationEncoder(rc.getLocation(), encodeID(rc.getID())));
               break;
            }
        }
    }

    @Override
    public void addPotentialEnemyArchonLocation(MapLocation loc) throws GameActionException {
        rc.writeSharedArray(INDEX_START_POTENTIAL_ENEMY_ARRAY + getPotentialEnemyArchonCounter(),
                locationEncoder(loc, 1));
        increasePotentialEnemyArchonCounter();
    }

    @Override
    public boolean getPotentialEnemyArchonLocationsLeft() throws GameActionException {
        return getPotentialEnemyArchonCounter() > 0;
    }

    @Override
    public MapLocation getClosestPotentialEnemyArchonLocation(MapLocation loc) throws GameActionException {
        if (getPotentialEnemyArchonCounter() == 0) return null; // no potential enemy locations

        MapLocation[] archonLocations = getMapLocationsArchons(INDEX_START_POTENTIAL_ENEMY_ARRAY,
                getPotentialEnemyArchonCounter());

        MapLocation closestPotentialArchon = archonLocations[0];

        int closestDistance = loc.distanceSquaredTo(closestPotentialArchon);
        for (int i = 1; i < archonLocations.length; ++i) { // ++i instead of i++ so IDE stops complaining ;)
            int dist = loc.distanceSquaredTo(closestPotentialArchon);
            if (dist < closestDistance) {
                closestPotentialArchon = archonLocations[i];
                closestDistance = dist;
            }
        }

        return closestPotentialArchon;
    }

    private int getPotentialEnemyArchonCounter() throws GameActionException {
        return rc.readSharedArray(INDEX_STATUS_BOOLS_ENEMY_COUNTER);
    }

    private void increasePotentialEnemyArchonCounter() throws GameActionException {
        rc.writeSharedArray(INDEX_STATUS_BOOLS_ENEMY_COUNTER,
                getPotentialEnemyArchonCounter() + 1);
    }

    private void decreasePotentialEnemyArchonCounter() throws GameActionException {
        rc.writeSharedArray(INDEX_STATUS_BOOLS_ENEMY_COUNTER,
                getPotentialEnemyArchonCounter() - 1);
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
