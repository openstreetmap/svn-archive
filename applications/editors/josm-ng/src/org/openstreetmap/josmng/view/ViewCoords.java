/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view;

/**
 * Coordinates in the view space. They represent "northings and "eastings",
 * that is, latitude and longitude after view transformation, moreover
 * encoded as shift-dot integers for easy view scaling.
 * 
 * The precision of the coordinates is (depending on the actually used
 * projection) about 1/8.000.000 of a degree (a unit),
 * that is, degrees to nearly 7 valid digits, or 13mm on equator per unit.
 * 
 * The coordinates gets converted to screen coordinates by dividing with
 * current scale, where scale is the number of units per pixel. As the scale
 * is encoded in inverse values, large zooms can be coarse. Let's check this:
 * <table><tr><th>scale</th><th>pixel[m]</th><th>ruler[m]</th></tr>
 * <tr><th>1</th><td>0.0132</td><td>1.2</td></tr>
 * <tr><th>2</th><td>0.0265</td><td>2.4</td></tr>
 * <tr><th>3</th><td>0.0397</td><td>3.6</tr>
 * <tr><th>19</th><td>0.252</td><td>22.6</td></tr>
 * <tr><th>20</th><td>0.265</td><td>23.8</td></tr>
 * <tr><th>21</th><td>0.278</td><td>25</td></tr>
 * </table>
 * 
 * @author nenik
 */
public class ViewCoords {
    public static final int SCALE = 1 << 23;
    private int lon;
    private int lat;
    
    ViewCoords() {
    }   
    
    public ViewCoords(double lon, double lat, boolean flag) {
        setCoordinates(lon, lat);
        setFlag(flag);
    }
    
    public ViewCoords(int lon, int lat) {
        setCoordinates(lon, lat);
    }

    protected final void setCoordinates(double lon, double lat) {
        setCoordinates((int)(lon*SCALE), (int)(lat*SCALE));
        
    }
    
    protected final void setFlag(boolean flag) {
        lat = lat & ~(1<<30) | (flag ? 1<<30 : 0);
    }

    public void setCoordinates(ViewCoords from) {
        lon = from.lon;
        lat = lat & 0x40000000 | from.lat & 0xBFFFFFFF;
    }
    
    private void setCoordinates(int lon, int lat) {
        assert lon <= (180 << 23) && lon >= (-180 << 23);
        assert lat <= (90 << 23) && lat >= (-90 << 23) : "lat=" + (((double)lat)/SCALE);
        this.lon = lon;
        this.lat = lat;
    }

    
    public int getIntLon() {
        return lon;
    }

    public int getIntLat() {
        return lat<0 ? lat | 0x40000000 : lat & 0xBFFFFFFF;
    }

    protected boolean getFlag() {
        return (lat & (1<<30)) != 0;
    }
    
    public double getLon() {
        return ((double) getIntLon()) / SCALE;
    }

    public double getLat() {
        return ((double) getIntLat()) / SCALE;
    }

    /**
     * Computes a ViewCoords shifted by the difference between from
     * and to.
     * @param from First reference point
     * @param to Second reference point
     * @return a ViewCoords that is shifted from this ViewCoords the same way
     * as point <code>to</code> is from point <code>from</code>.
     */
    public final ViewCoords movedByDelta(ViewCoords from, ViewCoords to) {
        return new ViewCoords(getIntLon() + from.getIntLon() - to.getIntLon(),
                    getIntLat() + from.getIntLat() - to.getIntLat());
    }

    public @Override String toString() {
        return "[" + getLat() +"," + getLon() + "]";
    }
}
