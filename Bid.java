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
    public final Trader trader;
    public final double[][] MRS;
    public final double[] invoice;
    public final double[] inventory;

    // Variables
    public boolean accepted;

    public String toString() {
        String s = "";
        s += String.format("%24s%6d", "Trader: ", trader.idNum);
        s += "\n";
        s += String.format("%24s%6s", "Accepted: ", accepted);
        s += "\n";
        s += String.format("%24s", "Inventory: ");
        for (int i = 0; i < inventory.length; i++) {
            s += String.format("%6.3f ", inventory[i]);
        }
        s += "\n";
        s += String.format("%24s", "Invoice: ");
        for (int i = 0; i < inventory.length; i++) {
            if (invoice == null) {
                s += String.format("%6.3f ", 0.0);
            } else {
                s += String.format("%6.3f ", invoice[i]);
            }
        }
        for (int i = 0; i < inventory.length; i++) {
            if (i == 0 ) {
                s += "\n" + String.format("%24s", "MRS: ");
            } else {
                s += String.format("%24s", " ");
            }
            for (int j = 0; j < inventory.length; j++) {
                s += String.format("%6.3f ", MRS[i][j]);
            }
            s += "\n";
        }
        return s;
    }

    /** Constructor */
    public Bid(Trader trader, double[][] MRS, double[] invoice, double[] inventory) {
        this.trader = trader;
        this.MRS = MRS;
        this.invoice = invoice;
        this.inventory = inventory;
        this.accepted = false;
    }
}
