package org.openstreetmap.applet;

import java.awt.Color;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.proj.Length;

public class Node extends OMCircle {

    private int nUID;
    private double dLatitude, dLongitude;

    public Node(int uid, double latitude, double longitude) {
        super((float) latitude, (float) longitude, 5f, Length.METER);
        setLinePaint(Color.black);
        setSelectPaint(Color.red);
        setFillPaint(Color.black);
        nUID = uid;
    }

    public int getUID() {
        return nUID;
    }

    public String toString() {
        return "(" + nUID + "," + getLatLon() + ")";
    }
    
}
