// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fastdraw;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Stroke;
import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Logging;

public class FDSettings {
    public NamedColorProperty COLOR_FIXED = new NamedColorProperty("fastdraw.color.delete", Color.red);
    public NamedColorProperty COLOR_NORMAL = new NamedColorProperty("fastdraw.color.edit", Color.orange);
    public NamedColorProperty COLOR_DELETE = new NamedColorProperty("fastdraw.color.fixed", Color.green);
    public NamedColorProperty COLOR_SELECTEDFRAGMENT = new NamedColorProperty("fastdraw.color.normal", Color.red);
    public NamedColorProperty COLOR_EDITEDFRAGMENT = new NamedColorProperty("fastdraw.color.select", Color.blue);
    public NamedColorProperty COLOR_SIMPLIFIED = new NamedColorProperty("fastdraw.color.simplified", Color.orange);

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

    public boolean allowEditExistingWays;

    public boolean drawClosed;
    public int simplifyMode;
    public String autoTags;
    public Stroke normalStroke;
    public Stroke simplifiedStroke;
    public Stroke deleteStroke;
    public int dotSize;
    public int bigDotSize;

    public void loadPrefs() {
        normalStroke = GuiHelper.getCustomizedStroke(Main.pref.get("fastdraw.stroke.normal", "2"));
        deleteStroke = GuiHelper.getCustomizedStroke(Main.pref.get("fastdraw.stroke.delete", "3"));
        simplifiedStroke = GuiHelper.getCustomizedStroke(Main.pref.get("fastdraw.stroke.simplified", "2"));

        bigDotSize = Main.pref.getInt("fastdraw.point.bigsize", 7);
        dotSize = Main.pref.getInt("fastdraw.point.normalsize", 5);

        maxDist = Main.pref.getDouble("fastdraw.maxdist", 5);
        epsilonMult = Main.pref.getDouble("fastdraw.epsilonmult", 1.1);
        //deltaLatLon = Main.pref.getDouble("fastdraw.deltasearch", 0.01);
        minPixelsBetweenPoints = Main.pref.getDouble("fastdraw.mindelta", 20);
        startingEps = Main.pref.getDouble("fastdraw.startingEps", 5);
        maxPointsPerKm = Main.pref.getDouble("fastdraw.maxpkm", 150);
        pkmBlockSize = Main.pref.getInt("fastdraw.pkmblocksize", 10);
        drawLastSegment = Main.pref.getBoolean("fastdraw.drawlastsegment", true);
        snapNodes = Main.pref.getBoolean("fastdraw.snapnodes", true);
        fixedClick = Main.pref.getBoolean("fastdraw.fixedclick", false);
        fixedSpacebar = Main.pref.getBoolean("fastdraw.fixedspacebar", false);
        drawClosed = Main.pref.getBoolean("fastdraw.drawclosed", false);
        simplifyMode = Main.pref.getInt("fastdraw.simplifymode", 0) % 3;
        allowEditExistingWays = Main.pref.getBoolean("fastdraw.alloweditexisting", false);

        autoTags = Main.pref.get("fastdraw.autotags");
    }

    public void savePrefs() {
        Main.pref.putDouble("fastdraw.maxdist", maxDist);
        Main.pref.putDouble("fastdraw.epsilonmult", epsilonMult);
        //Main.pref.putDouble("fastdraw.deltasearch", deltaLatLon);
        Main.pref.putDouble("fastdraw.mindelta", minPixelsBetweenPoints);
        Main.pref.putDouble("fastdraw.startingEps", startingEps);
        Main.pref.putDouble("fastdraw.maxpkm", maxPointsPerKm);
        Main.pref.putInt("fastdraw.pkmblocksize", pkmBlockSize);
        Main.pref.putBoolean("fastdraw.drawlastsegment", drawLastSegment);
        Main.pref.putBoolean("fastdraw.snapnodes", snapNodes);
        Main.pref.putBoolean("fastdraw.fixedclick", fixedClick);
        Main.pref.putBoolean("fastdraw.fixedspacebar", fixedSpacebar);
        Main.pref.putBoolean("fastdraw.drawclosed", drawClosed);
        Main.pref.putInt("fastdraw.simplifymode", simplifyMode);
        Main.pref.put("fastdraw.autotags", autoTags);
        Main.pref.putBoolean("fastdraw.alloweditexisting", allowEditExistingWays);
        try {
            Main.pref.save();
        } catch (IOException e) {
            Logging.error(tr("Can not save preferences"));
        }
    }
}
