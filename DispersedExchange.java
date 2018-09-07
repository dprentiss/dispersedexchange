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
    public int gridWidth;
    public int gridHeight;

    enum Good {
        ONE(1),
        TWO(2);

        private final int goodNum;

        private Good(final int num) {
            this.goodNum = num;
        }
        
        public int toInt() { return goodNum; }
    }

    // Grid of agent locations
    public ObjectGrid2D traderGrid;

    /** Constructor default */
    public DispersedExchange(long seed) {
        // Required by SimState
        super(seed);
    }
    
    /** Constructor */
    public DispersedExchange(long seed, int width) {
        // Required by SimState
        super(seed);

        this.gridWidth = width;
    }
    
    public void start() {
        super.start();

        traderGrid = new ObjectGrid2D(gridWidth, gridHeight, 0);
    }

    /** Main */
    public static void main(String[] args) {
        doLoop(DispersedExchange.class, args);
        System.exit(0);
    }
}
