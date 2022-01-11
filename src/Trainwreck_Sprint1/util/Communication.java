package Trainwreck_Sprint1.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public interface Communication {
    /**
     * Checks whether a location is on the map.
     *
     * @param loc location to be checked
     * @return boolean if it is on the map
     */
    boolean locationIsValid(MapLocation loc);

    /**
     * Sets a global boolean variable.
     *
     * @param s    state to change
     * @param bool boolean vlaue to assign to the status
     */
    void setState(Status s, boolean bool) throws GameActionException;

    /**
     * Gets a global boolean variable. Defaults to false if status type is not known.
     *
     * @param s state to request
     * @return boolean value of that state.
     */
    boolean getState(Status s) throws GameActionException;

    /**
     * Returns at which place the archon with given ID gets to do it's turn, relative to the other archons.
     * At least 0, at most 3. Returns -1 if archonID is unknown.
     *
     * @param ArchonID RobotID of archon in question
     * @return at what place in the order they go
     */
    int getArchonOrder(int ArchonID) throws GameActionException;

    /**
     * Gets all the locations of friendly archons on the map.
     *
     * @return locations of friendly archons
     */
    MapLocation[] getLocationsFriendlyArchons() throws GameActionException;

    /**
     * Gets the numbers of friendly archons (that were alive at the start of the match).
     *
     * @return number of friendly archons.
     */
    int getNumberFriendlyArchons() throws GameActionException;

    /**
     * Gets all the locations of enemy archons on the map.
     * NOTE: Can give invalid locations indicating true location is unknown
     *
     * @return locations of enemy archons
     */
    MapLocation[] getLocationsEnemyArchons() throws GameActionException;

    /**
     * Gets the location of the closest friendly archon.
     *
     * @return location of nearest friendly archon
     */
    MapLocation getLocationClosestFriendlyArchon() throws GameActionException;

    /**
     * Gets the locations of the closest enemy archon, returns Null if none are currently known.
     *
     * @return location of nearest enemy archon
     */
    MapLocation getLocationClosestEnemyArchon() throws GameActionException;

    /**
     * Gives location of friendly archon with a specified robot ID.
     * Returns a location outside of the battlefield if unknown/invalid ID.
     *
     * @param RobotID of friendly archon
     * @return location of friendly archon with given ID
     */
    MapLocation getLocationFriendlyArchonWithID(int RobotID) throws GameActionException;

    /**
     * Gives last known location of enemy archon with ID.
     * Returns a location outside of the battlefield if unknown/invalid ID, or if it is currently unknown.
     *
     * @param RobotID of enemy archon
     * @return location of enemy archon with given ID
     */
    MapLocation getLocationEnemyArchonWithID(int RobotID) throws GameActionException;

    /**
     * Invalidates a location of an enemy archon based on the ID of the archon.
     *
     * @param RobotID ID of the archon
     */
    void invalidateLocationEnemyArchon(int RobotID) throws GameActionException;

    /**
     * Invalidates a location of an enemy archon based on the MapLocation.
     * Also invalidates potential enemy archon locations.
     *
     * @param loc MapLocation of the archon
     */
    void invalidateLocationEnemyArchon(MapLocation loc) throws GameActionException;

    /**
     * Updates known location of friendly archon.
     *
     * @param RobotID of the friendly archon
     * @param loc     MapLocation of the archon
     */
    void updateLocationFriendlyArchon(int RobotID, MapLocation loc) throws GameActionException;

//    /**
//     * Updates known location of enemy archon.
//     *
//     * @param RobotID of the enemy archon
//     * @param loc     MapLocation of the archon
//     */
//    void updateLocationEnemyArchon(int RobotID, MapLocation loc) throws GameActionException;

    /**
     * Increases counter of certain unit type (per archon counter)
     *
     * @param ArchonID ID of the archon which created the unit
     * @param type     of the unit
     */
    void increaseUnitCounter(int ArchonID, RobotType type) throws GameActionException;

    /**
     * Gets current unit count of specified archon and created type.
     *
     * @param ArchonID ID of the archon which created the unit
     * @param type     of the unit
     * @return state of counter
     */
    int getUnitCounter(int ArchonID, RobotType type) throws GameActionException;

    /**
     * Resets the unit counter for a particular archon and unit type.
     *
     * @param ArchonID ID of the archon which created the unit
     * @param type     RobotType of the particular unit type
     */
    void resetUnitCounter(int ArchonID, RobotType type) throws GameActionException;

    /**
     * Resets all the unit counters for a particular archon.
     *
     * @param ArchonID ID of the archon which created the unit
     */
    void resetAllUnitCounters(int ArchonID) throws GameActionException;

    /**
     * Add a friendly archon to the known list.
     */
    void addFriendlyArchon() throws GameActionException;

    /**
     * Add an enemy archon to the known list. Updates location if it is known already.
     *
     * @param loc location of the enemy archon.
     */
    void addEnemyArchon(MapLocation loc, int RobotID) throws GameActionException;

    /**
     * Add a location of interest for a potential enemy archon location.
     * Most likely used at the start of the match with map symmetry.
     *
     * @param loc MapLocation of location of interest.
     */
    void addPotentialEnemyArchonLocation(MapLocation loc) throws GameActionException;

    /**
     * Checks if there are still potential enemy archon locations.
     *
     * @return whether there are still suspected enemy archon locations.
     */
    boolean getPotentialEnemyArchonLocationsLeft() throws GameActionException;

    /**
     * Gives the closest location that might have an enemy archon.
     * Returns null if there is none.
     *
     * @return location of closest suspected enemy archon, if there is one
     */
    MapLocation getClosestPotentialEnemyArchonLocation() throws GameActionException;

    /**
     * Encodes a location for use in the shared array.
     *
     * @param loc MapLocation to encode.
     */
    int locationEncoder(MapLocation loc);

    /**
     * Encodes a location and extra data for use in the shared array.
     *
     * @param loc   MapLocation to encode.
     * @param extra information to include.
     */
    int locationEncoder(MapLocation loc, int extra);

    /**
     * Turns a location from the shared array into a MapLocation object.
     *
     * @param input from shared array to consider.
     * @return location.
     */
    MapLocation locationDecoder(int input);

    /**
     * Extracts the extra information stored next to a location in the shared array.
     *
     * @param input from shared array to consider.
     * @return extra information
     */
    int locationExtraDecoder(int input);

    /**
     * Gives the number of suspected enemy archon locations.
     *
     * @return number of suspected enemy archon location
     */
    int getNumberPotentialEnemyArchonLocations() throws GameActionException;

    /**
     * Gives all suspected enemy archon locations.
     *
     * @return all currently suspected enemy archon locations
     */
    MapLocation[] getLocationsPotentialEnemyArchons() throws GameActionException;

    /**
     * Scans for enemy robots, marks archons. Not efficient if already looking for enemy bots before.
     */
    void checkForEnemyArchons() throws GameActionException;

    //TODO add protocols for when a friendly archon gets destroyed
}
