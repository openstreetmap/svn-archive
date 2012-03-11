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
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets;

import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.ParisConstants;

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
		return ICON_PARIS_24;
	}

	public final URL getLocalPortalURL() {
		try {
			if (documentId > 0) {
				return new URL(PORTAL + "jsp/site/Portal.jsp?document_id="+documentId + "&portlet_id="+portletId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected abstract String getDirectLink();

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURL()
	 */
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
