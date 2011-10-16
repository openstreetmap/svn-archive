//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Main constants of JOSM Imagery XML Bounds plugin.
 * @author Don-vip
 *
 */
public interface XmlBoundsConstants {

	/**
	 * Plugin version.
	 */
	public static final String PLUGIN_VERSION = "1.2";
	
	/**
	 * XML namespace for JOSM Imagery schema.
	 */
	public static final String XML_NAMESPACE = "http://josm.openstreetmap.de/maps-1.0";

    /**
     * XML file location.
     */
    public static final String XML_LOCATION = "http://josm.openstreetmap.de/maps";

	/**
	 * XML Schema
	 */
	public static final String XML_SCHEMA = "resource://data/maps.xsd";
	
    /**
     * XML tags
     */
    public static final String XML_NAME = "name";
    public static final String XML_TYPE = "type";
    public static final String XML_URL = "url";
    public static final String XML_DEFAULT = "default";
    public static final String XML_EULA = "eula";
    public static final String XML_ATTR_TEXT = "attribution-text";
    public static final String XML_ATTR_URL = "attribution-url";
    public static final String XML_TERMS_TEXT = "terms-of-use-text";
    public static final String XML_TERMS_URL = "terms-of-use-url";
    public static final String XML_PROJECTIONS = "projections";
    public static final String XML_MAX_ZOOM = "max-zoom";
    public static final String XML_MIN_ZOOM = "min-zoom";
    public static final String XML_COUNTRY_CODE = "country-code";
    public static final String XML_LOGO_IMAGE = "logo-image";// TODO
    public static final String XML_LOGO_URL = "logo-url";

    /**
     * Prefix used in front of OSM keys.
     */
    public static final String PREFIX = "imagery:";

	/**
	 * OSM keys, equivalent to those used in XML schema, but prefixed (except for name).
	 */
	public static final String KEY_NAME = XML_NAME;
	public static final String KEY_TYPE = PREFIX + XML_TYPE;
	public static final String KEY_URL = PREFIX + XML_URL;
	public static final String KEY_DEFAULT = PREFIX + XML_DEFAULT;
	public static final String KEY_EULA = PREFIX + XML_EULA;
	public static final String KEY_ATTR_TEXT = PREFIX + XML_ATTR_TEXT;
	public static final String KEY_ATTR_URL = PREFIX + XML_ATTR_URL;
    public static final String KEY_TERMS_TEXT = PREFIX + XML_TERMS_TEXT;
	public static final String KEY_TERMS_URL = PREFIX + XML_TERMS_URL;
	public static final String KEY_PROJECTIONS = PREFIX + XML_PROJECTIONS;
	public static final String KEY_MAX_ZOOM = PREFIX + XML_MAX_ZOOM;
	public static final String KEY_MIN_ZOOM = PREFIX + XML_MIN_ZOOM;
	public static final String KEY_COUNTRY_CODE = PREFIX + XML_COUNTRY_CODE;
    public static final String KEY_LOGO_IMAGE = PREFIX + XML_LOGO_IMAGE;// TODO
    public static final String KEY_LOGO_URL = PREFIX + XML_LOGO_URL;
	
	/**
	 * File extension.
	 */
	public static final String EXTENSION = "imagery.xml";
	
	/**
	 * File encoding.
	 */
	public static final String ENCODING = "UTF-8";
	
	/**
	 * File filter used in import/export dialogs.
	 */
	public static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION, tr("Imagery XML Files") + " (*."+EXTENSION+")");

	/**
	 * Plugin icons.
	 */
	public static ImageIcon XML_ICON_16 = ImageProvider.get("xml_16.png");
	public static ImageIcon XML_ICON_24 = ImageProvider.get("xml_24.png");
}
