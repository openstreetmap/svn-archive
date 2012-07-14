// License: GPL. See LICENSE file for details.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import org.wikipedia.WikipediaApp.WikipediaEntry;

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

    private void setWikipediaEntries(List<WikipediaEntry> entries) {
        Collections.sort(entries);
        WikipediaApp.updateWIWOSMStatus(wikipediaLang.get(), entries);
        model.clear();
        for (WikipediaEntry i : entries) {
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
                List<WikipediaEntry> entries = WikipediaApp.getEntriesFromCoordinates(
                        wikipediaLang.get(), min, max);
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
            final String category = JOptionPane.showInputDialog(
                    Main.parent,
                    tr("Enter the Wikipedia category"));
            if (category == null) {
                return;
            }
            List<WikipediaEntry> entries = WikipediaApp.getEntriesFromCategory(wikipediaLang.get(), category);
            setWikipediaEntries(entries);
            setTitle(/* I18n: [language].Wikipedia.org: [category] */ tr("{0}.Wikipedia.org: {1}", wikipediaLang.get(), category));
        }
    }

    class OpenWikipediaArticleAction extends AbstractAction {

        public OpenWikipediaArticleAction() {
            super(tr("Open Article"), ImageProvider.getIfAvailable("browser"));
            putValue(SHORT_DESCRIPTION, tr("Opens the Wikipedia article of the selected item in a browser"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedValue() != null) {
                final String url = ((WikipediaEntry) list.getSelectedValue()).getBrowserUrl();
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
                articles.addAll(WikipediaApp.getWikipediaArticles(wikipediaLang.get(), p));
            }
        }
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
}
