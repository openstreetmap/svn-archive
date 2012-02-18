/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */

package com.innovant.josm.plugin.routing.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.Shortcut;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingModel;


/**
 * @author jose
 *
 */
public class RoutingDialog extends ToggleDialog {

    private DefaultListModel model;
    private JList jList = null;
    private JScrollPane jScrollPane = null;

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8625615652900341987L;

    public RoutingDialog() {
        super(tr("Routing"), "routing", tr("Open a list of routing nodes"),
                Shortcut.registerShortcut("subwindow:relations", tr("Toggle: {0}", tr("Routing")), KeyEvent.VK_R, Shortcut.ALT), 150);
        model = new DefaultListModel();
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
    private JList getJList() {
        if (jList == null) {
            jList = new JList();
            jList.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jList.setModel(model);
        }
        return jList;
    }

    /**
     * Remove item from the list of nodes
     * @param index
     */
    public void removeNode(int index) {
        model.remove(index);
    }

    /**
     * Add item to the list of nodes
     * @param obj
     */
    public void addNode(Node n) {
        model.addElement(n.getId()+" ["+n.getCoor().toDisplayString()+"]");
    }

    /**
     * Insert item to the list of nodes
     * @param index
     * @param obj
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
        if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
            RoutingLayer routingLayer = (RoutingLayer)Main.map.mapView.getActiveLayer();
            RoutingModel routingModel = routingLayer.getRoutingModel();
            for (Node n : routingModel.getSelectedNodes()) {
                addNode(n);
            }
        }
    }
}
