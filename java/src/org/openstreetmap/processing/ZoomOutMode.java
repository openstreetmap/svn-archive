package org.openstreetmap.processing;


import processing.core.PImage;

public class ZoomOutMode extends EditMode {

    /*
     * Back reference to the applet 
     *
     */
    private final OsmApplet applet;
    long lastmove;
    
    public ZoomOutMode(OsmApplet applet) {
        this.applet = applet;
        zoomoutIco = applet.loadImage("/data/zoomout.png");
    }

     PImage zoomoutIco;

    public void mouseReleased() {  
        applet.cursor(OsmApplet.ARROW);
       
        System.out.println("Zoom Out Mode: mouseReleased");
    }

    public void mousePressed() {
        applet.tiles.zoomout();
        applet.redraw();
        }
        
    public void mouseDragged() {
    }

    public void draw() {
        // imagehere
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(1);
        applet.image(zoomoutIco, 1, 2);
    }
    
    
    public String getDescription() {
        
        return "Click to zoom out";
    }

}
