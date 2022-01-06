package Trainwreck.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public interface Communication {
    /**
     * Checks whether a location is on the map.
     * @param loc location to be checked
     * @return boolean if it is on the map
     */
    boolean locationIsValid(MapLocation loc);

    /**
     * Gets all the locations of friendly archons on the map.
     * @return locations of friendly archons
     */
    MapLocation[] getLocationsFriendlyArchons();

    /**
     * Gets all the locations of enemy archons on the map.
     * @return locations of enemy archons
     */
    MapLocation[] getLocationsEnemyArchons();

    /**
     * Gets the locations of the closest friendly archon.
     * @return location of nearest friendly archon
     */
    MapLocation getLocationsClosestFriendlyArchon();

    /**
     * Gets the locations of the closest enemy archon.
     * @return location of nearest enemy archon
     */
    MapLocation getLocationsClosestEnemyArchon();

    /**
     * Gives location of friendly archon with a specified robot ID.
     * Returns a location outside of the battlefield if unknown/invalid ID.
     *
     * @param RobotID of friendly archon
     * @return location of friendly archon with given ID
     */
    MapLocation getLocationFriendlyArchonWithID(int RobotID);

    /**
     * Gives last known location of enemy archon with ID.
     * Returns a location outside of the battlefield if unknown/invalid ID, or if it is currently unknown.
     *
     * @param RobotID of enemy archon
     * @return location of enemy archon with given ID
     */
    MapLocation getLocationEnemyArchonWithID(int RobotID);

    /**
     * Invalidates a location of an enemy archon based on the ID of the archon.
     * @param RobotID ID of the archon
     */
    void invalidateLocationEnemyArchon(int RobotID);

    /**
     * Invalidates a location of an enemy archon based on the MapLocation.
     * @param loc MapLocation of the archon
     */
    void invalidateLocationEnemyArchon(MapLocation loc);

    /**
     * Updates known location of friendly archon.
     * @param RobotID of the friendly archon
     * @param loc MapLocation of the archon
     */
    void updateLocationFriendlyArchon(int RobotID, MapLocation loc);

    /**
     * Updates known location of enemy archon.
     * @param RobotID of the enemy archon
     * @param loc MapLocation of the archon
     */
    void updateLocationEnemyArchon(int RobotID, MapLocation loc);

    /**
     * Increases counter of certain unit type (per archon counter)
     * @param ArchonID ID of the archon which created the unit
     * @param type of the unit
     */
    void increaseUnitCounter(int ArchonID, RobotType type);

    /**
     * Add a location of interest for a potential enemy archon location.
     * Most likely used at the start of the match with map symmetry.
     * @param loc MapLocation of location of interest.
     */
    void addPotentialEnemyArchonLocation(MapLocation loc);
}
