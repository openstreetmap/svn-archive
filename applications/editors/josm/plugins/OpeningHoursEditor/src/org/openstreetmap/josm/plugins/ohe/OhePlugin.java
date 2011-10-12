package org.openstreetmap.josm.plugins.ohe;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.ohe.gui.OheDialogPanel;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

public class OhePlugin extends Plugin {
    /**
     * Strings for choosing which key of an object with given tags should be
     * edited, the order is referencing the preference of the keys, String[] ->
     * {key, value, key-to-edit} key and value can contain regular expressions
     */
    private final String[][] TAG_EDIT_STRINGS = new String[][] {
            { "opening_hours", ".*", "opening_hours" },
            { "collection_times", ".*", "collection_times" },
            { "collection_times:local", ".*", "collection_times:local" },
            { "shop", ".*", "opening_hours" },
            { "amenity", "post_box", "collection_times" },
            { "amenity", "recycling", "collection_times" },
            { "amenity", ".*", "opening_hours" },
            { "lit", ".*", "lit" },
            { "highway", ".*", "lit" } };

    /**
     * Will be invoked by JOSM to bootstrap the plugin
     * 
     * @param info
     *            information about the plugin and its local installation
     */
    public OhePlugin(PluginInformation info) {
        super(info);
        Main.main.menu.toolsMenu.add(new OheMenuAction());
    }

    /**
     * this Action is used for calling the OpeningsHourEditor, the selected
     * objects in the active datalayer are edited
     * 
     * @author boman
     */
    class OheMenuAction extends JosmAction {
        private static final long serialVersionUID = 1456257438391417756L;

        public OheMenuAction() {
            super(tr("Edit opening hours"), "opening_hours.png",
                    tr("Edit time-tag of selected element in a graphical interface"), Shortcut.registerShortcut(
                            "tools:opening_hourseditor", tr("Tool: {0}", tr("Edit opening hours")), KeyEvent.VK_T,
                            Shortcut.GROUP_MENU), false);
        }

        @Override
        protected void updateEnabledState() {
            if (getCurrentDataSet() == null) {
                // if there is no current dataset, then the action is disabled
                setEnabled(false);
            } else {
                updateEnabledState(getCurrentDataSet().getSelected());
            }
        }

        @Override
        protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
            // only enable the action if something is selected
            setEnabled(selection != null && !selection.isEmpty());
        }

