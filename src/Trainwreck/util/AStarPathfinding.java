package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.HashSet;


public class AStarPathfinding implements Pathfinding {
    private int mapWidth = 0;
    private int mapHeight = 0;


    private ArrayList<MapLocation> retrace_steps(AStarNode start, AStarNode end) {
        AStarNode currentNode = end;
        ArrayList<MapLocation> path = new ArrayList<>();
        while (currentNode != start) {
            path.add(currentNode.place);
            currentNode = currentNode.previous;
        }
        throw new RuntimeException("RETRACED");
//        return path;
    }

    private ArrayList<MapLocation> findPath(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        int VisionRange = rc.getType().visionRadiusSquared;
        //MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), VisionRange); // maybe don't do this? Since we have to look for locations in the array as well now, maybe turn it into a map?
//        if (!source.isWithinDistanceSquared(target, VisionRange)) {
//            // Change target if distance is larger than 20. How do we want to change the target, maybe checkpoints?
//        }
        try {
            AStarOpen open = new AStarOpen(5 * VisionRange); //Actually 4*Range + 4*sqrt(Range)+1
            HashSet<MapLocation> closed = new HashSet<>(30);
//        // TODO overwrite hash function
////        AStarClosed closed = new AStarClosed();
//
            int distance = Math.max(Math.abs(target.x - source.x), Math.abs(target.y - source.y));
            AStarNode startNode = new AStarNode(null, source, 0, distance, distance);
            AStarNode endNode = new AStarNode(null, target, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

            AStarNode openNode;
            open.insert(startNode); // add start node to open lis

            int t = 0;
            while (!open.isEmpty()) { //Check if list is empty
                t++;
                if (t > 50){
                    throw new RuntimeException(open.printableFCosts());
                }
                AStarNode currentNode = open.popBest();
                if (currentNode == null){
                    throw new RuntimeException("current is null");
                }
                closed.add(currentNode.place);
                if ((currentNode.place.x == endNode.place.x) && (currentNode.place.y == endNode.place.y)) {
                    return retrace_steps(startNode, currentNode);
                }

                for (int i = currentNode.place.x - 1; i < currentNode.place.x + 2; i++) { //Loop over all Adjacent nodes
                    for (int j = currentNode.place.y - 1; j < currentNode.place.y + 2; j++) { //Loop over all Adjacent nodes
                        MapLocation childPlace = new MapLocation(i, j);

                        if (childPlace.isWithinDistanceSquared(target, VisionRange) && locationOnMap(childPlace) &&
                                (!closed.contains(childPlace))) {
                            distance = Math.max(Math.abs(target.x - childPlace.x), Math.abs(target.y - childPlace.y));

                            // get child node if it exists in open already, else add a new one
                            AStarNode childNode;
                            childNode = new AStarNode(currentNode, childPlace, currentNode.GCost + 1, distance,
                                    currentNode.GCost + 1 + distance); //rc.senseRubble(currentNode.place)

                            if (open.isOpen(childNode)) {
                                openNode = open.getNodeAtLoc(childNode);
                                if (childNode.compareToG(openNode) < 0) {
                                    open.updateNode(openNode, childNode.GCost, childNode.FCost, childNode.previous);
                                }
                            } else {
                                open.insert(childNode);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Outer: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc)
            throws GameActionException {
//        if (true) {
//            // Change target if distance is larger than 20. How do we want to change the target
//        }

        // set map width and height
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        try {
            ArrayList<MapLocation> path = findPath(source, target, rc);

            if (path == null) {
                throw new Exception("Path is null!");
            }

            return source.directionTo(path.get(path.size() - 1));
        } catch (Exception e) {
            rc.setIndicatorString("Top level: " + e.getMessage());
        }

        return Direction.CENTER;
    }

    private boolean locationOnMap(MapLocation loc) {
        return loc.x >= 0 && loc.x < mapWidth && loc.y >= 0 && loc.y < mapHeight;
    }
}

