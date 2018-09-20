/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.util.*;
import sim.engine.*;
import sim.field.network.*;

public class Trader implements Steppable {

    // Required by MASON for serialization
    private static final long serialVersionUID = 1;
    
    // Stopper
    Stoppable stopper;

    // Properties
    public final int idNum;

    // Variables
    final int numGoods;
    final double[] endowment;
    double[] allocation;
    double[][] MRS;
    Edge[] neighborsIn;
    Edge[] neighborsOut;
    Edge previousBid;

    // Accessors
    double getAllocation(int good) {
        return allocation[good];
    }

    double[] getAllocation() {
        return allocation;
    }

    void updateNeighbors(DispersedExchange market) {
        Bag edges = market.traderNet.getEdgesIn(this);
        neighborsIn = new Edge[edges.numObjs];
        neighborsOut = new Edge[edges.numObjs];
        for (int i = 0; i < edges.numObjs; i++) {
            neighborsIn[i] = (Edge)edges.objs[i];
            neighborsOut[i] = market.traderNet.getEdge(this, neighborsIn[i].getFrom());
        }
    }

    void updateMRS() {
        for (int i = 0; i < MRS.length; i++) {
            for (int j = 0; j < MRS.length; j++) {
                MRS[i][j] = allocation[j] / allocation[i];
            }
        }
    }

    void postBids(DispersedExchange market) {
        for (int i = 0; i < neighborsOut.length; i++) {
            Bid b = new Bid(MRS, null);
        }
    }

    void printMRS() {
        for (int i = 0; i < MRS.length; i++) {
            for (int j = 0; j < MRS.length; j++) {
                System.out.print(MRS[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    void checkPreviousBid(DispersedExchange market) {
        if (previousBid != null) {
            Bid bid = (Bid)previousBid.getInfo();
            if (bid.accepted == true) {
                for (int i = 0; i < numGoods; i++) {
                    allocation[i] -= bid.invoice[i];
                }
                updateMRS();
            }
            previousBid.setInfo(null);
        }
    }

    void acceptBid(Edge neighbor) {
        Bid bid = (Bid)neighbor.getInfo();
        for (int i = 0; i < numGoods; i++) {
            allocation[i] += bid.invoice[i];
        }
        bid.accepted = true;
        neighbor.setInfo(bid);
    }

    void chooseBid(DispersedExchange market) {
        int bestBidNum = -1;
        double bestUtility = 0;
        for (int i = 0; i < neighborsIn.length; i++) {
            Bid bid = (Bid)neighborsIn[i].getInfo();
            if (bid != null) {
                double tmp = getUtilityChange(bid.invoice);
                if (tmp > bestUtility) {
                    bestBidNum = i;
                    bestUtility = tmp;
                }
            }
        }
        if (bestBidNum > -1) {
            acceptBid(neighborsIn[bestBidNum]);
            updateMRS();
        }
    }

    void makeBid(DispersedExchange market) {
    }

    double getUtility(double[] alloc) {
        double utility = 1;
        for (int i = 0; i < numGoods; i++) {
            utility *= Math.pow(alloc[i], 1.0 / numGoods);
        }
        return utility;
    }

    double getUtility() {
        return getUtility(allocation);
    }

    double getUtilityChange(double[] invoice) {
        double[] tmp = new double[numGoods];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = allocation[i] + invoice[i];
        }
        return getUtility(tmp) - getUtility();
    }

    /*
    double getUtilityChangeAlt(double[] invoice) {
        // TODO this function is broken
        double oldUtility = 1;
        double newUtility = 1;
        for (int i = 0; i < numGoods; i++) {
            newUtility *= allocation[i] + invoice[i];
            oldUtility *= allocation[i];
        }
        return 1.0 / numGoods * (newUtility - oldUtility);
    }
    */

    /** Constructor */
    public Trader(int id, double[] endowment) {
        this.idNum = id;
        this.endowment = endowment;
        allocation = endowment;
        numGoods = allocation.length;
        System.out.printf("Trader %d has %f of good one and %f of good two.\n", idNum, allocation[0], allocation[1]);
        MRS = new double[numGoods][numGoods];
        updateMRS();
        printMRS();
    }

    public void step(final SimState state) {
        DispersedExchange market = (DispersedExchange)state;
        updateNeighbors(market);

        // Check if previuous bid was accepted
        checkPreviousBid(market);

        // Consider offers from neighbors during the previous round
        // and accept the best rational, affordable bid
        chooseBid(market);

        // Consider neighbors MRS and make the best rational
        // offer to one neighbor
        makeBid(market);
    }
}
