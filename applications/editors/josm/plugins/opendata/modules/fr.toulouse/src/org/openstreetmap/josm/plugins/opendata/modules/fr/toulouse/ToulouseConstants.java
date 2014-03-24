// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchConstants;

public interface ToulouseConstants extends FrenchConstants {
    
    /**
     * Sources and Refs
     */
    public static final String SOURCE_GRAND_TOULOUSE = "GrandToulouse";
    public static final String SOURCE_TOULOUSE_METROPOLE = "ToulouseMetropole";
    public static final String SOURCE_TISSEO = "Tisséo SMTC";
    public static final String REF_TOULOUSE_METROPOLE = "ref:FR:ToulouseMetropole";
    
    /**
     * Wiki
     */
    public static final String WIKI = "http://wiki.openstreetmap.org/wiki/Toulouse/ToulouseMetropoleData";

    /**
     * Portal
     */
    public static final String PORTAL = "http://data.toulouse-metropole.fr";

    /**
     * Icons
     */
    public static final String ICON_CROIX_16 = "data.fr.toulouse_16.png";
    public static final String ICON_CROIX_24 = "data.fr.toulouse_24.png";

    /**
     * NEPTUNE XML Schema modified to accept Tisséo files
     */
    public static final String TOULOUSE_NEPTUNE_XSD = "/neptune_toulouse/neptune.xsd";
}
