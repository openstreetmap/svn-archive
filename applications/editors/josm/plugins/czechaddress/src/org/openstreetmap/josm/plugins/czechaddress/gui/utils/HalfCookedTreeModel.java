package org.openstreetmap.josm.plugins.czechaddress.gui.utils;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Class for shorter and faster implementations of {@link TreeModel}s.
 *
 * <p>This creates a list of {@link TreeModelListener}s and implements
 * method for adding and removing them. Moreover it allows to notify all
 * listeners with the generic message telling that the whole tree
 * has changed.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public abstract class HalfCookedTreeModel implements TreeModel {

    List<TreeModelListener> listeneres = new ArrayList<TreeModelListener>();

    protected String root;
    public Object getRoot() {
        return root;
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeneres.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeneres.remove(l);
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public void notifyAllListeners() {
        TreeModelEvent evt = new TreeModelEvent(this, new Object[] {root});

        for (TreeModelListener l : listeneres)
            l.treeNodesChanged(evt);
    }
}
