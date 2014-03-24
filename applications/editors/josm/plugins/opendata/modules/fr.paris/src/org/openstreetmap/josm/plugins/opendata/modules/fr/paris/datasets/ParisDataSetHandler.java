// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.ParisConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.ParisLicense;

public abstract class ParisDataSetHandler extends FrenchDataSetHandler implements ParisConstants {
	
	private int documentId;
	private static final int portletId = 106; // FIXME
	
	public ParisDataSetHandler(int documentId) {
		init(documentId);
	}
	
	public ParisDataSetHandler(int documentId, String relevantTag) {
		super(relevantTag);
		init(documentId);
	}
	
	public ParisDataSetHandler(int documentId, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(documentId);
	}

	public ParisDataSetHandler(int documentId, String ... relevantTags) {
		this(documentId, false, relevantTags);
	}

	public ParisDataSetHandler(int documentId, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(documentId);
	}

	private final void init(int documentId) {
		this.documentId = documentId;
		setLicense(new ParisLicense());
		try {
			if (documentId > 0) {
				setLocalPortalURL(PORTAL + "jsp/site/Portal.jsp?document_id="+documentId + "&portlet_id="+portletId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSource() {
		return SOURCE;
	}

	@Override
	public String getLocalPortalIconName() {
		return ICON_PARIS_24;
	}
	
	protected abstract String getDirectLink();

	@Override
	public URL getDataURL() {
		try {
			if (documentId > 0) {
				return new URL(PORTAL + "rating/download/?id_resource="+documentId + "&type_resource=document&url="+getDirectLink());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