        public void actionPerformed(ActionEvent evt) {
            // fetch active Layer
            OsmDataLayer osmlayer = Main.main.getEditLayer();
            if (osmlayer == null)
                return;
            Collection<OsmPrimitive> selection = osmlayer.data.getSelected();

            // handling of multiple objects and their tags
            // copied from
            // org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog[rev4079][line802]
            Map<String, Integer> keyCount = new HashMap<String, Integer>();
            Map<String, Map<String, Integer>> valueCount = new TreeMap<String, Map<String, Integer>>();
            for (OsmPrimitive osm : selection) {
                for (String key : osm.keySet()) {
                    String value = osm.get(key);
                    keyCount.put(key, keyCount.containsKey(key) ? keyCount.get(key) + 1 : 1);
                    if (valueCount.containsKey(key)) {
                        Map<String, Integer> v = valueCount.get(key);
                        v.put(value, v.containsKey(value) ? v.get(value) + 1 : 1);
                    } else {
                        TreeMap<String, Integer> v = new TreeMap<String, Integer>();
                        v.put(value, 1);
                        valueCount.put(key, v);
                    }
                }
            }

            DefaultTableModel propertyData = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }
            };
            propertyData.setColumnIdentifiers(new String[] { tr("Key"), tr("Value") });
            for (Entry<String, Map<String, Integer>> e : valueCount.entrySet()) {
                int count = 0;
                for (Entry<String, Integer> e1 : e.getValue().entrySet()) {
                    count += e1.getValue();
                }
                if (count < selection.size()) {
                    e.getValue().put("", selection.size() - count);
                }
                propertyData.addRow(new Object[] { e.getKey(), e.getValue() });
            }
            final JTable propertyTable = new JTable(propertyData);
            propertyTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                    if (value == null)
                        return this;
                    if (c instanceof JLabel) {
                        String str = null;
                        if (value instanceof String) {
                            str = (String) value;
                        } else if (value instanceof Map<?, ?>) {
                            Map<?, ?> v = (Map<?, ?>) value;
                            if (v.size() != 1) {
                                str = tr("<different>");
                                c.setFont(c.getFont().deriveFont(Font.ITALIC));
                            } else {
                                final Map.Entry<?, ?> entry = v.entrySet().iterator().next();
                                str = (String) entry.getKey();
                            }
                        }
                        ((JLabel) c).setText(str);
                    }
                    return c;
                }
            });
            // end copy

            // showing the tags in a dialog
            propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setViewportView(propertyTable);

            final JTextField newTagField = new JTextField();

            JRadioButton editButton = new JRadioButton(new AbstractAction(tr("edit existing tag")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    propertyTable.setEnabled(true);
                    newTagField.setEnabled(false);
                }
            });
            JRadioButton newButton = new JRadioButton(new AbstractAction(tr("edit new tag")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    propertyTable.setEnabled(false);
                    newTagField.setEnabled(true);
                }
            });
            ButtonGroup group = new ButtonGroup();
            group.add(newButton);
            group.add(editButton);

            // search through the tags and choose which one should be selected
            String preSelectedKey = "";
            searchLoop: for (String[] pattern : TAG_EDIT_STRINGS) {
                Pattern keyPattern = Pattern.compile(pattern[0]);
                Pattern valuePattern = Pattern.compile(pattern[1]);
                for (int i = 0; i < propertyData.getRowCount(); ++i) {
                    Matcher keyMatcher = keyPattern.matcher((String) propertyData.getValueAt(i, 0));
                    if (keyMatcher.matches()) {
                        Object value = propertyData.getValueAt(i, 1);
                        if (value instanceof String && valuePattern.matcher((String) value).matches()) {
                            preSelectedKey = pattern[2];
                            break searchLoop;
                        } else if (value instanceof Map<?, ?>) {
                            for (String v : ((Map<String, Integer>) value).keySet())
                                if (valuePattern.matcher(v).matches()) {
                                    preSelectedKey = pattern[2];
                                    break searchLoop;
                                }
                        }
                    }
                }
            }
            int preSelectedRow = -1;
            for (int i = 0; i < propertyData.getRowCount(); ++i)
                if (preSelectedKey.equals(propertyData.getValueAt(i, 0))) {
                    preSelectedRow = i;
                }
            if (preSelectedRow != -1) {
                propertyTable.setEnabled(true);
                newTagField.setEnabled(false);
                propertyTable.setRowSelectionInterval(preSelectedRow, preSelectedRow);
                editButton.setSelected(true);
            } else {
                propertyTable.setEnabled(false);
                newTagField.setEnabled(true);
                newTagField.setText(preSelectedKey);
                newButton.setSelected(true);
            }

            // load the preference for the clocksystem (12h/24h)
            ClockSystem clockSystem = ClockSystem.valueOf(Main.pref.get("ohe.clocksystem",
                    ClockSystem.getClockSystem(Locale.getDefault()).toString()));

            JCheckBox useTwelveHourClock = new JCheckBox(tr("Display clock in 12h mode."),
                    clockSystem == ClockSystem.TWELVE_HOURS);

            JPanel dlgPanel = new JPanel(new GridBagLayout());
            dlgPanel.add(editButton, GBC.std().anchor(GBC.WEST));
            dlgPanel.add(sp, GBC.eol().fill(GBC.BOTH));
            dlgPanel.add(newButton, GBC.std().anchor(GBC.WEST));
            dlgPanel.add(newTagField, GBC.eol().fill(GBC.HORIZONTAL));
            dlgPanel.add(useTwelveHourClock, GBC.eol().fill(GBC.HORIZONTAL).insets(0, 5, 0, 5));

            JOptionPane optionPane = new JOptionPane(dlgPanel, JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            JDialog dlg = optionPane.createDialog(Main.parent, tr("Choose key"));
            dlg.pack();
            dlg.setResizable(true);
            dlg.setVisible(true);

            Object answer = optionPane.getValue();
            String keyToEdit = null;
            Object valuesToEdit = "";
            if (answer != null && answer != JOptionPane.UNINITIALIZED_VALUE
                    && (answer instanceof Integer && (Integer) answer == JOptionPane.OK_OPTION))
                if (editButton.isSelected() && propertyTable.getSelectedRow() != -1) {
                    keyToEdit = (String) propertyData.getValueAt(propertyTable.getSelectedRow(), 0);
                    valuesToEdit = propertyData.getValueAt(propertyTable.getSelectedRow(), 1);
                } else if (newButton.isSelected()) {
                    keyToEdit = newTagField.getText();
                }
            if (keyToEdit == null)
                return;

            // save the value for the clocksystem (12h/24h)
            Main.pref.put("ohe.clocksystem", (useTwelveHourClock.isSelected() ? ClockSystem.TWELVE_HOURS
                    : ClockSystem.TWENTYFOUR_HOURS).toString());

            OheDialogPanel panel = new OheDialogPanel(OhePlugin.this, keyToEdit, valuesToEdit,
                    useTwelveHourClock.isSelected() ? ClockSystem.TWELVE_HOURS : ClockSystem.TWENTYFOUR_HOURS);

            optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            dlg = optionPane.createDialog(Main.parent, tr("Edit"));
            dlg.setResizable(true);
            dlg.setVisible(true);

            String[] changedKeyValuePair = null;
            answer = optionPane.getValue();
            if (!(answer == null || answer == JOptionPane.UNINITIALIZED_VALUE || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION))) {
                changedKeyValuePair = panel.getChangedKeyValuePair();
            }
            if (changedKeyValuePair == null)
                return;
            String key = changedKeyValuePair[0].trim();
            String newkey = changedKeyValuePair[1].trim();
            String value = changedKeyValuePair[2].trim();

            if (value.equals("")) {
                value = null; // delete the key
            }
            if (newkey.equals("")) {
                newkey = key;
                value = null; // delete the key instead
            }
            if (key.equals(newkey) && tr("<different>").equals(value))
                return;
            if (key.equals(newkey) || value == null) {
                Main.main.undoRedo.add(new ChangePropertyCommand(selection, newkey, value));
            } else {
                Collection<Command> commands = new Vector<Command>();
                commands.add(new ChangePropertyCommand(selection, key, null));
                if (value.equals(tr("<different>"))) {
                    HashMap<String, Vector<OsmPrimitive>> map = new HashMap<String, Vector<OsmPrimitive>>();
                    for (OsmPrimitive osm : selection) {
                        String val = osm.get(key);
                        if (val != null) {
                            if (map.containsKey(val)) {
                                map.get(val).add(osm);
                            } else {
                                Vector<OsmPrimitive> v = new Vector<OsmPrimitive>();
                                v.add(osm);
                                map.put(val, v);
                            }
                        }
                    }
                    for (Entry<String, Vector<OsmPrimitive>> e : map.entrySet()) {
                        commands.add(new ChangePropertyCommand(e.getValue(), newkey, e.getKey()));
                    }
                } else {
                    commands.add(new ChangePropertyCommand(selection, newkey, value));
                }
                Main.main.undoRedo.add(new SequenceCommand(trn("Change properties of up to {0} object",
                        "Change properties of up to {0} objects", selection.size(), selection.size()), commands));
            }
        }
    }
}
