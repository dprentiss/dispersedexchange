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
    public final int gridHeight = 1;
    public final int numAgents;
    public final int gridWidth;
    public final int neighborhoodSize;
    public final double advertisingCost;

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
    Trader[] traderArray;

    // Grid of agent locations
    public ObjectGrid2D traderGrid;

    /** Constructor default */
    public DispersedExchange(long seed) {
        this(seed, 100, 2, 0.1);
    }
    
    /** Constructor */
    public DispersedExchange(long seed, int agents, int size, double cost) {
        // Required by SimState
        super(seed);

        this.numAgents = agents;
        this.gridWidth = this.numAgents;
        this.neighborhoodSize = size;
        this.advertisingCost = cost;
        traderArray = new Trader[numAgents];
    }

    //Fisher-Yates array shuffle
    private void shuffleArray(double[] array) {
        int index;
        double tmp; 
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }

    void setEndowments() {
        double[] endowments = new double[numAgents];
        for (int i = 0; i < endowments.length; i = i+2) {
            endowments[i] = random.nextDouble(false, false);
            endowments[i+1] = 1 - endowments[i];
        }
        shuffleArray(endowments);
        for (int i = 0; i < numAgents; i++) {
            traderArray[i] = new Trader(i, endowments[i], 1 - endowments[i], numAgents);
            traderArray[i].stopper = schedule.scheduleRepeating(traderArray[i]);
        }
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
