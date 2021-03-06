// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.roadsigns;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.MultiSplitLayout.Divider;
import org.openstreetmap.josm.gui.widgets.MultiSplitLayout.Leaf;
import org.openstreetmap.josm.gui.widgets.MultiSplitLayout.Node;
import org.openstreetmap.josm.gui.widgets.MultiSplitLayout.Split;
import org.openstreetmap.josm.gui.widgets.MultiSplitPane;
import org.openstreetmap.josm.plugins.roadsigns.RoadSignsPlugin.PresetMetaData;
import org.openstreetmap.josm.plugins.roadsigns.Sign.SignParameter;
import org.openstreetmap.josm.plugins.roadsigns.Sign.Tag;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Pair;

/**
 * Input dialog for road sign.
 *
 * Allows selection of a combination of road signs.
 * It tries to generate the corresponding tags for the object.
 *
 * As tagging schemes are under constant development, the definitions
 * need to be updated when needed.
 *
 * Often there is more than one way to tag a certain situation.
 * So easy configuration is needed (TODO).
 *
 */
class RoadSignInputDialog extends ExtendedDialog {
    protected SignSelection sel;
    protected List<Sign> signs;
    protected JTable previewTable;
    protected JCheckBox addTrafficSignTag;

    protected PreviewTableModel previewModel;
    protected JPanel pnlSignSelection;
    protected JPanel pnlPossibleSigns;
    protected JPanel pnlPossibleSupplements;
    protected JEditorPane info;
    protected JScrollPane scrollInfo;

    private MultiSplitPane multiSplitPane;

    RoadSignInputDialog() {
        super(MainApplication.getMainFrame(), tr("Road Sign Plugin"), new String[] {tr("OK"), tr("Cancel")}, false /* modal */);
        setRememberWindowGeometry(getClass().getName() + ".geometry",
            WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(750, 550)));

