// License: GPL. For details, see LICENSE file.
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

	@Override
	public String getSource() {
		return SOURCE_BRUXELLES;
	}

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
