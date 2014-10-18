// License: GPL. For details, see LICENSE file.
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

import org.openstreetmap.josm.Main;
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

    protected static final DecimalFormat DF = new DecimalFormat("#0.0000000", new DecimalFormatSymbols(Locale.UK));

    protected static final String ACTION_NAME = tr("XML Imagery Bounds");

    protected static final String EIGHT_SP = "        ";

    private final Set<Relation> multipolygons;
    private final Set<Way> closedWays;

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
        try {
            putValue(SMALL_ICON, XML_ICON_24);
        } catch (Exception e) {
            Main.error(e);
        }
        setEnabled(false);

        if (layer != null) {
            List<OsmPrimitive> primitives = new ArrayList<>();
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
            processIterator(it);
        }
        // Enable the action if at least one area is found
        setEnabled(!multipolygons.isEmpty() || !closedWays.isEmpty());
    }

    private void processIterator(Iterator<Way> it) {
        Way way = it.next();
        for (Relation mp : multipolygons) {
            for (RelationMember mb : mp.getMembers()) {
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
        List<OsmPrimitive> primitives = new ArrayList<>();
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

    /**
     * Escapes HTML entities.
     * @param s The string to update
     * @return String with escaped entities
     */
    public static String escapeReservedCharacters(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    protected static final String getXml(OsmPrimitive ... primitives) {
        List<String> entries = new ArrayList<>();
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
        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"").append(ENCODING).append("\" ?>\n");
        result.append("<!-- Generated with JOSM Imagery XML Plugin version ").append(PLUGIN_VERSION).append(" -->\n");
        result.append("<imagery xmlns=\"").append(XML_NAMESPACE).append("\">\n");
        for (String entry : entries) {
            result.append(entry).append("\n");
        }
        result.append("</imagery>");
        return result.toString();
    }

    protected static final String getEntry(OsmPrimitive p, String bounds) {
        return getEntry(p.get(KEY_NAME), p.get(KEY_TYPE), p.get(KEY_DEFAULT), p.get(KEY_URL), bounds, p.get(KEY_PROJECTIONS),
                p.get(KEY_LOGO_URL), p.get(KEY_EULA), p.get(KEY_ATTR_TEXT), p.get(KEY_ATTR_URL),  p.get(KEY_TERMS_TEXT),
                p.get(KEY_TERMS_URL), p.get(KEY_COUNTRY_CODE), p.get(KEY_MAX_ZOOM), p.get(KEY_MIN_ZOOM));
    }

    protected static final boolean isSet(String tag) {
        return tag != null && !tag.isEmpty();
    }

    protected static final String getEntry(String name, String type, String def, String url, String bounds, String projections,
            String logoURL, String eula, String attributionText, String attributionUrl, String termsText, String termsUrl,
            String countryCode, String maxZoom, String minZoom) {
        StringBuilder result = new StringBuilder();
        result.append("    <entry>\n"+
        EIGHT_SP + simpleTag(XML_NAME, name) + "\n"+
        EIGHT_SP + simpleTag(XML_TYPE, type, "wms") + "\n"+
        EIGHT_SP + simpleTag(XML_URL, url != null ? encodeUrl(url) : "", false) + "\n"+
                 bounds+"\n");
        if (projections != null && !projections.isEmpty()) {
            result.append(EIGHT_SP+startTag(XML_PROJECTIONS)+"\n");
            int i = 0;
            String[] codes = projections.split(";");
            for (String code : codes) {
                if (i%6 == 0) {
                    result.append("            ");
                }
                result.append(simpleTag("code", code.trim()));
                if (i%6 == 5 || i == codes.length-1 ) {
                    result.append("\n");
                }
                i++;
            }
            result.append(EIGHT_SP+endTag(XML_PROJECTIONS)+"\n");
        }
        if (isSet(def) && "true".equals(def)) {
            result.append(EIGHT_SP + simpleTag(XML_DEFAULT, def) + "\n");
        }
        if (isSet(eula)) {
            result.append(EIGHT_SP + mandatoryTag(XML_EULA, encodeUrl(eula), false) + "\n");
        }
        if (isSet(attributionText)) {
            result.append(EIGHT_SP + mandatoryTag(XML_ATTR_TEXT, attributionText) + "\n");
        }
        if (isSet(attributionUrl)) {
            result.append(EIGHT_SP + simpleTag(XML_ATTR_URL, encodeUrl(attributionUrl), false) + "\n");
        }
        if (isSet(termsText)) {
            result.append(EIGHT_SP + simpleTag(XML_TERMS_TEXT, termsText) + "\n");
        }
        if (isSet(termsUrl)) {
            result.append(EIGHT_SP + simpleTag(XML_TERMS_URL, encodeUrl(termsUrl), false) + "\n");
        }
        if (isSet(logoURL)) {
            result.append(EIGHT_SP + simpleTag(XML_LOGO_URL, encodeUrl(logoURL), false) + "\n");
        }
        if (isSet(countryCode)) {
            result.append(EIGHT_SP + simpleTag(XML_COUNTRY_CODE, countryCode) + "\n");
        }
        if ("tms".equals(type)) {
            if (isSet(maxZoom)) {
                result.append(EIGHT_SP + simpleTag(XML_MAX_ZOOM, maxZoom) + "\n");
            }
            if (isSet(minZoom)) {
                result.append(EIGHT_SP + simpleTag(XML_MIN_ZOOM, minZoom) + "\n");
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
                && mb.getMember() instanceof Way && ((Way) mb.getMember()).isClosed();
    }

    protected static final String getMultiPolygonBounds(Relation mp) {
        List<Way> outerClosedWays = new ArrayList<>();
        for (RelationMember mb : mp.getMembers()) {
            if (isValidOuterMember(mb)) {
                outerClosedWays.add((Way) mb.getMember());
            }
        }

        if (outerClosedWays.isEmpty() || (outerClosedWays.size() == 1 && areClosedWayAndBboxEqual(outerClosedWays.get(0), mp.getBBox()))) {
            return getBounds(mp, true);
        } else {
            StringBuilder result = new StringBuilder(getBounds(mp, false));
            for (Way way : outerClosedWays) {
                result.append(getClosedWayShape(way));
            }
            result.append("        </bounds>");
            return result.toString();
        }
    }

    protected static final boolean areClosedWayAndBboxEqual(Way way, BBox bBox) {
        if (way.getNodesCount() == 5) {
            Map<Double, List<Integer>> latMap = new HashMap<>();
            Map<Double, List<Integer>> lonMap = new HashMap<>();

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
        StringBuilder result = new StringBuilder("            <shape>\n");
        for (int i=0; i<cw.getNodesCount(); i++) {
            if (i%3 == 0) {
                result.append("                ");
            }
            int j = i;
            if(j == cw.getNodesCount())
                j = 0;
            result.append("<point ");
            result.append("lat='").append(DF.format(cw.getNode(i).getCoor().lat())).append("' ");
            result.append("lon='").append(DF.format(cw.getNode(i).getCoor().lon())).append("'/>");
            if (i%3 == 2 || i == cw.getNodesCount()-1 ) {
                result.append("\n");
            }
        }
        result.append("            </shape>\n");
        return result.toString();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Main.isDebugEnabled()) {
            Main.debug(getXml());
        }
    }
}
