// License: GPL. For details, see LICENSE file.
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

import org.openstreetmap.josm.gui.MainApplication;
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

    private final JMenuItem startMI;
    private final JMenuItem reverseMI;
    private final JMenuItem clearMI;
    private final JMenuItem regraphMI;
    private final JMenu criteriaM;
    private final JMenu menu;

    /**
     */
    public RoutingMenu() {
        MainMenu mm = MainApplication.getMenu();
        menu = mm.addMenu("Routing", tr("Routing"), KeyEvent.VK_O, mm.getDefaultMenuPos(), ht("/Plugin/Routing"));

        startMI = new JMenuItem(tr("Add routing layer"));
        startMI.addActionListener(new ActionListener() {
            @Override
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
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        routingModel.routingGraph.setTypeRoute(RouteType.SHORTEST);
                    } else {
                        routingModel.routingGraph.setTypeRoute(RouteType.FASTEST);
                    }
                    //  routingModel.routingGraph.resetGraph();
                    //  routingModel.routingGraph.createGraph();
                    //TODO: Change this way
                    //FIXME: do not change node but recalculate routing.
                    routingModel.setNodesChanged();
                    MainApplication.getMap().repaint();
                }
            }

        });

        JRadioButtonMenuItem rfaster = new JRadioButtonMenuItem(tr("Fastest"));
        group.add(rshorter);
        group.add(rfaster);
        criteriaM.add(rshorter);
        criteriaM.add(rfaster);

        criteriaM.addSeparator();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(tr("Ignore oneways"));
        cbmi.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    if (e.getStateChange() == ItemEvent.SELECTED)
                        routingModel.routingGraph.getRoutingProfile().setOnewayUse(false);
                    else
                        routingModel.routingGraph.getRoutingProfile().setOnewayUse(true);
                    routingModel.setNodesChanged();
                    routingModel.setOnewayChanged();
                    MainApplication.getMap().repaint();
                }
            }
        });
        criteriaM.add(cbmi);
        menu.add(criteriaM);

        menu.addSeparator();
        reverseMI = new JMenuItem(tr("Reverse route"));
        reverseMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    routingModel.reverseNodes();
                    MainApplication.getMap().repaint();
                }
            }
        });
        menu.add(reverseMI);

        clearMI = new JMenuItem(tr("Clear route"));
        clearMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    // Reset routing nodes and paths
                    routingModel.reset();
                    RoutingPlugin.getInstance().getRoutingDialog().clearNodes();
                    MainApplication.getMap().repaint();
                }
            }
        });
        menu.add(clearMI);

        regraphMI = new JMenuItem(tr("Reconstruct Graph"));
        regraphMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                    RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                    RoutingModel routingModel = layer.getRoutingModel();
                    routingModel.routingGraph.resetGraph();
                    routingModel.routingGraph.createGraph();
                }
            }
        });
        menu.add(regraphMI);

        // Initially disabled
        disableAllItems();
    }

    public void disableAllItems() {
        startMI.setEnabled(false);
        reverseMI.setEnabled(false);
        clearMI.setEnabled(false);
        criteriaM.setEnabled(false);
        regraphMI.setEnabled(false);
    }

    public void enableStartItem() {
        startMI.setEnabled(true);
    }

    public void enableRestOfItems() {
        reverseMI.setEnabled(true);
        clearMI.setEnabled(true);
        criteriaM.setEnabled(true);
        regraphMI.setEnabled(true);
    }

    public void disableRestOfItems() {
        reverseMI.setEnabled(false);
        clearMI.setEnabled(false);
        criteriaM.setEnabled(false);
        regraphMI.setEnabled(false);
    }
}
