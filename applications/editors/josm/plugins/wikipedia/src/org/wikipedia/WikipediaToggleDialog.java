// License: GPL. See LICENSE file for details.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.preferences.StringProperty;
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
                new SideButton(new AddWikipediaTagAction()),
                new SideButton(new OpenWikipediaArticleAction()),
                new SideButton(new WikipediaSettingsAction(), false)));
    }
    final StringProperty wikipediaLang = new StringProperty("wikipedia.lang", LanguageInfo.getJOSMLocaleCode().substring(0, 2));
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

        public String getHrefFromDescription() {
            final Matcher m = Pattern.compile(".*href=\"(.+?)\".*").matcher(description);
            if (m.matches()) {
                return m.group(1);
            } else {
                System.err.println("Could not parse URL from: " + description);
                return null;
            }
        }

        public Tag createWikipediaTag() {
            // get URL from description
            String url = getHrefFromDescription();
            if (url == null) {
                return null;
            }
            // decode URL for nicer value
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
            // extract Wikipedia language and
            final Matcher m = Pattern.compile("https?://(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
            if (!m.matches()) {
                System.err.println("Could not extract Wikipedia tag from: " + url);
                return null;
            }
            return new Tag("wikipedia", m.group(1) + ":" + m.group(2));
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
                        + "bbox=" + bbox + "&LANG=" + wikipediaLang.get();
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
                String url = list.getSelectedValue().getHrefFromDescription();
                if (url != null) {
                    System.out.println("Wikipedia: opening " + url);
                    OpenBrowser.displayUrl(url);
                }
            }
        }
    }

    class WikipediaSettingsAction extends AbstractAction {

        public WikipediaSettingsAction() {
            super(tr("Language"), ImageProvider.get("dialogs/settings"));
            putValue(SHORT_DESCRIPTION, tr("Sets the default language for the Wikipedia articles"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String lang = JOptionPane.showInputDialog(
                    WikipediaToggleDialog.this,
                    tr("Enter the Wikipedia language"),
                    wikipediaLang.get());
            if (lang != null) {
                wikipediaLang.put(lang);
            }
        }
    }

    class AddWikipediaTagAction extends AbstractAction {

        public AddWikipediaTagAction() {
            super(tr("Add Tag"), ImageProvider.get("pastetags"));
            putValue(SHORT_DESCRIPTION, tr("Adds a ''wikipedia'' tag corresponding to this article to the selected objects"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedValue() != null) {
                Tag tag = list.getSelectedValue().createWikipediaTag();
                if (tag != null) {
                    ChangePropertyCommand cmd = new ChangePropertyCommand(
                            Main.main.getCurrentDataSet().getSelected(),
                            tag.getKey(), tag.getValue());
                    Main.main.undoRedo.add(cmd);
                }
            }
        }
    }
}
