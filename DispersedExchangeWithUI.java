/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;

import sim.engine.*;
import sim.display.*;
import sim.util.*;
import sim.portrayal.grid.*;
import java.awt.*;
import javax.swing.*;

public class DispersedExchangeWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    /*
    FastValueGridPortrayal2D roadPortrayal = new FastValueGridPortrayal2D("Road");
    FastValueGridPortrayal2D blockPortrayal = new FastValueGridPortrayal2D("Block", true);
    FastValueGridPortrayal2D intersectionPortrayal = new FastValueGridPortrayal2D("Intersection");

    SparseGridPortrayal2D agentPortrayal = new SparseGridPortrayal2D();
    */

    public static void main(String[] args) {
        new DispersedExchangeWithUI().createController();
    }

    public DispersedExchangeWithUI() { super(new DispersedExchange(System.currentTimeMillis())); }
    public DispersedExchangeWithUI(SimState state) { super(state); }

    public Object getSimulationInspectedObject() { return state; }

    public static String getName() { return "Dispersed Exchange"; }

    public void setupPortrayals() {
        DispersedExchange market = (DispersedExchange)state;

        /*
        // Road colors
        int numDir = Direction.values().length; // count Directions enum
        Color roadColors[] = new Color[numDir]; // make an array of colors
        roadColors[0] = new Color(0,0,0,0); // Direction.NONE is transparent
        for (int i = 1; i < numDir; i++) {
            roadColors[i] = new Color(0,0,0,128);
        }
        roadPortrayal.setField(ac.roadGrid); 
        roadPortrayal.setMap(new sim.util.gui.SimpleColorMap(roadColors));

        // Block colors
        Color blockColors[] = new Color[2];
        blockColors[0] = new Color(0,0,0,0);
        blockColors[1] = new Color(0,255,0,128);
        blockPortrayal.setField(ac.blockGrid);
        blockPortrayal.setMap(new sim.util.gui.SimpleColorMap(blockColors));

        // Intersection colors
        Color intersectionColors[] = new Color[2];
        intersectionColors[0] = new Color(0,0,0,0);
        intersectionColors[1] = new Color(0,0,0,0);
        intersectionPortrayal.setField(ac.intersectionGrid);
        intersectionPortrayal.setMap(new sim.util.gui.SimpleColorMap(intersectionColors));

        // Agent Colors
        agentPortrayal.setField(ac.agentGrid);
        agentPortrayal.setPortrayalForClass(Vehicle.class, 
                new sim.portrayal.simple.OvalPortrayal2D(Color.red));
        Bag vehicles = ac.agentGrid.getAllObjects();
        for (int i = 0; i < vehicles.numObjs; i++) {
            Color newColor = new Color(ac.random.nextInt(255),
                    ac.random.nextInt(255), ac.random.nextInt(255));
            agentPortrayal.setPortrayalForObject(vehicles.objs[i],
                    new sim.portrayal.simple.OvalPortrayal2D(newColor));
        }
        */

        display.reset();
        display.repaint();
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void init(Controller c) {
        super.init(c);

        display = new Display2D(400,400,this);
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);

        /*
        display.attach(roadPortrayal, "Road");
        display.attach(blockPortrayal, "Block");
        display.attach(intersectionPortrayal, "Intersection");
        display.attach(agentPortrayal, "Agents");

        display.setBackdrop(Color.white);
        */
    }

    public void quit() {
        super.quit();
        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
}
