package Trainwreck_Sprint1.util;

import battlecode.common.Direction;

import java.util.Arrays;
import java.util.stream.Stream;

public class Constants {

    /**
     * Array containing all the possible movement directions.
     */
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

//    /**
//     * Directions array with center added to it.
//     */
//    public static final Direction[] directionsWCenter = Stream.concat(Arrays.stream(directions),
//            Arrays.stream(new Direction[]{Direction.CENTER})).toArray(Direction[]::new);
//
//    /**
//     * Minimum possible size of the sides of the map
//     */
//    int MINIMUM_MAP_SIDE_DIMENSION = 20; // so min 20x20
//
//    /**
//     * Maximum possible size of the sides of the map.
//     */
//    int MAXIMUM_MAP_SIDE_DIMENSION = 60; // so max 60x60
//
//    /**
//     * Highest number we can store in the shared memory.
//     */
//    int MAXIMUM_VALUE_COMMUNICATION = 65535; // 2^16 - 1
}
