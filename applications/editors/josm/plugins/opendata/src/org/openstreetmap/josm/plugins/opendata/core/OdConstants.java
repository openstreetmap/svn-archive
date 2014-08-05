// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.opendata.core.io.LambertCC9ZonesProjectionPatterns;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionPatterns;

public abstract class OdConstants {
	
	/**
	 * Encodings
	 */
	public static final String UTF8 = "UTF-8";
	public static final String ISO8859_15 = "ISO-8859-15";
	public static final String CP850 = "Cp850";
    public static final String CP1251 = "Cp1251";
	public static final String CP1252 = "Cp1252";
	public static final String MAC_ROMAN = "MacRoman";
	
	/**
	 * Patterns
	 */
	public static final String PATTERN_LANG = "{lang}";
	
	/**
	 * Preferences
	 */
/*	public static final String PREF_COORDINATES = "opendata.coordinates";
	public static final String VALUE_CC9ZONES = "cc9zones";
	public static final String VALUE_WGS84 = "wgs84";*/

	public static final String PREF_OAPI = "opendata.oapi";
    public static final String DEFAULT_OAPI = "http://www.overpass-api.de/api/interpreter?";
    
	public static final String PREF_XAPI = "opendata.xapi";
    public static final String DEFAULT_XAPI = "http://www.overpass-api.de/api/xapi?";

	public static final String PREF_RAWDATA = "opendata.rawdata";
    public static final boolean DEFAULT_RAWDATA = false;

	public static final String PREF_MAXDISTANCE = "opendata.maxdistance";
    public static final double DEFAULT_MAXDISTANCE = 10;

    public static final String PREF_TOLERANCE = "opendata.spreadsheet.tolerance";
    public static final double DEFAULT_TOLERANCE = 0.1;

    public static final String PREF_MODULES = "opendata.modules";
    public static final String PREF_MODULES_SITES = "opendata.modules.sites";
    public final static String OSM_SITE = "http://svn.openstreetmap.org/applications/editors/josm/plugins/opendata/";
    public final static String[] DEFAULT_MODULE_SITES = {OSM_SITE + "modules.txt%<?modules=>"};

    public static final String PREF_CRS_COMPARISON_TOLERANCE = "opendata.crs.comparison_tolerance";
    public static final Double DEFAULT_CRS_COMPARISON_TOLERANCE = 0.00000001;

    public static final String PREF_CRS_COMPARISON_DEBUG = "opendata.crs.comparison_debug";

	/**
	 * Icons
	 */
	public static final String ICON_CORE_16 = "o16.png";
	public static final String ICON_CORE_24 = "o24.png";
	public static final String ICON_CORE_48 = "o48.png";
	
	public static final String ICON_OSM_16 = "osm16.png";
	public static final String ICON_OSM_24 = "osm24.png";

	public static final String ICON_AGREEMENT_24 = "agreement24.png";
	public static final String ICON_EMPTY_24 = "empty24.png";

	public static final String ICON_LOOL_48 = "lool48.png";

    /**
     * File extensions.
     */
    public static final String CSV_EXT = "csv";
    public static final String KML_EXT = "kml";
    public static final String KMZ_EXT = "kmz";
    public static final String GML_EXT = "gml";
    public static final String XLS_EXT = "xls";
    public static final String ODS_EXT = "ods";
    public static final String SHP_EXT = "shp";
    public static final String MIF_EXT = "mif";
    public static final String TAB_EXT = "tab";
    public static final String MAPCSS_EXT = "mapcss";
    public static final String ZIP_EXT = "zip";
    public static final String SEVENZIP_EXT = "7z";
    public static final String JAR_EXT = "jar";
    public static final String XML_EXT = "xml";
    
    /**
     * Protocols
     */
    public static final String PROTO_FILE = "file://";
    public static final String PROTO_RSRC = "resource://";
    
    /**
     * Coordinates fields
     */
    public static final String X_STRING = "X|LON|LONGI|.*LONGITUDE.*|EASTING";
    public static final String Y_STRING = "Y|LAT|LATI|.*LATITUDE.*|NORTHING";
    
    // The list of all ProjectionPatterns (filled at each constructor call)
    public static final Collection<ProjectionPatterns> PROJECTIONS = new ArrayList<>();
    
    public static final ProjectionPatterns PRJ_WGS84 = new ProjectionPatterns("GPS|WGS84|°décimaux", Projections.getProjectionByCode("EPSG:4326"));
    public static final ProjectionPatterns PRJ_LAMBERT_93 = new ProjectionPatterns("LAMB93|L93", Projections.getProjectionByCode("EPSG:2154"));
    public static final ProjectionPatterns PRJ_LAMBERT_CC_9_ZONES = new LambertCC9ZonesProjectionPatterns("LAMBZ|CC(42|43|44|45|46|47|48|49|50)");

    public static final ProjectionPatterns PRJ_LAMBERT_1972 = new ProjectionPatterns("LAMB72|LAMB1972", Projections.getProjectionByCode("EPSG:31370"));
    public static final ProjectionPatterns PRJ_LAMBERT_2008 = new ProjectionPatterns("LAMB08|LAMB2008", Projections.getProjectionByCode("EPSG:3812"));

    // Must always be declared last
    public static final ProjectionPatterns PRJ_UNKNOWN = new ProjectionPatterns("");

    /**
     * Resources
     */
    public static final String RESOURCE_PATH = "/resources/org/openstreetmap/josm/plugins/opendata/core/resources/";
    public static final String DICTIONARY_FR = RESOURCE_PATH+"dictionary.fr.csv";
}
