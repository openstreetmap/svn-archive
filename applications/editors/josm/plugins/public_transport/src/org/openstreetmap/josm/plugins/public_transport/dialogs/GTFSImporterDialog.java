// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.plugins.public_transport.actions.GTFSImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.GTFSStopTableModel;

public class GTFSImporterDialog extends AbstractImporterDialog<GTFSImporterAction> {
    private final JTable gtfsStopTable = new JTable();

    public GTFSImporterDialog(GTFSImporterAction controller) {
        super(controller, tr("Create Stops from GTFS"), "gtfsImporter");
    }

    @Override
    protected void initDialog(GTFSImporterAction controller) {
        JPanel tabSettings = new JPanel();
        tabbedPane.addTab(tr("Settings"), tabSettings);
        JPanel tabWaypoints = new JPanel();
        tabbedPane.addTab(tr("GTFS-Stops"), tabWaypoints);
        tabbedPane.setEnabledAt(0, false);
        tabbedPane.setEnabledAt(1, true);

        // Settings Tab
        JPanel contentPane = tabSettings;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);

        JLabel label = new JLabel(tr("Type of stops to add"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 0;
        layoutCons.gridwidth = 2;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        layoutCons.gridx = 0;
        layoutCons.gridy = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(cbStoptype, layoutCons);
        contentPane.add(cbStoptype);

        label = new JLabel(tr("Time on your GPS device"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 2;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        layoutCons.gridx = 0;
        layoutCons.gridy = 3;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tfGPSTimeStart, layoutCons);
        contentPane.add(tfGPSTimeStart);

        /* I18n: Don't change the time format, you only may translate the letters */
        label = new JLabel(tr("HH:MM:SS.sss"));

        layoutCons.gridx = 1;
        layoutCons.gridy = 3;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        label = new JLabel(tr("Time on your stopwatch"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 4;
        layoutCons.gridwidth = 2;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        layoutCons.gridx = 0;
        layoutCons.gridy = 5;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tfStopwatchStart, layoutCons);
        contentPane.add(tfStopwatchStart);

        /* I18n: Don't change the time format, you only may translate the letters */
        label = new JLabel(tr("HH:MM:SS.sss"));

        layoutCons.gridx = 1;
        layoutCons.gridy = 5;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        label = new JLabel(tr("Time window"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 6;
        layoutCons.gridwidth = 2;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        layoutCons.gridx = 0;
        layoutCons.gridy = 7;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tfTimeWindow, layoutCons);
        contentPane.add(tfTimeWindow);

        label = new JLabel(tr("seconds"));

        layoutCons.gridx = 1;
        layoutCons.gridy = 7;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        label = new JLabel(tr("Move Threshold"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 8;
        layoutCons.gridwidth = 2;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        layoutCons.gridx = 0;
        layoutCons.gridy = 9;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tfThreshold, layoutCons);
        contentPane.add(tfThreshold);

        label = new JLabel(tr("meters"));

        layoutCons.gridx = 1;
        layoutCons.gridy = 9;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        JButton bSuggestStops = new JButton(tr("Suggest Stops"));
        bSuggestStops.setActionCommand("gtfsImporter.settingsSuggestStops");
        bSuggestStops.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 10;
        layoutCons.gridwidth = 3;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bSuggestStops, layoutCons);
        contentPane.add(bSuggestStops);

        // Waypoints Tab
        contentPane = tabWaypoints;
        gridbag = new GridBagLayout();
        layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt N"), "gtfsImporter.gtfsStopsFocusAdd");
        contentPane.getActionMap().put("gtfsImporter.gtfsStopsFocusAdd",
                controller.getFocusAddAction());
/*    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt S"), "gtfsImporter.focusShelterYes");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterYes",
     controller.getFocusWaypointShelterAction("yes"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt T"), "gtfsImporter.focusShelterNo");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterNo",
     controller.getFocusWaypointShelterAction("no"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt U"), "gtfsImporter.focusShelterImplicit");
    contentPane.getActionMap().put
    ("gtfsImporter.focusShelterImplicit",
     controller.getFocusWaypointShelterAction("implicit"));
    contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
        (KeyStroke.getKeyStroke("alt D"), "gtfsImporter.gtfsStopsDelete");
    contentPane.getActionMap().put
    ("gtfsImporter.gtfsStopsDelete",
     controller.getFocusWaypointDeleteAction());*/

        JScrollPane tableSP = new JScrollPane(gtfsStopTable);

        layoutCons.gridx = 0;
        layoutCons.gridy = 0;
        layoutCons.gridwidth = 4;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 1.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tableSP, layoutCons);
        contentPane.add(tableSP);

        JButton bFind = new JButton(tr("Find"));
        bFind.setActionCommand("gtfsImporter.gtfsStopsFind");
        bFind.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bFind, layoutCons);
        contentPane.add(bFind);

        JButton bShow = new JButton(tr("Show"));
        bShow.setActionCommand("gtfsImporter.gtfsStopsShow");
        bShow.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bShow, layoutCons);
        contentPane.add(bShow);

        JButton bMark = new JButton(tr("Mark"));
        bMark.setActionCommand("gtfsImporter.gtfsStopsMark");
        bMark.addActionListener(controller);

        layoutCons.gridx = 1;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bMark, layoutCons);
        contentPane.add(bMark);

        JButton bCatch = new JButton(tr("Catch"));
        bCatch.setActionCommand("gtfsImporter.gtfsStopsCatch");
        bCatch.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bCatch, layoutCons);
        contentPane.add(bCatch);

        JButton bJoin = new JButton(tr("Join"));
        bJoin.setActionCommand("gtfsImporter.gtfsStopsJoin");
        bJoin.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 2;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bJoin, layoutCons);
        contentPane.add(bJoin);

        JButton bAdd = new JButton(tr("Enable"));
        bAdd.setActionCommand("gtfsImporter.gtfsStopsAdd");
        bAdd.addActionListener(controller);

        layoutCons.gridx = 3;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bAdd, layoutCons);
        contentPane.add(bAdd);

        JButton bDelete = new JButton(tr("Disable"));
        bDelete.setActionCommand("gtfsImporter.gtfsStopsDelete");
        bDelete.addActionListener(controller);

        layoutCons.gridx = 3;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bDelete, layoutCons);
        contentPane.add(bDelete);
    }

    public JTable getGTFSStopTable() {
        return gtfsStopTable;
    }

    public void setGTFSStopTableModel(GTFSStopTableModel model) {
        gtfsStopTable.setModel(model);
        int width = gtfsStopTable.getPreferredSize().width;
        gtfsStopTable.getColumnModel().getColumn(0).setPreferredWidth((int) (width * 0.3));
        gtfsStopTable.getColumnModel().getColumn(1).setPreferredWidth((int) (width * 0.6));
        gtfsStopTable.getColumnModel().getColumn(2).setPreferredWidth((int) (width * 0.1));
    }

/*  private class TracksLSL implements ListSelectionListener
  {
    GTFSImporterAction root = null;

    public TracksLSL(GTFSImporterAction sia)
    {
      root = sia;
    }

    public void valueChanged(ListSelectionEvent e)
    {
      int selectedPos = tracksList.getAnchorSelectionIndex();
      if (tracksList.isSelectedIndex(selectedPos))
    root.tracksSelectionChanged(selectedPos);
      else
    root.tracksSelectionChanged(-1);
    }
  };*/
}
