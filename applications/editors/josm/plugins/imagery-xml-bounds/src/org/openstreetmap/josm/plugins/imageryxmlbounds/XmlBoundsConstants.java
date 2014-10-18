// License: GPL. For details, see LICENSE file.
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
    public static final String PLUGIN_VERSION = "1.3";

    /**
     * XML namespace for JOSM Imagery schema.
     */
    public static final String XML_NAMESPACE = "http://josm.openstreetmap.de/maps-1.0";

    /**
     * XML file location.
     */
    public static final String XML_LOCATION = "https?://josm.openstreetmap.de/maps";

    /**
     * XML Schema
     */
    public static final String XML_SCHEMA = "resource://data/maps.xsd";

    /** XML name tag */
    public static final String XML_NAME = "name";
    /** XML type tag */
    public static final String XML_TYPE = "type";
    /** XML url tag */
    public static final String XML_URL = "url";
    /** XML default tag */
    public static final String XML_DEFAULT = "default";
    /** XML eula tag */
    public static final String XML_EULA = "eula";
    /** XML attribution-text tag */
    public static final String XML_ATTR_TEXT = "attribution-text";
    /** XML attribution-url tag */
    public static final String XML_ATTR_URL = "attribution-url";
    /** XML terms-of-use-text tag */
    public static final String XML_TERMS_TEXT = "terms-of-use-text";
    /** XML terms-of-use-url tag */
    public static final String XML_TERMS_URL = "terms-of-use-url";
    /** XML projections tag */
    public static final String XML_PROJECTIONS = "projections";
    /** XML max-zoom tag */
    public static final String XML_MAX_ZOOM = "max-zoom";
    /** XML min-zoom tag */
    public static final String XML_MIN_ZOOM = "min-zoom";
    /** XML country-code tag */
    public static final String XML_COUNTRY_CODE = "country-code";
    /** XML logo-image tag */
    public static final String XML_LOGO_IMAGE = "logo-image";
    /** XML logo-url tag */
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
    public static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(
            EXTENSION, EXTENSION, tr("Imagery XML Files") + " (*."+EXTENSION+")");

    /**
     * Plugin 16x16 icon.
     */
    public static ImageIcon XML_ICON_16 = ImageProvider.get("xml_16.png");

    /**
     * Plugin 24x24 icon.
     */
    public static ImageIcon XML_ICON_24 = ImageProvider.get("xml_24.png");
}
