package org.openstreetmap.josm.plugins.piclayer.layer.kml;


public class KMLGroundOverlay {

    private String filename;
    private double north;
    private double south;
    private double east;
    private double west;
    private double rotate;
    private String name;

    public String getFileName() {
        return filename;
    }
    public void setFileName(String file) {
        this.filename = file;
    }
    public double getNorth() {
        return north;
    }
    public void setNorth(double north) {
        this.north = north;
    }
    public double getSouth() {
        return south;
    }
    public void setSouth(double south) {
        this.south = south;
    }
    public double getEast() {
        return east;
    }
    public void setEast(double east) {
        this.east = east;
    }
    public double getWest() {
        return west;
    }
    public void setWest(double west) {
        this.west = west;
    }
    public double getRotate() {
        return rotate;
    }
    public void setRotate(double rotate) {
        this.rotate = rotate;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
