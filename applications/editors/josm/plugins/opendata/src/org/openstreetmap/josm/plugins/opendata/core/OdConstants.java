//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.projection.BelgianLambert1972;
import org.openstreetmap.josm.data.projection.BelgianLambert2008;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.data.projection.Lambert93;
import org.openstreetmap.josm.plugins.opendata.core.io.LambertCC9ZonesProjectionPatterns;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionPatterns;

public interface OdConstants {
	
	/**
	 * Encodings
	 */
	public static final String UTF8 = "UTF-8";
	public static final String ISO8859_15 = "ISO-8859-15";
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

    public static final String PREF_MODULES = "opendata.modules";
    public static final String PREF_MODULES_SITES = "opendata.modules.sites";
    public final static String GOOGLE_SITE = "http://josm-toulouse-data.googlecode.com/svn/trunk/";
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

    /**
     * File extensions.
     */
    public static final String CSV_EXT = "csv";
    public static final String KML_EXT = "kml";
    public static final String KMZ_EXT = "kmz";
    public static final String XLS_EXT = "xls";
    public static final String ODS_EXT = "ods";
    public static final String SHP_EXT = "shp";
    public static final String MIF_EXT = "mif";
    public static final String TAB_EXT = "tab";
    public static final String MAPCSS_EXT = "mapcss";
    public static final String ZIP_EXT = "zip";
    public static final String JAR_EXT = "jar";
    public static final String XML_EXT = "xml";
    
    /**
     * Protocols
     */
    public static final String PROTO_FILE = "file://";
    public static final String PROTO_RSRC = "resource://";
    
    /**
     * File filter used in import/export dialogs.
     */
    public static final ExtensionFileFilter CSV_FILE_FILTER = new ExtensionFileFilter(CSV_EXT, CSV_EXT, tr("CSV files") + " (*."+CSV_EXT+")");
    public static final ExtensionFileFilter XLS_FILE_FILTER = new ExtensionFileFilter(XLS_EXT, XLS_EXT, tr("XLS files") + " (*."+XLS_EXT+")");
    public static final ExtensionFileFilter ODS_FILE_FILTER = new ExtensionFileFilter(ODS_EXT, ODS_EXT, tr("ODS files") + " (*."+ODS_EXT+")");
    public static final ExtensionFileFilter SHP_FILE_FILTER = new ExtensionFileFilter(SHP_EXT, SHP_EXT, tr("Shapefiles") + " (*."+SHP_EXT+")");
    public static final ExtensionFileFilter MIF_TAB_FILE_FILTER = new ExtensionFileFilter(MIF_EXT+","+TAB_EXT, MIF_EXT, tr("MapInfo files") + " (*."+MIF_EXT+",*."+TAB_EXT+")");
    public static final ExtensionFileFilter KML_KMZ_FILE_FILTER = new ExtensionFileFilter(KML_EXT+","+KMZ_EXT, KMZ_EXT, tr("KML/KMZ files") + " (*."+KML_EXT+",*."+KMZ_EXT+")");
    public static final ExtensionFileFilter ZIP_FILE_FILTER = new ExtensionFileFilter(ZIP_EXT, ZIP_EXT, tr("Zip Files") + " (*."+ZIP_EXT+")");
    public static final ExtensionFileFilter XML_FILE_FILTER = new ExtensionFileFilter(XML_EXT, XML_EXT, tr("OpenData XML files") + " (*."+XML_EXT+")");
    
    /**
     * Coordinates fields
     */
    public static final String X_STRING = "X|LON|LONGI|LONGITUDE|EASTING";
    public static final String Y_STRING = "Y|LAT|LATI|LATITUDE|NORTHING";
    
    // The list of all ProjectionPatterns (filled at each constructor call)
    public static final Collection<ProjectionPatterns> PROJECTIONS = new ArrayList<ProjectionPatterns>();
    
    public static final ProjectionPatterns PRJ_UNKNOWN = new ProjectionPatterns("");
    public static final ProjectionPatterns PRJ_WGS84 = new ProjectionPatterns("GPS|WGS84|°décimaux", new Epsg4326());
    public static final ProjectionPatterns PRJ_LAMBERT_93 = new ProjectionPatterns("LAMB93|L93", new Lambert93());
    public static final ProjectionPatterns PRJ_LAMBERT_CC_9_ZONES = new LambertCC9ZonesProjectionPatterns("LAMBZ|CC(42|43|44|45|46|47|48|49|50)");

    public static final ProjectionPatterns PRJ_LAMBERT_1972 = new ProjectionPatterns("LAMB72|LAMB1972", new BelgianLambert1972());
    public static final ProjectionPatterns PRJ_LAMBERT_2008 = new ProjectionPatterns("LAMB08|LAMB2008", new BelgianLambert2008());

    /**
     * KML tags
     */
    public static final String KML_PLACEMARK   = "Placemark";
    public static final String KML_NAME	       = "name";
    public static final String KML_COLOR       = "color";
    public static final String KML_SIMPLE_DATA = "SimpleData";
    public static final String KML_LINE_STRING = "LineString";
    public static final String KML_POINT       = "Point";
    public static final String KML_POLYGON     = "Polygon";
    public static final String KML_OUTER_BOUND = "outerBoundaryIs";
    public static final String KML_INNER_BOUND = "innerBoundaryIs";
    public static final String KML_LINEAR_RING = "LinearRing";
    public static final String KML_COORDINATES = "coordinates";
    
    /**
     * Resources
     */
    public static final String DICTIONARY_FR = "/org/openstreetmap/josm/plugins/opendata/core/resources/dictionary.fr.csv";
}
