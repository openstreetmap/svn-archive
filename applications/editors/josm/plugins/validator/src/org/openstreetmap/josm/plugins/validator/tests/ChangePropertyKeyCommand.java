package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.util.NameVisitor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Command that replaces the key of several objects
 *
 */
public class ChangePropertyKeyCommand extends Command {
    /**
     * All primitives, that are affected with this command.
     */
    private final List<OsmPrimitive> objects;
    /**
     * The key that is subject to change.
     */
    private final String key;
    /**
     * The mew key.
     */
    private final String newKey;

    /**
     * Constructor
     *
     * @param objects all objects subject to change replacement
     * @param key The key to replace
     * @param newKey the new value of the key
     */
    public ChangePropertyKeyCommand(Collection<? extends OsmPrimitive> objects, String key, String newKey) {
        this.objects = new LinkedList<OsmPrimitive>(objects);
        this.key = key;
        this.newKey = newKey;
    }

    @Override public boolean executeCommand() {
        if (!super.executeCommand()) return false; // save old
        for (OsmPrimitive osm : objects) {
            if(osm.hasKeys())
            {
                osm.modified = true;
                String oldValue= osm.get(key);
                osm.put(newKey, oldValue );
                osm.remove(key);
            }
        }
        return true;
    }

    @Override public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        modified.addAll(objects);
    }

    @Override public MutableTreeNode description() {
        String text = tr( "Replace \"{0}\" by \"{1}\" for", key, newKey);
        if (objects.size() == 1) {
            NameVisitor v = new NameVisitor();
            objects.iterator().next().visit(v);
            text += " "+tr(v.className)+" "+v.name;
        } else
            text += " "+objects.size()+" "+trn("object","objects",objects.size());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new JLabel(text, ImageProvider.get("data", "key"), JLabel.HORIZONTAL));
        if (objects.size() == 1)
            return root;
        NameVisitor v = new NameVisitor();
        for (OsmPrimitive osm : objects) {
            osm.visit(v);
            root.add(new DefaultMutableTreeNode(v.toLabel()));
        }
        return root;
    }
}
