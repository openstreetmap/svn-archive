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
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.DataChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.OsbObserver;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;
import org.openstreetmap.josm.plugins.osb.gui.action.AddCommentAction;
import org.openstreetmap.josm.plugins.osb.gui.action.CloseIssueAction;
import org.openstreetmap.josm.plugins.osb.gui.action.NewIssueAction;
import org.openstreetmap.josm.plugins.osb.gui.action.OsbAction;
import org.openstreetmap.josm.plugins.osb.gui.action.OsbActionObserver;
import org.openstreetmap.josm.plugins.osb.gui.action.PopupFactory;
import org.openstreetmap.josm.tools.OsmUrlToBounds;
import org.openstreetmap.josm.tools.Shortcut;

public class OsbDialog extends ToggleDialog implements OsbObserver, ListSelectionListener, LayerChangeListener,
DataChangeListener, MouseListener, OsbActionObserver {

    private static final long serialVersionUID = 1L;
    private DefaultListModel model;
    private JList list;
    private OsbPlugin osbPlugin;
    private boolean fireSelectionChanged = true;
    private JButton refresh;
    private JButton addComment = new JButton(new AddCommentAction());
    private JButton closeIssue = new JButton(new CloseIssueAction());
    private JToggleButton newIssue = new JToggleButton();

    private boolean buttonLabels = Main.pref.getBoolean(ConfigKeys.OSB_BUTTON_LABELS);

    public OsbDialog(final OsbPlugin plugin) {
        super(tr("Open OpenStreetBugs"), "icon_error22",
                tr("Opens the OpenStreetBugs window and activates the automatic download"), Shortcut.registerShortcut(
                        "view:openstreetbugs", tr("Toggle: {0}", tr("Open OpenStreetBugs")), KeyEvent.VK_O,
                        Shortcut.GROUP_MENU, Shortcut.SHIFT_DEFAULT), 150);

        osbPlugin = plugin;

        model = new DefaultListModel();
        list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.addMouseListener(this);
        list.setCellRenderer(new OsbListCellRenderer());
        add(new JScrollPane(list), BorderLayout.CENTER);

        // create dialog buttons
        GridLayout layout = buttonLabels ? new GridLayout(2, 2) : new GridLayout(1, 4);
        JPanel buttonPanel = new JPanel(layout);
        add(buttonPanel, BorderLayout.SOUTH);
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

        addComment.setEnabled(false);
        addComment.setToolTipText((String) addComment.getAction().getValue(Action.NAME));
        addComment.setIcon(OsbPlugin.loadIcon("add_comment22.png"));
        closeIssue.setEnabled(false);
        closeIssue.setToolTipText((String) closeIssue.getAction().getValue(Action.NAME));
        closeIssue.setIcon(OsbPlugin.loadIcon("icon_valid22.png"));
        NewIssueAction nia = new NewIssueAction(newIssue, osbPlugin);
        newIssue.setAction(nia);
        newIssue.setToolTipText((String) newIssue.getAction().getValue(Action.NAME));
        newIssue.setIcon(OsbPlugin.loadIcon("icon_error_add22.png"));

        buttonPanel.add(refresh);
        buttonPanel.add(newIssue);
        buttonPanel.add(addComment);
        buttonPanel.add(closeIssue);

        if (buttonLabels) {
            refresh.setHorizontalAlignment(SwingConstants.LEFT);
            addComment.setHorizontalAlignment(SwingConstants.LEFT);
            closeIssue.setHorizontalAlignment(SwingConstants.LEFT);
            newIssue.setHorizontalAlignment(SwingConstants.LEFT);
        } else {
            refresh.setText(null);
            addComment.setText(null);
            closeIssue.setText(null);
            newIssue.setText(null);
        }

        // add a selection listener to the data
        DataSet.selListeners.add(new SelectionChangedListener() {
            public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
                fireSelectionChanged = false;
                list.clearSelection();
                for (OsmPrimitive osmPrimitive : newSelection) {
                    for (int i = 0; i < model.getSize(); i++) {
                        OsbListItem item = (OsbListItem) model.get(i);
                        if (item.getNode() == osmPrimitive) {
                            list.addSelectionInterval(i, i);
                        }
                    }
                }
                fireSelectionChanged = true;
            }
        });

        AddCommentAction.addActionObserver(this);
        CloseIssueAction.addActionObserver(this);
    }

    public synchronized void update(final DataSet dataset) {
        Node lastNode = OsbAction.getSelectedNode();
        model = new DefaultListModel();
        List<Node> sortedList = new ArrayList<Node>(dataset.getNodes());
        Collections.sort(sortedList, new BugComparator());

        for (Node node : sortedList) {
            if (node.isUsable()) {
                model.addElement(new OsbListItem(node));
            }
        }
        list.setModel(model);
        list.setSelectedValue(new OsbListItem(lastNode), true);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (list.getSelectedValues().length == 0) {
            addComment.setEnabled(false);
            closeIssue.setEnabled(false);
            OsbAction.setSelectedNode(null);
            return;
        }

        List<OsmPrimitive> selected = new ArrayList<OsmPrimitive>();
        for (Object listItem : list.getSelectedValues()) {
            Node node = ((OsbListItem) listItem).getNode();
            selected.add(node);

            if ("1".equals(node.get("state"))) {
                addComment.setEnabled(false);
                closeIssue.setEnabled(false);
            } else {
                addComment.setEnabled(true);
                closeIssue.setEnabled(true);
            }

            OsbAction.setSelectedNode(node);
            scrollToSelected(node);
        }

        // CurrentDataSet may be null if there is no normal, edible map
        // If so, a temporary DataSet is created because it's the simplest way
        // to fire all necessary events so OSB updates its popups.
        DataSet ds = Main.main.getCurrentDataSet();
        if (fireSelectionChanged) {
            if(ds == null)
                ds = new DataSet();
            ds.setSelected(selected);
        }
    }

    private void scrollToSelected(Node node) {
        for (int i = 0; i < model.getSize(); i++) {
            Node current = ((OsbListItem) model.get(i)).getNode();
            if (current.getId()== node.getId()) {
                list.scrollRectToVisible(list.getCellBounds(i, i));
                list.setSelectedIndex(i);
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
            model.removeAllElements();
        }
    }

    public void dataChanged(OsmDataLayer l) {
        update(l.data);
    }

    public void zoomToNode(Node node) {
        Main.map.mapView.zoomTo(node.getEastNorth());
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            OsbListItem item = (OsbListItem) list.getSelectedValue();
            zoomToNode(item.getNode());
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
            int selectedRow = list.locationToIndex(e.getPoint());
            list.setSelectedIndex(selectedRow);
            Node n = ((OsbListItem) list.getSelectedValue()).getNode();
            OsbAction.setSelectedNode(n);
            PopupFactory.createPopup(n).show(e.getComponent(), e.getX(), e.getY());
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

    private class BugComparator implements Comparator<Node> {

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
}
