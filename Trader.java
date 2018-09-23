/*
  Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.util.*;
import sim.engine.*;
import sim.field.network.*;
import java.util.Arrays;

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

    /*
    void printMRS() {
        for (int i = 0; i < MRS.length; i++) {
            for (int j = 0; j < MRS.length; j++) {
                System.out.print(MRS[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
    */

    void checkPreviousBids(DispersedExchange market) {
        Bid bid;
        for (int i = 0; i < neighborsIn.length; i++) {
            if (neighborsIn[i].getInfo() != null) {
                bid = (Bid)neighborsIn[i].getInfo();
                if (bid != null) {
                    if (bid.invoice != null) {
                        if (bid.accepted == true) {
                            for (int j = 0; j < numGoods; j++) {
                                allocation[j] -= bid.invoice[j];
                            }
                            updateMRS();
                        }
                    }
                }
            }
        }
    }

    void acceptBid(Edge neighbor) {
        Bid bid = (Bid)neighbor.getInfo();
        for (int i = 0; i < numGoods; i++) {
            allocation[i] += bid.invoice[i];
        }
        bid.accepted = true;
        neighbor.setInfo(bid);
        System.out.println("Bid accepted");
        System.out.print(bid.toString());
    }

    boolean checkInventory(double[] invoice) {
        double tmp;
        for (int i = 0; i < numGoods; i++) {
            tmp = allocation[i] + invoice[i];
            if (tmp < 0) return false;
        }
        return true;
    }

    void chooseBids(DispersedExchange market) {
        int bestBidNum = -1;
        double bestUtility = 0;
        Bid bid;
        for (int i = 0; i < neighborsIn.length; i++) {
            if (neighborsIn[i].getInfo() != null) {
                bid = (Bid)neighborsIn[i].getInfo();
                if (bid != null) {
                    if (bid.invoice != null) {
                        if (checkInventory(bid.invoice)) {
                            double tmp = getUtilityChange(bid.invoice);
                            if (tmp > bestUtility) {
                                bestBidNum = i;
                                bestUtility = tmp;
                            }
                        }
                    }
                }
            }
        }
        if (bestBidNum > -1) {
            acceptBid(neighborsIn[bestBidNum]);
            updateMRS();
        }
    }

    void postBids(DispersedExchange market) {
        double bestUtility = 0;
        double tmpUtility;  
        Bid bestBid = null;
        Bid tmpBid;
        int bestBidNum = -1;
        for (int i = 0; i < neighborsIn.length; i++) {
            if (neighborsIn[i].getInfo() == null) continue;
            tmpBid = makeBid((Bid)neighborsIn[i].getInfo());
            tmpUtility = getUtilityChange(invertInvoice(tmpBid.invoice));
            System.out.println(Arrays.toString(tmpBid.invoice));
            System.out.println(getUtility(tmpBid.invoice));
            System.out.println(Arrays.toString(invertInvoice(tmpBid.invoice)));
            System.out.println(getUtility(invertInvoice(tmpBid.invoice)));
            if (tmpUtility > bestUtility) {         
                bestBid = tmpBid;
                bestUtility = tmpUtility;
                bestBidNum = i;
            }
        }
        for (int i = 0; i < neighborsOut.length; i++) {
            if (i == bestBidNum) {
                neighborsOut[i].setInfo(bestBid);
                            System.out.println();
                            System.out.println("Made bid");
            } else {
                neighborsOut[i].setInfo(new Bid(MRS, null, allocation));
                            System.out.println();
                            System.out.println("Did not bid");
            }
        }
    }

    void postBidsAlt(DispersedExchange market) {
        double bestUtility = 0;
        double tmpUtility;  
        Bid bestBid = null;
        Bid tmpBid;
        int bestBidNum = -1;
        for (int i = 0; i < neighborsIn.length; i++) {
            //System.out.println();
            //System.out.println(neighborsIn[i].getInfo());
            if (neighborsIn[i].getInfo() == null) continue;
            tmpBid = makeBid((Bid)neighborsIn[i].getInfo());
            tmpUtility = getUtility(invertInvoice(tmpBid.invoice));
            if (tmpUtility > bestUtility) {         
                bestBid = tmpBid;
                bestUtility = tmpUtility;
                bestBidNum = i;
            }
        }
        for (int i = 0; i < neighborsOut.length; i++) {
            if (i == bestBidNum) {
                neighborsOut[i].setInfo(bestBid);
            } else {
                neighborsOut[i].setInfo(new Bid(MRS, null, allocation));
            }
        }
    }

    Bid makeBid(Bid bid) {
        System.out.println("Bid in:");
        System.out.print(bid.toString());
        double[] newInvoice = new double[numGoods];
        double[] netInvoice = new double[numGoods];
        double[][] prices = new double[numGoods][numGoods];
        double bestPrice = 0;
        int buyGood= -1;
        int sellGood = -1;
        for (int i = 0; i < numGoods; i++) {
            for (int j = 0; j < numGoods; j++) {
                if (i == j) continue;
                prices[i][j] = MRS[i][j] * bid.MRS[j][i];
                prices[i][j] = Math.pow(prices[i][j], 0.5);
                if (prices[i][j] > bestPrice) {
                    bestPrice = prices[i][j];
                    buyGood = i;
                    sellGood = j;
                }
            }
        }
        System.out.printf("Price: %6.3f\n", bestPrice);
        if (buyGood > -1 && sellGood > - 1) {
            if (bestPrice > 1.0) {
                newInvoice[buyGood] = -1.0;
                newInvoice[sellGood] = bestPrice;
            } else {
                newInvoice[buyGood] = 1.0 / bestPrice;
                newInvoice[sellGood] = -1.0;
            }
        }
        System.out.println("Bid out:");
        System.out.printf("Utility change: %f\n", getUtilityChange(invertInvoice(newInvoice)));
        if (getUtilityChange(invertInvoice(newInvoice)) <= 0.0) {
            for (int i = 0; i < numGoods; i++) {
                newInvoice[i] = 0.0;
            }
        }
        Bid newBid = new Bid(MRS, newInvoice, allocation);
        System.out.print(newBid.toString());
        return new Bid(MRS, newInvoice, allocation);
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

    double[] invertInvoice(double[] invoice) {
        double[] tmp = new double[numGoods];
        for (int i = 0; i < numGoods; i++) {
            tmp[i] = 0.0 - invoice[i];
        }
        return tmp;
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
        MRS = new double[numGoods][numGoods];
        updateMRS();
        System.out.println();
        System.out.print(this.toString());
    }

    public String toString() {
        String s = "";
        s += "Trader: " + String.format("%2d  ", idNum);
        s += "Allocation: ";
        for (int i = 0; i < numGoods; i++) {
            s += String.format("%6.3f ", allocation[i]);
        }
        for (int i = 0; i < numGoods; i++) {
            if (i == 0 ) {
                s += "\n" + String.format("%24s", "MRS: ");
            } else {
                s += String.format("%24s", " ");
            }
            for (int j = 0; j < numGoods; j++) {
                s += String.format("%6.3f ", MRS[i][j]);
            }
            s += "\n";
        }
        return s;
    }

    public void step(final SimState state) {
        DispersedExchange market = (DispersedExchange)state;
        updateNeighbors(market);

        //System.out.println();
        //System.out.println(idNum);
        //System.out.println(Arrays.toString(neighborsIn));

        // Check if previuous bid was accepted
        checkPreviousBids(market);

        // Consider offers from neighbors during the previous round
        // and accept the best rational, affordable bid
        chooseBids(market);

        // Consider neighbors MRS and make the best rational
        // offer to one neighbor
        System.out.println();
        System.out.print(this.toString());
        postBids(market);
    }
}
