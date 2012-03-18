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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets;

import java.net.MalformedURLException;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.DataGouvFrConstants;

public abstract class DataGouvDataSetHandler extends FrenchDataSetHandler implements DataGouvFrConstants {
	
	public DataGouvDataSetHandler() {
		init(null, null);
	}

	public DataGouvDataSetHandler(String portalPath) {
		init(portalPath, null);
	}

	public DataGouvDataSetHandler(String portalPath, Projection singleProjection) {
		init(portalPath, singleProjection);
	}

	public DataGouvDataSetHandler(String portalPath, Projection singleProjection, String relevantTag) {
		super(relevantTag);
		init(portalPath, singleProjection);
	}

	public DataGouvDataSetHandler(String portalPath, String relevantTag) {
		super(relevantTag);
		init(portalPath, null);
	}

	private void init(String portalPath, Projection singleProjection) {
		setNationalPortalPath(portalPath);
		setSingleProjection(singleProjection);
		setLicense(License.LOOL);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_DATAGOUVFR;
	}
	
	protected final void setDownloadFileName(String filename) {
		try {
			setDataURL(FRENCH_PORTAL+"var/download/"+filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
