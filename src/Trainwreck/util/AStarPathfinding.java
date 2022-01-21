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


    public ArrayList<MapLocation> retrace_steps(AStarNode start, AStarNode end) {
        AStarNode currentNode = end;
        ArrayList<MapLocation> path = new ArrayList<>();
        while (currentNode != start) {
            path.add(currentNode.place);
            currentNode = currentNode.previous;
        }
        return path;
    }

    public ArrayList<MapLocation> findPath(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        int VisionRange = rc.getType().visionRadiusSquared;
        //MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), VisionRange); // maybe don't do this? Since we have to look for locations in the array as well now, maybe turn it into a map?
//        if (!source.isWithinDistanceSquared(target, VisionRange)) {
//            // Change target if distance is larger than 20. How do we want to change the target, maybe checkpoints?
//        }

        AStarOpen open = new AStarOpen(5 * VisionRange); //Actually 4*Range + 4*sqrt(Range)+1
        HashSet<MapLocation> closed = new HashSet<>(5*VisionRange);
        // TODO overwrite hash function

        int distance = Math.max(Math.abs(target.x - source.x), Math.abs(target.y - source.y));
        AStarNode startNode = new AStarNode(null, source, 0, distance, distance);
        AStarNode endNode = new AStarNode(null, target, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        AStarNode openNode;
        open.insert(startNode); // add start node to open lis


        while (!open.isEmpty()) { //Check if list is empty
            AStarNode currentNode = open.popBest();
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
                        childNode = new AStarNode(currentNode, childPlace, currentNode.GCost + 1, distance, 0); //rc.senseRubble(currentNode.place)
                        childNode.FCost = childNode.HCost + childNode.GCost;

                        try {
                            if (open.isOpen(childNode.place.x, childNode.place.y)) {
                                openNode = open.getNodeAtLoc(childPlace.x, childPlace.y);
                                if (childNode.compareToG(openNode) < 0) {
                                    open.updateNode(openNode, childNode.GCost, childNode.FCost, childNode.previous);
                                }
                            } else {
                                open.insert(childNode);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("INNER");
                        }


                    }
                }
            }
        }

        return null;
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
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
            rc.setIndicatorString("Top level: " + e);
        }

        return Direction.CENTER;
    }

    private boolean locationOnMap(MapLocation loc) {
        return loc.x >= 0 && loc.x < mapWidth && loc.y >= 0 && loc.y < mapHeight;
    }
}

