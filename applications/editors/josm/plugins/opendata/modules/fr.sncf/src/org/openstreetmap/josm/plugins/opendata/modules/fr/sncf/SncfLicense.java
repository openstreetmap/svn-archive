// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf;

import java.net.MalformedURLException;

import org.openstreetmap.josm.plugins.opendata.core.licenses.License;

public class SncfLicense extends License {

	public SncfLicense() {
		try {
			setURL("http://test.data-sncf.com/licence", "fr");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
