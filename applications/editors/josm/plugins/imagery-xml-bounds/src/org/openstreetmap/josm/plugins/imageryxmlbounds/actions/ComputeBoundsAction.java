// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
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
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IRelationMember;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon.JoinedWay;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon.PolyData;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 *
 * @author Don-vip
 *
 */
public class ComputeBoundsAction extends AbstractAction implements XmlBoundsConstants {

    protected static final DecimalFormat DF = new DecimalFormat("#0.#####", new DecimalFormatSymbols(Locale.UK));

    protected static final String ACTION_NAME = tr("XML Imagery Bounds");

    protected static final String EIGHT_SP = "        ";

    private final Set<IRelation<?>> multipolygons;
    private final Set<IWay<?>> closedWays;

    static { DF.setRoundingMode(RoundingMode.CEILING); }
    /**
     * Constructs a new {@code ComputeBoundsAction}.
     */
    public ComputeBoundsAction() {
        this(null);
    }

    /**
     * Constructs a new {@code ComputeBoundsAction}.
     * @param layer data layer
     */
    public ComputeBoundsAction(OsmDataLayer layer) {
        this.multipolygons = new HashSet<>();
        this.closedWays = new HashSet<>();

        putValue(SHORT_DESCRIPTION, tr("Generate Imagery XML bounds for the selection"));
        putValue(NAME, ACTION_NAME);
        new ImageProvider("xml_24.png").getResource().attachImageIcon(this, true);
        setEnabled(false);

        if (layer != null) {
            List<OsmPrimitive> primitives = new ArrayList<>();
            primitives.addAll(layer.data.getRelations());
            primitives.addAll(layer.data.getWays());
            updateOsmPrimitives(primitives);
        }
    }

    protected void updateOsmPrimitives(Collection<? extends IPrimitive> primitives) {
        multipolygons.clear();
        closedWays.clear();
        // Store selected multipolygons and closed ways
        for (IPrimitive value : primitives) {
            if (value instanceof IRelation) {
                IRelation<?> r = (IRelation<?>) value;
                if (r.isMultipolygon()) {
                    multipolygons.add(r);
                }
            } else if (value instanceof IWay) {
                IWay<?> w = (IWay<?>) value;
                if (w.isClosed()) {
                    closedWays.add(w);
                }
            }
        }
        // Remove closed ways already inside a selected multipolygon
        for (Iterator<IWay<?>> it = closedWays.iterator(); it.hasNext();) {
            processIterator(it);
        }
        // Enable the action if at least one area is found
        setEnabled(!multipolygons.isEmpty() || !closedWays.isEmpty());
    }

