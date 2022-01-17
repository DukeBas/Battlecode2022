package Trainwreck.util;

/**
 * Uses minHeap and 2d pointer array to quickly get highest priority node and search if a node has been seen before.
 */
public class AStarOpen {

    private final int capacity; // capacity of array to use for the heap
    private int size; // current size of heap
    private final AStarNode[] heap; // do not use index 0 for ease of use
    private final boolean[][] openSet = new boolean[61][61]; // max size of the map, do not use 0 indices


    /**
     * Constructor.
     */
    public AStarOpen(int maxCapacity) {
        this.capacity = maxCapacity + 1; // +1 to account for index 0 being unused
        this.heap = new AStarNode[capacity];
        size = 0;
    }

    /**
     * Inserts a node into the minHeap.
     *
     * @param node to insert
     */
    public void insert(AStarNode node) {
        /*
         * Insert into heap
         */

        if (size >= capacity) {
            return; // Heap full! Ignore this node :(
        }

        heap[++size] = node;
        int current = size;


        while (current != 1 && heap[current].compareTo(heap[parentPos(current)]) < 0) {
            swap(current, parentPos(current));
            current = parentPos(current);
        }


        /*
         * Add to open set
         */
        openSet[node.place.x][node.place.y] = true;
    }

    /**
     * Checks whether a node is open.
     *
     * @param node to check
     * @return whether node is open
     */
    public boolean isOpen(AStarNode node) {
        return openSet[node.place.x][node.place.y];
    }

    /**
     * Updates a node with new information. MODIFIES NODE ITSELF, NO NEED TO MODIFY NODE BEFOREHAND.
     * Assumes new G and F costs are lower, as there would be no need to update if they're the same.
     *
     * @param node     to update
     * @param new_G    new G cost
     * @param new_F    new F cost
     * @param new_prev new previous A* node
     */
    public void updateNode(AStarNode node, int new_G, int new_F, AStarNode new_prev) { // update based on new F-cost
        /*
         * We assume new costs are always lower, so swap up until we are either at the top or node above us is smaller
         */
        int pos = getPosNode(node);
        node.GCost = new_G;
        node.FCost = new_F;
        node.previous = new_prev;

        while (pos != 1 && heap[parentPos(pos)].compareTo(heap[pos]) > 0) {
            int parentPos = parentPos(pos);
            swap(pos, parentPos);
            pos = parentPos;
        }
    }

    /**
     * Gets the position of a certain node in the heap. Returns -1 if it is not in.
     *
     * @param node to get the position of
     * @return position of node
     */
    private int getPosNode(AStarNode node) {
        //TODO turn bool array into position array and update positions in array each time
        for (int i = 1; i <= size; i++) {
            if (heap[i] == node) {
                // same pointer! Node found!
                return i;
            }
        }

        return -1;
    }

    /**
     * Pops the current highest priority node from the heap (get and removes from heap).
     *
     * @return next A* node
     */
    public AStarNode popBest() {
        /*
         * Pop from minHeap
         */
        AStarNode node = heap[1];
        heap[1] = heap[size--];
        if (!isEmpty()) {
            minHeapify(1);
        }

        System.out.println("Popping A* node with F-Cost: " + node.FCost);

        /*
         * Remove from open set
         */
        openSet[node.place.x][node.place.y] = false;

        return node;
    }

    /**
     * Gets position of parent element of an element at a position.
     *
     * @param pos of element to get parent of
     * @return position parent element
     */
    private int parentPos(int pos) {
        return pos / 2;
    }

    /**
     * Gets position of left child of an element at a position.
     *
     * @param pos of element to get the left child of
     * @return position left child
     */
    private int leftChildPos(int pos) {
        return 2 * pos;
    }


//    /**
//     * Gets position of right child of an element at a position
//     *
//     * @param pos of element to get the right child of
//     * @return position right child
//     */
//    private int rightChildPos(int pos) {
//        return 2 * pos + 1;
//    }

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

    /**
     * Method to get if data structure is empty.
     *
     * @return whether there are no items in open
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * MinHeapify node at pos.
     *
     * @param pos to minHeapify
     */
    private void minHeapify(int pos) {
        // If the node is a non-leaf node and greater
        // than any of its child
//        System.out.println(pos + " " + isLeaf(pos) + " " + (pos > (size / 2)) + " " + size);
        if (!isLeaf(pos)) {
            AStarNode current = heap[pos];
            int posLeft = leftChildPos(pos);
            int posRight = posLeft + 1;
            if (current.compareTo(heap[posLeft]) > 0 || current.compareTo(heap[posRight]) > 0) {
                if (heap[posLeft].compareTo(heap[posRight]) < 0) { // swap with the left child and heapify left child
                    swap(pos, posLeft);
                    minHeapify(posLeft);
                } else { // swap with the right child and heapify the right child
                    swap(pos, posRight);
                    minHeapify(posRight);
                }
            }
        }
    }

    public String printableFCosts() {
        StringBuilder out = new StringBuilder("[");

        for (int i = 1; i <= size; i++) {
            if (isPowerOfTwo(i + 1) && i < size){
                out.append(heap[i].FCost).append(" | ");
            } else {
                out.append(heap[i].FCost).append(" ");
            }
        }

        if (out.length() > 1) {
            out.delete(out.length() - 1, out.length()); // remove last space
        }
        out.append("]");

        return out.toString();
    }

    /**
     * Used by printing function
     *
     * @param n number to check
     * @return whether the number is a power of 2
     */
    private boolean isPowerOfTwo(int n) {
        return (int) (Math.ceil((Math.log(n) / Math.log(2))))
                == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
    }
}
