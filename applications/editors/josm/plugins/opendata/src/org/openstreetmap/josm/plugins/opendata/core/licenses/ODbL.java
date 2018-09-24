// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.licenses;

import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ODbL extends License {

    public ODbL() {
        for (String lang : new String[]{"", "fr"}) {
            if (lang.isEmpty()) {
                setURL(ODbL.class.getResource(OdConstants.RESOURCE_PATH+"odbl-1.0.htm"));
                setSummaryURL(ODbL.class.getResource(OdConstants.RESOURCE_PATH+"odbl-summary-1.0.htm"));
            } else {
                setURL(ODbL.class.getResource(OdConstants.RESOURCE_PATH+"odbl-1.0-"+lang+".htm"), lang);
                setSummaryURL(ODbL.class.getResource(OdConstants.RESOURCE_PATH+"odbl-summary-1.0-"+lang+".htm"), lang);
            }
        }
    }
}