    private void processIterator(Iterator<IWay<?>> it) {
        IWay<?> way = it.next();
        for (IRelation<?> mp : multipolygons) {
            for (IRelationMember<?> mb : mp.getMembers()) {
                if (mb.getMember().equals(way)) {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * Replies XML code.
     * @return XML code
     */
    public final String getXml() {
        List<IPrimitive> primitives = new ArrayList<>();
        primitives.addAll(multipolygons);
        primitives.addAll(closedWays);
        return getXml(primitives.toArray(new OsmPrimitive[primitives.size()]));
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

    /**
     * Escapes HTML entities.
     * @param s The string to update
     * @return String with escaped entities
     */
    public static String escapeReservedCharacters(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    protected static final String getXml(OsmPrimitive... primitives) {
        List<String> entries = new ArrayList<>();
        for (OsmPrimitive p : primitives) {
            if (p instanceof Relation) {
                entries.add(getEntry(p, getMultiPolygonBounds((Relation) p)));
            } else if (p instanceof Way) {
                entries.add(getEntry(p, getClosedWayBounds((Way) p)));
            }
        }
        return getImagery(entries.toArray(new String[entries.size()]));
    }

    protected static final String getImagery(String... entries) {
        String version = "UNKNOWN";
        try {
            InputStream revision = ComputeBoundsAction.class.getResourceAsStream("/REVISION");
            if (revision != null) {
	            Properties p = new Properties();
	            p.load(revision);
	            version = p.getProperty("Revision");
            }
        } catch(IOException e) {
        	Logging.warn(e);
        }
        StringBuilder result = new StringBuilder(256);
        result.append("<?xml version=\"1.0\" encoding=\"").append(ENCODING).append("\" ?>\n")
        	  .append("<!-- Generated with JOSM Imagery XML Plugin version ").append(version).append(" -->\n")
        	  .append("<imagery xmlns=\"").append(XML_NAMESPACE).append("\">\n");
        for (String entry : entries) {
            result.append(entry).append('\n');
        }
        result.append("</imagery>");
        return result.toString();
    }

    protected static final String getEntry(OsmPrimitive p, String bounds) {
        return getEntry(p.get(KEY_NAME), p.get(KEY_TYPE), p.get(KEY_DEFAULT), p.get(KEY_URL), bounds, p.get(KEY_PROJECTIONS),
                p.get(KEY_LOGO_URL), p.get(KEY_EULA), p.get(KEY_ATTR_TEXT), p.get(KEY_ATTR_URL), p.get(KEY_TERMS_TEXT),
                p.get(KEY_TERMS_URL), p.get(KEY_COUNTRY_CODE), p.get(KEY_MAX_ZOOM), p.get(KEY_MIN_ZOOM), p.get(KEY_ID),
                p.get(KEY_DATE));
    }

    protected static final boolean isSet(String tag) {
        return tag != null && !tag.isEmpty();
    }

    protected static final String getEntry(String name, String type, String def, String url, String bounds, String projections,
            String logoURL, String eula, String attributionText, String attributionUrl, String termsText, String termsUrl,
            String countryCode, String maxZoom, String minZoom, String id, String date) {
        StringBuilder result = new StringBuilder(128);
        result.append("    <entry>\n"+
        EIGHT_SP + simpleTag(XML_NAME, name) + '\n'+
        EIGHT_SP + simpleTag(XML_TYPE, type, "wms") + '\n'+
        EIGHT_SP + simpleTag(XML_URL, url != null ? encodeUrl(url) : "", false) + '\n'+
                 bounds+'\n');
        if (projections != null && !projections.isEmpty()) {
            result.append(EIGHT_SP+startTag(XML_PROJECTIONS)+'\n');
            int i = 0;
            String[] codes = projections.split(";");
            for (String code : codes) {
                if (i%6 == 0) {
                    result.append("            ");
                }
                result.append(simpleTag("code", code.trim()));
                if (i%6 == 5 || i == codes.length-1) {
                    result.append('\n');
                }
                i++;
            }
            result.append(EIGHT_SP+endTag(XML_PROJECTIONS)+'\n');
        }
        if (isSet(def) && "true".equals(def)) {
            result.append(EIGHT_SP + simpleTag(XML_DEFAULT, def) + '\n');
        }
        if (isSet(id)) {
            result.append(EIGHT_SP + simpleTag(XML_ID, id, false) + '\n');
        }
        if (isSet(date)) {
            result.append(EIGHT_SP + simpleTag(XML_DATE, date, false) + '\n');
        }
        if (isSet(eula)) {
            result.append(EIGHT_SP + mandatoryTag(XML_EULA, encodeUrl(eula), false) + '\n');
        }
        if (isSet(attributionText)) {
            result.append(EIGHT_SP + mandatoryTag(XML_ATTR_TEXT, attributionText) + '\n');
        }
        if (isSet(attributionUrl)) {
            result.append(EIGHT_SP + simpleTag(XML_ATTR_URL, encodeUrl(attributionUrl), false) + '\n');
        }
        if (isSet(termsText)) {
            result.append(EIGHT_SP + simpleTag(XML_TERMS_TEXT, termsText) + '\n');
        }
        if (isSet(termsUrl)) {
            result.append(EIGHT_SP + simpleTag(XML_TERMS_URL, encodeUrl(termsUrl), false) + '\n');
        }
        if (isSet(logoURL)) {
            result.append(EIGHT_SP + simpleTag(XML_LOGO_URL, encodeUrl(logoURL), false) + '\n');
        }
        if (isSet(countryCode)) {
            result.append(EIGHT_SP + simpleTag(XML_COUNTRY_CODE, countryCode) + '\n');
        }
        if ("tms".equals(type)) {
            if (isSet(maxZoom)) {
                result.append(EIGHT_SP + simpleTag(XML_MAX_ZOOM, maxZoom) + '\n');
            }
            if (isSet(minZoom)) {
                result.append(EIGHT_SP + simpleTag(XML_MIN_ZOOM, minZoom) + '\n');
            }
        }
        result.append("    </entry>");
        return result.toString();
    }

    protected static final String encodeUrl(String url) {
        return "<![CDATA["+url+"]]>";
    }

    protected static final String getBounds(OsmPrimitive p, boolean closeTag) {
        BBox bbox = p.getBBox();
        String result = "        <bounds ";
        result += "min-lat='"+DF.format(bbox.getBottomRight().lat())+"' ";
        result += "min-lon='"+DF.format(bbox.getTopLeft().lon())+"' ";
        result += "max-lat='"+DF.format(bbox.getTopLeft().lat())+"' ";
        result += "max-lon='"+DF.format(bbox.getBottomRight().lon())+"'";
        result += closeTag ? " />" : ">\n";
        return result;
    }

    protected static final boolean isValidOuterMember(RelationMember mb) {
        return (mb.getRole() == null || mb.getRole().isEmpty() || "outer".equals(mb.getRole()))
                && mb.getMember() instanceof Way;
    }

    protected static final boolean isValidOuterClosedMember(RelationMember mb) {
        return isValidOuterMember(mb) && ((Way) mb.getMember()).isClosed();
    }

    protected static final String getMultiPolygonBounds(Relation mp) {
        List<PolyData> polygons = new Multipolygon(mp).getOuterPolygons();
        if (polygons.isEmpty() || (polygons.size() == 1 && areJoinedWayAndBboxEqual(polygons.get(0), mp.getBBox()))) {
            return getBounds(mp, true);
        } else {
            StringBuilder result = new StringBuilder(getBounds(mp, false));
            for (PolyData way : polygons) {
                result.append(getJoinedWayShape(way));
            }
            result.append("        </bounds>");
            return result.toString();
        }
    }

    protected static final boolean areClosedWayAndBboxEqual(Way way, BBox bBox) {
        return areNodeListAndBboxEqual(way.getNodes(), bBox);
    }

    protected static final boolean areJoinedWayAndBboxEqual(JoinedWay way, BBox bBox) {
        return areNodeListAndBboxEqual(way.getNodes(), bBox);
    }

    protected static final boolean areNodeListAndBboxEqual(List<Node> nodes, BBox bBox) {
        if (nodes.size() == 5) {
            Map<Double, List<Integer>> latMap = new HashMap<>();
            Map<Double, List<Integer>> lonMap = new HashMap<>();

            for (int i=0; i<4; i++) {
                LatLon c = nodes.get(i).getCoor();
                if (i > 1) {
                    LatLon b = nodes.get(i-1).getCoor();
                    if (b.lat() != c.lat() && b.lon() != c.lon()) {
                        return false;
                    }
                }
                List<Integer> latList = latMap.get(c.lat());
                if (latList == null) {
                    latList = new ArrayList<>();
                    latMap.put(c.lat(), latList);
                }
                latList.add(i);
                List<Integer> lonList = lonMap.get(c.lon());
                if (lonList == null) {
                    lonList = new ArrayList<>();
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
        return getNodeListShape(cw.getNodes());
    }

    protected static final String getJoinedWayShape(JoinedWay jw) {
        return getNodeListShape(jw.getNodes());
    }

    protected static final String getNodeListShape(List<Node> nodes) {
        StringBuilder result = new StringBuilder("            <shape>\n");
        int size = nodes.size();
        for (int i=0; i<size; i++) {
            if (i%3 == 0) {
                result.append("                ");
            }
            int j = i;
            if (j == size)
                j = 0;
            LatLon ll = nodes.get(i).getCoor();
            result.append("<point lat='")
                  .append(DF.format(ll.lat())).append("' lon='")
                  .append(DF.format(ll.lon())).append("'/>");
            if (i%3 == 2 || i == size-1) {
                result.append('\n');
            }
        }
        result.append("            </shape>\n");
        return result.toString();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Logging.isDebugEnabled()) {
            Logging.debug(getXml());
        }
    }
}
