// License: GPL. For details, see LICENSE file.
package com.innovant.josm.plugin.routing.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

import com.innovant.josm.jrt.osm.OsmWayTypes;

public class RoutingPreferenceDialog extends DefaultTabPreferenceSetting {

    /**
     * Logger
     */
    static Logger logger = Logger.getLogger(RoutingPreferenceDialog.class);

    private Map<String, String> orig;
    private DefaultTableModel model;

    /**
     * Constructor
     */
    public RoutingPreferenceDialog() {
        super("routing", tr("Routing Plugin Preferences"), tr("Configure routing preferences."));
        readPreferences();
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {

        JPanel principal = gui.createPreferenceTab(this);

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        model = new DefaultTableModel(new String[] {tr("Highway type"),
                tr("Speed (Km/h)") }, 0) {
            private static final long serialVersionUID = 4253339034781567453L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        final JTable list = new JTable(model);
        loadSpeeds(model);

        JScrollPane scroll = new JScrollPane(list);

        p.add(scroll, GBC.eol().fill(GBC.BOTH));
        scroll.setPreferredSize(new Dimension(200, 200));

        JButton add = new JButton(tr("Add"));
        p.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
        p.add(add, GBC.std().insets(0, 5, 0, 0));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel p = new JPanel(new GridBagLayout());
                p.add(new JLabel(tr("Weight")), GBC.std().insets(0, 0, 5, 0));
                JComboBox<String> key = new JComboBox<>();
                for (OsmWayTypes pk : OsmWayTypes.values()) {
                    key.addItem(pk.getTag());
                }
                JTextField value = new JTextField(10);
                p.add(key, GBC.eop().insets(5, 0, 0, 0).fill(GBC.HORIZONTAL));
                p.add(new JLabel(tr("Value")), GBC.std().insets(0, 0, 5, 0));
                p.add(value, GBC.eol().insets(5, 0, 0, 0).fill(GBC.HORIZONTAL));
                int answer = JOptionPane.showConfirmDialog(gui, p,
                        tr("Enter weight values"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                    model
                    .addRow(new String[] {
                            key.getSelectedItem().toString(),
                            value.getText() });
                }
            }
        });

        JButton delete = new JButton(tr("Delete"));
        p.add(delete, GBC.std().insets(0, 5, 0, 0));
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedRow() == -1)
                    JOptionPane.showMessageDialog(gui,
                            tr("Please select the row to delete."));
                else {
                    Integer i;
                    while ((i = list.getSelectedRow()) != -1) {
                        model.removeRow(i);
                    }
                }
            }
        });

        JButton edit = new JButton(tr("Edit"));
        p.add(edit, GBC.std().insets(5, 5, 5, 0));
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit(gui, list);
            }
        });

        JTabbedPane Opciones = new JTabbedPane();
        Opciones.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        Opciones.addTab("Profile", null, p, null);
        //      Opciones.addTab("Preferences", new JPanel());

        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    edit(gui, list);
            }
        });

        principal.add(Opciones, GBC.eol().fill(GBC.BOTH));

    }

    @Override
    public boolean ok() {
        for (int i = 0; i < model.getRowCount(); ++i) {
            String value = model.getValueAt(i, 1).toString();
            if (value.length() != 0) {
                String key = model.getValueAt(i, 0).toString();
                String origValue = orig.get(key);
                if (origValue == null || !origValue.equals(value))
                    Config.getPref().put(key, value);
                orig.remove(key); // processed.
            }
        }
        for (Entry<String, String> e : orig.entrySet()) {
            Config.getPref().put(e.getKey(), null);
        }
        return false;
    }

    private void edit(final PreferenceTabbedPane gui, final JTable list) {
        if (list.getSelectedRowCount() != 1) {
            JOptionPane.showMessageDialog(gui,
                    tr("Please select the row to edit."));
            return;
        }
        String v = JOptionPane.showInputDialog(tr("New value for {0}", model
                .getValueAt(list.getSelectedRow(), 0)), model.getValueAt(list
                        .getSelectedRow(), 1));
        if (v != null)
            model.setValueAt(v, list.getSelectedRow(), 1);
    }

    private void loadSpeeds(DefaultTableModel model) {
        // Read dialog values from preferences
        readPreferences();
        // Put these values in the model
        for (String tag : orig.keySet()) {
            model.addRow(new String[] {tag, orig.get(tag)});
        }
    }

    private void readPreferences() {
        orig = Preferences.main().getAllPrefix("routing.profile.default.speed");
        if (orig.size() == 0) { // defaults
            logger.debug("Loading Default Preferences.");
            for (OsmWayTypes owt : OsmWayTypes.values()) {
                Config.getPref().putInt("routing.profile.default.speed."
                        + owt.getTag(), owt.getSpeed());
            }
            orig = Preferences.main().getAllPrefix("routing.profile.default.speed");
        } else logger.debug("Default preferences already exist.");
    }
    /*
    private String getKeyTag(String tag) {
        return tag.split(".", 5)[4];
    }

    private String getTypeTag(String tag) {
        return tag.split(".", 5)[3];
    }

    private String getNameTag(String tag) {
        return tag.split(".", 5)[2];
    }
     */
}