        this.signs = RoadSignsPlugin.signs;
        sel = new SignSelection();
        setButtonIcons("ok", "cancel");
        final JTabbedPane tabs = new JTabbedPane();
        tabs.add(tr("signs"), buildSignsPanel());
        Action updateAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RoadSignInputDialog.this.signs = RoadSignsPlugin.signs;
                sel = new SignSelection();
                tabs.setComponentAt(0, buildSignsPanel());
            }
        };
        tabs.add(tr("settings"), new SettingsPanel(false, updateAction));
        setContent(tabs, false);
    }

    @Override
    protected void buttonAction(int i, ActionEvent evt) {
        if (i == 0) { // OK Button
            Collection<OsmPrimitive> selPrim = MainApplication.getLayerManager().getEditDataSet().getSelected();
            if (!selPrim.isEmpty()) {
                Config.getPref().putBoolean("plugin.roadsigns.addTrafficSignTag", addTrafficSignTag.isSelected());

                Command cmd = createCommand(selPrim);
                if (cmd != null) {
                    UndoRedoHandler.getInstance().add(cmd);
                }
            }
        }
        super.buttonAction(i, evt);
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            if (multiSplitPane != null) {
                Node model = multiSplitPane.getMultiSplitLayout().getModel();
                File f = new File(RoadSignsPlugin.pluginDir(), "roadsigns-layout.xml");
                try (
                    XMLEncoder xmlenc = new XMLEncoder(
                            new BufferedOutputStream(new FileOutputStream(f))
                    )) {
                    xmlenc.writeObject(model);
                } catch (FileNotFoundException ex) {
                    Logging.warn("unable to write dialog layout: "+ex);
                }
            }
        }
        super.setVisible(visible);
    }

    private Command createCommand(Collection<OsmPrimitive> selPrim) {
        List<Command> cmds = new LinkedList<>();
        for (int i = 0; i < previewModel.getRowCount(); i++) {
            String key = (String) previewModel.getValueAt(i, 0);
            String value = (String) previewModel.getValueAt(i, 1);
            cmds.add(new ChangePropertyCommand(selPrim, key, value));
        }
        if (cmds.isEmpty())
            return null;
        else if (cmds.size() == 1)
            return cmds.get(0);
        else
            return new SequenceCommand(tr("Change Properties"), cmds);
    }

    private JComponent buildSignsPanel() {
        FlowLayout fLayout = new FlowLayout(FlowLayout.LEFT);
        fLayout.setAlignOnBaseline(true);

        pnlSignSelection = new JPanel();
        pnlSignSelection.setLayout(fLayout);

        pnlPossibleSigns = new FixedWidthPanel();
        pnlPossibleSupplements = new FixedWidthPanel();
        fillSigns();

        multiSplitPane = new MultiSplitPane();
        File f = new File(RoadSignsPlugin.pluginDir(), "roadsigns-layout.xml");
        try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)))) {
            Node model = (Node) decoder.readObject();
            multiSplitPane.getMultiSplitLayout().setModel(model);
            multiSplitPane.getMultiSplitLayout().setFloatingDividers(false);
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            // (COLUMN
            //    (ROW weight=0.3 (LEAF name=upperleft weight=1.0) upperright)
            //    (ROW weight=0.5 (LEAF name=middleleft weight=0.5) (LEAF name=middleright weight=0.5))
            //    (LEAF name=bottom weight=0.2))

            Split modelRoot = new Split();
            modelRoot.setRowLayout(false);

            Split row1 = new Split();
            row1.setWeight(0.3);
            Leaf upperleft = new Leaf("upperleft");
            upperleft.setWeight(1.0);
            row1.setChildren(Arrays.asList(upperleft, new Divider(), new Leaf("upperright")));

            Split row2 = new Split();
            row2.setWeight(0.5);
            Leaf middleleft = new Leaf("middleleft");
            middleleft.setWeight(0.5);
            Leaf middleright = new Leaf("middleright");
            middleright.setWeight(0.5);
            row2.setChildren(Arrays.asList(middleleft, new Divider(), middleright));

            Leaf bottom = new Leaf("bottom");
            bottom.setWeight(0.2);

            modelRoot.setChildren(Arrays.asList(row1, new Divider(), row2, new Divider(), bottom));

            multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
        }
        multiSplitPane.add(new JScrollPane(pnlSignSelection), "upperleft");
        multiSplitPane.add(buildPreviewPanel(), "upperright");
        JScrollPane scroll1 = new JScrollPane(pnlPossibleSigns,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll1.setPreferredSize(new Dimension(10, 10));
        multiSplitPane.add(scroll1, "middleleft");

        JScrollPane scroll2 = new JScrollPane(pnlPossibleSupplements,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll2.setPreferredSize(new Dimension(10, 10));
        multiSplitPane.add(scroll2, "middleright");
        info = new JEditorPane();
        info.setEditable(false);
        info.setContentType("text/html");
        info.setText(" ");
        info.setBackground(this.getBackground());
        info.addHyperlinkListener(e -> {
            if (e == null || e.getURL() == null)
                return;
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OpenBrowser.displayUrl(e.getURL().toString());
            }
        });

        scrollInfo = new JScrollPane(info, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        multiSplitPane.add(scrollInfo, "bottom");

        return multiSplitPane;
    }

    /**
     * Add the possible signs to the panel for selection
     */
    private void fillSigns() {
        pnlPossibleSigns.removeAll();
        pnlPossibleSupplements.removeAll();
        for (Sign s : signs) {
            JLabel lbl = new JLabel(s.getIcon());
            String tt = "<html>"+s.name;
            String ref = s.getDefaultRef();
            if (ref != null) {
                tt += "  <i><small>("+ref+")</small></i>";
            }
            tt += "</html>";
            lbl.setToolTipText(tt);
            s.label = lbl;
            lbl.addMouseListener(new SignClickListener(s));
            if (s.isSupplementing) {
                pnlPossibleSupplements.add(lbl);
            } else {
                pnlPossibleSigns.add(lbl);
            }
        }
    }

    /**
     * Represents a certain selection of signs by the user.
     *
     * Manages the update of gui elements when the selection changes.
     */
    public class SignSelection {
        private final LinkedList<SignCombination> combos = new LinkedList<>();

        public void remove(SignCombination sc) {
            int i = findIndex(sc);
            combos.remove(i);
            previewModel.update();
            updatePanel(pnlSignSelection);
        }

        public void remove(SignWrapper sw) {
            Pair<Integer, Integer> tmp = findIndex(sw);
            int i = tmp.a;
            int j = tmp.b;
            if (j == 0) {
                combos.remove(i);
                previewModel.update();
                updateSelectableSignsEnabledState();
                updatePanel(pnlSignSelection);
            } else {
                combos.get(i).remove(j);
                previewModel.update();
                updateSelectableSignsEnabledState();
                updatePanel(pnlSignSelection);
            }
        }

        public void add(Sign sAdd) {
            if (!sAdd.isSupplementing || combos.isEmpty()) {
                SignCombination combo = new SignCombination();
                combos.add(combo);
                combo.add(sAdd);

                previewModel.update();
                updateSelectableSignsEnabledState();
                updatePanel(pnlSignSelection);
            } else {
                SignCombination last = combos.getLast();
                last.add(sAdd);

                previewModel.update();
                updatePanel(pnlSignSelection);
            }
        }

        private int findIndex(SignCombination scFind) {
            int i = 0;
            for (SignCombination sc : combos) {
                if (sc == scFind) {
                    return i;
                }
                i++;
            }
            throw new AssertionError("Could not find sign combination.");
        }

        private Pair<Integer, Integer> findIndex(SignWrapper swFind) {
            int selIdx = 0;
            for (SignCombination sc : combos) {
                int combIdx = 0;
                for (SignWrapper sw : sc.signs) {
                    if (swFind == sw) {
                        return new Pair<>(selIdx, combIdx);
                    }
                    combIdx++;
                }
                selIdx++;
            }
            throw new AssertionError("Could not find sign");
        }

        public void updatePanel(JPanel panel) {
            panel.removeAll();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(10, 10, 0, 10);

            for (SignCombination sc : combos) {
                JPanel pnlCombo = new JPanel(new GridBagLayout());
                sc.updatePanel(pnlCombo);
                panel.add(pnlCombo, gbc);
                gbc.gridx++;
                gbc.insets = new Insets(10, 0, 0, 10);
            }
            gbc.weightx = 1.0;
            panel.add(new JLabel(""), gbc); /* filler */
            panel.revalidate();
            panel.repaint();
        }
    }

    /**
     * Describes a list of selected signs where the
     * first sign is a normal or supplementary sign and the
     * rest are all supplementary signs.
     *
     * It can fill a Panel with the necessary gui elements.
     */
    public class SignCombination {
        public LinkedList<SignWrapper> signs;

        SignCombination() {
            signs = new LinkedList<>();
        }

        public void updatePanel(JPanel panel) {
            panel.removeAll();
            panel.setLayout(new GridBagLayout());

            Border etched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
            Border empty = BorderFactory.createEmptyBorder(3, 3, 3, 3);
            panel.setBorder(BorderFactory.createCompoundBorder(etched, empty));

            int i = 0;
            for (SignWrapper sw : signs) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.NORTH;
                panel.add(sw.getSignIcon(), gbc);

                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(sw.getParamsPanel(), gbc);

                i++;
            }
        }

        public void remove(int index) {
            signs.remove(index);
        }

        public void add(final Sign sign) {
            if (!signs.isEmpty() && !sign.isSupplementing)
                throw new IllegalArgumentException("any sign but the first must be a supplement sign"); //FIXME
            final SignWrapper signWrp = new SignWrapper(sign);
            signs.add(signWrp);
        }
    }

    /**
     * Describes a single selected sign, including the parameters entered by
     * the user.
     *
     * It provides the necessary gui elements.
     */
    public class SignWrapper {
        Sign sign;
        JLabel signIcon;
        String signRef;
        JPanel paramsPanel;
        Map<String, String> paramValues = new HashMap<>();

        SignWrapper(Sign sign) {
            this.sign = sign;
            for (final SignParameter p : this.sign.params) {
                paramValues.put(p.ident, p.getDefault());
            }
        }

        @Override
        public String toString() {
            return sign.toString();
        }

        public JLabel getSignIcon() {
            if (signIcon != null)
                return signIcon;
            signIcon = new JLabel(sign.getIcon());
            signIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    sel.remove(SignWrapper.this);
                }
            });
            return signIcon;
        }

        public JPanel getParamsPanel() {
            if (paramsPanel != null)
                return paramsPanel;
            paramsPanel = new JPanel(new GridBagLayout());
            int i = 0;
            for (final SignParameter p : this.sign.params) {
                JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                switch (p.input) {
                    case COMBO: // TODO
//                        create_gui_elements();
//                        break;
                    case TEXTFIELD:
                        final JTextField tf;
                        if (p.fieldWidth != null) {
                            tf = new JTextField(p.getDefault(), p.fieldWidth);
                        } else {
                            tf = new JTextField(p.getDefault());
                        }
                        class TFDocumentListener implements DocumentListener {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                update();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                update();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                update();
                            }

                            public void update() {
                                paramValues.put(p.ident, tf.getText());
                                previewModel.update();
                            }
                        }

                        TFDocumentListener listener = new TFDocumentListener();
                        tf.getDocument().addDocumentListener(listener);
                        JLabel lblPrefix = new JLabel(p.getPrefix());
                        JLabel lblSuffix = new JLabel(p.getSuffix());
                        pnlInput.add(lblPrefix);
                        pnlInput.add(tf);
                        pnlInput.add(lblSuffix);
                        break;
                    default:
                        throw new RuntimeException();
                }
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.WEST;
                paramsPanel.add(pnlInput, gbc);
                i++;
            }
            if (i > 0) {
                paramsPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            }
            return paramsPanel;
        }
    }

    /**
     * Give the user a hint, which supplementary signs fit the current selection.
     * Disabled signs can still be clicked.
     */
    private void updateSelectableSignsEnabledState() {
        if (sel.combos.isEmpty()) {
            for (Sign s : signs) {
                if (s.isSupplementing) {
                    // TODO: only those that have no free parameter
                    s.label.setEnabled(true);
                }
            }
        } else {
            Sign main = sel.combos.getLast().signs.getFirst().sign;
            for (Sign s : signs) {
                if (s.isSupplementing) {
                    s.label.setEnabled(main.supplements.contains(s));
                }
            }
        }
    }

    public JComponent buildPreviewPanel() {
        JPanel previewPanel = new JPanel(new GridBagLayout());

        String[] columnNames = {tr("Key"), tr("Value")};
        String[][] data = {{}};
        previewTable = new JTable(data, columnNames) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int rowIndex = rowAtPoint(e.getPoint());
                int colIndex = columnAtPoint(e.getPoint());
                if (rowIndex == -1 || colIndex == -1)
                    return null;
                //int realColumnIndex = convertColumnIndexToModel(colIndex);
                return (String) getValueAt(rowIndex, colIndex);
            }
        };
        previewTable.setFillsViewportHeight(true);
        previewTable.setRowSelectionAllowed(false);
        previewTable.setColumnSelectionAllowed(false);

        previewModel = new PreviewTableModel();
        previewTable.setModel(previewModel);

        JScrollPane scroll = new JScrollPane(previewTable);
        Dimension dim = new Dimension(336, 10);
        scroll.setPreferredSize(dim);
        scroll.setMinimumSize(dim); /* minimum size is relevant for multisplit layout */

        addTrafficSignTag = new JCheckBox(tr("{0} tag", "traffic_sign"));
        addTrafficSignTag.setSelected(Config.getPref().getBoolean("plugin.roadsigns.addTrafficSignTag"));
        addTrafficSignTag.addActionListener(e -> previewModel.update());

        previewPanel.add(scroll, GBC.eol().fill());
        previewPanel.add(addTrafficSignTag, GBC.eol());
        return previewPanel;
    }

    public class PreviewTableModel extends AbstractTableModel {
        private List<String> keys = new ArrayList<>();
        private List<String> values = new ArrayList<>();

        int rows = 3;
        String[] header = {tr("Key"), tr("Value")};

        @Override
        public int getRowCount() {
            return keys.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return keys.get(rowIndex);
            } else if (columnIndex == 1) {
                return values.get(rowIndex);
            } else
                throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int col) {
            return header[col];
        }

        /**
         * Analyse the selection and derive corresponding tags.
         */
        public void update() {
            final TreeMap<String, String> map = new TreeMap<>();
            String traffic_sign = "";

            for (SignCombination sc : sel.combos) {
                final Map<String, String> env = new HashMap<>();
                String combo_traffic_sign = "";

                /**
                 * Keep track of a named tag. It may be changed by
                 * adding values or conditions.
                 */
                class TagEvaluater {
                    String key;
                    String default_value;
                    List<String> values = new ArrayList<>();
                    List<String> conditions = new ArrayList<>();
                    TagEvaluater(Tag t) {
                        key = t.key.evaluate(env);
                        default_value = t.value.evaluate(env);
                    }

                    public void append_value(String v) {
                        values.add(v);
                    }

                    public void condition(String c) {
                        conditions.add(c);
                    }

                    public Map<String, String> evaluate() {
                        String value = "";
                        if (values.isEmpty()) {
                            value = default_value;
                        } else {
                            String sep = "";
                            for (String v : values) {
                                value += sep+v;
                                sep = ";";
                            }
                        }

                        if (conditions.isEmpty())
                            return Collections.singletonMap(key, value);
                        else {
                            Map<String, String> result = new HashMap<>();
                            for (String c : conditions) {
                                result.put(key+":"+c, value);
                            }
                            return result;
                        }
                    }
                }

                Map<String, TagEvaluater> tags = new LinkedHashMap<>();
                for (SignWrapper sw : sc.signs) {
                    for (Map.Entry<String, String> entry : sw.paramValues.entrySet()) {
                        env.put(entry.getKey(), entry.getValue());
                    }
                    if (sw.sign.ref != null) {
                        sw.signRef = sw.sign.ref.evaluate(env);
                        if (combo_traffic_sign.length() != 0) {
                            combo_traffic_sign += ",";
                        }
                        if (sw.sign.traffic_sign_tag != null) {
                            combo_traffic_sign += sw.sign.traffic_sign_tag.evaluate(env);
                        } else {
                            combo_traffic_sign += sw.signRef;
                        }
                    }
                    for (Tag t : sw.sign.tags) {
                        if (t.tag_ref != null) {
                            if (t.ident != null) {
                                env.put(t.ident+"_key", t.key.evaluate(env));
                                env.put(t.ident+"_value", t.value.evaluate(env));
                            }
                            if (t.append_value != null) {
                                TagEvaluater te = tags.get(t.tag_ref);
                                if (te == null) {
                                    Logging.warn(String.format("referenced tag with ident '%s' not found for appending tag %s.",
                                            t.tag_ref, t.toString()));
                                } else {
                                    te.append_value(t.append_value.evaluate(env));
                                }
                            } else if (t.condition != null) {
                                TagEvaluater te = tags.get(t.tag_ref);
                                if (te == null) {
                                    Logging.warn(String.format("referenced tag with ident '%s' not found for condition tag %s.",
                                            t.tag_ref, t.toString()));
                                } else {
                                    te.condition(t.condition.evaluate(env));
                                }
                            } else {
                                Logging.warn(String.format("found tag_ref but neither append_value nor condition for tag %s.",
                                        t.toString()));
                            }
                        } else if (t.ident != null) {
                            env.put(t.ident+"_key", t.key.evaluate(env));
                            env.put(t.ident+"_value", t.value.evaluate(env));

                            if (tags.get(t.ident) != null) {
                                Logging.warn(String.format("tag identifier %s for %s already in use. ", t.ident, t.toString()));
                            }
                            tags.put(t.ident, new TagEvaluater(t));
                        } else {
                            map.put(t.key.evaluate(env), t.value.evaluate(env));
                        }
                    }
                }
                for (TagEvaluater te : tags.values()) {
                    Map<String, String> result = te.evaluate();
                    map.putAll(result);
                }

                if (combo_traffic_sign.length() != 0) {
                    if (traffic_sign.length() != 0) {
                        traffic_sign += ";";
                    }
                    traffic_sign += combo_traffic_sign;
                }
            }
            if (addTrafficSignTag.isSelected()) {
                map.put("traffic_sign", traffic_sign);
            }

            keys.clear();
            values.clear();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                    keys.add(entry.getKey());
                    values.add(entry.getValue());
                }
            }
            fireTableDataChanged();
        }
    }

    /**
     * Mouse events for the possible signs.
     * Click selects it.
     * MouseOver shows info.
     */
    private class SignClickListener extends MouseAdapter {
        private Sign sign;
        SignClickListener(Sign sign) {
            this.sign = sign;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            info.setText(longText());
            /* scroll up again */
            SwingUtilities.invokeLater(() -> scrollInfo.getVerticalScrollBar().setValue(0));
            sel.add(sign);
        }

        private String longText() {
            StringBuilder txt = new StringBuilder();
            txt.append(sign.long_name == null ? sign.name : sign.long_name);
            String ref = sign.getDefaultRef();
            if (ref != null) {
                txt.append("  <i><small>("+ref+")</small></i>");
            }

            if (sign.help != null) {
                txt.append("<p>");
                txt.append(sign.help);
                txt.append("</p>");
            }

            if (sign.wiki != null || sign.loc_wiki != null) {
                String wikiPrefix = Config.getPref().get("plugin.roadsigns.wikiprefix", "https://wiki.openstreetmap.org/wiki/");
                txt.append("<p>");
                if (sign.loc_wiki != null) {
                    String link = wikiPrefix+sign.loc_wiki;
                    txt.append("<a href=\""+link+"\">"+link+"</a>");
                    txt.append("<br>");
                }
                if (sign.wiki != null && !sign.wiki.equals(sign.loc_wiki)) {
                    String link = wikiPrefix+sign.wiki;
                    txt.append("<a href=\""+link+"\">"+link+"</a>");
                }
                txt.append("</p>");
            }
            return txt.toString();
        }
    }

    /**
     * Panel with FlowLayout that can be put inside a JScrollPane.
     * (Normally it would not flow, but put all its children
     * in a single row. This implementation respects the width of the parent
     * component.)
     */
    public static class FixedWidthPanel extends JPanel implements Scrollable {
        FixedWidthPanel() {
            super(new FlowLayout(FlowLayout.LEFT));
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, getParent().getWidth(), height);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getPreferredHeight());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return super.getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            final int FRAC = 20;
            int inc = (orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth()) / FRAC;
            return Math.max(inc, 1);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth();
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        private int getPreferredHeight() {
            int prefH = 0;
            int num = getComponentCount();
            for (int i = 0; i < num; ++i) {
                Rectangle rect = getComponent(i).getBounds();
                int h = rect.y + rect.height;
                if (h > prefH) {
                    prefH = h;
                }
            }
            prefH += ((FlowLayout) getLayout()).getVgap();
            return prefH;
        }
    }

    public static class SettingsPanel extends JPanel {

        private List<PresetMetaData> presetsData;
        private JComboBox<PresetMetaData> selectionBox;
        JRadioButton rbAll, rbUseful;

        SettingsPanel(boolean standalone, final Action update) {
            super(new GridBagLayout());
            presetsData = RoadSignsPlugin.getAvailablePresetsMetaData();

            selectionBox = new JComboBox<>(presetsData.toArray(new PresetMetaData[0]));
            String code = Config.getPref().get("plugin.roadsigns.preset.selection", null);
            if (code != null) {
                for (PresetMetaData data : presetsData) {
                    if (code.equals(data.code)) {
                        selectionBox.setSelectedItem(data);
                    }
                }
            }
            this.add(new JLabel(tr("Country preset:")), GBC.std().insets(5, 5, 5, 5));
            this.add(selectionBox, GBC.eol().insets(0, 5, 5, 5));
            if (!standalone) {
                //String snd = "Hide signs that do not have an OSM tag assigned";

                rbAll = new JRadioButton(tr("Show all signs"));
                rbUseful = new JRadioButton(tr("Show a selection of the most useful signs"));

                ButtonGroup grp = new ButtonGroup();
                grp.add(rbAll);
                grp.add(rbUseful);

                String filterPref = Config.getPref().get("plugin.roadsigns.preset.filter");
                if (filterPref.equals("useful")) {
                    rbUseful.setSelected(true);
                } else {
                    rbAll.setSelected(true);
                }

                JPanel pnFilter = new JPanel(new GridBagLayout());
                pnFilter.setBorder(BorderFactory.createTitledBorder(tr("Filter")));
                pnFilter.add(rbAll, GBC.eop());
                pnFilter.add(rbUseful, GBC.eop());

                this.add(pnFilter, GBC.eol().insets(5, 0, 5, 5));

                JButton apply = new JButton(new AbstractAction(tr("Apply")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            apply();
                        } catch (IOException ex) {
                            return;
                        }
                        update.actionPerformed(null);
                    }
                });
                this.add(apply, GBC.eol().insets(5, 0, 5, 5));
            }
            this.add(Box.createVerticalGlue(), GBC.eol().fill());
        }

        public void apply() throws IOException {
            String filter = null;
            if (rbAll != null) {
                if (rbAll.isSelected()) {
                    filter = "all";
                } else if (rbUseful.isSelected()) {
                    filter = "useful";
                }
            }
            if (filter != null) {
                Config.getPref().put("plugin.roadsigns.preset.filter", filter);
            }
            RoadSignsPlugin.setSelectedPreset(presetsData.get(selectionBox.getSelectedIndex()));
        }
    }

}
