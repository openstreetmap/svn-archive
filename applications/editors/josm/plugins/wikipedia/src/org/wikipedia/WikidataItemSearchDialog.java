// License: GPL. For details, see LICENSE file.
package org.wikipedia;

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
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.GBC;

public final class WikidataItemSearchDialog extends ExtendedDialog {

    private final Selector selector;
    private final AutoCompletingComboBox targetKey;
    private static final WikidataItemSearchDialog INSTANCE = new WikidataItemSearchDialog();

    private WikidataItemSearchDialog() {
        super(Main.parent, tr("Search Wikidata items"), new String[]{tr("Add Tag"), tr("Cancel")});
        this.selector = new Selector();
        this.selector.setDblClickListener(e -> buttonAction(0, null));
        this.targetKey = new AutoCompletingComboBox();
        this.targetKey.setEditable(true);
        this.targetKey.setSelectedItem(new AutoCompletionListItem("wikidata"));

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
        final DataSet editDataSet = Main.getLayerManager().getEditDataSet();
        if (editDataSet == null) {
            return;
        }
        final Collection<AutoCompletionListItem> keys = new TreeSet<>();
        // from http://wiki.openstreetmap.org/wiki/Proposed_features/Wikidata#Tagging
        keys.add(new AutoCompletionListItem("wikidata"));
        keys.add(new AutoCompletionListItem("operator:wikidata"));
        keys.add(new AutoCompletionListItem("brand:wikidata"));
        keys.add(new AutoCompletionListItem("architect:wikidata"));
        keys.add(new AutoCompletionListItem("artist:wikidata"));
        keys.add(new AutoCompletionListItem("subject:wikidata"));
        keys.add(new AutoCompletionListItem("name:etymology:wikidata"));
        editDataSet.getAutoCompletionManager().getKeys().stream()
                .filter(v -> v.getValue().contains("wikidata"))
                .forEach(keys::add);
        targetKey.setPossibleACItems(keys);
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
        if (buttonIndex != 0) {
            return;
        }
        final WikipediaApp.WikidataEntry selected = selector.getSelectedItem();
        if (selected == null) {
            return;
        }
        final String key = Tag.removeWhiteSpaces(targetKey.getEditor().getItem().toString());
        final String value = selected.createWikipediaTag().getValue();
        WikipediaToggleDialog.AddWikipediaTagAction.addTag(new Tag(key, value));
    }

    private static class Selector extends WikiSearchTextResultListPanel<WikipediaApp.WikidataEntry> {

        Selector() {
            super();
            lsResult.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    final WikipediaApp.WikidataEntry entry = (WikipediaApp.WikidataEntry) value;
                    final String labelText = "<html>" + entry.getLabelText();
                    return super.getListCellRendererComponent(list, labelText, index, isSelected, cellHasFocus);
                }
            });
        }

        @Override
        protected void filterItems() {
            final String query = edSearchText.getText();
            debouncer.debounce(getClass(), () -> {
                final List<WikipediaApp.WikidataEntry> entries = query == null || query.isEmpty()
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
