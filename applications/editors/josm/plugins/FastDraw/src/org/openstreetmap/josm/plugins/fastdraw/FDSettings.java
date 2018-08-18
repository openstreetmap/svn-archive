// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fastdraw;

import java.awt.Color;
import java.awt.Stroke;

import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.spi.preferences.Config;

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
        normalStroke = GuiHelper.getCustomizedStroke(Config.getPref().get("fastdraw.stroke.normal", "2"));
        deleteStroke = GuiHelper.getCustomizedStroke(Config.getPref().get("fastdraw.stroke.delete", "3"));
        simplifiedStroke = GuiHelper.getCustomizedStroke(Config.getPref().get("fastdraw.stroke.simplified", "2"));

        bigDotSize = Config.getPref().getInt("fastdraw.point.bigsize", 7);
        dotSize = Config.getPref().getInt("fastdraw.point.normalsize", 5);

        maxDist = Config.getPref().getDouble("fastdraw.maxdist", 5);
        epsilonMult = Config.getPref().getDouble("fastdraw.epsilonmult", 1.1);
        //deltaLatLon = Config.getPref().getDouble("fastdraw.deltasearch", 0.01);
        minPixelsBetweenPoints = Config.getPref().getDouble("fastdraw.mindelta", 20);
        startingEps = Config.getPref().getDouble("fastdraw.startingEps", 5);
        maxPointsPerKm = Config.getPref().getDouble("fastdraw.maxpkm", 150);
        pkmBlockSize = Config.getPref().getInt("fastdraw.pkmblocksize", 10);
        drawLastSegment = Config.getPref().getBoolean("fastdraw.drawlastsegment", true);
        snapNodes = Config.getPref().getBoolean("fastdraw.snapnodes", true);
        fixedClick = Config.getPref().getBoolean("fastdraw.fixedclick", false);
        fixedSpacebar = Config.getPref().getBoolean("fastdraw.fixedspacebar", false);
        drawClosed = Config.getPref().getBoolean("fastdraw.drawclosed", false);
        simplifyMode = Config.getPref().getInt("fastdraw.simplifymode", 0) % 3;
        allowEditExistingWays = Config.getPref().getBoolean("fastdraw.alloweditexisting", false);

        autoTags = Config.getPref().get("fastdraw.autotags");
    }

    public void savePrefs() {
        Config.getPref().putDouble("fastdraw.maxdist", maxDist);
        Config.getPref().putDouble("fastdraw.epsilonmult", epsilonMult);
        //Config.getPref().putDouble("fastdraw.deltasearch", deltaLatLon);
        Config.getPref().putDouble("fastdraw.mindelta", minPixelsBetweenPoints);
        Config.getPref().putDouble("fastdraw.startingEps", startingEps);
        Config.getPref().putDouble("fastdraw.maxpkm", maxPointsPerKm);
        Config.getPref().putInt("fastdraw.pkmblocksize", pkmBlockSize);
        Config.getPref().putBoolean("fastdraw.drawlastsegment", drawLastSegment);
        Config.getPref().putBoolean("fastdraw.snapnodes", snapNodes);
        Config.getPref().putBoolean("fastdraw.fixedclick", fixedClick);
        Config.getPref().putBoolean("fastdraw.fixedspacebar", fixedSpacebar);
        Config.getPref().putBoolean("fastdraw.drawclosed", drawClosed);
        Config.getPref().putInt("fastdraw.simplifymode", simplifyMode);
        Config.getPref().put("fastdraw.autotags", autoTags);
        Config.getPref().putBoolean("fastdraw.alloweditexisting", allowEditExistingWays);
    }
}
