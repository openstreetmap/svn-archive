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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.CheckParameterUtil;


/**
 *  Class HgtReader reads data from SRTM HGT files. Currently this class is restricted to a resolution of 3 arc seconds.
 *  
 *  SRTM data files are available at the <a href="http://dds.cr.usgs.gov/srtm/version2_1/SRTM3">NASA SRTM site</a>
 *  @author Oliver Wieland <oliver.wieland@online.de>
 */
public class HgtReader {
    private static final int SECONDS_PER_MINUTE = 60;

    public static final String HGT_EXT = ".hgt";
    
    // alter these values for different SRTM resolutions
    public static final int HGT_RES = 3; // resolution in arc seconds
    public static final int HGT_ROW_LENGTH = 1201; // number of elevation values per line
    public static final int HGT_VOID = -32768; // magic number which indicates 'void data' in HGT file
    
    private HashMap<String, ShortBuffer> cache = new HashMap<String, ShortBuffer>(); 
    
    public double getElevationFromHgt(LatLon coor) {
	try {
	    String file = getHgtFileName(coor);
	    // given area in cache?
	    if (!cache.containsKey(file)) {
			
		// fill initial cache value. If no file is found, then
		// we use it as a marker to indicate 'file has been searched
		// but is not there'
		cache.put(file, null);
		 // Try all resource directories
	        for (String location : Main.pref.getAllPossiblePreferenceDirs()) {	            
	    		String fullPath = new File(location + File.separator + "elevation", file).getPath();
    	    		File f = new File(fullPath);
    	    		if (f.exists()) {
    	    		    // found something: read HGT file...
    	    		    ShortBuffer data = readHgtFile(fullPath);    	    		    
    	    		    // ... and store result in cache
    	    		    cache.put(file, data);
    	    		    break;
    	    		}
	        }
	    } 
	    
	    // read elevation value
	    return readElevation(coor);
	} catch (FileNotFoundException e) {
	    System.err.println("Get elevation from HGT " + coor + " failed: => " + e.getMessage());
	    // no problem... file not there
	    return ElevationHelper.NO_ELEVATION;
	} catch (Exception ioe) {
	    // oops...
	    ioe.printStackTrace(System.err);	    
	    // fallback
	    return ElevationHelper.NO_ELEVATION;	    
	} 
    }
    
    @SuppressWarnings("resource")
    private ShortBuffer readHgtFile(String file) throws Exception {
	CheckParameterUtil.ensureParameterNotNull(file);

	FileChannel fc = null;
	ShortBuffer sb = null;
	try {	    
	    // Eclipse complains here about resource leak on 'fc' - even with 'finally' clause???
	    fc = new FileInputStream(file).getChannel();
	    // choose the right endianness
	    
	    ByteBuffer bb = ByteBuffer.allocateDirect((int) fc.size());
	    while (bb.remaining() > 0) fc.read(bb);
	    
	    bb.flip();
	    //sb = bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();	
	    sb = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
	} finally {
	    if (fc != null) fc.close();
	}
		    
	return sb;	
    }
    
    /**
     * Reads the elevation value for the given coordinate.
     *
     * See also <a href="http://gis.stackexchange.com/questions/43743/how-to-extract-elevation-from-hgt-file">stackexchange.com</a>
     * @param coor the coordinate to get the elevation data for
     * @return the elevation value or <code>Double.NaN</code>, if no value is present
     */
    public double readElevation(LatLon coor) {
	String tag = getHgtFileName(coor);
		
	ShortBuffer sb = cache.get(tag);
	
	if (sb == null) {
	    return ElevationHelper.NO_ELEVATION;
	}
	
	// see http://gis.stackexchange.com/questions/43743/how-to-extract-elevation-from-hgt-file
	double fLat = frac(coor.lat()) * SECONDS_PER_MINUTE;
	double fLon = frac(coor.lon()) * SECONDS_PER_MINUTE;
	
	// compute offset within HGT file
	int row = (int)Math.round(fLat * SECONDS_PER_MINUTE / HGT_RES); 
	int col = (int)Math.round(fLon * SECONDS_PER_MINUTE / HGT_RES);
	
	row = HGT_ROW_LENGTH - row;
	int cell = (HGT_ROW_LENGTH*  (row - 1)) + col;
	
	//System.out.println("Read SRTM elevation data from row/col/cell " + row + "," + col + ", " + cell + ", " + sb.limit());

	// valid position in buffer?
	if (cell < sb.limit()) {
	    short ele = sb.get(cell);
	    //System.out.println("==> Read SRTM elevation data from row/col/cell " + row + "," + col + ", " + cell + " = " + ele);
	    // check for data voids 
	    if (ele == HGT_VOID) {
		return ElevationHelper.NO_ELEVATION;
	    } else {
		return ele;
	    }
	} else {
	    return ElevationHelper.NO_ELEVATION;
	}
    }
    
    /**
     * Gets the associated HGT file name for the given way point. Usually the
     * format is <tt>[N|S]nn[W|E]mmm.hgt</tt> where <i>nn</i> is the integral latitude
     * without decimals and <i>mmm</i> is the longitude.
     *
     * @param latLon the coordinate to get the filename for
     * @return the file name of the HGT file
     */
    public String getHgtFileName(LatLon latLon) {
	int lat = (int) latLon.lat();
	int lon = (int) latLon.lon();
	
	String latPref = "N";
	if (lat < 0) latPref = "S";
	
	String lonPref = "E";
	if (lon < 0) {
	    lonPref = "W";
	}
	
	return String.format("%s%02d%s%03d%s", latPref, lat, lonPref, lon, HGT_EXT);
    }
    
    public static double frac(double d) {
	long iPart;
	double fPart;

	// Get user input
	iPart = (long) d;
	fPart = d - iPart;
	return fPart;
    }
}
