package org.openstreetmap.processing;

import processing.core.PImage;

public class ZoomInMode extends EditMode {

    /*
     * Back reference to the applet 
     *
     */
    private final OsmApplet applet;
    long lastmove;
    
    
    public ZoomInMode(OsmApplet applet) {
        this.applet = applet;
        zoominIco = applet.loadImage("/data/zoomin.png");
    }

     PImage zoominIco;

    public void mouseReleased() {  
        applet.cursor(OsmApplet.DEGREES);
        System.out.println("Zoom Mode: mouseReleased");
    }

    public void mousePressed() {
        applet.tiles.zoomin();
        applet.redraw();
        }
        

    public void mouseDragged() {
    }

    public void draw() {
        // imagehere
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(1);
        applet.image(zoominIco, 1, 2);
    }
    
    
    public String getDescription() {
        
        return "Click to zoom in";
    }

}
