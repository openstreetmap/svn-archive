package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.SncfConstants;

public abstract class SncfDataSetHandler extends FrenchDataSetHandler implements SncfConstants {
	
	private String portalId;
	
	public SncfDataSetHandler(String portalId) {
		init(portalId);
	}
	
	public SncfDataSetHandler(String portalId, String relevantTag) {
		super(relevantTag);
		init(portalId);
	}
	
	public SncfDataSetHandler(String portalId, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId);
	}

	public SncfDataSetHandler(String portalId, String ... relevantTags) {
		this(portalId, false, relevantTags);
	}

	/*public ToulouseDataSetHandler(int portalId, Tag relevantTag) {
		super(relevantTag);
		init(portalId);
	}*/
	
	public SncfDataSetHandler(String portalId, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId);
	}

	/*public ToulouseDataSetHandler(int portalId, Tag ... relevantTags) {
		this(portalId, false, relevantTags);
	}*/
	
	private final void init(String portalId) {
		this.portalId = portalId;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_24;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataLayerIconName()
	 */
	@Override
	public String getDataLayerIconName() {
		return ICON_16;
	}

	public final URL getLocalPortalURL() {
		try {
			if (portalId != null) {
				return new URL(PORTAL + portalId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLicenseURL()
	 */
	@Override
	public URL getLicenseURL() {
		try {
			return new URL("http://test.data-sncf.com/licence");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
