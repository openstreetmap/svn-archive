// License: GPL. Copyright 2013 by Alexei Kasatkin

package org.openstreetmap.josm.plugins.utilsplugin2.multitagger;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.table.AbstractTableModel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Geometry;

/**
 *
 */
public class MultiTaggerTableModel extends AbstractTableModel implements SelectionChangedListener {

    ArrayList<OsmPrimitive> list = new ArrayList<OsmPrimitive>(50);
    String mainTags[] = new String[]{};
    boolean isSpecialTag[] = new boolean[]{};

    
    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return mainTags.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!isSpecialTag[columnIndex]) {
            return list.get(rowIndex).get(mainTags[columnIndex]);
        }
        String var = mainTags[columnIndex];
        OsmPrimitive p = list.get(rowIndex);
        if (var.equals("id")) {
            return String.valueOf(p.getUniqueId());
        } else if (var.equals("type")) {
            return OsmPrimitiveType.from(p).toString().substring(0,1);
        } else if (var.equals("area")) {
            if (p.getType() == OsmPrimitiveType.CLOSEDWAY) {
                return String.format("%.1f", Geometry.closedWayArea((Way) p));
            } else {
                return tr("not closed");
            }
        } else if (var.equals("length")) {
            if (p instanceof Way) {
                return String.format("%.1f", ((Way) p).getLength());
            }
        } 
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
   
    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        Main.info("new selection: n="+newSelection.size());
        list.clear();
        list.addAll(newSelection);
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !isSpecialTag[columnIndex];
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (isSpecialTag[columnIndex]) return;
        if (columnIndex >= getColumnCount() || rowIndex >= getRowCount()) return;
        String val = ((String) value).trim();
        OsmPrimitive sel = list.get(rowIndex);
        String key = mainTags[columnIndex];
        String newValue = sel.get(key);
        if (newValue == null) newValue="";
        if (!val.equals(newValue)) {
            Main.main.undoRedo.add(new ChangePropertyCommand(sel, key, (String) value));
        }
    }
    

    @Override
    public String getColumnName(int column) {
        return mainTags[column];
    }
    
    public OsmPrimitive getPrimitiveAt(int number) {
        if (number<0 || number>=list.size()) {
            return null;
        } else {
            return list.get(number);
        }
    }
    
    public void setupColumnsFromText(String txt) {
        String[] tags = txt.trim().split("[\\s,]+");
        mainTags = new String[tags.length];
        isSpecialTag = new boolean[tags.length];
        int i=0;
        for (String t: tags) {
            if (t.startsWith("${") && t.endsWith("}")) {
                mainTags[i] = t.substring(2, t.length()-1).trim();
                isSpecialTag[i] = true;
            } else {
                mainTags[i] = t;
                isSpecialTag[i] = false;
            }
            i++;
        }
    }

    public String getSearchExpression() {
        StringBuilder sb = new StringBuilder();
        boolean notFirst = false;
        for (int i=0; i<mainTags.length; i++) {
            if (!isSpecialTag[i]) {
                 if (notFirst) sb.append(" | ");
                 sb.append("\"");
                 sb.append(mainTags[i]);
                 sb.append("\":");
                 notFirst = true;
            }
        };
        return sb.toString();
    }
}
