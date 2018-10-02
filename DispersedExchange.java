/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;
import sim.field.network.*;
import java.util.Arrays;

public class DispersedExchange extends SimState {

    // Required for serialization
    private static final long serialVersionUID = 1;

    // Grid dimensions
    public final int numAgents;
    public final int numGoods;

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
        numGoods = goods;
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
            endowments[i] = random.nextDouble(false, false) * 100;
            endowments[i+1] = 100 - endowments[i];
        }
        shuffleArray(endowments);
        for (int i = 0; i < numAgents; i++) {
            traderArray[i] = new Trader(i, new double[]{endowments[i], 100 - endowments[i]});
        }
    }

    void resetTraders() {
        for (int i =0; i < traderArray.length; i++) {
            traderArray[i].allocation = traderArray[i].endowment.clone();
            traderArray[i].lastTradeStep = -1;
        }
    }

    void initNetwork() {
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
    }
    
    public void start() {
        super.start();
        
        Steppable checkActivity = new Steppable() {
            public void step(final SimState state) {
                DispersedExchange market = (DispersedExchange)state;
                boolean hasTraded = false;
                for (int i = 0; i < traderArray.length; i++) {
                    if (traderArray[i].hasTraded) hasTraded = true;
                }
                if (schedule.getSteps() > 2 && !hasTraded) {
                    market.finish();
                }
            }
        };

        for (int i = 0; i < traderArray.length; i++) {
            traderArray[i].stopper = schedule.scheduleRepeating(traderArray[i]);
        }
        schedule.scheduleRepeating(checkActivity, 1, 1);

    }

    public void printMRS() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < traderArray.length; i++) {
            s.append(String.format("%6.3f, ", traderArray[i].MRS[0][1])); 
        }
        System.out.println(s.toString());
    }

    double[] prices() {
        double[] prices = new double[numGoods];
        for (int i = 0; i < numAgents; i++) {
            for (int j = 0; j < numGoods; j++) {
                prices[j] += traderArray[i].MRS[0][j];
            }
        }
        for (int i = 0; i < numGoods; i++) {
            prices[i] = prices[i] / numAgents;
        }
        return prices;
    }
    
    double wealth(double[] allocation, double[] prices) {
        double wealth = 0; 
        for (int i = 0; i < numGoods; i++) {
            wealth += allocation[i] * prices[i];
        }   
        return wealth;
    }

    double[] getWealthChanges(double[] prices) {
        double[] w = new double[numAgents];
        for (int i = 0; i < numAgents; i++) {
            w[i] = wealth(traderArray[i].endowment, prices);
            w[i] -= wealth(traderArray[i].allocation, prices);
        }
        return w;
    }

    void updateNetwork(DispersedExchange state, double[] wealthChange) {
        double[] wealth = wealthChange.clone();
        Trader worstTrader = null;
        Trader nextTrader = null;
        int worstTraderNum = -1;
        int nextTraderNum = -1;
        int neighborNum = -1;
        double worstWealth = Double.POSITIVE_INFINITY;

        while (nextTraderNum == -1 || worstTraderNum == -1) {
            // find worst-off Trader
            worstWealth = Double.POSITIVE_INFINITY;
            for (int i = 0; i < wealth.length; i++) {
                if (wealth[i] < worstWealth) {
                    worstWealth = wealth[i];
                    worstTraderNum = i;
                }
            }
            worstTrader = traderArray[worstTraderNum];

            // skip worst-off trader if already fully connected
            if (worstTrader.neighborsIn.length == numAgents - 1) {
                wealth[worstTraderNum] = Double.POSITIVE_INFINITY;
                worstTraderNum = -1;
                continue;
            }

            // ignore neighbors, self, and fully connected nodes
            for (int i = 0; i < worstTrader.neighborsIn.length; i++) {
                neighborNum = 
                    ((Trader)worstTrader.neighborsIn[i].getFrom()).idNum;
                wealth[neighborNum] = Double.POSITIVE_INFINITY;
            }
            wealth[worstTraderNum] = Double.POSITIVE_INFINITY;

            // find new neighbor
            worstWealth = Double.POSITIVE_INFINITY;
            for (int i = 0; i < wealth.length; i++) {
                if (wealth[i] < worstWealth
                        && i != worstTraderNum) {
                    worstWealth = wealth[i];
                    nextTraderNum = i;
                        }
            }
            nextTrader = traderArray[nextTraderNum];
           
            // skip new neighbor if already fully connected
            if (nextTrader.neighborsIn.length == numAgents - 1) {
                wealth[nextTraderNum] = Double.POSITIVE_INFINITY;
            }
        }

        // connect the two
        traderNet.addEdge(worstTrader, nextTrader, null);
        traderNet.addEdge(nextTrader, worstTrader, null);

        // update their neighbor lists
        worstTrader.updateNeighbors(state);
        nextTrader.updateNeighbors(state);


        /*
        System.out.print(worstTrader.toString());
        System.out.println(worstTrader.neighborsIn.length);
        System.out.print(nextTrader.toString());
        System.out.println(nextTrader.neighborsIn.length);
        System.out.println(Arrays.toString(wealth));
        */
    }

    /** Main */
    public static void main(String[] args) {
        //doLoop(DispersedExchange.class, args);
        DispersedExchange state = new DispersedExchange(System.currentTimeMillis());
        //DispersedExchange state = new DispersedExchange(0);
        int maxEdges = ((state.numAgents * (state.numAgents-1)) / 2) - state.numAgents;
        double[] wealthChange;
        double[] prices;
        state.setEndowments();
        state.initNetwork();
        state.start();
        while (true) if (!state.schedule.step(state)) break;
        prices = state.prices();
        wealthChange = state.getWealthChanges(prices);
        state.printMRS();
        System.out.println(Arrays.toString(prices));
        System.out.println(Arrays.toString(wealthChange));

        for (int i = 0; i < maxEdges; i++) {
            state.updateNetwork(state, wealthChange);
            state.resetTraders();
            state.start();
            while (true) if (!state.schedule.step(state)) break;
            prices = state.prices();
            wealthChange = state.getWealthChanges(prices);
            state.printMRS();
            System.out.println(Arrays.toString(state.prices()));
            System.out.println(Arrays.toString(state.getWealthChanges(state.prices())));
            System.out.println(i + state.numAgents + 1);
            //System.out.println(Arrays.deepToString(state.traderNet.getAdjacencyMatrix()));
        }

        System.exit(0);
    }
}
