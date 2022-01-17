package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;


public class AStarPathfinding implements Pathfinding {

    public MapLocation[] retrace_steps(AStarNode start, AStarNode end) {
        AStarNode currentNode = end;
        MapLocation[] path = new MapLocation[70];
        int i = 0;
        while (currentNode != start) {
            path[i] = currentNode.place;
            currentNode = currentNode.previous;
            i++;
        }
        path[i] = start.place;
        return path;
    }

    public MapLocation[] findPath(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        int VisionRange = rc.getType().visionRadiusSquared;
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), VisionRange); // maybe don't do this? Since we have to look for locations in the array as well now, maybe turn it into a map?
        if (!source.isWithinDistanceSquared(target, VisionRange)) {
            // Change target if distance is larger than 20. How do we want to change the target, maybe checkpoints?
        }

        PriorityQueue<AStarNode> open = new PriorityQueue<>();
        HashSet<MapLocation> openset = new HashSet<>(200);
        HashSet<MapLocation> closed = new HashSet<>(200); // how to actually go about this is still a bit unknown
        // TODO overwrite hash function
        // TODO set

        int distance = Math.max(Math.abs(target.x - source.x), Math.abs(target.y - source.y));
        AStarNode startNode = new AStarNode(null, source, 0, distance, distance);
        AStarNode endNode = new AStarNode(null, target, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        AStarNode childNode = new AStarNode(null, null, 0, 0, 0);
        AStarNode openNode;
        MapLocation currentPlace;
        open.add(startNode); // add start node to open list
        openset.add(startNode.place);

        while (!open.isEmpty()) { //Check if list is empty
            AStarNode currentNode = open.poll(); //Get the node with the lowest f-cost, maybe other data structure
            if (currentNode.place == endNode.place) {
                return retrace_steps(startNode, currentNode);
            }

            open.remove(currentNode);
            openset.remove(currentNode.place);
            closed.add(currentNode.place);
            for (int i = currentNode.place.x - 1; i < currentNode.place.x + 2; i++) { //Loop over all Adjacent nodes
                for (int j = currentNode.place.y - 1; j < currentNode.place.y + 2; j++) { //Loop over all Adjacent nodes
                    MapLocation childPlace = new MapLocation(i, j);
                    if ((childPlace.isWithinDistanceSquared(target, VisionRange)) & (!closed.contains(childPlace))) { // Should only check pos
                        childNode = new AStarNode(currentNode, childPlace, currentNode.GCost + rc.senseRubble(currentNode.place), currentNode.HCost - 1, childNode.HCost + childNode.GCost);
                        if (openset.contains(childPlace)) { //Should only check pos

                            // If childNode is already in openList and GCost is better now than in the list, replace.
                        } else {
                            open.add(childNode);
                            openset.add(childNode.place);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) {
        if (true) {
            // Change target if distance is larger than 20. How do we want to change the target
        }
        return null;
    }
}

/**
 * Record(ish) type for an A* node
 */
class AStarNode implements Comparable<AStarNode>{
    public AStarNode previous;
    public final MapLocation place; // place does not change
    public int GCost;
    final public int HCost; // never changes
    public int FCost;

    AStarNode(AStarNode previous, MapLocation place, int GCost, int HCost, int FCost) {
        this.previous = previous;
        this.place = place;
        this.GCost = GCost;
        this.HCost = HCost;
        this.FCost = FCost;
    }

    /*
     * Compare nodes based on f cost
     */
    @Override
    public int compareTo(AStarNode o) {
        return Integer.compare(this.FCost, o.FCost);
    }
}
