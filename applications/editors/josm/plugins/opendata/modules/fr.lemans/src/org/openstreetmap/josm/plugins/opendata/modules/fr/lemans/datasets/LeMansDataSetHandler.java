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
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.LeMansConstants;
import org.openstreetmap.josm.tools.Pair;

public abstract class LeMansDataSetHandler extends FrenchDataSetHandler implements LeMansConstants {
	
	private String kmzUuid;
	private String shpUuid;
	
	public LeMansDataSetHandler(String uuid) {
		init(uuid);
	}
	
	public LeMansDataSetHandler(String uuid, String relevantTag) {
		super(relevantTag);
		init(uuid);
	}
	
	public LeMansDataSetHandler(String uuid, boolean relevantUnion, String ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(uuid);
	}

	public LeMansDataSetHandler(String uuid, String ... relevantTags) {
		this(uuid, false, relevantTags);
	}

	
	public LeMansDataSetHandler(String uuid, boolean relevantUnion, Tag ... relevantTags) {
		super(relevantUnion, relevantTags);
		init(uuid);
	}

	private final void init(String uuid) {
		try {
			setLicense(License.ODbL);
			if (uuid != null && !uuid.isEmpty()) {
				setLocalPortalURL(PORTAL + "page.do?t=2&uuid=" + uuid);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
		
	protected final void setKmzShpUuid(String kmzUuid, String shpUuid) {
		this.kmzUuid = kmzUuid;
		this.shpUuid = shpUuid;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
	 */
	@Override
	public String getSource() {
		return SOURCE_LE_MANS;
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLicenseURL()
	 */
	/*@Override
	public URL getLicenseURL() {
		try {
			return new URL(PORTAL + "download.do?uuid=3E907F53-550EA533-5AE8381B-44AE9F93");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURLs()
	 */
	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
		try {
			if (kmzUuid != null && !kmzUuid.isEmpty()) result.add(new Pair<String, URL>("KMZ", new URL(PORTAL + "download.do?uuid=" + kmzUuid)));
			if (shpUuid != null && !shpUuid.isEmpty()) result.add(new Pair<String, URL>("SHP", new URL(PORTAL + "download.do?uuid=" + shpUuid)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
