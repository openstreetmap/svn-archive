/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.OsbObserver;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;
import org.openstreetmap.josm.plugins.osb.gui.action.ActionQueue;
import org.openstreetmap.josm.plugins.osb.gui.action.AddCommentAction;
import org.openstreetmap.josm.plugins.osb.gui.action.CloseIssueAction;
import org.openstreetmap.josm.plugins.osb.gui.action.OsbAction;
import org.openstreetmap.josm.plugins.osb.gui.action.OsbActionObserver;
import org.openstreetmap.josm.plugins.osb.gui.action.PointToNewIssueAction;
import org.openstreetmap.josm.plugins.osb.gui.action.PopupFactory;
import org.openstreetmap.josm.plugins.osb.gui.action.ToggleConnectionModeAction;
import org.openstreetmap.josm.tools.OsmUrlToBounds;
import org.openstreetmap.josm.tools.Shortcut;

public class OsbDialog extends ToggleDialog implements OsbObserver, ListSelectionListener, LayerChangeListener,
DataSetListener, SelectionChangedListener, MouseListener, OsbActionObserver {

    private static final long serialVersionUID = 1L;
    private JPanel bugListPanel, queuePanel;
    private DefaultListModel bugListModel;
    private JList bugList;
    private JList queueList;
    private OsbPlugin osbPlugin;
    private boolean fireSelectionChanged = true;
    private JButton refresh;
    private JButton addComment;
    private JButton closeIssue;
    private JButton processQueue = new JButton(tr("Process queue"));
    private JToggleButton newIssue = new JToggleButton();
    private JToggleButton toggleConnectionMode;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private boolean queuePanelVisible = false;
    private final ActionQueue actionQueue = new ActionQueue();

    private boolean buttonLabels = Main.pref.getBoolean(ConfigKeys.OSB_BUTTON_LABELS);

    public OsbDialog(final OsbPlugin plugin) {
        super(tr("Open OpenStreetBugs"), "icon_error24",
                tr("Opens the OpenStreetBugs window and activates the automatic download"), Shortcut.registerShortcut(
                        "view:openstreetbugs", tr("Toggle: {0}", tr("Open OpenStreetBugs")), KeyEvent.VK_B,
                        Shortcut.GROUP_LAYER+Shortcut.GROUPS_ALT1), 150);

        osbPlugin = plugin;
        bugListPanel = new JPanel(new BorderLayout());
        bugListPanel.setName(tr("Bug list"));
        add(bugListPanel, BorderLayout.CENTER);

        bugListModel = new DefaultListModel();
        bugList = new JList(bugListModel);
        bugList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bugList.addListSelectionListener(this);
        bugList.addMouseListener(this);
        bugList.setCellRenderer(new OsbBugListCellRenderer());
        bugListPanel.add(new JScrollPane(bugList), BorderLayout.CENTER);

        // create dialog buttons
        GridLayout layout = buttonLabels ? new GridLayout(3, 2) : new GridLayout(1, 5);
        JPanel buttonPanel = new JPanel(layout);
        refresh = new JButton(tr("Refresh"));
        refresh.setToolTipText(tr("Refresh"));
        refresh.setIcon(OsbPlugin.loadIcon("view-refresh22.png"));
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int zoom = OsmUrlToBounds.getZoom(Main.map.mapView.getRealBounds());
                // check zoom level
                if (zoom > 15 || zoom < 9) {
                    JOptionPane.showMessageDialog(Main.parent,
                            tr("The visible area is either too small or too big to download data from OpenStreetBugs"),
                            tr("Warning"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                plugin.updateData();
            }
        });
        bugListPanel.add(buttonPanel, BorderLayout.SOUTH);
        Action toggleConnectionModeAction = new ToggleConnectionModeAction(this, osbPlugin);
        toggleConnectionMode = new JToggleButton(toggleConnectionModeAction);
        toggleConnectionMode.setToolTipText(ToggleConnectionModeAction.MSG_OFFLINE);
        boolean offline = Main.pref.getBoolean(ConfigKeys.OSB_API_OFFLINE);
        toggleConnectionMode.setIcon(OsbPlugin.loadIcon("online22.png"));
        toggleConnectionMode.setSelectedIcon(OsbPlugin.loadIcon("offline22.png"));
        if(offline) {
            // inverse the current value and then do a click, so that
            // we are offline and the gui represents the offline state, too
            Main.pref.put(ConfigKeys.OSB_API_OFFLINE, false);
            toggleConnectionMode.doClick();
        }

        AddCommentAction addCommentAction = new AddCommentAction(this);
        addComment = new JButton(addCommentAction);
        addComment.setEnabled(false);
        addComment.setToolTipText((String) addComment.getAction().getValue(Action.NAME));
        addComment.setIcon(OsbPlugin.loadIcon("add_comment22.png"));
        CloseIssueAction closeIssueAction = new CloseIssueAction(this);
        closeIssue = new JButton(closeIssueAction);
        closeIssue.setEnabled(false);
        closeIssue.setToolTipText((String) closeIssue.getAction().getValue(Action.NAME));
        closeIssue.setIcon(OsbPlugin.loadIcon("icon_valid22.png"));
        PointToNewIssueAction nia = new PointToNewIssueAction(newIssue, osbPlugin);
        newIssue.setAction(nia);
        newIssue.setToolTipText((String) newIssue.getAction().getValue(Action.NAME));
        newIssue.setIcon(OsbPlugin.loadIcon("icon_error_add22.png"));

        buttonPanel.add(toggleConnectionMode);
        buttonPanel.add(refresh);
        buttonPanel.add(newIssue);
        buttonPanel.add(addComment);
        buttonPanel.add(closeIssue);

        queuePanel = new JPanel(new BorderLayout());
        queuePanel.setName(tr("Queue"));
        queueList = new JList(getActionQueue());
        queueList.setCellRenderer(new OsbQueueListCellRenderer());
        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);
        queuePanel.add(processQueue, BorderLayout.SOUTH);
        processQueue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.pref.put(ConfigKeys.OSB_API_OFFLINE, "false");
                setConnectionMode(false);
                try {
                    getActionQueue().processQueue();

                    // refresh, if the api is enabled
                    if(!Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
                        plugin.updateData();
                    }
                } catch (Exception e1) {
                    System.err.println("Couldn't process action queue");
                    e1.printStackTrace();
                }
            }
        });
        tabbedPane.add(queuePanel);

        if (buttonLabels) {
            toggleConnectionMode.setHorizontalAlignment(SwingConstants.LEFT);
            refresh.setHorizontalAlignment(SwingConstants.LEFT);
            addComment.setHorizontalAlignment(SwingConstants.LEFT);
            closeIssue.setHorizontalAlignment(SwingConstants.LEFT);
            newIssue.setHorizontalAlignment(SwingConstants.LEFT);
        } else {
            toggleConnectionMode.setText(null);
            refresh.setText(null);
            addComment.setText(null);
            closeIssue.setText(null);
            newIssue.setText(null);
        }

        addCommentAction.addActionObserver(this);
        closeIssueAction.addActionObserver(this);
        setConnectionMode(offline);


        MapView.addLayerChangeListener(this);
    }

    @Override
    public void showNotify() {
        DataSet.addSelectionListener(this);
    }

    @Override
    public void hideNotify() {
        DataSet.removeSelectionListener(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        MapView.removeLayerChangeListener(this);

    }

    public synchronized void update(final DataSet dataset) {
        // create a new list model
        bugListModel = new DefaultListModel();
        List<Node> sortedList = new ArrayList<Node>(dataset.getNodes());
        Collections.sort(sortedList, new BugComparator());
        for (Node node : sortedList) {
            if (node.isUsable()) {
                bugListModel.addElement(new OsbListItem(node));
            }
        }
        bugList.setModel(bugListModel);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (bugList.getSelectedValues().length == 0) {
            addComment.setEnabled(false);
            closeIssue.setEnabled(false);
            return;
        }

        List<OsmPrimitive> selected = new ArrayList<OsmPrimitive>();
        for (Object listItem : bugList.getSelectedValues()) {
            Node node = ((OsbListItem) listItem).getNode();
            selected.add(node);

            if ("1".equals(node.get("state"))) {
                addComment.setEnabled(false);
                closeIssue.setEnabled(false);
            } else {
                addComment.setEnabled(true);
                closeIssue.setEnabled(true);
            }

            scrollToSelected(node);
        }

        // CurrentDataSet may be null if there is no normal, edible map
        // If so, a temporary DataSet is created because it's the simplest way
        // to fire all necessary events so OSB updates its popups.
        DataSet ds = osbPlugin.getLayer().getDataSet();
        if (fireSelectionChanged) {
            if(ds == null)
                ds = new DataSet();
            ds.setSelected(selected);
        }
    }

    private void scrollToSelected(Node node) {
        for (int i = 0; i < bugListModel.getSize(); i++) {
            Node current = ((OsbListItem) bugListModel.get(i)).getNode();
            if (current.getId()== node.getId()) {
                bugList.scrollRectToVisible(bugList.getCellBounds(i, i));
                bugList.setSelectedIndex(i);
                return;
            }
        }
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    public void layerAdded(Layer newLayer) {
        if (newLayer == osbPlugin.getLayer()) {
            update(osbPlugin.getDataSet());
            Main.map.mapView.moveLayer(newLayer, 0);
        }
    }

    public void layerRemoved(Layer oldLayer) {
        if (oldLayer == osbPlugin.getLayer()) {
            bugListModel.removeAllElements();
        }
    }

    public void zoomToNode(Node node) {
        Main.map.mapView.zoomTo(node.getEastNorth());
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            Node selectedNode = getSelectedNode();
            if(selectedNode != null) {
                zoomToNode(selectedNode);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        mayTriggerPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        mayTriggerPopup(e);
    }

    private void mayTriggerPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int selectedRow = bugList.locationToIndex(e.getPoint());
            bugList.setSelectedIndex(selectedRow);
            Node selectedNode = getSelectedNode();
            if(selectedNode != null) {
                PopupFactory.createPopup(selectedNode, this).show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void actionPerformed(OsbAction action) {
        if (action instanceof AddCommentAction || action instanceof CloseIssueAction) {
            update(osbPlugin.getDataSet());
        }
    }

    private static class BugComparator implements Comparator<Node> {

        public int compare(Node o1, Node o2) {
            String state1 = o1.get("state");
            String state2 = o2.get("state");
            if (state1.equals(state2)) {
                return o1.get("note").compareTo(o2.get("note"));
            }
            return state1.compareTo(state2);
        }

    }

    private boolean downloaded = false;
    protected void initialDownload() {
        Main.worker.execute(new Runnable() {
            public void run() {
                osbPlugin.updateData();
            }
        });
    }

    @Override
    public void showDialog() {
        if (!downloaded) {
            initialDownload();
            downloaded = true;
        }
        super.showDialog();
    }

    public void showQueuePanel() {
        if(!queuePanelVisible) {
            remove(bugListPanel);
            tabbedPane.add(bugListPanel, 0);
            add(tabbedPane, BorderLayout.CENTER);
            tabbedPane.setSelectedIndex(0);
            queuePanelVisible = true;
            invalidate();
            repaint();
        }
    }

    public void hideQueuePanel() {
        if(queuePanelVisible) {
            tabbedPane.remove(bugListPanel);
            remove(tabbedPane);
            add(bugListPanel, BorderLayout.CENTER);
            queuePanelVisible = false;
            invalidate();
            repaint();
        }
    }

    public Node getSelectedNode() {
        if(bugList.getSelectedValue() != null) {
            return ((OsbListItem)bugList.getSelectedValue()).getNode();
        } else {
            return null;
        }
    }

    public void setSelectedNode(Node node) {
        if(node == null) {
            bugList.clearSelection();
        } else {
            bugList.setSelectedValue(new OsbListItem(node), true);
        }
    }

    public void setConnectionMode(boolean offline) {
        refresh.setEnabled(!offline);
        setTitle(tr("OpenStreetBugs ({0})", (offline ? tr("offline") : tr("online"))));
        toggleConnectionMode.setSelected(offline);
    }

    public void dataChanged(DataChangedEvent event) {
        update(event.getDataset());
    }

    public void nodeMoved(NodeMovedEvent event) {}

    public void otherDatasetChange(AbstractDatasetChangedEvent event) {}

    public void primitivesAdded(PrimitivesAddedEvent event) {}

    public void primitivesRemoved(PrimitivesRemovedEvent event) {}

    public void relationMembersChanged(RelationMembersChangedEvent event) {}

    public void tagsChanged(TagsChangedEvent event) {}

    public void wayNodesChanged(WayNodesChangedEvent event) {}

    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        if(newSelection.size() == 1 && newSelection.iterator().next() instanceof Node) {
            Node selectedNode = (Node) newSelection.iterator().next();
            if(osbPlugin.getLayer() != null && osbPlugin.getLayer().getDataSet() != null
                    && osbPlugin.getLayer().getDataSet().getNodes() != null
                    && osbPlugin.getLayer().getDataSet().getNodes().contains(selectedNode))
            {
                setSelectedNode(selectedNode);
            } else {
                bugList.clearSelection();
            }
        } else {
            bugList.clearSelection();
        }
    }

    public ActionQueue getActionQueue() {
        return actionQueue;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        bugList.setEnabled(enabled);
        queueList.setEnabled(enabled);
        addComment.setEnabled(enabled);
        closeIssue.setEnabled(enabled);
    }
}
