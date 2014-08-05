// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.licenses;

import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ODbL extends License {
	
    //public static final String URL            = "http://opendatacommons.org/licenses/odbl/1-0";
    //public static final String SUMMARY_URL    = "http://opendatacommons.org/licenses/odbl/summary";
    //public static final String URL_FR         = "http://vvlibri.org/fr/licence/odbl/10/fr/legalcode#La_Licence_ODbL";
    //public static final String SUMMARY_URL_FR = "http://vvlibri.org/fr/licence/odbl/10/fr";
    
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
