// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.dialogs;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.TrackStoplistTableModel;
import org.openstreetmap.josm.plugins.public_transport.models.WaypointTableModel;
import org.openstreetmap.josm.plugins.public_transport.refs.TrackReference;

public class StopImporterDialog extends AbstractImporterDialog<StopImporterAction> {
    private JList<TrackReference> tracksList = null;

    private final JTable stoplistTable = new JTable();

    private final JTable waypointTable = new JTable();

    public StopImporterDialog(StopImporterAction controller) {
        super(controller, tr("Create Stops from GPX"), "stopImporter");
    }

    @Override
    protected void initDialog(StopImporterAction controller) {
        JPanel tabTracks = new JPanel();
        tabbedPane.addTab(tr("Tracks"), tabTracks);
        JPanel tabSettings = new JPanel();
        tabbedPane.addTab(tr("Settings"), tabSettings);
        JPanel tabStops = new JPanel();
        tabbedPane.addTab(tr("Stops"), tabStops);
        JPanel tabWaypoints = new JPanel();
        tabbedPane.addTab(tr("Waypoints"), tabWaypoints);
        tabbedPane.setEnabledAt(0, true);
        tabbedPane.setEnabledAt(1, true);
        tabbedPane.setEnabledAt(2, false);
        tabbedPane.setEnabledAt(3, true);

        // Tracks Tab
        JPanel contentPane = tabTracks;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);

        JLabel label = new JLabel(tr("Tracks in this GPX file:"));

        layoutCons.gridx = 0;
        layoutCons.gridy = 0;
        layoutCons.gridwidth = 3;
        layoutCons.weightx = 0.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(label, layoutCons);
        contentPane.add(label);

        DefaultListModel<TrackReference> tracksListModel = controller.getTracksListModel();
        tracksList = new JList<>(tracksListModel);
        JScrollPane rpListSP = new JScrollPane(tracksList);
        String[] data = {"1", "2", "3", "4", "5", "6"};
        tracksListModel.copyInto(data);
        tracksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tracksList.addListSelectionListener(new TracksLSL(controller));

        layoutCons.gridx = 0;
        layoutCons.gridy = 1;
        layoutCons.gridwidth = 3;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 1.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(rpListSP, layoutCons);
        contentPane.add(rpListSP);

