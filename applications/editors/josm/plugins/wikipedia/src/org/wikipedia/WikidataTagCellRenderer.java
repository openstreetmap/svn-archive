package org.wikipedia;

import java.awt.Component;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.LanguageInfo;
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
            final String label = WikipediaApp.getLabelForWikidata(id, LanguageInfo.getJOSMLocaleCode());
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
        if (!WikipediaApp.WIKIDATA_PATTERN.matcher(id).matches()) {
            return null;
        }

        if (!labelCache.containsKey(id)) {
            labelCache.put(id, Main.worker.submit(new LabelLoader(id, table)));
        }
        try {
            final String label = labelCache.get(id).isDone() ? labelCache.get(id).get() : null;
            final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setText("<html>" + Utils.escapeReservedCharactersHTML(id) + (label != null
                    ? " <span color='gray'>" + Utils.escapeReservedCharactersHTML(label) + "</span>"
                    : ""));
            component.setToolTipText(label);
            return component;
        } catch (InterruptedException | ExecutionException e) {
            Main.warn("Could not fetch Wikidata label for " + id);
            Main.warn(e);
            return null;
        }
    }
}
