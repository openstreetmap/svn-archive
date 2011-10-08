//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;

/**
 * 
 * @author Don-vip
 *
 */
public class ComputeBoundsAction extends AbstractAction implements XmlBoundsConstants {

	protected static final DecimalFormat df = new DecimalFormat("#0.0000000", new DecimalFormatSymbols(Locale.UK));

	protected static final String ACTION_NAME = tr("XML Imagery Bounds");
	
	private final Set<Relation> multipolygons;
	private final Set<Way> closedWays;
	
	public ComputeBoundsAction() {
		this(null);
	}
	
	public ComputeBoundsAction(OsmDataLayer layer) {
		this.multipolygons = new HashSet<Relation>();
		this.closedWays = new HashSet<Way>();
		
        putValue(SHORT_DESCRIPTION, tr("Generate Imagery XML bounds for the selection"));
        putValue(NAME, ACTION_NAME);
        try {
        	putValue(SMALL_ICON, XML_ICON_24);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	setEnabled(false);
    	
		if (layer != null) {
			List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
			primitives.addAll(layer.data.getRelations());
			primitives.addAll(layer.data.getWays());
			updateOsmPrimitives(primitives);
		}
	}
	
	protected void updateOsmPrimitives(Collection<? extends OsmPrimitive> primitives) {
		multipolygons.clear();
		closedWays.clear();
		// Store selected multipolygons and closed ways
		for (OsmPrimitive value : primitives) {
			if (value instanceof Relation) {
				Relation r = (Relation) value;
				if (r.isMultipolygon()) {
					multipolygons.add(r);
				}
			} else if (value instanceof Way) {
				Way w = (Way) value;
				if (w.isClosed()) {
					closedWays.add(w);
				}
			}
		}
		// Remove closed ways already inside a selected multipolygon
		for (Iterator<Way> it = closedWays.iterator(); it.hasNext(); ) {
			Way way = it.next();
			for (Relation mp : multipolygons) {
				for (RelationMember mb : mp.getMembers()) {
					if (mb.getMember().equals(way)) {
						it.remove();
					}
				}
			}
		}
		// Enable the action if at least one area is found
		setEnabled(!multipolygons.isEmpty() || !closedWays.isEmpty());
	}

	public final String getXml() {
		List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
		primitives.addAll(multipolygons);
		primitives.addAll(closedWays);
		return getXml(primitives.toArray(new OsmPrimitive[0]));
	}
	
    protected static final String startTag(String tag) {
        return "<" + tag + ">";
    }

    protected static final String startMandatoryTag(String tag) {
        return "<" + tag + " mandatory='true'>";
    }

    protected static final String endTag(String tag) {
        return "</" + tag + ">";
    }

    protected static final String simpleTag(String tag, String content) {
        return simpleTag(tag, content, "");
    }

    protected static final String simpleTag(String tag, String content, boolean escape) {
        return simpleTag(tag, content, "", escape);
    }

    protected static final String simpleTag(String tag, String content, String def) {
        return simpleTag(tag, content, def, true);
    }

    protected static final String simpleTag(String tag, String content, String def, boolean escape) {
        return startTag(tag) + (content != null 
                ? (escape ? escapeReservedCharacters(content) : content) 
                : def
                ) + endTag(tag);
    }

    protected static final String mandatoryTag(String tag, String content) {
        return mandatoryTag(tag, content, true);
    }

    protected static final String mandatoryTag(String tag, String content, boolean escape) {
        return startMandatoryTag(tag) + (escape ? escapeReservedCharacters(content) : content) + endTag(tag);
    }

    public static String escapeReservedCharacters(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
	protected static final String getXml(OsmPrimitive ... primitives) {
		List<String> entries = new ArrayList<String>();
		for (OsmPrimitive p : primitives) {
			if (p instanceof Relation) {
				entries.add(getEntry(p, getMultiPolygonBounds((Relation) p)));
			} else if (p instanceof Way) {
				entries.add(getEntry(p, getClosedWayBounds((Way) p)));
			}
		}
		return getImagery(entries.toArray(new String[0]));
	}

	protected static final String getImagery(String ... entries) {
		String result = "<?xml version=\"1.0\" encoding=\""+ENCODING+"\" ?>\n";
		result += "<!-- Generated with JOSM Imagery XML Plugin version "+PLUGIN_VERSION+" -->\n";
		result += "<imagery xmlns=\""+XML_NAMESPACE+"\">\n";
		for (String entry : entries) {
			result += entry+"\n";
		}
	    result += "</imagery>";
	    return result;
	}
	
	protected static final String getEntry(OsmPrimitive p, String bounds) {
		return getEntry(p.get(KEY_NAME), p.get(KEY_TYPE), p.get(KEY_DEFAULT), p.get(KEY_URL), bounds, p.get(KEY_PROJECTIONS), p.get(KEY_LOGO_URL),
		        p.get(KEY_EULA), p.get(KEY_ATTR_TEXT), p.get(KEY_ATTR_URL),  p.get(KEY_TERMS_TEXT), p.get(KEY_TERMS_URL), p.get(KEY_COUNTRY_CODE), p.get(KEY_MAX_ZOOM), p.get(KEY_MIN_ZOOM));
	}
	
	protected static final boolean isSet(String tag) {
	    return tag != null && !tag.isEmpty();
	}
	
	protected static final String getEntry(String name, String type, String def, String url, String bounds, String projections, String logoURL,
	        String eula, String attributionText, String attributionUrl, String termsText, String termsUrl, String countryCode, String maxZoom, String minZoom) {
		String result =
	    "    <entry>\n"+
        "        " + simpleTag(XML_NAME, name) + "\n"+
        "        " + simpleTag(XML_TYPE, type, "wms") + "\n"+
        "        " + simpleTag(XML_URL, url != null ? encodeUrl(url) : "") + "\n"+
	             bounds+"\n";
		if (projections != null && !projections.isEmpty()) {
			result += "        "+startTag(XML_PROJECTIONS)+"\n";
			int i = 0;
			String[] codes = projections.split(";");
			for (String code : codes) {
				if (i%6 == 0) {
					result += "            ";
				}
				result += simpleTag("code", code.trim());
				if (i%6 == 5 || i == codes.length-1 ) {
					result += "\n";
				}
				i++;
			}
			result += "        "+endTag(XML_PROJECTIONS)+"\n";
		}
        if (isSet(def) && def.equals("true")) {
            result += "        " + simpleTag(XML_DEFAULT, def) + "\n";
        }
		if (isSet(eula)) {
			result += "        " + mandatoryTag(XML_EULA, encodeUrl(eula), false) + "\n";
		}
		if (isSet(attributionText)) {
            result += "        " + mandatoryTag(XML_ATTR_TEXT, attributionText) + "\n";
		}
		if (isSet(attributionUrl)) {
            result += "        " + simpleTag(XML_ATTR_URL, encodeUrl(attributionUrl), false) + "\n";
		}
        if (isSet(termsText)) {
            result += "        " + simpleTag(XML_TERMS_TEXT, termsText) + "\n";
        }
        if (isSet(termsUrl)) {
            result += "        " + simpleTag(XML_TERMS_URL, encodeUrl(termsUrl), false) + "\n";
        }
        if (isSet(logoURL)) {
            result += "        " + simpleTag(XML_LOGO_URL, encodeUrl(logoURL), false) + "\n";
        }
		if (isSet(countryCode)) {
            result += "        " + simpleTag(XML_COUNTRY_CODE, countryCode) + "\n";
		}
		if ("tms".equals(type)) {
		    if (isSet(maxZoom)) {
		        result += "        " + simpleTag(XML_MAX_ZOOM, maxZoom) + "\n";
		    }
            if (isSet(minZoom)) {
                result += "        " + simpleTag(XML_MIN_ZOOM, minZoom) + "\n";
            }
		}
	    result += "    </entry>";
	    return result;
	}
	
	protected static final String encodeUrl(String url) {
	    return "<![CDATA["+url+"]]>";
	}
	
	protected static final String getBounds(OsmPrimitive p, boolean closeTag) {
		BBox bbox = p.getBBox();
		String result = "        <bounds ";
		result += "min-lat='"+df.format(bbox.getBottomRight().lat())+"' "; 
		result += "min-lon='"+df.format(bbox.getTopLeft().lon())+"' "; 
		result += "max-lat='"+df.format(bbox.getTopLeft().lat())+"' "; 
		result += "max-lon='"+df.format(bbox.getBottomRight().lon())+"'";
		result += closeTag ? " />" : ">\n";
		return result;
	}
	
	protected static final boolean isValidOuterMember(RelationMember mb) {
		return (mb.getRole() == null || mb.getRole().isEmpty() || mb.getRole().equals("outer")) 
				&& mb.getMember() instanceof Way && ((Way) mb.getMember()).isClosed();
	}
	
	protected static final String getMultiPolygonBounds(Relation mp) {
		List<Way> outerClosedWays = new ArrayList<Way>();
		for (RelationMember mb : mp.getMembers()) {
			if (isValidOuterMember(mb)) {
				outerClosedWays.add((Way) mb.getMember());
			}
		}
		
		if (outerClosedWays.isEmpty() || (outerClosedWays.size() == 1 && areClosedWayAndBboxEqual(outerClosedWays.get(0), mp.getBBox()))) {
			return getBounds(mp, true);
		} else {
			String result = getBounds(mp, false);
			for (Way way : outerClosedWays) {
				result += getClosedWayShape(way);
			}
			result += "        </bounds>";
			return result;
		}
	}
	
	protected static final boolean areClosedWayAndBboxEqual(Way way, BBox bBox) {
		if (way.getNodesCount() == 5) {
			Map<Double, List<Integer>> latMap = new HashMap<Double, List<Integer>>();
			Map<Double, List<Integer>> lonMap = new HashMap<Double, List<Integer>>();

			for (int i=0; i<4; i++) {
				LatLon c = way.getNode(i).getCoor();
				if (i > 1) {
					LatLon b = way.getNode(i-1).getCoor();
					if (b.lat() != c.lat() && b.lon() !=  c.lon()) {
						return false;
					}
				}
				List<Integer> latList = latMap.get(c.lat());
				if (latList == null) {
					latList = new ArrayList<Integer>();
					latMap.put(c.lat(), latList);
				}
				latList.add(i);
				List<Integer> lonList = lonMap.get(c.lon());
				if (lonList == null) {
					lonList = new ArrayList<Integer>();
					lonMap.put(c.lon(), lonList);
				}
				lonList.add(i);
			}
			
			return latMap.size() == 2 && lonMap.size() == 2 
					&& latMap.containsKey(bBox.getBottomRight().lat()) && latMap.containsKey(bBox.getTopLeft().lat())
					&& lonMap.containsKey(bBox.getBottomRight().lon()) && lonMap.containsKey(bBox.getTopLeft().lon());
		}
		return false;
	}

	protected static final String getClosedWayBounds(Way cw) {
		if (areClosedWayAndBboxEqual(cw, cw.getBBox())) {
			return getBounds(cw, true);
		} else {
			return getBounds(cw, false) + getClosedWayShape(cw) + "        </bounds>";
		}
	}

	protected static final String getClosedWayShape(Way cw) {
		String result = "            <shape>\n";
		for (int i=0; i<cw.getNodesCount()-1; i++) {
			if (i%3 == 0) {
				result += "                ";
			}
			result += "<point ";
			result += "lat='" + df.format(cw.getNode(i).getCoor().lat()) + "' ";
			result += "lon='" + df.format(cw.getNode(i).getCoor().lon()) + "'/>";
			if (i%3 == 2 || i == cw.getNodesCount()-2 ) {
				result += "\n";
			}
		}
		result += "            </shape>\n";
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(getXml());
	}
}
