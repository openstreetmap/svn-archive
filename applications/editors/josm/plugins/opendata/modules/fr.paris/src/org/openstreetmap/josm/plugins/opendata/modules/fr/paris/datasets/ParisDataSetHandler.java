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
	private int portletId;
	
	public ParisDataSetHandler(int documentId, int portletId) {
		init(documentId, portletId);
	}
	
	public ParisDataSetHandler(int documentId, int portletId, String relevantTag) {
		super(relevantTag);
		init(documentId, portletId);
	}
	
	public ParisDataSetHandler(int documentId, int portletId, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(documentId, portletId);
	}

	public ParisDataSetHandler(int documentId, int portletId, String ... relevantTags) {
		this(documentId, portletId, false, relevantTags);
	}

	public ParisDataSetHandler(int documentId, int portletId, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(documentId, portletId);
	}

	private final void init(int documentId, int portletId) {
		this.documentId = documentId;
		this.portletId = portletId;
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
				return new URL(PORTAL + "document_id="+documentId + "&portlet_id="+portletId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
