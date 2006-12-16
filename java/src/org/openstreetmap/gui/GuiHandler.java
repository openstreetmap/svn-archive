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
 * The advanced_table is build up so that the names of the cells are 
 *  key_name resp. value_name, if "name" is the content of the first row.
 * 
 * Example: An table with the following content
 * 
 * name    Baker Street
 * class   primary
 * oneway  0
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
    
    /**
     * The name (eg 'highway') of the last tag we set via the
     *  basic tab.
     * We need to know this, so if the user picks something else
     *  via the basic tab, we can remove the previous one.
     */
    private String lastBasicType = null;

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

    /**
     * Called when the object's name is changed on the basic tab
     */
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

    /**
     * Update the basic tab, based on the values in the advanced tab.
     * This will always update the name, blanking it if there is none.
     * If your basic tab is class based, it'll update that
     * If your basic tab is type based, it'll try to find a name/value pair
     *  in the type options that matches something you have set.
     */
    protected void updateBasic() {
    	// Update the name, with the current name tag
        Object name = find("value_name");
        setString(find("name"), "text", name==null ? "" : getString(name, "text"));
        
        // Is it class based, or type based?
        Object classObj = find("class");
        Object cl = find("value_class");
        if(classObj != null) {
        	// Class based
        	// Just set the basic tab's class field to the current
        	//  value of the 'class' tag
            setString(classObj, "text", cl==null ? "" : getString(cl, "text"));
        } else {
        	// Type based.
        	// Start with blanking the Type field
        	Object type = find("type");
        	setString(type, "text", "");
        	
        	// Walk through the type's choices, until we find one
        	//  which that tag has been set
        	Object[] choices = getItems( type );
        	for(int i=0; i<choices.length; i++) {
        		// Grab the details of the current choice
        		String displayText = getString(choices[i], "text");
            	String[] nv = getOSMProperty(choices[i]);
            	if(nv == null) { continue; }
            	String optName = nv[0];
            	String optVal = nv[1];
            	
            	// Do we already have a property with this choice's name?
            	// (i.e. tag name is 'highway', do they already have a 
            	//   'highway' entry?)
            	Object nameProp = find("value_" + optName);
            	if(nameProp == null) {
            		// We don't, so skip on
            		continue;
            	}
            	
            	// Does the property have the same value as this choice?
            	// (i.e. choice is 'highway=trunk', does that match 
            	//   whatever the current highway tag is?)
            	String valProp = getString(nameProp, "text");
            	if(valProp.equals(optVal)) {
            		// We have a match, select this for the basic tab
            		setString(type, "text", displayText);
            		break;
            	}
        	}
        }
    }

    /**
     * The simple value 'class' has been changed on the
     *  basic tab
     */
    public void classChanged() {
        Object name = getTableValue("class");
        String val = getString(find("class"), "text");
        setString(name, "text", val);
    }
    
    /**
     * The complex value 'type' has been changed on the
     *  basic tab. Figure out the name/value pair that
     *  goes with it, and update it
     */
    public void typeChanged(Object combobox, String type) {
    	// Grab the selected choice object
    	Object choice = getSelectedItem(combobox);
    	
    	String text = getString(choice, "text");
    	if(!text.equals(type)) {
    		System.err.println("Warning - the two text's didn't match - " + type + " vs " + text);
    		return;
    	}
    	
    	// Grab OSM data from the choice object
    	String[] nv = getOSMProperty(choice);
    	if(nv == null) { return; }
    	String name = nv[0];
    	String value = nv[1];
    	
    	// Remove the last one, if there is one
    	if(lastBasicType != null) {
    		remove( find("key_" + lastBasicType) );
    		remove( find("value_" + lastBasicType) );
    	}
    	lastBasicType = name;
    	
    	// Save the new name+value
    	setString( getTableValue(name), "text", value );
    }

    /**
     * They have changed something on the advanced tab, update it
     */
    public void tableSelectionChanged() {
        Object sel = getSelectedItem(find("advanced_table"));
        if (sel == null)
            return; // deselected
        setString(find("edit_key"), "text", getString(getItem(sel, 0), "text"));
        setString(find("edit_value"), "text", getString(getItem(sel, 1), "text"));
    }

    /**
     * They have added a new tag
     */
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

    /**
     * They have removed an old tag
     */
    public void propDelete() {
        Object selected = getSelectedItem(find("advanced_table"));
        if (selected != null) {
            remove(selected);
            setString(find("edit_key"), "text", "");
            setString(find("edit_value"), "text", "");
            updateBasic();
        }
    }

    /**
     * The user has edited the key (eg highway) of an 
     *  existing entry in the advanced tab
     */
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

    /**
     * The user has edited the value of a tag (eg primary of
     *  'highway=primary') of an existing entry in the 
     *  advanced tab
     */
    public void valueChanged() {
        String value = getString(find("edit_value"), "text");
        Object row = getSelectedItem(find("advanced_table"));
        if (row == null)
            return; // nothing selected
        setString(getItem(row, 1), "text", value);
        updateBasic();
    }

    /**
     * The user hit ok, so save the tags etc back onto the
     *  underlying OSM object.
     */
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
    
    /**
     * Grab the OSM property (if there is one) for the given (normally
     *  choice) object, and return it as a String[] which is node,value
     */
    private String[] getOSMProperty(Object o) {
    	Object prop = getProperty(o, "osm");
    	if(o == null || !(prop instanceof String)) { return null; }
    	
    	String osmData = (String)prop;
    	int splitAt = osmData.indexOf('=');
    	if(splitAt == -1) {
    		System.err.println("Warning - OSM data wasn't name=value - " + osmData);
    	}
    	String name = osmData.substring(0, splitAt);
    	String value = osmData.substring(splitAt + 1);
    	
    	return new String[] { name, value }; 
    }
}
