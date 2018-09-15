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
    double x1;
    double x2;
    int numGoods;
    double[] endowment;
    double[] allocation;
    double[][] MRS;
    Bag neighbors;

    // Accessors
    double getAllocation(int good) {
        double allocation = 0;
        switch (good) {
            case 1:
                allocation = x1;
                break;
            case 2:
                allocation = x2;
                break;
        }
        return allocation;
    };

    void updateMRS() {
        for (int i = 0; i < MRS.length; i++) {
            for (int j = 0; j < MRS.length; j++) {
                MRS[i][j] = allocation[j] / allocation[i];
            }
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
        neighbors = market.traderNet.getEdgesIn(this);
        for (int i = 0; i < neighbors.numObjs; i++) {
            Edge e = (Edge)neighbors.objs[i];
            Trader n = (Trader)e.getOtherNode(this);
        }

        //Advertise

        //Trade
    }
}
