package trainwreck.util;

import Trainwreck.bots.Archon;
import Trainwreck.util.FirstCommunication;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.world.GameWorld;
import battlecode.world.LiveMap;
import battlecode.world.RobotControllerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class FirstCommunicationTest {
    FirstCommunication c;

    @Before
    public void init() {
        c = new FirstCommunication();
    }

    void testEncodingDecoding(int x, int y) {
        MapLocation loc = new MapLocation(x, y);
        int toDecode = c.locationEncoder(loc);
        MapLocation decoded = c.locationDecoder(toDecode);
        assertEquals(loc, decoded);
    }

    @Test
    public void testSanity() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testEncodingDecodingBoundary1() {
        testEncodingDecoding(0, 0);
    }

    @Test
    public void testEncodingDecodingBoundary2() {
        testEncodingDecoding(60, 60);
    }

    @Test
    public void testEncodingDecoding1() {
        testEncodingDecoding(0, 10);
    }

    @Test
    public void testEncodingDecoding2() {
        testEncodingDecoding(10, 0);
    }

    @Test
    public void testEncodingDecoding3() {
        testEncodingDecoding(10, 10);
    }

    @Test
    public void testEncodingDecoding4() {
        testEncodingDecoding(25, 37);
    }

}
