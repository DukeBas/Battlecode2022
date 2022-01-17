package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.HashSet;


public class AStarPathfinding implements Pathfinding {

    public ArrayList<MapLocation> retrace_steps(AStarNode start, AStarNode end) {
        AStarNode currentNode = end;
        ArrayList<MapLocation> path = new ArrayList<MapLocation>();
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

        AStarOpen open = new AStarOpen(5*VisionRange); //Actually 4*Range + 4*sqrt(Range)+1
        HashSet<MapLocation> closed = new HashSet<>(300);
        // TODO overwrite hash function

        int distance = Math.max(Math.abs(target.x - source.x), Math.abs(target.y - source.y));
        AStarNode startNode = new AStarNode(null, source, 0, distance, distance);
        AStarNode endNode = new AStarNode(null, target, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        AStarNode childNode;
        AStarNode openNode;
        open.insert(startNode); // add start node to open lis

        while (!open.isEmpty()) { //Check if list is empty
            AStarNode currentNode = open.popBest(); //Get the node with the lowest f-cost, maybe other data structure
            closed.add(currentNode.place);
            if (currentNode.place == endNode.place) {
                return retrace_steps(startNode, currentNode);
            }

            for (int i = currentNode.place.x - 1; i < currentNode.place.x + 2; i++) { //Loop over all Adjacent nodes
                for (int j = currentNode.place.y - 1; j < currentNode.place.y + 2; j++) { //Loop over all Adjacent nodes
                    MapLocation childPlace = new MapLocation(i, j);

                    if ((childPlace.isWithinDistanceSquared(target, VisionRange)) && (!closed.contains(childPlace))) { // Should only check pos
                        distance = Math.max(Math.abs(target.x - childPlace.x), Math.abs(target.y - childPlace.y));
                        childNode = new AStarNode(currentNode, childPlace, currentNode.GCost + 1, distance, 0); //rc.senseRubble(currentNode.place)
                        childNode.FCost = childNode.HCost + childNode.GCost;

                        if (open.isOpen(childNode)) {
                            openNode = open.getNodeAtLoc(childPlace.x, childPlace.y);
                            if (childNode.compareToG(openNode) < 0) {
                                open.updateNode(openNode, childNode.GCost, childNode.FCost, childNode.previous);
                            }
                        }
                        else {
                            open.insert(childNode);
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

        try {
            ArrayList<MapLocation> path = findPath(source, target, rc);
            return source.directionTo(path.get(path.size() - 1));
        } catch (Exception e) {
            rc.setIndicatorString("" + e);
        }

        return Direction.CENTER;
    }
}

