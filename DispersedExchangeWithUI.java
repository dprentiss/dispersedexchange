/*
 * Copyright 2018 David Prentiss
 */

package sim.app.dispersedexchange;

import sim.engine.*;
import sim.display.*;
import sim.util.*;
import sim.portrayal.network.*;
import sim.portrayal.continuous.*;
import sim.portrayal.simple.*;
import java.awt.*;
import javax.swing.*;

public class DispersedExchangeWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();
    NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();

    public static void main(String[] args) {
        new DispersedExchangeWithUI().createController();
    }

    public DispersedExchangeWithUI() {
        super(new DispersedExchange(System.currentTimeMillis()));
    }

    public DispersedExchangeWithUI(SimState state) { super(state); }

    public Object getSimulationInspectedObject() { return state; }

    public static String getName() { return "Dispersed Exchange"; }

    public void setupPortrayals() {
        DispersedExchange market = (DispersedExchange)state;

        fieldPortrayal.setField(market.traderField);
        fieldPortrayal.setPortrayalForAll(new OvalPortrayal2D());

        networkPortrayal.setField(new SpatialNetwork2D(market.traderField,
                                                       market.traderNet));
        networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());

        display.reset();
        display.repaint();
        display.setBackdrop(Color.white);
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

        display = new Display2D(400, 400, this);
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);

        display.attach(fieldPortrayal, "Field");
        display.attach(networkPortrayal, "Traders");

    }

    public void quit() {
        super.quit();
        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
}
