// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.licensechange.util.Bag;
import org.openstreetmap.josm.data.validation.util.MultipleNameVisitor;

/**
 * A panel that displays the tree of license change problems.
 */

public class ProblemTreePanel extends JTree {

    /**
     * The validation data.
     */
    protected DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());

    /** The list of errors shown in the tree */
    private List<LicenseProblem> errors;

    /**
     * If {@link #filter} is not <code>null</code> only errors are displayed
     * that refer to one of the primitives in the filter.
     */
    private Set<OsmPrimitive> filter = null;

    private int updateCount;

    /**
     * Constructor
     * @param errors The list of errors
     */
    public ProblemTreePanel(List<LicenseProblem> errors) {
        ToolTipManager.sharedInstance().registerComponent(this);
        this.setModel(treeModel);
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.expandRow(0);
        this.setVisibleRowCount(8);
        this.setCellRenderer(new ProblemTreeRenderer());
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setErrorList(errors);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        String res = null;
        TreePath path = getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object nodeInfo = node.getUserObject();

            if (nodeInfo instanceof LicenseProblem) {
                LicenseProblem error = (LicenseProblem) nodeInfo;
                MultipleNameVisitor v = new MultipleNameVisitor();
                v.visit(error.getPrimitives());
                res = "<html>" + v.getText() + "<br>" + error.getMessage();
                res += "</html>";
            } else
                res = node.toString();
        }
        return res;
    }

    /** Constructor */
    public ProblemTreePanel() {
        this(null);
    }

    @Override
    public void setVisible(boolean v) {
        if (v)
            buildTree();
        else
            treeModel.setRoot(new DefaultMutableTreeNode());
        super.setVisible(v);
    }

    /**
     * Builds the errors tree
     */
    public void buildTree() {
        updateCount++;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        if (errors == null || errors.isEmpty()) {
            treeModel.setRoot(rootNode);
            return;
        }

        // Remember the currently expanded rows
        Set<Object> oldSelectedRows = new HashSet<Object>();
        Enumeration<TreePath> expanded = getExpandedDescendants(new TreePath(getRoot()));
        if (expanded != null) {
            while (expanded.hasMoreElements()) {
                TreePath path = expanded.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof Severity)
                    oldSelectedRows.add(userObject);
                else if (userObject instanceof String) {
                    String msg = (String) userObject;
                    msg = msg.substring(0, msg.lastIndexOf(" ("));
                    oldSelectedRows.add(msg);
                }
            }
        }

        Map<Severity, Bag<String, LicenseProblem>> errorTree = new HashMap<Severity, Bag<String, LicenseProblem>>();
        Map<Severity, HashMap<String, Bag<String, LicenseProblem>>> errorTreeDeep = new HashMap<Severity, HashMap<String, Bag<String, LicenseProblem>>>();
        for (Severity s : Severity.values()) {
            errorTree.put(s, new Bag<String, LicenseProblem>(20));
            errorTreeDeep.put(s, new HashMap<String, Bag<String, LicenseProblem>>());
        }

        for (LicenseProblem e : errors) {
            Severity s = e.getSeverity();
            String m = e.getMessage();
            if (filter != null) {
                boolean found = false;
                for (OsmPrimitive p : e.getPrimitives()) {
                    if (filter.contains(p)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    continue;
            } else
                errorTree.get(s).add(m, e);
        }

        List<TreePath> expandedPaths = new ArrayList<TreePath>();
        for (Severity s : Severity.values()) {
            Bag<String, LicenseProblem> severityErrors = errorTree.get(s);
            Map<String, Bag<String, LicenseProblem>> severityErrorsDeep = errorTreeDeep.get(s);
            if (severityErrors.isEmpty() && severityErrorsDeep.isEmpty())
                continue;

            // Severity node
            DefaultMutableTreeNode severityNode = new DefaultMutableTreeNode(s);
            rootNode.add(severityNode);

            if (oldSelectedRows.contains(s))
                expandedPaths.add(new TreePath(new Object[] { rootNode, severityNode }));

            for (Entry<String, List<LicenseProblem>> msgErrors : severityErrors.entrySet()) {
                // Message node
                List<LicenseProblem> errors = msgErrors.getValue();
                String msg = msgErrors.getKey() + " (" + errors.size() + ")";
                DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(msg);
                severityNode.add(messageNode);

                if (oldSelectedRows.contains(msgErrors.getKey())) {
                    expandedPaths.add(new TreePath(new Object[] { rootNode, severityNode, messageNode }));
                }

                for (LicenseProblem error : errors) {
                    // Error node
                    DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(error);
                    messageNode.add(errorNode);
                }
            }
            for (Entry<String, Bag<String, LicenseProblem>> bag : severityErrorsDeep.entrySet()) {
                // Group node
                Bag<String, LicenseProblem> errorlist = bag.getValue();
                DefaultMutableTreeNode groupNode = null;
                if (errorlist.size() > 1) {
                    String nmsg = bag.getKey() + " (" + errorlist.size() + ")";
                    groupNode = new DefaultMutableTreeNode(nmsg);
                    severityNode.add(groupNode);
                    if (oldSelectedRows.contains(bag.getKey())) {
                        expandedPaths.add(new TreePath(new Object[] { rootNode, severityNode, groupNode }));
                    }
                }

                for (Entry<String, List<LicenseProblem>> msgErrors : errorlist.entrySet()) {
                    // Message node
                    List<LicenseProblem> errors = msgErrors.getValue();
                    String msg;
                    if (groupNode != null)
                        msg = msgErrors.getKey() + " (" + errors.size() + ")";
                    else
                        msg = bag.getKey() + " - " + msgErrors.getKey() + " (" + errors.size() + ")";
                    DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(msg);
                    if (groupNode != null)
                        groupNode.add(messageNode);
                    else
                        severityNode.add(messageNode);

                    if (oldSelectedRows.contains(msgErrors.getKey())) {
                        if (groupNode != null) {
                            expandedPaths.add(new TreePath(new Object[] { rootNode, severityNode, groupNode,
                                    messageNode }));
                        } else {
                            expandedPaths.add(new TreePath(new Object[] { rootNode, severityNode, messageNode }));
                        }
                    }

                    for (LicenseProblem error : errors) {
                        // Error node
                        DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(error);
                        messageNode.add(errorNode);
                    }
                }
            }
        }

        treeModel.setRoot(rootNode);
        for (TreePath path : expandedPaths) {
            this.expandPath(path);
        }
    }

    /**
     * Sets the errors list used by a data layer
     * @param errors The error list that is used by a data layer
     */
    public void setErrorList(List<LicenseProblem> errors) {
        this.errors = errors;
        if (isVisible())
            buildTree();
    }

    /**
     * Clears the current error list and adds these errors to it
     * @param errors The validation errors
     */
    public void setErrors(List<LicenseProblem> newerrors) {
        if (errors == null)
            return;
        errors.clear();
        for (LicenseProblem error : newerrors) {
            errors.add(error);
        }
        if (isVisible())
            buildTree();
    }

    /**
     * Returns the errors of the tree
     * @return  the errors of the tree
     */
    public List<LicenseProblem> getErrors() {
        return errors != null ? errors : Collections.<LicenseProblem> emptyList();
    }

    public Set<OsmPrimitive> getFilter() {
        return filter;
    }

    public void setFilter(Set<OsmPrimitive> filter) {
        if (filter != null && filter.size() == 0)
            this.filter = null;
        else
            this.filter = filter;
        if (isVisible())
            buildTree();
    }

    /**
     * Updates the current errors list
     * @param errors The validation errors
     */
    public void resetErrors() {
        List<LicenseProblem> e = new ArrayList<LicenseProblem>(errors);
        setErrors(e);
    }

    /**
     * Expands all tree
     */
    @SuppressWarnings("unchecked")
    public void expandAll() {
        DefaultMutableTreeNode root = getRoot();

        int row = 0;
        Enumeration<DefaultMutableTreeNode> children = root.breadthFirstEnumeration();
        while (children.hasMoreElements()) {
            children.nextElement();
            expandRow(row++);
        }
    }

    /**
     * Returns the root node model.
     * @return The root node model
     */
    public DefaultMutableTreeNode getRoot() {
        return (DefaultMutableTreeNode) treeModel.getRoot();
    }

    public int getUpdateCount() {
        return updateCount;
    }
}
