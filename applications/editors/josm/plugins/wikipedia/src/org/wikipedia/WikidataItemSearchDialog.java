// License: GPL. For details, see LICENSE file.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.SearchTextResultListPanel;
import org.openstreetmap.josm.tools.Utils;

public final class WikidataItemSearchDialog extends ExtendedDialog {

    private final Selector selector;
    private static final WikidataItemSearchDialog INSTANCE = new WikidataItemSearchDialog();

    private WikidataItemSearchDialog() {
        super(Main.parent, tr("Search Wikidata items"), new String[]{tr("Add ''wikipedia'' tag"), tr("Cancel")});
        this.selector = new Selector();
        this.selector.setDblClickListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonAction(0, null);
            }
        });
        setContent(selector, false);
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
        selector.init();
        super.showDialog();
        selector.clearSelection();
        return this;
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
        if (buttonIndex != 0) {
            return;
        }
        WikipediaToggleDialog.AddWikipediaTagAction.addTag(selector.getSelectedItem());
    }

    private static class Selector extends SearchTextResultListPanel<WikipediaApp.WikidataEntry> {

        final Debouncer debouncer = new Debouncer(
                Executors.newSingleThreadScheduledExecutor(Utils.newThreadFactory("wikidata-search-%d", Thread.NORM_PRIORITY)));

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

        public WikipediaApp.WikidataEntry getSelectedItem() {
            final WikipediaApp.WikidataEntry selected = lsResult.getSelectedValue();
            if (selected != null) {
                return selected;
            } else if (!lsResultModel.isEmpty()) {
                return lsResultModel.getElementAt(0);
            } else {
                return null;
            }
        }

        @Override
        protected void filterItems() {
            final String query = edSearchText.getText();
            debouncer.debounce(Void.class, new Runnable() {
                @Override
                public void run() {
                    final List<WikipediaApp.WikidataEntry> entries = query == null || query.isEmpty()
                            ? Collections.<WikipediaApp.WikidataEntry>emptyList()
                            : WikipediaApp.getWikidataEntriesForQuery(WikipediaToggleDialog.wikipediaLang.get(), query, Locale.getDefault());
                    GuiHelper.runInEDT(new Runnable() {
                        @Override
                        public void run() {
                            lsResultModel.setItems(entries);
                        }
                    });
                }
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
