// License: GPL. See LICENSE file for details.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.SearchAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.LanguageInfo;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WikipediaToggleDialog extends ToggleDialog implements MapView.EditLayerChangeListener, DataSetListenerAdapter.Listener {

    public WikipediaToggleDialog() {
        super(tr("Wikipedia"), "wikipedia", tr("Fetch Wikipedia articles with coordinates"), null, 150);
        createLayout(list, true, Arrays.asList(
                new SideButton(new WikipediaLoadCoordinatesAction()),
                new SideButton(new WikipediaLoadCategoryAction()),
                new SideButton(new AddWikipediaTagAction()),
                new SideButton(new OpenWikipediaArticleAction()),
                new SideButton(new WikipediaSettingsAction(), false)));
        setTitle(/* I18n: [language].Wikipedia.org */ tr("{0}.Wikipedia.org", wikipediaLang.get()));
    }
    final StringProperty wikipediaLang = new StringProperty("wikipedia.lang", LanguageInfo.getJOSMLocaleCode().substring(0, 2));
    final Set<String> articles = new HashSet<String>();
    final DefaultListModel model = new DefaultListModel();
    final JList list = new JList(model) {

        {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && getSelectedValue() != null) {
                        final WikipediaEntry entry = (WikipediaEntry) getSelectedValue();
                        if (entry.coordinate != null) {
                            BoundingXYVisitor bbox = new BoundingXYVisitor();
                            bbox.visit(entry.coordinate);
                            Main.map.mapView.recalculateCenterScale(bbox);
                        }
                        SearchAction.search(entry.name.replaceAll("\\(.*\\)", ""), SearchAction.SearchMode.replace);
                    }
                }
            });

            setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public JLabel getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    final WikipediaEntry entry = (WikipediaEntry) value;
                    if (entry.getWiwosmStatus() != null && entry.getWiwosmStatus()) {
                        label.setIcon(ImageProvider.getIfAvailable("misc", "grey_check"));
                        label.setToolTipText(/* I18n: WIWOSM server already links Wikipedia article to object/s */ tr("Available via WIWOSM server"));
                    } else if (articles.contains(entry.wikipediaArticle)) {
                        label.setIcon(ImageProvider.getIfAvailable("misc", "green_check"));
                        label.setToolTipText(/* I18n: object/s from dataset contain link to Wikipedia article */ tr("Available in local dataset"));
                    } else {
                        label.setToolTipText(tr("Not linked yet"));
                    }
                    if (entry.description != null) {
                        label.setToolTipText("<html>" + entry.description + "</html>");
                    }
                    return label;
                }
            });
        }
    };

    static class WikipediaLangArticle {

        final String lang, article;

        public WikipediaLangArticle(String lang, String article) {
            this.lang = lang;
            this.article = article;
        }

        public static WikipediaLangArticle parseFromUrl(String url) {
            if (url == null) {
                return null;
            }
            // decode URL for nicer value
            url = decodeURL(url);
            // extract Wikipedia language and
            final Matcher m = Pattern.compile("https?://(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
            if (!m.matches()) {
                return null;
            }
            return new WikipediaLangArticle(m.group(1), m.group(2));
        }
    }

    static class WikipediaEntry implements Comparable<WikipediaEntry> {

        final String name, description;
        final String wikipediaLang, wikipediaArticle;
        final LatLon coordinate;
        private Boolean wiwosmStatus;

        public WikipediaEntry(String name, String description, LatLon coordinate) {
            this.name = name;
            this.description = description;
            this.coordinate = coordinate;

            final WikipediaLangArticle wp = WikipediaLangArticle.parseFromUrl(getHrefFromDescription());
            if (wp == null) {
                System.err.println("Could not extract Wikipedia tag from: " + getHrefFromDescription());
            }
            this.wikipediaLang = wp == null ? null : wp.lang;
            this.wikipediaArticle = wp == null ? null : wp.article;
        }

        public WikipediaEntry(String name, String wikipediaLang, String wikipediaArticle) {
            this.name = name;
            this.description = null;
            this.wikipediaLang = wikipediaLang;
            this.wikipediaArticle = wikipediaArticle;
            this.coordinate = null;
        }

        protected final String getHrefFromDescription() {
            if (description == null) {
                return null;
            }
            final Matcher m = Pattern.compile(".*href=\"(.+?)\".*").matcher(description);
            if (m.matches()) {
                return m.group(1);
            } else {
                System.err.println("Could not parse URL from: " + description);
                return null;
            }
        }

        protected final Tag createWikipediaTag() {
            return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
        }

        private void updateWiwosmStatus() {
            try {
                final String url = "http://toolserver.org/~master/osmjson/getGeoJSON.php?action=check"
                        + "&lang=" + wikipediaLang
                        + "&article=" + encodeURL(wikipediaArticle);
                System.out.println("Wikipedia: GET " + url);
                final Scanner scanner = new Scanner(new URL(url).openStream());
                wiwosmStatus = scanner.hasNextInt() && scanner.nextInt() == 1;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void setWiwosmStatus(Boolean wiwosmStatus) {
            this.wiwosmStatus = wiwosmStatus;
        }

        public Boolean getWiwosmStatus() {
            return wiwosmStatus;
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

    private void setWikipediaEntries(Collection<WikipediaEntry> entries) {
        Collection<String> articleNames = new ArrayList<String>();
        for (WikipediaEntry i : entries) {
            articleNames.add(i.wikipediaArticle);
        }
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        if (!articleNames.isEmpty()) {
            final String url = "https://toolserver.org/~simon04/getGeoJSONStatus.php"
                    + "?lang=" + wikipediaLang.get()
                    + "&articles=" + encodeURL(Utils.join(",", articleNames));
            System.out.println("Wikipedia: GET " + url);

            try {
                final Scanner scanner = new Scanner(new URL(url).openStream()).useDelimiter("\n");
                while (scanner.hasNext()) {
                    //[article]\t[0|1]
                    final String line = scanner.next();
                    final String[] x = line.split("\t");
                    if (x.length == 2) {
                        status.put(x[0], "1".equals(x[1]));
                    } else {
                        System.err.println("Unknown element " + line);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        model.clear();
        for (WikipediaEntry i : entries) {
            i.setWiwosmStatus(status.get(i.wikipediaArticle));
            model.addElement(i);
        }
    }

    class WikipediaLoadCoordinatesAction extends AbstractAction {

        public WikipediaLoadCoordinatesAction() {
            super(tr("Coordinates"), ImageProvider.get("dialogs", "refresh"));
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
                List<WikipediaEntry> entries = new ArrayList<WikipediaEntry>(nodes.getLength());
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
                setWikipediaEntries(entries);
                setTitle(/* I18n: [language].Wikipedia.org: coordinates */ tr("{0}.Wikipedia.org: coordinates", wikipediaLang.get()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class WikipediaLoadCategoryAction extends AbstractAction {

        public WikipediaLoadCategoryAction() {
            super(tr("Category"), ImageProvider.get("dialogs", "refresh"));
            putValue(SHORT_DESCRIPTION, tr("Fetches a list of all Wikipedia articles of a category"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final String category = JOptionPane.showInputDialog(
                        Main.parent,
                        tr("Enter the Wikipedia category"));
                if (category == null) {
                    return;
                }
                final String url = "http://toolserver.org/~daniel/WikiSense/CategoryIntersect.php?"
                        + "wikilang=" + wikipediaLang.get()
                        + "&wikifam=.wikipedia.org"
                        + "&basecat=" + encodeURL(category)
                        + "&basedeep=3&templates=&mode=al&format=csv";
                System.out.println("Wikipedia: GET " + url);
                final Scanner scanner = new Scanner(new URL(url).openStream()).useDelimiter("\n");
                final List<WikipediaEntry> entries = new ArrayList<WikipediaEntry>();
                while (scanner.hasNext()) {
                    final String article = scanner.next().split("\t")[1].replace("_", " ");
                    entries.add(new WikipediaEntry(article, wikipediaLang.get(), article));
                }
                Collections.sort(entries);
                setWikipediaEntries(entries);
                setTitle(/* I18n: [language].Wikipedia.org: [category] */ tr("{0}.Wikipedia.org: {1}", wikipediaLang.get(), category));
            } catch (IOException ex) {
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
                WikipediaEntry entry = (WikipediaEntry) list.getSelectedValue();
                final String url;
                if (entry.getHrefFromDescription() != null) {
                    url = entry.getHrefFromDescription();
                } else {
                    url = "http://" + entry.wikipediaLang + ".wikipedia.org/wiki/"
                            + encodeURL(entry.wikipediaArticle.replace(" ", "_"));
                }
                System.out.println("Wikipedia: opening " + url);
                OpenBrowser.displayUrl(url);
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
                    Main.parent,
                    tr("Enter the Wikipedia language"),
                    wikipediaLang.get());
            if (lang != null) {
                wikipediaLang.put(lang);
                updateWikipediaArticles();
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
                Tag tag = ((WikipediaEntry) list.getSelectedValue()).createWikipediaTag();
                if (tag != null) {
                    ChangePropertyCommand cmd = new ChangePropertyCommand(
                            Main.main.getCurrentDataSet().getSelected(),
                            tag.getKey(), tag.getValue());
                    Main.main.undoRedo.add(cmd);
                }
            }
        }
    }

    protected void updateWikipediaArticles() {
        articles.clear();
        if (Main.main != null && Main.main.getCurrentDataSet() != null) {
            for (final OsmPrimitive p : Main.main.getCurrentDataSet().allPrimitives()) {
                articles.addAll(getWikipediaArticles(p));
            }
        }
    }

    protected Collection<String> getWikipediaArticles(OsmPrimitive p) {
        Collection<String> r = new ArrayList<String>();
        final Map<String, String> tags = p.getKeys();
        // consider wikipedia=[lang]:*
        final String wp = tags.get("wikipedia");
        if (wp != null && wp.startsWith("http")) {
            //wikipedia=http...
            final WikipediaLangArticle item = WikipediaLangArticle.parseFromUrl(wp);
            if (item != null && wikipediaLang.get().equals(item.lang)) {
                r.add(item.article.replace("_", " "));
            }
        } else if (wp != null) {
            //wikipedia=[lang]:[article]
            String[] item = decodeURL(wp).split(":", 2);
            if (item.length == 2 && wikipediaLang.get().equals(item[0])) {
                r.add(item[1].replace("_", " "));
            }
        }
        // consider wikipedia:[lang]=*
        final String wpLang = tags.get("wikipedia:" + wikipediaLang.get());
        if (wpLang != null && wpLang.startsWith("http")) {
            //wikipedia:[lang]=http...
            final WikipediaLangArticle item = WikipediaLangArticle.parseFromUrl(wpLang);
            if (wikipediaLang.get().equals(item.lang)) {
                r.add(item.article.replace("_", " "));
            }
        } else if (wpLang != null) {
            //wikipedia:[lang]=[lang]:[article]
            //wikipedia:[lang]=[article]
            String[] item = decodeURL(wpLang).split(":", 2);
            r.add(item[item.length == 2 ? 1 : 0].replace("_", " "));
        }
        return r;
    }

    private final DataSetListenerAdapter dataChangedAdapter = new DataSetListenerAdapter(this);

    @Override
    public void showNotify() {
        DatasetEventManager.getInstance().addDatasetListener(dataChangedAdapter, FireMode.IN_EDT_CONSOLIDATED);
        MapView.addEditLayerChangeListener(this);
        updateWikipediaArticles();
    }

    @Override
    public void hideNotify() {
        DatasetEventManager.getInstance().removeDatasetListener(dataChangedAdapter);
        MapView.removeEditLayerChangeListener(this);
        articles.clear();
    }

    @Override
    public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
        updateWikipediaArticles();
        list.repaint();
    }

    @Override
    public void processDatasetEvent(AbstractDatasetChangedEvent event) {
        updateWikipediaArticles();
        list.repaint();
    }

    public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
