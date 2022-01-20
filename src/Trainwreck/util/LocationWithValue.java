package Trainwreck.util;

import battlecode.common.MapLocation;

/**
 * Record type to hold a location and integer value.
 */
public class LocationWithValue {
    final public MapLocation loc;
    final public int value;

    public LocationWithValue(final MapLocation loc, final int val){
        this.loc = loc;
        this.value = val;
    }
}
