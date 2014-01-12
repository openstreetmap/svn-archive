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
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import java.net.MalformedURLException;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.SncfConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.SncfLicense;

public abstract class SncfDataSetHandler extends FrenchDataSetHandler implements SncfConstants {
	
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
		setLicense(new SncfLicense());
		if (portalId != null && !portalId.isEmpty()) {
			try {
				setLocalPortalURL(PORTAL + portalId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
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
}
