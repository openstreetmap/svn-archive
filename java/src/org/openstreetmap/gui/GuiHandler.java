package org.openstreetmap.gui;



import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.OsmPrimitive;

import thinlet.Thinlet;

/**
 * An handler class for the applet gui framework. Subclass this and add your actions
 * specified in the xml as handler functions.
 * 
 * The advanced_table is build up so that the names of the cells are key_name resp.
 * value_name, if "name" is the content of the first row.
 * 
 * Example: An table with the following content
 * 
 * name    Baker Street
 * class   primary
 * oneway  0
 * 
 * 
 * would have the following names:
 * 
 * key_name    value_name
 * key_class   value_class
 * key_oneway  value_oneway
 *
 * @author Imi
 */
public class GuiHandler extends Thinlet {

    /**
     * The gui dialog back reference behind this handler.
     */
    GuiLauncher dlg;

    protected final OsmApplet applet;
    private static int lastSelectedTab = 0;

    /**
     * The primitive that is changed via this dialog
     */
    public OsmPrimitive osm;

    public boolean cancelled = true;

    /**
     * Load the given ressource from /data/resName.xml
     */
    public GuiHandler(OsmPrimitive osm, OsmApplet applet) {
        this.osm = osm;
        this.applet = applet;
        URL ressource = getClass().getResource("/data/"+osm.getTypeName()+".xml");
        try {
            add(parse(ressource.openStream()));
        } catch (IOException e) {
            throw new RuntimeException("IO error while reading "+ressource+".", e);
        }

        if (getCount(find("mainTab")) > lastSelectedTab)
            setInteger(find("mainTab"), "selected", lastSelectedTab);

        // fill properties
        Object table = find("advanced_table");
        for (Iterator it = osm.getTags().keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            String value = (String)osm.getTags().get(key);
            Object row = createRow(key, value);
            add(table, row);
        }
    }

    public void nameChanged() {
        Object name = getTableValue("name");
        String newName = getString(find("name"), "text");
        if ("".equals(newName)) {
            remove(find("key_name"));
            remove(find("value_name"));
        } else
            setString(name, "text", newName);
    }

    /**
     * Retrieve the thinlet-object for the given property - key from the
     * advanced table. Does not return <code>null</code> but will create
     * the table entry if not there yet.
     * @param key The key name, e.g. "class"
     * @return The thinlet object. Never <code>null</code>.
     */
    protected Object getTableValue(String key) {
        Object o = find("value_"+key);
        if (o == null) {
            Object row = createRow(key, "");
            add(find("advanced_table"), row);
            o = find("value_"+key);
        }
        return o;
    }

    /**
     * Create a row for the property table and return the thinlet object
     */
    protected Object createRow(String key, String value) {
        Object row = Thinlet.create("row");
        Object cell = Thinlet.create("cell");
        setString(cell, "name", "key_"+key);
        setString(cell, "text", key);
        add(row, cell);
        cell = Thinlet.create("cell");
        setString(cell, "name", "value_"+key);
        setString(cell, "text", value);
        add(row, cell);
        return row;
    }

    /**
     * Either add the option if not already present to the property list or remove
     * it, accordingly to the object with the same name as key.
     * @param key The name of the input checkbox (has to have attribute "selected")
     * 		and also name of the property to add/remove
     */
    protected void addOrRemoveOption(String key) {
        boolean option = getBoolean(find(key), "selected");
        boolean optionTable = getStringAsBoolean(find("value_"+key));
        if (option && !optionTable) {
            Object o = getTableValue(key);
            setString(o, "text", "yes");
        } else if (!option && optionTable) {
            remove(find("key_"+key));
            remove(find("value_"+key));
        }
    }

    /**
     * Convert the string to a boolean by accepting different things as true/false.
     */
    protected boolean getStringAsBoolean(Object object) {
        if (object == null)
            return false;
        String s = getString(object, "text").toLowerCase();
        return s.equals("yes") || s.equals("1") || s.equals("true") || s.equals("on");
    }

    protected void updateBasic() {
        Object name = find("value_name");
        setString(find("name"), "text", name==null ? "" : getString(name, "text"));
        Object cl = find("value_class");
        setString(find("class"), "text", cl==null ? "" : getString(cl, "text"));
    }

    public void classChanged() {
        Object name = getTableValue("class");
        setString(name, "text", getString(find("class"), "text"));
    }

    public void tableSelectionChanged() {
        Object sel = getSelectedItem(find("advanced_table"));
        if (sel == null)
            return; // deselected
        setString(find("edit_key"), "text", getString(getItem(sel, 0), "text"));
        setString(find("edit_value"), "text", getString(getItem(sel, 1), "text"));
    }

    public void propAdd() {
        Object sel = getSelectedItem(find("advanced_table"));
        if (sel != null)
            setBoolean(sel, "selected", false);

        String key = getString(find("edit_key"), "text");
        if (key.equals(""))
            return;
        String value = getString(find("edit_value"), "text");
        if (value.equals(""))
            value = "yes";

        setString(find("edit_key"), "text", "");
        setString(find("edit_value"), "text", "");

        if (find("key_"+key) != null)
            return; // already there

        Object row = createRow(key, value);
        add(find("advanced_table"), row);
        updateBasic();
    }

    public void propDelete() {
        Object selected = getSelectedItem(find("advanced_table"));
        if (selected != null) {
            remove(selected);
            setString(find("edit_key"), "text", "");
            setString(find("edit_value"), "text", "");
            updateBasic();
        }
    }

    public void keyChanged() {
        String key = getString(find("edit_key"), "text");
        Object row = getSelectedItem(find("advanced_table"));
        if (row == null)
            return; // nothing selected
        Object item = getItem(row, 0);
        setString(item, "text", key);
        setString(item, "name", "key_"+key);
        setString(getItem(row, 1), "name", "value_"+key);
        updateBasic();
    }

    public void valueChanged() {
        String value = getString(find("edit_value"), "text");
        Object row = getSelectedItem(find("advanced_table"));
        if (row == null)
            return; // nothing selected
        setString(getItem(row, 1), "text", value);
        updateBasic();
    }

    public void ok() {
        Object tags = find("advanced_table");
        Object[] rows = getItems(tags);
        osm.getTags().clear();
        for (int i = 0; i < rows.length; ++i) {
            Object keyObj = getItem(rows[i],0);
            Object valueObj = getItem(rows[i],1);
            if (keyObj == null || valueObj == null)
                continue; // bug in thinlet: old values that did not get deleted properly
            String key = getString(keyObj, "text");
            String value = getString(valueObj,"text");
            osm.tagsput(key, value);
        }
        cancelled = false;
        lastSelectedTab = getInteger(find("mainTab"), "selected");
        dlg.setVisible(false);
    }
    public void cancel() {
        osm = null;
        lastSelectedTab = getInteger(find("mainTab"), "selected");
        dlg.setVisible(false);
    }
    
    public void mainTabChanged(int newTab) {
        lastSelectedTab = newTab;
    }
}
