package org.openstreetmap.josm.plugins.czechaddress.proposal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Stores a list of {@link ProposalContainer}s, which represent the whole
 * changeset of alternations to be applied to some nodes.
 *
 * ProposalDatabase also impemets {@link TreeModel}, which allows an easy
 * display of such object in an {@link TreeView}.
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class ProposalDatabase implements TreeModel {

    /**
     * The internal database of {@link ProposalContainer}s.
     */
    protected List<ProposalContainer> changeSet =
             new ArrayList<ProposalContainer>();

    /**
     * Listeners for the {@link TreeModel} interface.
     */
    protected List<TreeModelListener> listeners =
            new ArrayList<TreeModelListener>();

    /**
     * The root element for the {@link TreeView}.
     */
    protected String root = new String("Navrhované změny");

    /**
     * Adds a new {@link ProposalContainer} to the internal database.
     * @param pac
     */
    public void addContainer(ProposalContainer newContainer) {
        assert !changeSet.contains(newContainer)
             : "Containers in the database must unique.";

        changeSet.add(newContainer);
    }

    /**
     * Removes the given {@link ProposalContainer} from the internal list.
     * @param containerToAdd
     */
    public void removeContainer(ProposalContainer containerToAdd) {
        changeSet.remove(containerToAdd);
    }

    /**
     * Finds a {@link PropsalContainer} containing given {@code Primitive}.
     *
     * <p>If no container with the given primitive is found, null is returned.
     * If there are multiple containers (which should not be the case),
     * the first is returned.</p>
     *
     * @param primitive the primitive to be found
     * @return the ProposalContainer containing primitive or null
     */
    public ProposalContainer findContainer(OsmPrimitive primitive) {
        for (ProposalContainer pac : changeSet)
            if (pac.getTarget().equals(primitive))
                    return pac;
        return null;
    }

    /**
     * Adds proposals corresponding to a primitive into the database.
     *
     * <p>If the primitive is already in the database, the proposal
     * is added to its container. If not, a new container is created.</p>
     *
     * @param primitive
     * @param proposal
     */
    public void addProposals(OsmPrimitive primitive,
                             Collection<Proposal> proposal) {
        
        ProposalContainer container = findContainer(primitive);
        if (container == null) {
            container = new ProposalContainer(primitive);
            addContainer(container);
        }

        container.addProposals(proposal);
    }

    /**
     * Replaces the internal changeset of {@link ProposalContainer}s
     * with a new one.
     *
     * @param newChangeSet new changeset to replace the current one
     */
    public void setContainers(ArrayList<ProposalContainer> newChangeSet) {
        this.changeSet = newChangeSet;
    }

    /**
     * Removes all {@link ProposalContainer}s from the changeset.
     */
    public void clear() {
        changeSet.clear();
    }

    /**
     * Gives a reference to the internal list of {@link ProposalContainer}s.
     * 
     * @return the refernence to internal changeset.
     */
    public List<ProposalContainer> getContainers() {
        return changeSet;
    }

    /**
     * Applies all {@link Proposal}s in all {@link ProposalContainer}s.
     */
    public void applyAll() {
        for (ProposalContainer a : changeSet)
            a.applyAll();
    }

//==============================================================================
//  IMPLEMENTATION OF THE TREEMODEL INTERFACE
//==============================================================================

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent.equals(root))
            return changeSet.get(index);

        if (parent instanceof ProposalContainer)
            return ((ProposalContainer) parent).getProposals().get(index);

        return null;
    }

    public int getChildCount(Object parent) {
        if (parent.equals(root))
            return changeSet.size();

        if (parent instanceof ProposalContainer)
            return ((ProposalContainer) parent).getProposals().size();

        return 0;
    }

    public boolean isLeaf(Object node) {
        if (node.equals(root))
            return changeSet.size() == 0;

        if (node instanceof ProposalContainer)
            return ((ProposalContainer) node).getProposals().size() == 0;

        return true;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // We are a read-only model... Nothing to do here.
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent.equals(root))
            return changeSet.indexOf(child);

        if (parent instanceof ProposalContainer)
            return ((ProposalContainer) parent).getProposals().indexOf(child);

        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    /**
     * Deletes a proposal at the given tree path.
     *
     * @param path path containing the element to be deleted
     */
    public void deteleObjectAtPath(TreePath path) {

        // The root element cannot be deleted
        if (path.getPathCount() <= 1)
            return;


        // If path-length is 2, the whole ProposalContainer is deleted.
        if (path.getPathCount() == 2) {
            changeSet.remove((ProposalContainer) path.getPathComponent(1));

            TreeModelEvent event = new TreeModelEvent(this, path);
            for (TreeModelListener l : listeners)
                l.treeNodesRemoved(event);
            return;
        }


        // If path-length is 3, only a single Proposal is deleted.
        ProposalContainer ac = (ProposalContainer) path.getPathComponent(1);
        if (path.getPathCount() == 3) {
            ac.getProposals().remove((Proposal) path.getPathComponent(2));

            TreeModelEvent event = new TreeModelEvent(this, path);
            for (TreeModelListener l : listeners)
                l.treeNodesRemoved(event);
            return;
        }

        assert false : path;
        return;
    }
}
