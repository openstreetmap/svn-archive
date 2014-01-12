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
package org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe.datasets;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.datasets.be.BelgianDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe.DataGovBeConstants;

public abstract class DataGovDataSetHandler extends BelgianDataSetHandler implements DataGovBeConstants {
	
	public DataGovDataSetHandler() {
		init(null, null, null, null, null);
	}

	public DataGovDataSetHandler(String portalPathDe, String portalPathEn, String portalPathFr, String portalPathNl) {
		init(portalPathDe, portalPathEn, portalPathFr, portalPathNl, null);
	}

	public DataGovDataSetHandler(String portalPathDe, String portalPathEn, String portalPathFr, String portalPathNl, Projection singleProjection) {
		init(portalPathDe, portalPathEn, portalPathFr, portalPathNl, singleProjection);
	}

	public DataGovDataSetHandler(String portalPathDe, String portalPathEn, String portalPathFr, String portalPathNl, Projection singleProjection, String relevantTag) {
		super(relevantTag);
		init(portalPathDe, portalPathEn, portalPathFr, portalPathNl, singleProjection);
	}

	public DataGovDataSetHandler(String portalPathDe, String portalPathEn, String portalPathFr, String portalPathNl, String relevantTag) {
		super(relevantTag);
		init(portalPathDe, portalPathEn, portalPathFr, portalPathNl, null);
	}

	private void init(String portalPathDe, String portalPathEn, String portalPathFr, String portalPathNl, Projection singleProjection) {
		setNationalPortalPath(portalPathDe, portalPathEn, portalPathFr, portalPathNl);
		setSingleProjection(singleProjection);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_DATAGOVBE;
	}
}
