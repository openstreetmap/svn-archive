package org.wikipedia;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.Predicates;
import org.openstreetmap.josm.tools.Utils;

public class WikidataTagCellRenderer extends DefaultTableCellRenderer {

    final Map<String, Future<String>> labelCache = new ConcurrentHashMap<>();

    static class LabelLoader implements Callable<String> {
        final String id;
        JTable table;

        public LabelLoader(String id, JTable table) {
            this.id = id;
            this.table = table;
        }

        @Override
        public String call() throws Exception {
            final String label = WikipediaApp.getLabelForWikidata(id, Locale.getDefault());
            table.repaint();
            table = null;
            return label;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (column != 1
                || !(value instanceof Map<?, ?> && ((Map<?, ?>) value).size() == 1)) {
            return null;
        }
        final String key = table.getValueAt(row, 0).toString();
        if (!("wikidata".equals(key) || key != null && key.endsWith(":wikidata"))) {
            return null;
        }

        final String id = ((Map<?, ?>) value).keySet().iterator().next().toString();
        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (WikipediaApp.WIKIDATA_PATTERN.matcher(id).matches()) {
            return renderValues(Collections.singleton(id), table, component);
        } else if (id.contains(";")) {
            final List<String> ids = Arrays.asList(id.split("\\s*;\\s*"));
            if (Utils.forAll(ids, Predicates.stringMatchesPattern(WikipediaApp.WIKIDATA_PATTERN))) {
                return renderValues(ids, table, component);
            }
        }
        return null;
    }

    protected JLabel renderValues(Collection<String> ids, JTable table, JLabel component) {

        for (String id : ids) {
            if (!labelCache.containsKey(id)) {
                labelCache.put(id, Main.worker.submit(new LabelLoader(id, table)));
            }
        }

        final Collection<String> texts = new ArrayList<>(ids.size());
        for (String id : ids) {
            if (!labelCache.get(id).isDone()) {
                return null;
            }
            final String label;
            try {
                label = labelCache.get(id).get();
            } catch (InterruptedException | ExecutionException e) {
                Main.warn("Could not fetch Wikidata label for " + id);
                Main.warn(e);
                return null;
            }
            if (label == null) {
                return null;
            }
            texts.add(WikipediaApp.WikidataEntry.getLabelText(id, label));
        }
        component.setText("<html>" + Utils.join("; ", texts));
        component.setToolTipText("<html>" + Utils.joinAsHtmlUnorderedList(texts));
        return component;
    }
}
