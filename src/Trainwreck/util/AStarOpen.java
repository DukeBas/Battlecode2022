package Trainwreck.util;

/**
 * Uses minHeap and 2d pointer array to quickly get highest priority node and search if a node has been seen before.
 */
public class AStarOpen {

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
     * Updates a node with new information
     *
     * @param node     to update
     * @param new_G    new G cost
     * @param new_F    new F cost
     * @param new_prev new previous A* node
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
     *
     * @param a position
     * @param b position
     */
    private void swap(int a, int b) {
        AStarNode temp = heap[a];

        heap[a] = heap[b];
        heap[b] = temp;
    }

}
