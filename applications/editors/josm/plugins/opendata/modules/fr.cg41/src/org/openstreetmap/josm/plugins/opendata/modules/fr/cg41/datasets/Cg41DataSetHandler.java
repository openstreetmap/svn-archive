package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.Cg41Constants;

public abstract class Cg41DataSetHandler extends FrenchDataSetHandler implements Cg41Constants {
	
	private int portalId;
	
	public Cg41DataSetHandler(int portalId, String nationalPath) {
		init(portalId, nationalPath);
	}
	
	public Cg41DataSetHandler(int portalId, String nationalPath, String relevantTag) {
		super(relevantTag);
		init(portalId, nationalPath);
	}
	
	public Cg41DataSetHandler(int portalId, String nationalPath, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId, nationalPath);
	}

	public Cg41DataSetHandler(int portalId, String nationalPath, String ... relevantTags) {
		this(portalId, nationalPath, false, relevantTags);
	}

	public Cg41DataSetHandler(int portalId, String nationalPath, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId, nationalPath);
	}

	private final void init(int portalId, String nationalPath) {
		this.portalId = portalId;
		setNationalPortalPath(nationalPath);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_CG41;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_CG41_24;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataLayerIconName()
	 */
	@Override
	public String getDataLayerIconName() {
		return ICON_CG41_16;
	}

	public final URL getLocalPortalURL() {
		try {
			if (portalId > 0) {
				return new URL(PORTAL_CG41 + portalId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
