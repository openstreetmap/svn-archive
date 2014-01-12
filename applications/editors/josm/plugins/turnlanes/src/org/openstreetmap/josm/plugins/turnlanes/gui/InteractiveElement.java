package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

abstract class InteractiveElement {
    interface Type {
        Type INCOMING_CONNECTOR = new Type() {};
        Type OUTGOING_CONNECTOR = new Type() {};
        Type TURN_CONNECTION = new Type() {};
        Type LANE_ADDER = new Type() {};
        Type EXTENDER = new Type() {};
        Type VIA_CONNECTOR = new Type() {};
    }
    
    public void paintBackground(Graphics2D g2d, State state) {}
    
    abstract void paint(Graphics2D g2d, State state);
    
    abstract boolean contains(Point2D p, State state);
    
    abstract Type getType();
    
    State activate(State old) {
        return old;
    }
    
    boolean beginDrag(double x, double y) {
        return false;
    }
    
    State drag(double x, double y, InteractiveElement target, State old) {
        return old;
    }
    
    State drop(double x, double y, InteractiveElement target, State old) {
        return old;
    }
    
    abstract int getZIndex();
    
    State click(State old) {
        return old;
    }
}
