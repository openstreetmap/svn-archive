package org.openstreetmap.josm.plugins.ohe;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

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

    // Strings for choosing which key of an object with given tags should be
    // edited
    // the order is referencing the preference of the keys
    // String[] -> {key, value, to-editing-key} key and value can contain regexp
    private final String[][] TAG_EDIT_STRINGS = new String[][] {
            { "opening_hours", ".*", "opening_hours" },
            { "collection_times", ".*", "collection_times" },
            { "collection_times:local", ".*", "collection_times:local" },
            { "lit", ".*", "lit" },
            { "amenity", "post_box", "collection_times" },
            { "amenity", ".*", "opening_hours" },
            { "shop", ".*", "opening_hours" }, { "highway", ".*", "lit" } };

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

    class OheMenuAction extends JosmAction {
        public OheMenuAction() {
            super(
                    tr("Edit opening hours"),
                    "opening_hours.png",
                    tr("Edit time-tag of selected element in a graphical interface"),
                    Shortcut.registerShortcut("tools:opening_hourseditor", tr(
                            "Tool: {0}", tr("Edit opening hours")),
                            KeyEvent.VK_T, Shortcut.GROUP_MENU), false);
        }

        @Override
        protected void updateEnabledState() {
            if (getCurrentDataSet() == null) {
                setEnabled(false);
            } else {
                updateEnabledState(getCurrentDataSet().getSelected());
            }
        }

        @Override
        protected void updateEnabledState(
                Collection<? extends OsmPrimitive> selection) {
            setEnabled(selection != null && !selection.isEmpty());
        }

        public void actionPerformed(ActionEvent evt) {
            // fetch active Layer
            OsmDataLayer osmlayer = Main.main.getEditLayer();
            if (osmlayer != null) {
                Collection<OsmPrimitive> selection = osmlayer.data
                        .getSelected();
                if (selection.size() == 1) { // one object selected
                    OsmPrimitive object = selection.iterator().next();
                    String[] keyValuePair = editTimeTags(object.getKeys());
                    if (keyValuePair != null) {
                        String key = keyValuePair[0].trim();
                        String newkey = keyValuePair[1].trim();
                        String value = keyValuePair[2].trim();

                        if (value.equals("")) {
                            value = null; // delete the key
                        }
                        if (newkey.equals("")) {
                            newkey = key;
                            value = null; // delete the key instead
                        }
                        if (key.equals(newkey)
                                && tr("<different>").equals(value))
                            return;
                        if (key.equals(newkey) || value == null) {
                            Main.main.undoRedo.add(new ChangePropertyCommand(
                                    object, newkey, value));
                        } else {
                            Collection<Command> commands = new Vector<Command>();
                            commands.add(new ChangePropertyCommand(object, key,
                                    null));
                            commands.add(new ChangePropertyCommand(object,
                                    newkey, value));
                            Main.main.undoRedo.add(new SequenceCommand(
                                    tr("Change properties of 1 object"),
                                    commands));
                        }
                    }
                } else { // Not possible to edit 0, 2 or more objects
                    JOptionPane
                            .showMessageDialog(
                                    Main.parent,
                                    tr(
                                            "You have {0} Elements selected. But you can edit only one element!",
                                            selection.size()),
                                    "openingHoursEditor Warning",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // opens up dialogs to change one of the key-value-pairs and returns the
    // changed pair
    private String[] editTimeTags(Map<String, String> keyValueMap) {
        String selectedKey = "";

        if ((selectedKey = tagChooseDialog(keyValueMap)) == null)
            return null;

        final String value = (keyValueMap.containsKey(selectedKey)) ? keyValueMap
                .get(selectedKey)
                : "";
        OheDialogPanel panel = new OheDialogPanel(this, selectedKey, value);

        final JOptionPane optionPane = new JOptionPane(panel,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        final JDialog dlg = optionPane.createDialog(Main.parent, tr("Edit"));

        dlg.setResizable(true);
        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (!(answer == null || answer == JOptionPane.UNINITIALIZED_VALUE || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION)))
            return panel.getChangedKeyValuePair();

        return null;
    }

    // opens a dialog for choosing from a set of tags which can be edited
    // the chosen one is returned
    private String tagChooseDialog(Map<String, String> keyValueMap) {
        String preSelectedKey = getPreSelectedKey(keyValueMap);
        int preSelectedRow = -1;

        String[][] rowData = new String[keyValueMap.size()][2];
        int cnt = 0;
        for (Object key : keyValueMap.keySet().toArray()) {
            rowData[cnt][0] = key.toString();
            rowData[cnt][1] = keyValueMap.get(key);
            if (key.toString().equals(preSelectedKey))
                preSelectedRow = cnt;
            cnt++;
        }

        final JTable table = new JTable(rowData,
                new String[] { "key", "value" }) {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; // Disallow the editing of any cell
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setViewportView(table);

        final JTextField tf = new JTextField();

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("edit")) {
                    table.setEnabled(true);
                    tf.setEnabled(false);
                } else if (e.getActionCommand().equals("new")) {
                    table.setEnabled(false);
                    tf.setEnabled(true);
                }
            }
        };

        JRadioButton editButton = new JRadioButton("edit existing tag");
        editButton.setActionCommand("edit");
        editButton.addActionListener(al);
        JRadioButton newButton = new JRadioButton("edit new tag");
        newButton.setActionCommand("new");
        newButton.addActionListener(al);
        ButtonGroup group = new ButtonGroup();
        group.add(newButton);
        group.add(editButton);

        if (preSelectedRow != -1) {
            table.setEnabled(true);
            tf.setEnabled(false);
            table.setRowSelectionInterval(preSelectedRow, preSelectedRow);
            editButton.setSelected(true);
        } else {
            table.setEnabled(false);
            tf.setEnabled(true);
            tf.setText(preSelectedKey);
            newButton.setSelected(true);
        }

        JPanel dlgPanel = new JPanel(new GridBagLayout());
        dlgPanel.add(editButton, GBC.std().anchor(GBC.CENTER));
        dlgPanel.add(sp, GBC.eol().fill(GBC.BOTH));
        dlgPanel.add(newButton, GBC.std().anchor(GBC.CENTER));
        dlgPanel.add(tf, GBC.eol().fill(GBC.HORIZONTAL));

        JOptionPane optionPane = new JOptionPane(dlgPanel,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dlg = optionPane.createDialog(Main.parent, tr("Choose key"));

        dlg.pack();
        dlg.setResizable(true);
        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (answer != null
                && answer != JOptionPane.UNINITIALIZED_VALUE
                && (answer instanceof Integer && (Integer) answer == JOptionPane.OK_OPTION))
            if (editButton.isSelected() && table.getSelectedRow() != -1)
                return rowData[table.getSelectedRow()][0];
            else if (newButton.isSelected())
                return tf.getText();

        return null;
    }

    private String getPreSelectedKey(Map<String, String> keyValueMap) {
        for (String[] pattern : TAG_EDIT_STRINGS) {
            Pattern keyPattern = Pattern.compile(pattern[0]);
            Pattern valuePattern = Pattern.compile(pattern[1]);
            for (Object key : keyValueMap.keySet().toArray()) {
                Matcher keyMatcher = keyPattern.matcher(key.toString());
                if (keyMatcher.matches()) {
                    Matcher valueMatcher = valuePattern.matcher(keyValueMap
                            .get(key));
                    if (valueMatcher.matches()) {
                        return pattern[2];
                    }
                }
            }
        }
        return "";
    }
}
