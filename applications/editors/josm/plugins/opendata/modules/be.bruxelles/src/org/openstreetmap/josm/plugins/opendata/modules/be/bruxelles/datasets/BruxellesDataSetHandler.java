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
package org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.datasets.be.BelgianDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles.BruxellesConstants;

public abstract class BruxellesDataSetHandler extends BelgianDataSetHandler implements BruxellesConstants {
	
	private Integer localPortalId;
	
	public BruxellesDataSetHandler() {
		init(null, null);
	}

	public BruxellesDataSetHandler(Integer portalId) {
		init(portalId, null);
	}

	public BruxellesDataSetHandler(Integer portalId, Projection singleProjection) {
		init(portalId, singleProjection);
	}

	public BruxellesDataSetHandler(Integer portalId, Projection singleProjection, String relevantTag) {
		super(relevantTag);
		init(portalId, singleProjection);
	}

	public BruxellesDataSetHandler(Integer portalId, String relevantTag) {
		super(relevantTag);
		init(portalId, null);
	}

	private void init(Integer portalId, Projection singleProjection) {
		setSingleProjection(singleProjection);
		this.localPortalId = portalId;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_BRUXELLES;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalURL()
	 */
	@Override
	public URL getLocalPortalURL() {
		String basePortal = null;
		String lang = OdUtils.getJosmLanguage();
			
		if (lang.startsWith("fr")) {
			basePortal = PORTAL_FR;
		} else if (lang.startsWith("nl")) {
			basePortal = PORTAL_NL;
		} else {
			basePortal = PORTAL_EN;
		}

		try {
			return new URL(basePortal + "/artdet.cfm?id=" + localPortalId);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
