package Trainwreck.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashMap;
import java.util.PriorityQueue;


public class AStarPathfinding implements Pathfinding{

    public MapLocation[] findPath(MapLocation source, MapLocation target, RobotController rc) throws GameActionException {
        int VisionRange = rc.getType().visionRadiusSquared;
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), VisionRange); // maybe don't do this? Since we have to look for locations in the array as well now, maybe turn it into a map?
        if (!source.isWithinDistanceSquared(target, VisionRange)){
            // Change target if distance is larger than 20. How do we want to change the target, maybe checkpoints?
        }

        PriorityQueue<AStarNode> open = new PriorityQueue<>();
        HashMap<AStarNode, Boolean> closed = new HashMap<>(); // how to actually go about this is still a bit unknown

        int distance = Math.max(Math.abs(target.x-source.x), Math.abs(target.y-source.y));
        AStarNode startNode = new AStarNode(null, source, 0, distance*11, distance*11);
        AStarNode endNode = new AStarNode(null, target, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        AStarNode childNode = new AStarNode(null, null,0, 0, 0);
        MapLocation currentPlace;
        open.add(startNode); // add start node to open list

        while(!open.isEmpty()){ //Check if list is empty
            AStarNode currentNode = open.poll(); //Get the node with the lowest f-cost, maybe other data structure
            if (currentNode.place == endNode.place){
                // We've found the end, we're done!
            }

            for (int i = currentNode.place.x-1 ; i < currentNode.place.x+2 ; i++){ //Loop over all Adjacent nodes
                for (int j = currentNode.place.y-1 ; j < currentNode.place.y+2 ; j++){ //Loop over all Adjacent nodes
                    MapLocation childPlace = new MapLocation(i, j);
                    if ((childPlace.isWithinDistanceSquared(target, VisionRange))){ // And if not on ClosedList
                        childNode = new AStarNode(currentNode, childPlace, currentNode.GCost + rc.senseRubble(currentNode.place) , currentNode.HCost - rc.senseRubble(currentNode.place), childNode.HCost + childNode.GCost); //TODO
                        // If childNode is already in openList and GCost is better now than in the list, replace.
                        // Otherwise, add childNode to openList.
                        // Move currentNode to closedList
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Direction getDirection(MapLocation source, MapLocation target, RobotController rc) {
        if (true){
            // Change target if distance is larger than 20. How do we want to change the target
        }
        return null;
    }
}

class AStarNode{
    AStarNode(AStarNode previous, MapLocation place, int GCost, int HCost, int FCost){
        this.previous = previous;
        this.place = place;
        this.GCost = GCost;
        this.HCost = HCost;
        this.FCost = FCost;
    }
    AStarNode previous;
    MapLocation place;
    int GCost;
    int HCost;
    int FCost;
}
