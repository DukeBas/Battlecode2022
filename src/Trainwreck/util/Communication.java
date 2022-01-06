package Trainwreck.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public interface Communication {
    /**
     * Checks whether a location is on the map.
     * @param loc location to be checked.
     * @return boolean if it is on the map.
     */
    boolean locationIsValid(MapLocation loc);

    /**
     * Gets all the locations of friendly archons on the map.
     * @return locations of friendly archons.
     */
    MapLocation[] getLocationsFriendlyArchons();

    /**
     * Gets all the locations of enemy archons on the map.
     * @return locations of enemy archons.
     */
    MapLocation[] getLocationsEnemyArchons();

    MapLocation getLocationsClosestFriendlyArchon();
    MapLocation getLocationsClosestEnemyArchon();

    MapLocation getLocationFriendlyArchonWithID(int RobotID);
    MapLocation getLocationEnemyArchonWithID(int RobotID);

    void invalidateLocationEnemyArchon(int RobotID);
    void invalidateLocationEnemyArchon(MapLocation loc);

    void updateLocationFriendlyArchon(int RobotID, MapLocation loc);
    void updateLocationEnemyArchon(int RobotID, MapLocation loc);

    void increaseUnitCounter(int ArchonID, RobotType type);

    void addPotentialEnemyArchonLocation(MapLocation loc);
}
