/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;
import sim.field.network.*;

public class DispersedExchange extends SimState {

    // Required for serialization
    private static final long serialVersionUID = 1;

    // Grid dimensions
    public final int numAgents;

    public Network traderNet = null;

    // Array of Traders
    Trader[] traderArray;

    // Grid of agent locations
    public ObjectGrid2D traderGrid;

    /** Constructor default */
    public DispersedExchange(long seed) {
        this(seed, 10, 2);
    }
    
    /** Constructor */
    public DispersedExchange(long seed, int agents, int goods) {
        // Required by SimState
        super(seed);

        this.numAgents = agents;
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
            traderArray[i] = new Trader(i, new double[]{endowments[i], 1 - endowments[i]});
            traderArray[i].stopper = schedule.scheduleRepeating(traderArray[i]);
        }
    }
    
    public void start() {
        super.start();
        setEndowments();
        traderNet = new Network(true);
        for (int i = 0; i < traderArray.length; i++) {
            traderNet.addNode(traderArray[i]);
        }
        for (int i = 0; i < traderArray.length - 1; i++) {
            traderNet.addEdge(traderArray[i], traderArray[i + 1], null);
            traderNet.addEdge(traderArray[i + 1], traderArray[i], null);
        }
        traderNet.addEdge(traderArray[traderArray.length - 1],
                traderArray[0], null);
        traderNet.addEdge(traderArray[0],
                traderArray[traderArray.length - 1], null);
        //System.out.print(traderNet.getAdjacencyList(false)[4].length);
        /*
        System.out.println(
                traderArray[0].getUtilityChange(traderArray[2].getAllocation())
                );
                */
    }

    /** Main */
    public static void main(String[] args) {
        doLoop(DispersedExchange.class, args);
        System.exit(0);
    }
}
