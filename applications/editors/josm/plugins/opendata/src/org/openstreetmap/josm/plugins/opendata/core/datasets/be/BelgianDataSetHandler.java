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
package org.openstreetmap.josm.plugins.opendata.core.datasets.be;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.datasets.SimpleDataSetHandler;

public abstract class BelgianDataSetHandler extends SimpleDataSetHandler implements BelgianConstants {

	private Projection singleProjection;

	private String nationalPortalPathDe;
	private String nationalPortalPathEn;
	private String nationalPortalPathFr;
	private String nationalPortalPathNl;

	protected static final Projection lambert1972 = PRJ_LAMBERT_1972.getProjection();
	protected static final Projection lambert2008 = PRJ_LAMBERT_2008.getProjection();
	
	protected static final Projection[] projections = new Projection[]{
		lambert1972,
		lambert2008
	};

	public BelgianDataSetHandler() {
		
	}

	public BelgianDataSetHandler(String relevantTag) {
		super(relevantTag);
	}

	public BelgianDataSetHandler(boolean relevantUnion, String[] relevantTags) {
		super(relevantUnion, relevantTags);
	}

	public BelgianDataSetHandler(boolean relevantUnion, Tag[] relevantTags) {
		super(relevantUnion, relevantTags);
	}
	
	protected final void setNationalPortalPath(String nationalPortalPathDe, String nationalPortalPathEn, String nationalPortalPathFr, String nationalPortalPathNl) {
		this.nationalPortalPathDe = nationalPortalPathDe;
		this.nationalPortalPathEn = nationalPortalPathEn;
		this.nationalPortalPathFr = nationalPortalPathFr;
		this.nationalPortalPathNl = nationalPortalPathNl;
	}

	protected final void setSingleProjection(Projection singleProjection) {
		this.singleProjection = singleProjection;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getNationalPortalURL()
	 */
	@Override
	public URL getNationalPortalURL() {
		try {
			String nationalPortalPath = "";
			String lang = Main.pref.get("language");
			if (lang == null || lang.isEmpty()) {
				lang = Locale.getDefault().toString();
			}
				
			if (lang.startsWith("de") && nationalPortalPathDe != null) {
				nationalPortalPath = nationalPortalPathDe;
			} else if (lang.startsWith("fr") && nationalPortalPathFr != null) {
				nationalPortalPath = nationalPortalPathFr;
			} else if (lang.startsWith("nl") && nationalPortalPathNl != null) {
				nationalPortalPath = nationalPortalPathNl;
			} else {
				nationalPortalPath = nationalPortalPathEn;
			}
			return new URL(BELGIAN_PORTAL.replace(PATTERN_LANG, lang.substring(0, 2))+nationalPortalPath);//FIXME
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
	 */
	@Override
	public String getLocalPortalIconName() {
		return ICON_BE_24;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getNationalPortalIconName()
	 */
	@Override
	public String getNationalPortalIconName() {
		return ICON_BE_24;
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#handlesCsvProjection()
	 */
	@Override
	public boolean handlesSpreadSheetProjection() {
		return singleProjection != null ? true : super.handlesSpreadSheetProjection();
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvProjections()
	 */
	@Override
	public List<Projection> getSpreadSheetProjections() {
		if (singleProjection != null) {
			return Arrays.asList(new Projection[]{singleProjection});
		} else {
			return Arrays.asList(projections);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvCoor(org.openstreetmap.josm.data.coor.EastNorth, java.lang.String[])
	 */
	@Override
	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		if (singleProjection != null) {
			return singleProjection.eastNorth2latlon(en);
		} else {
			return super.getSpreadSheetCoor(en, fields);
		}
	}
}
