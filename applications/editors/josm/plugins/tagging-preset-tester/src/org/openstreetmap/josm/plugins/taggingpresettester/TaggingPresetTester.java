package org.openstreetmap.josm.plugins.taggingpresettester;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.tagging.TaggingPreset;

/**
 * The tagging presets tester window
 */
public class TaggingPresetTester extends JFrame {

    private JComboBox taggingPresets;
    private final String[] args;
    private JPanel taggingPresetPanel = new JPanel(new BorderLayout());
    private JPanel panel = new JPanel(new BorderLayout());

    public void reload() {
        Vector<TaggingPreset> allPresets = new Vector<TaggingPreset>(TaggingPreset.readAll(Arrays.asList(args), true));
        taggingPresets.setModel(new DefaultComboBoxModel(allPresets));
    }

    public void reselect() {
        taggingPresetPanel.removeAll();
        TaggingPreset preset = (TaggingPreset)taggingPresets.getSelectedItem();
        if (preset == null)
            return;
        Collection<OsmPrimitive> x = Collections.emptySet();
        JPanel p = preset.createPanel(x);
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        if (p != null)
            taggingPresetPanel.add(p, BorderLayout.NORTH);
        panel.validate();
        panel.repaint();
    }

    public TaggingPresetTester(String[] args) {
        super(tr("Tagging Preset Tester"));
        this.args = args;
        taggingPresets = new JComboBox();
        taggingPresets.setRenderer(new TaggingCellRenderer());
        reload();

        panel.add(taggingPresets, BorderLayout.NORTH);
        panel.add(taggingPresetPanel, BorderLayout.CENTER);
        taggingPresets.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                reselect();
            }
        });
        reselect();

        JButton b = new JButton(tr("Reload"));
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int i = taggingPresets.getSelectedIndex();
                reload();
                if (i < taggingPresets.getItemCount()) {
                	taggingPresets.setSelectedIndex(i);
                }
            }
        });
        panel.add(b, BorderLayout.SOUTH);

        setContentPane(panel);
        setSize(300,500);
        setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            JFileChooser c = new JFileChooser();
            if (c.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                return;
            args = new String[]{c.getSelectedFile().getPath()};
        }
        JFrame f = new TaggingPresetTester(args);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
