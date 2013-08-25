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
 *  Class HgtReader reads data from SRTM HGT files. Currently this the routines are restricted to a resolution of 3 arc seconds.
 *  @author Oliver Wieland <oliver.wieland@online.de>
 */
public class HgtReader {
    public static final String HGT_EXT = ".hgt";
    
    // alter these values for different SRTM resolutions
    public static final int HGT_RES = 3; // resolution in arc seconds
    public static final int HGT_ROW_LENGTH = 1201; // number of elevation values per line
    
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
		 // Try all other resource directories
	        for (String location : Main.pref.getAllPossiblePreferenceDirs()) {	            
	    		String fullPath = new File(location + File.separator + "elevation", file).getPath();
	    			  
	    		System.out.println("Search in " + location);
    	    		File f = new File(fullPath);
    	    		if (f.exists()) {
    	    		    // nope: read HGT file...
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
	    // no problem... file not there
	    return Double.NaN;
	} catch (Exception ioe) {
	    // oops...
	    ioe.printStackTrace();	    
	    // fallback
	    return Double.NaN;	    
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
	    return Double.NaN;
	}
	
	// see http://gis.stackexchange.com/questions/43743/how-to-extract-elevation-from-hgt-file
	double fLat = frac(coor.lat()) * 60;
	double fLon = frac(coor.lon()) * 60;
	
	
	int row = (int)Math.round(fLat * 60 / HGT_RES); 
	int col = (int)Math.round(fLon * 60 / HGT_RES);
	
	row = HGT_ROW_LENGTH - row;
	int cell = (HGT_ROW_LENGTH*  (row - 1)) + col;

	return sb.get(cell);
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
