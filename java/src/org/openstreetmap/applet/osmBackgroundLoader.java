package org.openstreetmap.applet;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;
import org.openstreetmap.client.osmServerClient;
import org.openstreetmap.util.Logger;
import org.openstreetmap.util.gpspoint;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

public class osmBackgroundLoader extends Thread {

    OMGraphicList graphics;
    osmPointsLayer osmPL;
    LatLonPoint llTopLeft;
    LatLonPoint llBotRight;
    osmServerClient osc;

    public osmBackgroundLoader(osmServerClient o, OMGraphicList list, osmPointsLayer op, LatLonPoint tl, LatLonPoint br) {
        graphics = list;
        osmPL = op;
        llTopLeft = tl;
        llBotRight = br;
        osc = o;
    }

    public void run() {
        Logger.log("OSM background loader starting");
        OMCircle omc;
        long lLastTime = System.currentTimeMillis();
        Vector v = new Vector();
        v = osc.getPoints(llTopLeft, llBotRight);
        Enumeration en = v.elements();
        while (en.hasMoreElements()) {
            gpspoint p = (gpspoint) en.nextElement();
            omc = new OMCircle(p.getLatitude(), p.getLongitude(), 5f, com.bbn.openmap.proj.Length.METER);
            omc.setLinePaint(Color.gray);
            omc.setSelectPaint(Color.red);
            omc.setFillPaint(OMGraphic.clear);
            graphics.add(omc);
            if (lLastTime + 1000 < System.currentTimeMillis()) {
                lLastTime = System.currentTimeMillis();
                osmPL.fireBackgroundRedraw();
            }
        }
        osmPL.fireBackgroundRedraw();
        float time = System.currentTimeMillis() - lLastTime;
        Logger.log("*** Full refresh took " + time + "ms");
    }
 
}
