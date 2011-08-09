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
    public Color COLOR_SIMPLIFIED;
    
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
    // snap to nodes
    public boolean snapNodes; 
    // add fixed foints on mouse click
    public boolean fixedClick; 
    // add fixed foints on spacebar
    public boolean fixedSpacebar;
    // option for simplifiction: 0="Autosimplify and wait",
    //1="Simplify and wait", 2="Save as is"
    public int simplifyMode;
    public float lineWidth;
    
    public void loadPrefs() {
        COLOR_DELETE = Main.pref.getColor("fastdraw.color.delete", Color.red);
        COLOR_EDITEDFRAGMENT = Main.pref.getColor("fastdraw.color.edit", Color.orange);
        COLOR_FIXED = Main.pref.getColor("fastdraw.color.fixed", Color.green);
        COLOR_NORMAL = Main.pref.getColor("fastdraw.color.normal", Color.red);
        COLOR_SELECTEDFRAGMENT = Main.pref.getColor("fastdraw.color.select", Color.blue);
        COLOR_SIMPLIFIED = Main.pref.getColor("fastdraw.color.simplified", Color.orange);
        maxDist = Main.pref.getDouble("fastdraw.maxdist", 5);
        epsilonMult = Main.pref.getDouble("fastdraw.epsilonmult", 1.1);
        //deltaLatLon = Main.pref.getDouble("fastdraw.deltasearch", 0.01);
        minPixelsBetweenPoints = Main.pref.getDouble("fastdraw.mindelta", 20);
        startingEps = Main.pref.getDouble("fastdraw.startingEps", 0.1);
        maxPointsPerKm = Main.pref.getDouble("fastdraw.maxpkm", 20);
        pkmBlockSize = Main.pref.getInteger("fastdraw.pkmblocksize", 10);
        drawLastSegment = Main.pref.getBoolean("fastdraw.drawlastsegment", true);
        snapNodes = Main.pref.getBoolean("fastdraw.snapnodes", true);
        fixedClick = Main.pref.getBoolean("fastdraw.fixedclick", false);
        fixedSpacebar = Main.pref.getBoolean("fastdraw.fixedspacebar", false);
        simplifyMode = Main.pref.getInteger("fastdraw.simplifymode", 0);
        lineWidth = (float) Main.pref.getDouble("fastdraw.linewidth", 2);
    }

    public void savePrefs() {
         Main.pref.putColor("fastdraw.color.delete", COLOR_DELETE );
         Main.pref.putColor("fastdraw.color.edit", COLOR_EDITEDFRAGMENT);
         Main.pref.putColor("fastdraw.color.fixed", COLOR_FIXED);
         Main.pref.putColor("fastdraw.color.normal", COLOR_NORMAL);
         Main.pref.putColor("fastdraw.color.select", COLOR_SELECTEDFRAGMENT);
         Main.pref.getColor("fastdraw.color.simplified", COLOR_SIMPLIFIED);
         Main.pref.putDouble("fastdraw.maxdist", maxDist);
         Main.pref.putDouble("fastdraw.epsilonmult", epsilonMult);
         //Main.pref.putDouble("fastdraw.deltasearch", deltaLatLon);
         Main.pref.putDouble("fastdraw.mindelta",minPixelsBetweenPoints);
         Main.pref.putDouble("fastdraw.startingEps",startingEps);
         Main.pref.putDouble("fastdraw.maxpkm",maxPointsPerKm);
         Main.pref.putInteger("fastdraw.pkmblocksize",pkmBlockSize);
         Main.pref.put("fastdraw.drawlastsegment",drawLastSegment);
         Main.pref.put("fastdraw.snapnodes", snapNodes);
         Main.pref.put("fastdraw.fixedclick", fixedClick);
         Main.pref.put("fastdraw.fixedspacebar", fixedSpacebar);
         Main.pref.putInteger("fastdraw.simplifymode", simplifyMode);
         Main.pref.putDouble("fastdraw.linewidth",(double)lineWidth);
         try {Main.pref.save();} catch (IOException e) {
             System.err.println(tr("Can not save preferences"));
         }
    }
}
