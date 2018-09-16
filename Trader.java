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
    int numGoods;
    final double[] endowment;
    double[] allocation;
    double[][] MRS;
    Edge[] neighborsIn;
    Edge[] neighborsOut;
    Edge previousBid;

    // Accessors
    double getAllocation(int good) {
        return allocation[good];
    };

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

    void chooseBid(DispersedExchange market) {
        int bestBid = -1;
        double bestUtility = 0;
        for (int i = 0; i < neighborsIn.length; i++) {
            Bid bid = (Bid)neighborsIn[i].getInfo();
            double tmp = getUtilityChange(bid);
            if (tmp > bestUtility) {
                bestBid = i;
                bestUtility = tmp;
            }
        }
    }

    void acceptBid(Bid bid) {
        bid.accepted = true;
        for (int i = 0; i < numGoods; i++) {
            allocation[i] += bid.invoice[i];
        }
    }

    double getUtility(double[] alloc) {
        double utility = 0;
        for (int i = 0; i < numGoods; i++) {
            utility *= Math.pow(alloc[i], 1 / numGoods);
        }
        return utility;
    }

    double getUtility() {
        return getUtility(allocation);
    }

    double getUtilityChange(Bid bid) {
        double[] tmp = new double[numGoods];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = allocation[i] + bid.invoice[i];
        }
        return getUtility(tmp) - getUtility();
    }

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

        updateMRS();

        // Consider neighbors MRS and make the best rational
        // offer one neighbor
    }
}
