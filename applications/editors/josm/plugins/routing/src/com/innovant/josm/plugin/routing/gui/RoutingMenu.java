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

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;

import com.innovant.josm.jrt.core.RoutingGraph.RouteType;
import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingModel;
import com.innovant.josm.plugin.routing.RoutingPlugin;

/**
 * The menu bar from this plugin
 * @author jvidal
 *
 */
public class RoutingMenu extends JMenu {

    /**
     * Default serial version UID
     */
    private static final long serialVersionUID = 3559922048225708480L;

    private JMenuItem startMI;
    private JMenuItem reverseMI;
    private JMenuItem clearMI;
    private JMenu criteriaM;
    private JMenu menu;

    /**
     * @param s
     */
    public RoutingMenu() {
        MainMenu mm = Main.main.menu;
        menu = mm.addMenu(tr("Routing"), KeyEvent.VK_O, mm.defaultMenuPos, ht("/Plugin/Routing"));

        startMI = new JMenuItem(tr("Add routing layer"));
        startMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RoutingPlugin.getInstance().addLayer();
            }
        });
        menu.add(startMI);

        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();

        criteriaM = new JMenu(tr("Criteria"));

        JRadioButtonMenuItem rshorter = new JRadioButtonMenuItem(tr("Shortest"));
        rshorter.setSelected(true);
        rshorter.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    if (e.getStateChange()==ItemEvent.SELECTED) {
                        routingModel.routingGraph.setTypeRoute(RouteType.SHORTEST);
                    } else {
                        routingModel.routingGraph.setTypeRoute(RouteType.FASTEST);
                    }
                //  routingModel.routingGraph.resetGraph();
                //  routingModel.routingGraph.createGraph();
                    //TODO: Change this way
                    //FIXME: do not change node but recalculate routing.
                    routingModel.setNodesChanged();
                    Main.map.repaint();
                }
            }

        });

        JRadioButtonMenuItem rfaster = new JRadioButtonMenuItem(tr("Fastest"));
        group.add(rshorter);
        group.add(rfaster);
        criteriaM.add(rshorter);
        criteriaM.add(rfaster);

        criteriaM.addSeparator();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Ignore oneways");
        cbmi.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    if (e.getStateChange()==ItemEvent.SELECTED)
                        routingModel.routingGraph.getRoutingProfile().setOnewayUse(false);
                    else
                        routingModel.routingGraph.getRoutingProfile().setOnewayUse(true);
                    routingModel.setNodesChanged();
                    Main.map.repaint();
                }
            }
        });
        criteriaM.add(cbmi);
        menu.add(criteriaM);

        menu.addSeparator();
        reverseMI = new JMenuItem(tr("Reverse route"));
        reverseMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    routingModel.reverseNodes();
                    Main.map.repaint();
                }
            }
        });
        menu.add(reverseMI);

        clearMI = new JMenuItem(tr("Clear route"));
        clearMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    // Reset routing nodes and paths
                    routingModel.reset();
                    RoutingPlugin.getInstance().getRoutingDialog().clearNodes();
                    Main.map.repaint();
                }
            }
        });
        menu.add(clearMI);

        // Initially disabled
        disableAllItems();
    }

    public void disableAllItems() {
        startMI.setEnabled(false);
        reverseMI.setEnabled(false);
        clearMI.setEnabled(false);
        criteriaM.setEnabled(false);
    }

    public void enableStartItem() {
        startMI.setEnabled(true);
    }

    public void enableRestOfItems() {
        reverseMI.setEnabled(true);
        clearMI.setEnabled(true);
        criteriaM.setEnabled(true);
    }

    public void disableRestOfItems() {
        reverseMI.setEnabled(false);
        clearMI.setEnabled(false);
        criteriaM.setEnabled(false);
    }
}
