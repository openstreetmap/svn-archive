package org.openstreetmap.josm.plugins.tageditor.preset;

import java.util.ArrayList;
import java.util.List;

/**
 * Group represents a named group of preset items. Groups can be nested.
 * 
 */
public class Group extends AbstractNameIconProvider {
    
    //static final private Logger logger = Logger.getLogger(Group.class.getName());
    
    private List<Item> items = null;
    
    public Group() {
        items = new ArrayList<Item>();
    }
    
    public Group(String name) {
        this();
        setName(name);
    }
    
    public void addItem(Item item) {
        item.setParent(this);
        items.add(item);
    }
    
    public void removeItem(Item item) {
        items.remove(item);
    }
    
    public List<Item> getItems() {
        return items; 
    }
}
