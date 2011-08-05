package org.openstreetmap.josm.plugins.fastdraw;

import java.awt.Color;
import java.io.IOException;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

public class FDSettings {
    public Color COLOR_FIXED;
    public Color COLOR_NORMAL;
    public Color COLOR_DELETE;
    public Color COLOR_SELECTEDFRAGMENT;
    public Color COLOR_EDITEDFRAGMENT;

    public double maxDist;
    public double epsilonMult;
    //public double deltaLatLon;
    /// When drawing line, distance between points will be this
    public double minPixelsBetweenPoints;
    /// Initial tolerance for Douglas-Pecker algorithm
    public double startingEps;    
    /// Maximum number of points per 1 km of way
    public double maxPointsPerKm;
    public int pkmBlockSize;
    public boolean drawLastSegment;
    
    public void loadPrefs() {
        COLOR_DELETE = Main.pref.getColor("fastdraw.color.delete", Color.red);
        COLOR_EDITEDFRAGMENT = Main.pref.getColor("fastdraw.color.edit", Color.orange);
        COLOR_FIXED = Main.pref.getColor("fastdraw.color.fixed", Color.green);
        COLOR_NORMAL = Main.pref.getColor("fastdraw.color.normal", Color.red);
        COLOR_SELECTEDFRAGMENT = Main.pref.getColor("fastdraw.color.select", Color.blue);
        maxDist = Main.pref.getDouble("fastdraw.maxdist", 5);
        epsilonMult = Main.pref.getDouble("fastdraw.epsilonmult", 1.1);
        //deltaLatLon = Main.pref.getDouble("fastdraw.deltasearch", 0.01);
        minPixelsBetweenPoints = Main.pref.getDouble("fastdraw.mindelta", 20);
        startingEps = Main.pref.getDouble("fastdraw.startingEps", 0.1);
        maxPointsPerKm = Main.pref.getDouble("fastdraw.maxpkm", 20);
        pkmBlockSize = Main.pref.getInteger("fastdraw.pkmblocksize", 10);
        drawLastSegment = Main.pref.getBoolean("fastdraw.drawlastsegment", true);
    }

    public void savePrefs() {
         Main.pref.putColor("fastdraw.color.delete", COLOR_DELETE );
         Main.pref.putColor("fastdraw.color.edit", COLOR_EDITEDFRAGMENT);
         Main.pref.putColor("fastdraw.color.fixed", COLOR_FIXED);
         Main.pref.putColor("fastdraw.color.normal", COLOR_NORMAL);
         Main.pref.putColor("fastdraw.color.select", COLOR_SELECTEDFRAGMENT);
         Main.pref.putDouble("fastdraw.maxdist", maxDist);
         Main.pref.putDouble("fastdraw.epsilonmult", epsilonMult);
         //Main.pref.putDouble("fastdraw.deltasearch", deltaLatLon);
         Main.pref.putDouble("fastdraw.mindelta",minPixelsBetweenPoints);
         Main.pref.putDouble("fastdraw.startingEps",startingEps);
         Main.pref.putDouble("fastdraw.maxpkm",maxPointsPerKm);
         Main.pref.putInteger("fastdraw.pkmblocksize",pkmBlockSize);
         Main.pref.put("fastdraw.drawlastsegment",drawLastSegment);
         try {Main.pref.save();} catch (IOException e) {
             System.err.println(tr("Can not save preferences"));
         }
    
    }
}
