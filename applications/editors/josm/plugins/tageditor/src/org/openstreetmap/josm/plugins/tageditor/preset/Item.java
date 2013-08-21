package org.openstreetmap.josm.plugins.tageditor.preset;

import java.util.ArrayList;
import java.util.List;

public class Item  extends AbstractNameIconProvider {

    //private final static Logger logger = Logger.getLogger(Item.class.getName());

    private String label;
    private List<Tag> tags;
    private Group parent;

    public Item() {
        tags = new ArrayList<Tag>();
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Item(String name) {
        setName(name);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        StringBuilder builder  = new StringBuilder();
        builder.append("[")
        .append(getClass().getName())
        .append(":")
        .append("name=")
        .append(name)
        .append("]");

        return builder.toString();
    }
}
