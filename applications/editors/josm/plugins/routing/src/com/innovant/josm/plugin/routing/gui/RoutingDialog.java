// License: GPL. For details, see LICENSE file.
package com.innovant.josm.plugin.routing.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.ComponentOrientation;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingModel;


/**
 * @author jose
 *
 */
public class RoutingDialog extends ToggleDialog {

    private final DefaultListModel<String> model;
    private JList<String> jList = null;
    private JScrollPane jScrollPane = null;

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8625615652900341987L;

    public RoutingDialog() {
        super(tr("Routing"), "routing", tr("Open a list of routing nodes"),
                Shortcut.registerShortcut("subwindow:routing", tr("Toggle: {0}", tr("Routing")), KeyEvent.VK_R, Shortcut.ALT_CTRL_SHIFT), 150);
        model = new DefaultListModel<>();
        createLayout(getJScrollPane(), false, null);
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList<String> getJList() {
        if (jList == null) {
            jList = new JList<>();
            jList.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jList.setModel(model);
        }
        return jList;
    }

    /**
     * Remove item from the list of nodes
     */
    public void removeNode(int index) {
        model.remove(index);
    }

    /**
     * Add item to the list of nodes
     */
    public void addNode(Node n) {
        model.addElement(n.getId()+" ["+n.getCoor().toDisplayString()+"]");
    }

    /**
     * Insert item to the list of nodes
     */
    public void insertNode(int index, Node n) {
        model.insertElementAt(n.getId()+" ["+n.getCoor().toDisplayString()+"]", index);
    }

    /**
     * Clear list of nodes
     */
    public void clearNodes() {
        model.clear();
    }

    public void refresh() {
        clearNodes();
        if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
            RoutingLayer routingLayer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
            RoutingModel routingModel = routingLayer.getRoutingModel();
            for (Node n : routingModel.getSelectedNodes()) {
                addNode(n);
            }
        }
    }
}
