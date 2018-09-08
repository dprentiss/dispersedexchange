/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
import sim.util.*;
import sim.engine.*;

public class Trader implements Steppable {

    // Required by MASON for serialization
    private static final long serialVersionUID = 1;
    
    // Stopper
    Stoppable stopper;

    // Properties
    public int idNum;
    
    // Variables
    double x1;
    double x2;

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

    /** Constructor */
    public Trader(int id, double x1, double x2) {
        this.idNum = id;
        this.x1 = x1;
        this.x2 = x2;
        System.out.printf("Trader %d has %f of good one and %f of good two.\n", idNum, x1, x2);
    }

    public void step(final SimState state) {
        DispersedExchange de = (DispersedExchange)state;
    }
}
