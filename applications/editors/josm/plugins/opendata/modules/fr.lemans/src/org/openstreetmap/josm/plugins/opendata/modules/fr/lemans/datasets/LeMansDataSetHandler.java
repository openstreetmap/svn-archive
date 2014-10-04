// License: GPL. For details, see LICENSE file.
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

	@Override
	public String getSource() {
		return SOURCE_LE_MANS;
	}
	
	/*@Override
	public URL getLicenseURL() {
		try {
			return new URL(PORTAL + "download.do?uuid=3E907F53-550EA533-5AE8381B-44AE9F93");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}*/

	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<>();
		try {
			if (kmzUuid != null && !kmzUuid.isEmpty()) result.add(new Pair<>(getName() + " (KMZ)", new URL(PORTAL + "download.do?uuid=" + kmzUuid)));
			if (shpUuid != null && !shpUuid.isEmpty()) result.add(new Pair<>(getName() + " (SHP)", new URL(PORTAL + "download.do?uuid=" + shpUuid)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
