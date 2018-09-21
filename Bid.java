/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;
//import sim.util.*;
//import sim.engine.*;
//import sim.field.network.*;

public class Bid {

    // Required by MASON for serialization
    private static final long serialVersionUID = 1;
    
    // Properties
    public final double[][] MRS;
    public final double[] invoice;
    public final double[] inventory;

    // Variables
    public boolean accepted;

    /** Constructor */
    public Bid(double[][] MRS, double[] invoice, double[] inventory) {
        this.MRS = MRS;
        this.invoice = invoice;
        this.inventory = inventory;
    }
}
