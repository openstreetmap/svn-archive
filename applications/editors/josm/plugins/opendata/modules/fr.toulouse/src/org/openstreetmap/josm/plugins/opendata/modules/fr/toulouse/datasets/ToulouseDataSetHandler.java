package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseConstants;

public abstract class ToulouseDataSetHandler extends FrenchDataSetHandler implements ToulouseConstants {
	
	private int portalId;
	private String wikiPage;
	
	public ToulouseDataSetHandler(int portalId) {
		init(portalId);
	}
	
	public ToulouseDataSetHandler(int portalId, String relevantTag) {
		super(relevantTag);
		init(portalId);
	}
	
	public ToulouseDataSetHandler(int portalId, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId);
	}

	public ToulouseDataSetHandler(int portalId, String ... relevantTags) {
		this(portalId, false, relevantTags);
	}

	/*public ToulouseDataSetHandler(int portalId, Tag relevantTag) {
		super(relevantTag);
		init(portalId);
	}*/
	
	public ToulouseDataSetHandler(int portalId, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(portalId);
	}

	/*public ToulouseDataSetHandler(int portalId, Tag ... relevantTags) {
		this(portalId, false, relevantTags);
	}*/
	
	private final void init(int portalId) {
		this.portalId = portalId;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_GRAND_TOULOUSE;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_CROIX_24;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataLayerIconName()
	 */
	@Override
	public String getDataLayerIconName() {
		return ICON_CROIX_16;
	}

	public final URL getLocalPortalURL() {
		try {
			if (portalId > 0) {
				return new URL(PORTAL + portalId + "--");
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
			return new URL(getLocalPortalURL().toString()+"/license");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.AbstractDataSetHandler#getWikiURL()
	 */
	@Override
	public URL getWikiURL() {
		try {
			if (wikiPage != null && !wikiPage.isEmpty()) {
				return new URL(WIKI + "/" + wikiPage);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected final void setWikiPage(String wikiPage) {
		this.wikiPage = wikiPage;
	}
}
