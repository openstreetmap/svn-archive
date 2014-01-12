/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.elevation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Provides a set of color maps to map an elevation value to a color.
 * @author Olli
 *
 */
public class ColorMap {
    private List<ColorMapEntry> colorList;
    private String name;
    private static HashMap<String, ColorMap> colorMaps;

    static {
	colorMaps = new HashMap<String, ColorMap>();
    }

    // Private ctor to enforce use of create
    private ColorMap() {
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /**
     * Gets the color according to the given elevation value.
     *
     * @param elevation the elevation
     * @return the color
     */
    public Color getColor(int elevation) {
	// empty color map?
	if (colorList == null || colorList.size() == 0) {
	    return Color.white;
	}

	// out of range?
	if (elevation < colorList.get(0).ele) {
	    return colorList.get(0).getColor();
	}

	int last = colorList.size() - 1;
	if (elevation > colorList.get(last).ele) {
	    return colorList.get(last).getColor();
	}

	// find elevation section
	for (int i = 0; i < last; i++) {
	    ColorMapEntry e1 = colorList.get(i);
	    ColorMapEntry e2 = colorList.get(i + 1);

	    // elevation within range?
	    if (e1.getEle() <= elevation && e2.getEle() >= elevation) {

		// interpolate color between both
		double val = (elevation - e1.getEle()) / (double)(e2.getEle() - e1.getEle());
		return interpolate(e1.getColor(), e2.getColor(), val);
	    }
	}

	// here we should never end!
	throw new RuntimeException("Inconsistent color map - found no entry for elevation " + elevation);
    }


    /**
     * Gets the color map with the given name.
     *
     * @param name the name
     * @return the map or <code>null</code>, if no such map exists
     */
    public static ColorMap getMap(String name) {
	if (colorMaps.containsKey(name)) {
	    return colorMaps.get(name);
	}
	return null;
    }

    /**
     * Gets the number of available color maps.
     *
     * @return the int
     */
    public static int size() {
	return colorMaps != null ? colorMaps.size() : 0;
    }


    /**
     * Gets the available color map names.
     *
     * @param name the name
     * @return the map or <code>null</code>, if no such map exists
     */
    public static String[] getNames() {
	return colorMaps.keySet().toArray(new String[size()]);
    }

    private static void registerColorMap(ColorMap newMap) {
	CheckParameterUtil.ensureParameterNotNull(newMap);
	colorMaps.put(newMap.getName(), newMap);
    }

    public static void unregisterColorMap(String name) {
	if (colorMaps.containsKey(name)) {
	    colorMaps.remove(name);
	}
    }

    public static Color interpolate(java.awt.Color c1, java.awt.Color c2, double ratio) {
	double r1 = 1 -ratio;
	// clip
	if (r1 < 0) r1 = 0d;
	if (r1 > 1) r1 = 1d;
	double r2 = 1 - r1;

	int r = (int) Math.round((r1 * c1.getRed()) + (r2 * c2.getRed()));
	int g = (int) Math.round((r1 * c1.getGreen()) + (r2 * c2.getGreen()));
	int b = (int) Math.round((r1 * c1.getBlue()) + (r2 * c2.getBlue()));
	return new Color(r, g, b);
    }

    /**
     * Creates a color map using the given colors/elevation values.
     * Both arrays must have same length.
     *
     * @param name the name of the color map
     * @param colors the array containing the colors
     * @param ele the elevation values
     * @return the color map
     */
    public static ColorMap create(String name, Color[] colors, int[] ele) {
	CheckParameterUtil.ensureParameterNotNull(colors);
	CheckParameterUtil.ensureParameterNotNull(ele);

	if (colors.length != ele.length) {
	    throw new IllegalArgumentException("Arrays colors and ele must have same length: " + colors.length + " vs " + ele.length);
	}

	ColorMap map = new ColorMap();
	map.colorList = new ArrayList<ColorMap.ColorMapEntry>();
	map.name = name;
	for (int i = 0; i < ele.length; i++) {
	    map.colorList.add(map.new ColorMapEntry(colors[i], ele[i]));
	}

	// sort by elevation
	Collections.sort(map.colorList);

	registerColorMap(map);
	return map;
    }


    class ColorMapEntry implements Comparable<ColorMapEntry> {
	private final int ele; // limit
	private final Color color;

	public ColorMapEntry(Color color, int ele) {
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

	@Override
	public int compareTo(ColorMapEntry o) {
	    return this.ele - o.ele;
	}
    }
}
