// License: GPL. Copyright 2013 by Alexei Kasatkin

package org.openstreetmap.josm.plugins.utilsplugin2.multitagger;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Geometry;

/**
 *
 */
public class MultiTaggerTableModel extends AbstractTableModel implements SelectionChangedListener {

    ArrayList<OsmPrimitive> list = new ArrayList<>(50);
    String mainTags[] = new String[]{};
    boolean isSpecialTag[] = new boolean[]{};
    Set<OsmPrimitiveType> shownTypes = new HashSet<>();
    private boolean autoCommit = true;
    List<Command> cmds = new ArrayList<>();
    private boolean watchSelection = true;
    private JTable table;

    public MultiTaggerTableModel() {
        Collections.addAll(shownTypes, OsmPrimitiveType.values());
    }
    
    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return mainTags.length+1;
    }

    public void setWatchSelection(boolean watchSelection) {
        this.watchSelection = watchSelection;
        if (watchSelection && Main.getLayerManager().getEditLayer() != null) 
            selectionChanged(Main.getLayerManager().getEditDataSet().getSelected());
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex==0) {
            return list.get(rowIndex).getDisplayType();
        }
        if (!isSpecialTag[columnIndex-1]) {
            return list.get(rowIndex).get(mainTags[columnIndex-1]);
        }
        String var = mainTags[columnIndex-1];
        OsmPrimitive p = list.get(rowIndex);
        if (var.equals("id")) {
            return String.valueOf(p.getUniqueId());
        } else if (var.equals("type")) {
            return OsmPrimitiveType.from(p).getAPIName().substring(0,1).toUpperCase();
        } else if (var.equals("area")) {
            if (p.getDisplayType() == OsmPrimitiveType.CLOSEDWAY) {
                return String.format("%.1f", Geometry.closedWayArea((Way) p));
            } else {
                return "";
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
        if (columnIndex==0) {
            return OsmPrimitiveType.class;
        } else {
            return String.class;
        }
    }
   
    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        if (watchSelection)
            updateData(newSelection);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex==0) return false;
        return !isSpecialTag[columnIndex-1];
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex==0 || isSpecialTag[columnIndex-1]) return;
        if (columnIndex >= getColumnCount() || rowIndex >= getRowCount()) return;
        if (value==null) value="";
        String val = ((String) value).trim();
        OsmPrimitive sel = list.get(rowIndex);
        String key = mainTags[columnIndex-1];
        String newValue = sel.get(key);
        if (newValue == null) newValue="";
        if (!val.equals(newValue)) {
            Command cmd = new ChangePropertyCommand(sel, key, (String) value);
            if (autoCommit) {
                Main.main.undoRedo.add(cmd);
            } else {
                cmds.add(cmd);
            }
            
        }
    }
    

    @Override
    public String getColumnName(int column) {
        if (column==0) return "";
        return mainTags[column-1];
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

    void updateData(Collection<? extends OsmPrimitive> sel) {
       if (table.isEditing()) table.getCellEditor().stopCellEditing();
       
       list.clear();
        for (OsmPrimitive p : sel) {
            if (shownTypes.contains(p.getDisplayType())) {
                list.add(p);
            }
        }
        fireTableDataChanged();
    }

    void setAutoCommit(boolean b) {
        autoCommit = b;
    }

    void commit(String commandTitle) {
        Main.main.undoRedo.add(new SequenceCommand(commandTitle, cmds));
        cmds.clear();
    }

    void setTable(JTable t) {
        table = t;
    }
}
