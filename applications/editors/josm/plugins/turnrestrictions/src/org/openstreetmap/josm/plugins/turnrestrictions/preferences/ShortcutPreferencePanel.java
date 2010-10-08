package org.openstreetmap.josm.plugins.turnrestrictions.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.turnrestrictions.CreateOrEditTurnRestrictionAction;

/**
 * ShortcutPreferencePanel allows to configure the global shortcut to trigger
 * creating/editing a turn restriction.
 */
public class ShortcutPreferencePanel extends JPanel {
    
    private JCheckBox cbCtrl;
    private JCheckBox cbAlt;
    private JCheckBox cbShift;
    private JCheckBox cbMeta;
    private JComboBox cmKeyCodes;

    protected JPanel buildMessagePanel() {
        HtmlPanel pnl = new HtmlPanel();
        pnl.setText("<html><body>"
            + tr("Please configure the <strong>keyboard shortcut</strong> which triggers "
                + "creating/editing a turn restriction from the current JOSM selection.")
            + "</body></html>"
        );
        return pnl;
    }
    
    protected JPanel buildShortCutConfigPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;
        gc.gridx = 0;
        gc.gridy = 0;
        
        pnl.add(new JLabel(trc("keyboard-key", "Key:")), gc);
        gc.gridx++;
        gc.gridwidth=4;
        gc.weightx = 1.0;
        pnl.add(cmKeyCodes = new JComboBox(new VKeyComboBoxModel()), gc);
        cmKeyCodes.setRenderer(new VKeyCellRenderer());
        
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.weightx = 0.0;
        pnl.add(new JLabel(trc("keyboard-modifiers", "Modifiers:")), gc);
    
        gc.gridx++;
        pnl.add(cbShift = new JCheckBox(trc("keyboard-modifiers", "Shift")), gc);
        gc.gridx++;
        pnl.add(cbCtrl = new JCheckBox(trc("keyboard-modifiers", "Ctrl")), gc);
        gc.gridx++;
        pnl.add(cbAlt = new JCheckBox(trc("keyboard-modifiers", "Alt")), gc);
        gc.gridx++;
        gc.weightx = 1.0;
        pnl.add(cbMeta = new JCheckBox(trc("keyboard-modifiers", "Meta")), gc);
        
        return pnl;
    }
    
    protected void build() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        add(buildMessagePanel(), gc);
        gc.gridy++;
        add(buildShortCutConfigPanel(), gc);
    }
    
    public ShortcutPreferencePanel() {
        build();
    }
    
    public void initFromPreferences(Preferences pref){
        String value = pref.get(PreferenceKeys.EDIT_SHORTCUT, "shift ctrl T");
        KeyStroke key = KeyStroke.getKeyStroke(value);
        if (key == null){
            System.out.println(tr("Warning: illegal value ''{0}'' for preference key ''{1}''. Falling back to default value ''shift ctrl T''.", value, PreferenceKeys.EDIT_SHORTCUT));
            key = KeyStroke.getKeyStroke("shift ctrl T");
        }
        cmKeyCodes.getModel().setSelectedItem(key.getKeyCode());
        cbAlt.setSelected((key.getModifiers() & KeyEvent.ALT_DOWN_MASK) != 0);
        cbCtrl.setSelected((key.getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0);
        cbShift.setSelected((key.getModifiers() & KeyEvent.SHIFT_DOWN_MASK) != 0);
        cbMeta.setSelected((key.getModifiers() & KeyEvent.META_DOWN_MASK) != 0);
    }
    
    public void saveToPreferences(Preferences pref){
        Integer code  = (Integer)cmKeyCodes.getModel().getSelectedItem();
        if (code == null) {
            code = KeyEvent.VK_T;
        }
        int modifiers = 0;
        if (cbAlt.isSelected()) modifiers |= KeyEvent.ALT_DOWN_MASK;
        if (cbCtrl.isSelected()) modifiers |= KeyEvent.CTRL_DOWN_MASK;
        if (cbShift.isSelected()) modifiers |= KeyEvent.SHIFT_DOWN_MASK;
        if (cbMeta.isSelected()) modifiers |= KeyEvent.META_DOWN_MASK;      
        KeyStroke ks = KeyStroke.getKeyStroke(code, modifiers);
        
        pref.put(PreferenceKeys.EDIT_SHORTCUT, ks.toString());     
        Main.registerActionShortcut(CreateOrEditTurnRestrictionAction.getInstance(), ks);
    }
    
    static private class VKeyComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private final ArrayList<Integer> keys = new ArrayList<Integer>();
        private Integer selected = null;

        public VKeyComboBoxModel() {
            populate();
        }
        
        public void populate() {
            for (Field f :KeyEvent.class.getFields()) {
                if (! Modifier.isStatic(f.getModifiers())) continue;
                if (! f.getName().startsWith("VK_")) continue;
                try {
                    keys.add((Integer)f.get(null));
                } catch(IllegalAccessException e){
                    // ignore
                }
            }
            
            Collections.sort(keys, new KeyCodeComparator());
        }
        
        public Object getSelectedItem() {
            return selected;
        }

        public void setSelectedItem(Object anItem) {
            this.selected = (Integer)anItem;            
        }

        public Object getElementAt(int index) {
            return keys.get(index);
        }

        public int getSize() {
            return keys.size();
        }       
    }
    
    static private class VKeyCellRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(UIManager.getColor("ComboBox.selectionBackground"));
                setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("ComboBox.background"));
                setForeground(UIManager.getColor("ComboBox.foreground"));
            }
            setText(KeyEvent.getKeyText((Integer)value));
            return this;
        }       
    }
    
    static private class KeyCodeComparator implements Comparator<Integer> {
        private final static Map<Integer, String> keyNames = new HashMap<Integer, String>();
        
        protected String keyName(Integer code){
            String name = keyNames.get(code);
            if (name == null){
                name = KeyEvent.getKeyText(code);
                keyNames.put(code, name);
            }
            return name;
        }
        /**
         * Make sure single letter keys (A-Z, 0-9) are at the top of the list.
         * Make sure function key F1-F19 are sorted numerically, not lexicografically.
         * 
         */
        public int compare(Integer kc1, Integer kc2) {
            String n1 = keyName(kc1);
            String n2 = keyName(kc2);
            if (n1.length() == 1 && n2.length()==1){
                return n1.compareTo(n2);
            } else if (n1.length() == 1){
                return -1;
            } else if (n2.length() == 1){
                return 1;
            } else if (n1.matches("F\\d+") && n2.matches("F\\d+")){
                int f1 = Integer.parseInt(n1.substring(1));
                int f2 = Integer.parseInt(n2.substring(1));
                return new Integer(f1).compareTo(f2);               
            } else {
                return n1.compareTo(n2);
            }               
        }       
    }
}
