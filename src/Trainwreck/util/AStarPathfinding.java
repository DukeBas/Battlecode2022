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
 * Record type for an A* node
 */
class AStarNode {
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
}


/**
 * Uses minHeap and 2d pointer array to quickly get highest priority node and search if a node has been seen before.
 */
class AStarOpen {

    private final int capacity; // capacity of array to use for the heap
    private int size; // current size of heap
    private final AStarNode[] heap;


    /**
     * Constructor
     */
    public AStarOpen(int maxCapacity) {
        this.capacity = maxCapacity;
        this.heap = new AStarNode[capacity];
    }

    public void addNode(AStarNode node) {
        //TODO
    }

    /**
     * Update ???
     *
     * @param node
     * @param NUMBER_TO_UPDATE
     */
    public void updateNode(AStarNode node, int new_G, int new_F, AStarNode new_prev) { // update based on new F-cost
        //TODO
    }

    /**
     * Gets the current highest priority node from the heap
     *
     * @return next a* node
     */
    public AStarNode popBest() {
        return null; //TODO
    }

    /**
     * Gets position of parent element of an element at a position
     *
     * @param pos of element to get parent of
     * @return position parent element
     */
    private int parentPos(int pos) {
        return pos / 2;
    }

    /**
     * Gets position of left child of an element at a position
     *
     * @param pos of element to get the left child of
     * @return position left child
     */
    private int leftChildPos(int pos) {
        return 2 * pos;
    }


    /**
     * Gets position of right child of an element at a position
     *
     * @param pos of element to get the right child of
     * @return position right child
     */
    private int rightChildPos(int pos) {
        return 2 * pos + 1;
    }

    /**
     * Checks whether an element at a position is a leaf node (i.e. has no children)
     *
     * @param pos to check
     * @return whether element at position is a leaf node
     */
    private boolean isLeaf(int pos) {
        return (pos > (size / 2) && pos <= size);
    }

    /**
     * Swap nodes at position a and b
     * @param a position
     * @param b position
     */
    private void swap(int a, int b){
        AStarNode temp = heap[a];

        heap[a] = heap[b];
        heap[b] = temp;
    }

}