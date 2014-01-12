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
package org.openstreetmap.josm.plugins.opendata.core.licenses;

import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ODbL extends License implements OdConstants {
	
    //public static final String URL            = "http://opendatacommons.org/licenses/odbl/1-0";
    //public static final String SUMMARY_URL    = "http://opendatacommons.org/licenses/odbl/summary";
    //public static final String URL_FR         = "http://vvlibri.org/fr/licence/odbl/10/fr/legalcode#La_Licence_ODbL";
    //public static final String SUMMARY_URL_FR = "http://vvlibri.org/fr/licence/odbl/10/fr";
    
	public ODbL() {
		for (String lang : new String[]{"", "fr"}) {
			if (lang.isEmpty()) {
				setURL(ODbL.class.getResource(RESOURCE_PATH+"odbl-1.0.htm"));
				setSummaryURL(ODbL.class.getResource(RESOURCE_PATH+"odbl-summary-1.0.htm"));
			} else {
				setURL(ODbL.class.getResource(RESOURCE_PATH+"odbl-1.0-"+lang+".htm"), lang);
				setSummaryURL(ODbL.class.getResource(RESOURCE_PATH+"odbl-summary-1.0-"+lang+".htm"), lang);
			}
		}
	}
}
