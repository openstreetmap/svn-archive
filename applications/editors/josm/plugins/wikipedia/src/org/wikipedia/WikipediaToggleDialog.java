// License: GPL. See LICENSE file for details.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.LanguageInfo;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WikipediaToggleDialog extends ToggleDialog {

    public WikipediaToggleDialog() {
        super(tr("Wikipedia"), "wikipedia", tr("Fetch Wikipedia articles with coordinates"), null, 150);
        createLayout(list, true, Arrays.asList(
                new SideButton(new WikipediaDownloadAction()),
                new SideButton(new OpenWikipediaArticleAction())));
    }
    final DefaultListModel<WikipediaEntry> model = new DefaultListModel<WikipediaEntry>();
    final JList<WikipediaEntry> list = new JList<WikipediaEntry>(model) {

        {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && getSelectedValue() != null) {
                        BoundingXYVisitor bbox = new BoundingXYVisitor();
                        bbox.visit(getSelectedValue().coordinate);
                        Main.map.mapView.recalculateCenterScale(bbox);
                    }
                }
            });
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            final int index = locationToIndex(e.getPoint());
            if (index >= 0) {
                return "<html>" + model.getElementAt(index).description + "</html>";
            } else {
                return null;
            }
        }
    };

    static class WikipediaEntry implements Comparable<WikipediaEntry> {

        String name, description;
        LatLon coordinate;

        public WikipediaEntry(String name, String description, LatLon coordinate) {
            this.name = name;
            this.description = description;
            this.coordinate = coordinate;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(WikipediaEntry o) {
            return name.compareTo(o.name);
        }
    }

    class WikipediaDownloadAction extends AbstractAction {

        public WikipediaDownloadAction() {
            super(tr("Reload"), ImageProvider.get("dialogs", "refresh"));
            putValue(SHORT_DESCRIPTION, tr("Fetches all coordinates from Wikipedia in the current view"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // determine bbox
                LatLon min = Main.map.mapView.getLatLon(0, Main.map.mapView.getHeight());
                LatLon max = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
                final String bbox = min.lon() + "," + min.lat() + "," + max.lon() + "," + max.lat();
                // construct url
                final String url = "http://toolserver.org/~kolossos/geoworld/marks.php?"
                        + "bbox=" + bbox + "&lang=" + LanguageInfo.getWikiLanguagePrefix();
                System.out.println("Wikipedia: GET " + url);
                // parse XML document
                final XPathExpression xpathPlacemark = XPathFactory.newInstance().newXPath().compile("//Placemark");
                final XPathExpression xpathName = XPathFactory.newInstance().newXPath().compile("name/text()");
                final XPathExpression xpathCoord = XPathFactory.newInstance().newXPath().compile("Point/coordinates/text()");
                final XPathExpression xpathDescr = XPathFactory.newInstance().newXPath().compile("description");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(url).openStream());
                NodeList nodes = (NodeList) xpathPlacemark.evaluate(doc, XPathConstants.NODESET);
                // construct WikipediaEntry for each XML element
                List<WikipediaEntry> entries = new LinkedList<WikipediaEntry>();
                for (int i = 0; i < nodes.getLength(); i++) {
                    final String[] coord = xpathCoord.evaluate(nodes.item(i)).split(",");
                    if (coord.length <= 2) {
                        continue;
                    }
                    final String name = xpathName.evaluate(nodes.item(i));
                    final String descr = xpathDescr.evaluate(nodes.item(i));
                    entries.add(new WikipediaEntry(name, descr,
                            new LatLon(Double.parseDouble(coord[1]), Double.parseDouble(coord[0]))));
                }
                Collections.sort(entries);
                // add entries to list model
                model.clear();
                for (WikipediaEntry i : entries) {
                    model.addElement(i);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class OpenWikipediaArticleAction extends AbstractAction {

        public OpenWikipediaArticleAction() {
            super(tr("Open Article"));
            putValue(SHORT_DESCRIPTION, tr("Opens the Wikipedia article of the selected item in a browser"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedValue() != null) {
                Matcher m = Pattern.compile(".*href=\"([^\"]*)\".*").matcher(list.getSelectedValue().description);
                if (m.matches()) {
                    System.out.println("Wikipedia: opening " + m.group(1));
                    OpenBrowser.displayUrl(m.group(1));
                } else {
                    System.err.println("No match: " + list.getSelectedValue().description);
                }
            }
        }
    }
}
