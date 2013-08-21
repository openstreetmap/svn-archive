package org.openstreetmap.josm.plugins.tageditor.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openstreetmap.josm.plugins.tageditor.preset.Item;

public class PresetManager extends JPanel {

    //static private final Logger logger = Logger.getLogger(PresetManager.class.getName());

    private JComboBox presets;
    private JButton btnRemove;
    private JButton btnHighlight;
    private TagEditorModel model = null;

    protected void build() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // create the combobox to display the list of applied presets
        //
        presets = new JComboBox() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 200;
                return d;
            }
        };

        presets.addItemListener(
                new ItemListener(){
                    public void itemStateChanged(ItemEvent e) {
                        syncWidgetStates();
                    }
                }
        );

        presets.setRenderer(new PresetItemListCellRenderer());
        add(presets);

        btnHighlight = new JButton(tr("Highlight"));
        btnHighlight.addActionListener(
                new ActionListener()  {
                    public void actionPerformed(ActionEvent arg0) {
                        highlightCurrentPreset();
                    }
                }
        );

        add(btnHighlight);

        btnRemove = new JButton(tr("Remove"));
        btnRemove.addActionListener(
                new ActionListener()  {
                    public void actionPerformed(ActionEvent arg0) {
                        removeCurrentPreset();
                    }
                }
        );

        add(btnRemove);
        syncWidgetStates();
    }

    protected void syncWidgetStates() {
        btnRemove.setEnabled(presets.getSelectedItem() != null);
        btnHighlight.setEnabled(presets.getSelectedItem() != null);
    }

    protected void removeCurrentPreset() {
        Item item= (Item)presets.getSelectedItem();
        if (item != null && model !=null) {
            model.removeAppliedPreset(item);
        }
    }

    protected void highlightCurrentPreset() {
        model.highlightCurrentPreset();
    }

    public PresetManager() {
        build();
    }

    public void setModel(TagEditorModel model) {
        presets.setModel(model.getAppliedPresetsModel());
        this.model = model;
    }
}