        // Settings Tab
        contentPane = tabSettings;
        gridbag = new GridBagLayout();
        layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);

        label = new JLabel(tr("Type of stops to add"));

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
        bSuggestStops.setActionCommand("stopImporter.settingsSuggestStops");
        bSuggestStops.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 10;
        layoutCons.gridwidth = 3;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bSuggestStops, layoutCons);
        contentPane.add(bSuggestStops);

        // Stops Tab
        contentPane = tabStops;
        gridbag = new GridBagLayout();
        layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt N"), "stopImporter.focusName");
        contentPane.getActionMap().put("stopImporter.focusName",
                controller.getFocusTrackStoplistNameAction());
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt S"), "stopImporter.focusShelterYes");
        contentPane.getActionMap().put("stopImporter.focusShelterYes",
                controller.getFocusTrackStoplistShelterAction("yes"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt T"), "stopImporter.focusShelterNo");
        contentPane.getActionMap().put("stopImporter.focusShelterNo",
                controller.getFocusTrackStoplistShelterAction("no"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt U"), "stopImporter.focusShelterImplicit");
        contentPane.getActionMap().put("stopImporter.focusShelterImplicit",
                controller.getFocusTrackStoplistShelterAction("implicit"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt D"), "stopImporter.stoplistDelete");
        contentPane.getActionMap().put("stopImporter.stoplistDelete",
                controller.getFocusStoplistDeleteAction());

        JScrollPane tableSP = new JScrollPane(stoplistTable);

        layoutCons.gridx = 0;
        layoutCons.gridy = 0;
        layoutCons.gridwidth = 4;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 1.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tableSP, layoutCons);
        contentPane.add(tableSP);

        JButton bFind = new JButton(tr("Find"));
        bFind.setActionCommand("stopImporter.stoplistFind");
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
        bShow.setActionCommand("stopImporter.stoplistShow");
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
        bMark.setActionCommand("stopImporter.stoplistMark");
        bMark.addActionListener(controller);

        layoutCons.gridx = 1;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bMark, layoutCons);
        contentPane.add(bMark);

        JButton bDetach = new JButton(tr("Detach"));
        bDetach.setActionCommand("stopImporter.stoplistDetach");
        bDetach.addActionListener(controller);

        layoutCons.gridx = 1;
        layoutCons.gridy = 2;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bDetach, layoutCons);
        contentPane.add(bDetach);

        JButton bAdd = new JButton(tr("Add"));
        bAdd.setActionCommand("stopImporter.stoplistAdd");
        bAdd.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bAdd, layoutCons);
        contentPane.add(bAdd);

        JButton bDelete = new JButton(tr("Delete"));
        bDelete.setActionCommand("stopImporter.stoplistDelete");
        bDelete.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bDelete, layoutCons);
        contentPane.add(bDelete);

        JButton bSort = new JButton(tr("Sort"));
        bSort.setActionCommand("stopImporter.stoplistSort");
        bSort.addActionListener(controller);

        layoutCons.gridx = 3;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bSort, layoutCons);
        contentPane.add(bSort);

        // Waypoints Tab
        contentPane = tabWaypoints;
        gridbag = new GridBagLayout();
        layoutCons = new GridBagConstraints();
        contentPane.setLayout(gridbag);
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt N"), "stopImporter.focusName");
        contentPane.getActionMap().put("stopImporter.focusName",
                controller.getFocusWaypointNameAction());
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt S"), "stopImporter.focusShelterYes");
        contentPane.getActionMap().put("stopImporter.focusShelterYes",
                controller.getFocusWaypointShelterAction("yes"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt T"), "stopImporter.focusShelterNo");
        contentPane.getActionMap().put("stopImporter.focusShelterNo",
                controller.getFocusWaypointShelterAction("no"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt U"), "stopImporter.focusShelterImplicit");
        contentPane.getActionMap().put("stopImporter.focusShelterImplicit",
                controller.getFocusWaypointShelterAction("implicit"));
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("alt D"), "stopImporter.waypointsDelete");
        contentPane.getActionMap().put("stopImporter.waypointsDelete",
                controller.getFocusWaypointDeleteAction());

        tableSP = new JScrollPane(waypointTable);

        layoutCons.gridx = 0;
        layoutCons.gridy = 0;
        layoutCons.gridwidth = 3;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 1.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(tableSP, layoutCons);
        contentPane.add(tableSP);

        bFind = new JButton(tr("Find"));
        bFind.setActionCommand("stopImporter.waypointsFind");
        bFind.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bFind, layoutCons);
        contentPane.add(bFind);

        bShow = new JButton(tr("Show"));
        bShow.setActionCommand("stopImporter.waypointsShow");
        bShow.addActionListener(controller);

        layoutCons.gridx = 0;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bShow, layoutCons);
        contentPane.add(bShow);

        bMark = new JButton(tr("Mark"));
        bMark.setActionCommand("stopImporter.waypointsMark");
        bMark.addActionListener(controller);

        layoutCons.gridx = 1;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bMark, layoutCons);
        contentPane.add(bMark);

        bDetach = new JButton(tr("Detach"));
        bDetach.setActionCommand("stopImporter.waypointsDetach");
        bDetach.addActionListener(controller);

        layoutCons.gridx = 1;
        layoutCons.gridy = 2;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bDetach, layoutCons);
        contentPane.add(bDetach);

        bAdd = new JButton(tr("Enable"));
        bAdd.setActionCommand("stopImporter.waypointsAdd");
        bAdd.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 1;
        layoutCons.gridheight = 1;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bAdd, layoutCons);
        contentPane.add(bAdd);

        bDelete = new JButton(tr("Disable"));
        bDelete.setActionCommand("stopImporter.waypointsDelete");
        bDelete.addActionListener(controller);

        layoutCons.gridx = 2;
        layoutCons.gridy = 2;
        layoutCons.gridwidth = 1;
        layoutCons.weightx = 1.0;
        layoutCons.weighty = 0.0;
        layoutCons.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(bDelete, layoutCons);
        contentPane.add(bDelete);
    }

    public JTable getStoplistTable() {
        return stoplistTable;
    }

    public void setStoplistTableModel(TrackStoplistTableModel model) {
        stoplistTable.setModel(model);
        JComboBox<TransText> comboBox = new JComboBox<>();
        comboBox.addItem(new TransText(null));
        comboBox.addItem(new TransText(marktr("yes")));
        comboBox.addItem(new TransText(marktr("no")));
        comboBox.addItem(new TransText(marktr("implicit")));
        stoplistTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
        int width = stoplistTable.getPreferredSize().width;
        stoplistTable.getColumnModel().getColumn(0).setPreferredWidth((int) (width * 0.4));
        stoplistTable.getColumnModel().getColumn(1).setPreferredWidth((int) (width * 0.5));
        stoplistTable.getColumnModel().getColumn(2).setPreferredWidth((int) (width * 0.1));
    }

    public JTable getWaypointsTable() {
        return waypointTable;
    }

    public void setWaypointsTableModel(WaypointTableModel model) {
        waypointTable.setModel(model);
        JComboBox<TransText> comboBox = new JComboBox<>();
        comboBox.addItem(new TransText(null));
        comboBox.addItem(new TransText(marktr("yes")));
        comboBox.addItem(new TransText(marktr("no")));
        comboBox.addItem(new TransText(marktr("implicit")));
        waypointTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
        int width = waypointTable.getPreferredSize().width;
        waypointTable.getColumnModel().getColumn(0).setPreferredWidth((int) (width * 0.4));
        waypointTable.getColumnModel().getColumn(1).setPreferredWidth((int) (width * 0.5));
        waypointTable.getColumnModel().getColumn(2).setPreferredWidth((int) (width * 0.1));
    }

    private class TracksLSL implements ListSelectionListener {
        StopImporterAction root = null;

        TracksLSL(StopImporterAction sia) {
            root = sia;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selectedPos = tracksList.getAnchorSelectionIndex();
            if (tracksList.isSelectedIndex(selectedPos))
                root.tracksSelectionChanged(selectedPos);
            else
                root.tracksSelectionChanged(-1);
        }
    }
}
