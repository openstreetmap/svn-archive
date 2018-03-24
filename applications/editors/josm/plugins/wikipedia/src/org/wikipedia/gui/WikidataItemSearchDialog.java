// License: GPL. For details, see LICENSE file.
package org.wikipedia.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.tagging.ac.AutoCompletionItem;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.GBC;
import org.wikipedia.WikipediaApp;
import org.wikipedia.data.WikidataEntry;

public final class WikidataItemSearchDialog extends ExtendedDialog {

    private final Selector selector;
    private final AutoCompletingComboBox targetKey;
    private static final WikidataItemSearchDialog INSTANCE = new WikidataItemSearchDialog();

    private WikidataItemSearchDialog() {
        super(Main.parent, tr("Search Wikidata items"), tr("Add Tag"), tr("Cancel"));
        this.selector = new Selector();
        this.selector.setDblClickListener(e -> buttonAction(0, null));
        this.targetKey = new AutoCompletingComboBox();
        this.targetKey.setEditable(true);
        this.targetKey.setSelectedItem(new AutoCompletionItem("wikidata"));

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.add(selector, GBC.eop().fill(GBC.BOTH));
        panel.add(new JLabel(tr("Target key: ")));
        panel.add(targetKey, GBC.eol().fill(GBC.HORIZONTAL));
        setContent(panel, false);
        setPreferredSize(new Dimension(600, 300));
    }

    /**
     * Returns the unique instance of {@code MenuItemSearchDialog}.
     *
     * @return the unique instance of {@code MenuItemSearchDialog}.
     */
    public static synchronized WikidataItemSearchDialog getInstance() {
        return INSTANCE;
    }

    @Override
    public ExtendedDialog showDialog() {
        initTargetKeys();
        selector.init();
        super.showDialog();
        selector.clearSelection();
        selector.requestFocus();
        return this;
    }

    private void initTargetKeys() {
        final DataSet editDataSet = MainApplication.getLayerManager().getEditDataSet();
        if (editDataSet == null) {
            return;
        }
        final Collection<AutoCompletionItem> keys = new TreeSet<>();
        // from https://wiki.openstreetmap.org/wiki/Proposed_features/Wikidata#Tagging
        keys.add(new AutoCompletionItem("wikidata"));
        keys.add(new AutoCompletionItem("operator:wikidata"));
        keys.add(new AutoCompletionItem("brand:wikidata"));
        keys.add(new AutoCompletionItem("architect:wikidata"));
        keys.add(new AutoCompletionItem("artist:wikidata"));
        keys.add(new AutoCompletionItem("subject:wikidata"));
        keys.add(new AutoCompletionItem("name:etymology:wikidata"));
        AutoCompletionManager.of(editDataSet).getTagKeys().stream()
                .filter(v -> v.getValue().contains("wikidata"))
                .forEach(keys::add);
        targetKey.setPossibleAcItems(keys);
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
        if (buttonIndex != 0) {
            return;
        }
        final WikidataEntry selected = selector.getSelectedItem();
        if (selected == null) {
            return;
        }
        final String key = Tag.removeWhiteSpaces(targetKey.getEditor().getItem().toString());
        final String value = selected.createWikipediaTag().getValue();
        WikipediaToggleDialog.AddWikipediaTagAction.addTag(new Tag(key, value));
    }

    private static class Selector extends WikiSearchTextResultListPanel<WikidataEntry> {

        Selector() {
            super();
            lsResult.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    final WikidataEntry entry = (WikidataEntry) value;
                    final String labelText = "<html>" + entry.getLabelText();
                    return super.getListCellRendererComponent(list, labelText, index, isSelected, cellHasFocus);
                }
            });
        }

        @Override
        protected void filterItems() {
            final String query = edSearchText.getText();
            debouncer.debounce(getClass(), () -> {
                final List<WikidataEntry> entries = query == null || query.isEmpty()
                        ? Collections.emptyList()
                        : WikipediaApp.getWikidataEntriesForQuery(WikipediaToggleDialog.wikipediaLang.get(), query, Locale.getDefault());
                GuiHelper.runInEDT(() -> lsResultModel.setItems(entries));
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    public static class Action extends JosmAction {

        public Action() {
            super(tr("Search Wikidata items"), "dialogs/wikidata", null,
                    null, true, "dialogs/search-wikidata-items", false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            WikidataItemSearchDialog.getInstance().showDialog();
        }
    }
}
