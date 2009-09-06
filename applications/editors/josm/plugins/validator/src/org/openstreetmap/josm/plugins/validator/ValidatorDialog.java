package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * A small tool dialog for displaying the current errors. The selection manager
 * respects clicks into the selection list. Ctrl-click will remove entries from
 * the list while single click will make the clicked entry the only selection.
 *
 * @author frsantos
 */
public class ValidatorDialog extends ToggleDialog implements ActionListener, SelectionChangedListener {
    private OSMValidatorPlugin plugin;

    /** Serializable ID */
    private static final long serialVersionUID = 2952292777351992696L;

    /** The display tree */
    protected ErrorTreePanel tree;

    private SideButton fixButton;
    /** The fix button */
    private SideButton ignoreButton;
    /** The ignore button */
    private SideButton selectButton;
    /** The select button */

    private JPopupMenu popupMenu;
    private TestError popupMenuError = null;

    /** Last selected element */
    private DefaultMutableTreeNode lastSelectedNode = null;

    /**
     * Constructor
     */
    public ValidatorDialog(OSMValidatorPlugin plugin) {
        super(tr("Validation errors"), "validator", tr("Open the validation window."),
        Shortcut.registerShortcut("subwindow:validator", tr("Toggle: {0}", tr("Validation errors")),
        KeyEvent.VK_V, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT), 150);

        this.plugin = plugin;
        popupMenu = new JPopupMenu();

        JMenuItem zoomTo = new JMenuItem(tr("Zoom to problem"));
        zoomTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomToProblem();
            }
        });
        popupMenu.add(zoomTo);

        tree = new ErrorTreePanel();
        tree.addMouseListener(new ClickWatch());
        tree.addTreeSelectionListener(new SelectionWatch());

        add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

        selectButton = new SideButton(marktr("Select"), "select", "Validator",
                tr("Set the selected elements on the map to the selected items in the list above."), this);
        selectButton.setEnabled(false);
        buttonPanel.add(selectButton);
        buttonPanel.add(new SideButton(plugin.validateAction), "refresh");
        fixButton = new SideButton(marktr("Fix"), "fix", "Validator", tr("Fix the selected errors."), this);
        fixButton.setEnabled(false);
        buttonPanel.add(fixButton);
        if (Main.pref.getBoolean(PreferenceEditor.PREF_USE_IGNORE, true)) {
            ignoreButton = new SideButton(marktr("Ignore"), "delete", "Validator",
                    tr("Ignore the selected errors next time."), this);
            ignoreButton.setEnabled(false);
            buttonPanel.add(ignoreButton);
        } else {
            ignoreButton = null;
        }
        add(buttonPanel, BorderLayout.SOUTH);
        DataSet.selListeners.add(this);
    }

    @Override
    public void setVisible(boolean v) {
        if (tree != null)
            tree.setVisible(v);
        super.setVisible(v);
        Main.map.repaint();
    }

    /**
     * Fix selected errors
     *
     * @param e
     */
    @SuppressWarnings("unchecked")
    private void fixErrors(ActionEvent e) {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null)
            return;

        Set<DefaultMutableTreeNode> processedNodes = new HashSet<DefaultMutableTreeNode>();
        for (TreePath path : selectionPaths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == null)
                continue;

            Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
                if (processedNodes.contains(childNode))
                    continue;

                processedNodes.add(childNode);
                Object nodeInfo = childNode.getUserObject();
                if (nodeInfo instanceof TestError) {
                    TestError error = (TestError) nodeInfo;
                    Command fixCommand = error.getFix();
                    if (fixCommand != null) {
                        Main.main.undoRedo.add(fixCommand);
                        error.setIgnored(true);
                    }
                }
            }
        }

        Main.map.repaint();
        tree.resetErrors();
        DataSet.fireSelectionChanged(Main.main.getCurrentDataSet().getSelected());
    }

    /**
     * Set selected errors to ignore state
     *
     * @param e
     */
    @SuppressWarnings("unchecked")
    private void ignoreErrors(ActionEvent e) {
        int asked = JOptionPane.DEFAULT_OPTION;
        boolean changed = false;
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null)
            return;

        Set<DefaultMutableTreeNode> processedNodes = new HashSet<DefaultMutableTreeNode>();
        for (TreePath path : selectionPaths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == null)
                continue;

            Object mainNodeInfo = node.getUserObject();
            if (!(mainNodeInfo instanceof TestError)) {
                Set<String> state = new HashSet<String>();
                // ask if the whole set should be ignored
                if (asked == JOptionPane.DEFAULT_OPTION) {
                    String[] a = new String[] { tr("Whole group"), tr("Single elements"), tr("Nothing") };
                    asked = JOptionPane.showOptionDialog(Main.parent, tr("Ignore whole group or individual elements?"),
                            tr("Ignoring elements"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            a, a[1]);
                }
                if (asked == JOptionPane.YES_NO_OPTION) {
                    Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
                    while (children.hasMoreElements()) {
                        DefaultMutableTreeNode childNode = children.nextElement();
                        if (processedNodes.contains(childNode))
                            continue;

                        processedNodes.add(childNode);
                        Object nodeInfo = childNode.getUserObject();
                        if (nodeInfo instanceof TestError) {
                            TestError err = (TestError) nodeInfo;
                            err.setIgnored(true);
                            changed = true;
                            state.add(node.getDepth() == 1 ? err.getIgnoreSubGroup() : err.getIgnoreGroup());
                        }
                    }
                    for (String s : state)
                        plugin.ignoredErrors.add(s);
                    continue;
                } else if (asked == JOptionPane.CANCEL_OPTION)
                    continue;
            }

            Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
                if (processedNodes.contains(childNode))
                    continue;

                processedNodes.add(childNode);
                Object nodeInfo = childNode.getUserObject();
                if (nodeInfo instanceof TestError) {
                    TestError error = (TestError) nodeInfo;
                    String state = error.getIgnoreState();
                    if (state != null)
                        plugin.ignoredErrors.add(state);
                    changed = true;
                    error.setIgnored(true);
                }
            }
        }
        if (changed) {
            tree.resetErrors();
            plugin.saveIgnoredErrors();
            Main.map.repaint();
        }
    }

    private void showPopupMenu(MouseEvent e) {
        if (!e.isPopupTrigger())
            return;
        popupMenuError = null;
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath == null)
            return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getPathComponent(selPath.getPathCount() - 1);
        if (!(node.getUserObject() instanceof TestError))
            return;
        popupMenuError = (TestError) node.getUserObject();
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void zoomToProblem() {
        if (popupMenuError == null)
            return;
        ValidatorBoundingXYVisitor bbox = new ValidatorBoundingXYVisitor();
        popupMenuError.visitHighlighted(bbox);
        if (bbox.getBounds() == null)
            return;
        bbox.enlargeBoundingBox();
        Main.map.mapView.recalculateCenterScale(bbox);
    }

    /**
     * Sets the selection of the map to the current selected items.
     */
    @SuppressWarnings("unchecked")
    private void setSelectedItems() {
        if (tree == null)
            return;

        Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>(40);

        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null)
            return;

        for (TreePath path : selectedPaths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
                Object nodeInfo = childNode.getUserObject();
                if (nodeInfo instanceof TestError) {
                    TestError error = (TestError) nodeInfo;
                    sel.addAll(error.getPrimitives());
                }
            }
        }

        Main.main.getCurrentDataSet().setSelected(sel);
    }

    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("Select"))
            setSelectedItems();
        else if (actionCommand.equals("Fix"))
            fixErrors(e);
        else if (actionCommand.equals("Ignore"))
            ignoreErrors(e);
    }

    /**
     * Checks for fixes in selected element and, if needed, adds to the sel
     * parameter all selected elements
     *
     * @param sel
     *            The collection where to add all selected elements
     * @param addSelected
     *            if true, add all selected elements to collection
     * @return whether the selected elements has any fix
     */
    @SuppressWarnings("unchecked")
    private boolean setSelection(Collection<OsmPrimitive> sel, boolean addSelected) {
        boolean hasFixes = false;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (lastSelectedNode != null && !lastSelectedNode.equals(node)) {
            Enumeration<DefaultMutableTreeNode> children = lastSelectedNode.breadthFirstEnumeration();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
                Object nodeInfo = childNode.getUserObject();
                if (nodeInfo instanceof TestError) {
                    TestError error = (TestError) nodeInfo;
                    error.setSelected(false);
                }
            }
        }

        lastSelectedNode = node;
        if (node == null)
            return hasFixes;

        Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childNode = children.nextElement();
            Object nodeInfo = childNode.getUserObject();
            if (nodeInfo instanceof TestError) {
                TestError error = (TestError) nodeInfo;
                error.setSelected(true);

                hasFixes = hasFixes || error.isFixable();
                if (addSelected) {
                    sel.addAll(error.getPrimitives());
                }
            }
        }
        selectButton.setEnabled(true);
        if (ignoreButton != null)
            ignoreButton.setEnabled(true);

        return hasFixes;
    }

    /**
     * Watches for clicks.
     */
    public class ClickWatch extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            fixButton.setEnabled(false);
            if (ignoreButton != null)
                ignoreButton.setEnabled(false);
            selectButton.setEnabled(false);

            boolean isDblClick = e.getClickCount() > 1;

            Collection<OsmPrimitive> sel = isDblClick ? new HashSet<OsmPrimitive>(40) : null;

            boolean hasFixes = setSelection(sel, isDblClick);
            fixButton.setEnabled(hasFixes);

            if (isDblClick) {
                Main.main.getCurrentDataSet().setSelected(sel);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            showPopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopupMenu(e);
        }

    }

    /**
     * Watches for tree selection.
     */
    public class SelectionWatch implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            fixButton.setEnabled(false);
            if (ignoreButton != null)
                ignoreButton.setEnabled(false);
            selectButton.setEnabled(false);

            if (e.getSource() instanceof JScrollPane) {
                System.out.println(e.getSource());
                return;
            }

            boolean hasFixes = setSelection(null, false);
            fixButton.setEnabled(hasFixes);
            Main.map.repaint();
        }
    }

    public static class ValidatorBoundingXYVisitor extends BoundingXYVisitor implements ValidatorVisitor {

        public void visit(OsmPrimitive p) {
            if (p.isUsable()) {
                p.visit(this);
            }
        }

        public void visit(WaySegment ws) {
            if (ws.lowerIndex < 0 || ws.lowerIndex + 1 >= ws.way.getNodesCount())
                return;
            visit(ws.way.getNodes().get(ws.lowerIndex));
            visit(ws.way.getNodes().get(ws.lowerIndex + 1));
        }
    }

    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        if (!Main.pref.getBoolean(PreferenceEditor.PREF_FILTER_BY_SELECTION, false))
            return;
        if (newSelection == null || newSelection.size() == 0)
            tree.setFilter(null);
        HashSet<OsmPrimitive> filter = new HashSet<OsmPrimitive>(newSelection);
        tree.setFilter(filter);
    }
}
