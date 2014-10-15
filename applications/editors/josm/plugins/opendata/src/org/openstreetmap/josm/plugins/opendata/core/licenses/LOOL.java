// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.licenses;

import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class LOOL extends License {
    
    //public static final String URL_FR = "http://www.data.gouv.fr/Licence-Ouverte-Open-Licence";
    
    public LOOL() {
        setIcon(OdUtils.getImageIcon(OdConstants.ICON_LOOL_48, true));
        setURL(LOOL.class.getResource(OdConstants.RESOURCE_PATH+"Licence-Ouverte-Open-Licence-ENG.rtf"), "en");
        setURL(LOOL.class.getResource(OdConstants.RESOURCE_PATH+"Licence-Ouverte-Open-Licence.rtf"), "fr");
    }
}
