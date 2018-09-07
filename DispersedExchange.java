/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;

public class DispersedExchange extends SimState {

    // Required for serialization
    private static final long serialVersionUID = 1;

    // Grid dimensions
    public int numAgents;
    public int gridWidth;
    public final int gridHeight = 1;

    enum Good {
        ONE(1),
        TWO(2);

        private final int goodNum;

        private Good(final int num) {
            this.goodNum = num;
        }
        
        public int toInt() { return goodNum; }
    }

    // Array of Traders
    Trader[] traderArray = new Trader[numAgents];

    // Grid of agent locations
    public ObjectGrid2D traderGrid;

    /** Constructor default */
    public DispersedExchange(long seed) {
        this(seed, 100);
    }
    
    /** Constructor */
    public DispersedExchange(long seed, int agents) {
        // Required by SimState
        super(seed);

        this.numAgents = agents;
        this.gridWidth = this.numAgents;
    }

    void setEndowments() {
        int[] indexArray = new int[numAgents];
    }
    
    public void start() {
        super.start();

        setEndowments();

        traderGrid = new ObjectGrid2D(gridWidth, gridHeight, traderArray);
    }

    /** Main */
    public static void main(String[] args) {
        doLoop(DispersedExchange.class, args);
        System.exit(0);
    }
}
