package Trainwreck.util;

import battlecode.common.MapLocation;

/**
 * Record(ish) type for an A* node
 */
public class AStarNode implements Comparable<AStarNode> {
    public AStarNode previous;
    public final MapLocation place; // place does not change
    public int GCost;
    public final int HCost; // never changes
    public int FCost;

    public AStarNode(AStarNode previous, MapLocation place, int GCost, int HCost, int FCost) {
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
        if (o == null){
            return -1; // doesn't make too much sense but makes A* work nicely
        }
        return Integer.compare(this.FCost, o.FCost);
    }
    public int compareToG(AStarNode o) {
        if (o == null){
            return -1; // doesn't make too much sense but makes A* work nicely
        }
        return Integer.compare(this.GCost, o.GCost);
    }
}
