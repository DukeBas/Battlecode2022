package Trainwreck.util;

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

    /**
     * Directions array with center added to it.
     */
    public static final Direction[] directionsWCenter = Stream.concat(Arrays.stream(directions),
            Arrays.stream(new Direction[]{Direction.CENTER})).toArray(Direction[]::new);

}
