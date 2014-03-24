// License: GPL. For details, see LICENSE file.
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

	@Override
	public String getSource() {
		return SOURCE_DATAGOVBE;
	}
}
