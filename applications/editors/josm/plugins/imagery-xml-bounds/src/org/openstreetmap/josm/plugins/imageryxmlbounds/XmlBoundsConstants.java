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
	public static final String PLUGIN_VERSION = "1.0";
	
	/**
	 * XML namespace for JOSM Imagery schema.
	 */
	public static final String XML_NAMESPACE = "http://josm.openstreetmap.de/maps-1.0";
	
	/**
	 * XML Schema
	 */
	public static final String XML_SCHEMA = "resource://data/maps.xsd";
	
	/**
	 * Prefix used in front of OSM keys.
	 */
	public static final String PREFIX = "imagery:";
	
	/**
	 * OSM keys, equivalent to those used in XML schema, but prefixed (except for name).
	 */
	public static final String KEY_NAME = "name";
	public static final String KEY_TYPE = PREFIX + "type";
	public static final String KEY_URL = PREFIX + "url";
	public static final String KEY_DEFAULT = PREFIX + "default";
	public static final String KEY_EULA = PREFIX + "eula";
	public static final String KEY_ATTR_TEXT = PREFIX + "attribution-text";
	public static final String KEY_ATTR_URL = PREFIX + "attribution-url";
	public static final String KEY_TERMS_URL = PREFIX + "terms-of-use-url";
	public static final String KEY_PROJECTIONS = PREFIX + "projections";
	public static final String KEY_MAX_ZOOM = PREFIX + "max-zoom";
	public static final String KEY_MIN_ZOOM = PREFIX + "min-zoom";
	public static final String KEY_COUNTRY_CODE = PREFIX + "country-code";
	
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
