// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gui;

import java.awt.Color;

import org.openstreetmap.josm.plugins.elevation.ElevationHelper;

/**
 * Contains some extra predefined colors.
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public final class ElevationColors {

    private ElevationColors() {
        // Hide default constructor for utilities classes
    }

    public static Color EPDarkBlue = new Color(21, 59, 99);
    public static Color EPMidBlue = new Color(115, 140, 180);
    public static Color EPLightBlue = new Color(176, 187, 208);

    public static Color EPOrange = new Color(180, 140, 115);

    public static Color EPLightBeige = new Color(235, 235, 215);
    public static Color EPMidBeige = new Color(227, 222, 215);

    // TODO: Move to ColorMap.java or delete it
    static class ColorMapEntry {
        private final int ele; // limit
        private final Color color;

        ColorMapEntry(Color color, int ele) {
            super();
            this.color = color;
            this.ele = ele;
        }

        public int getEle() {
            return ele;
        }

        public Color getColor() {
            return color;
        }
    }

    private static ColorMapEntry[] colors = new ColorMapEntry[]{
            new ColorMapEntry(new Color(0, 128, 0), 0),
            new ColorMapEntry(new Color(156, 187, 105), 1),
            new ColorMapEntry(new Color(193, 208, 107), 100),
            new ColorMapEntry(new Color(244, 224, 100), 200),
            new ColorMapEntry(new Color(242, 216, 149), 500),
            new ColorMapEntry(new Color(234, 191, 104), 1000),
            new ColorMapEntry(new Color(207, 169, 96), 2000),
    };

    public static Color getElevationColor(double ele) {
        if (!ElevationHelper.isValidElevation(ele)) {
            return Color.white;
        }

        // TODO: Better color model...
        Color col = Color.green;

        if (ele < 0) {
            col = Color.blue;
        }

        if (ele > 200) {
            col = colors[1].getColor();
        }

        if (ele > 300) {
            col = colors[2].getColor();
        }

        if (ele > 400) {
            col = colors[3].getColor();
        }

        if (ele > 500) {
            col = Color.yellow;
        }

        if (ele > 750) {
            col = Color.orange;
        }

        if (ele > 1000) {
            col = Color.lightGray;
        }

        if (ele > 2000) {
            col = Color.darkGray;
        }

        return col;
    }
}
