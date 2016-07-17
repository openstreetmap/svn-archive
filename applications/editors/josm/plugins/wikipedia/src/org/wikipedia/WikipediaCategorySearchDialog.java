// License: GPL. For details, see LICENSE file.
package org.wikipedia;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.util.GuiHelper;

public final class WikipediaCategorySearchDialog extends ExtendedDialog {

    private final Selector selector;
    private static final WikipediaCategorySearchDialog INSTANCE = new WikipediaCategorySearchDialog();

    private WikipediaCategorySearchDialog() {
        super(Main.parent, tr("Search Wikipedia category"), new String[]{tr("Load category"), tr("Cancel")});
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
    public static synchronized WikipediaCategorySearchDialog getInstance() {
        return INSTANCE;
    }

    @Override
    public ExtendedDialog showDialog() {
        selector.init();
        super.showDialog();
        selector.clearSelection();
        selector.requestFocus();
        return this;
    }

    public String getCategory() {
        return selector.getSelectedItem();
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        super.buttonAction(buttonIndex, evt);
    }

    private static class Selector extends WikiSearchTextResultListPanel<String> {

        @Override
        protected void filterItems() {
            final String query = edSearchText.getText();
            debouncer.debounce(Void.class, new Runnable() {
                @Override
                public void run() {
                    final List<String> entries = query == null || query.isEmpty()
                            ? Collections.<String>emptyList()
                            : WikipediaApp.getCategoriesForPrefix(WikipediaToggleDialog.wikipediaLang.get(), query);
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
}
