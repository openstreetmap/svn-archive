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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.tagging.TaggingPreset;

/**
 * The tagging presets tester window
 */
public class TaggingPresetTester extends JFrame {

    private JComboBox taggingPresets;
    private final String[] args;
    private JPanel taggingPresetPanel = new JPanel(new BorderLayout());
    private JPanel panel = new JPanel(new BorderLayout());

    public final void reload() {
        Vector<TaggingPreset> allPresets = new Vector<TaggingPreset>(TaggingPreset.readAll(Arrays.asList(args), true));
        taggingPresets.setModel(new DefaultComboBoxModel(allPresets));
    }

    public final void reselect() {
        taggingPresetPanel.removeAll();
        TaggingPreset preset = (TaggingPreset)taggingPresets.getSelectedItem();
        if (preset == null)
            return;
        Collection<OsmPrimitive> x;
        if (Main.main.hasEditLayer()) {
            x = Main.main.getCurrentDataSet().getSelected();
        } else {
            x = makeFakeSuitablePrimitive(preset);
        }
        JPanel p = preset.createPanel(x);
        if (p != null) {
            p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            taggingPresetPanel.add(p, BorderLayout.NORTH);
        }
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
            @Override
            public void actionPerformed(ActionEvent e) {
                reselect();
            }
        });
        reselect();

        JButton b = new JButton(tr("Reload"));
        b.addActionListener(new ActionListener(){
            @Override
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

    private Collection<OsmPrimitive> makeFakeSuitablePrimitive(TaggingPreset preset) {
        if (preset.typeMatches(Collections.singleton(TaggingPreset.PresetType.NODE))) {
            return Collections.<OsmPrimitive>singleton(new Node());
        } else if (preset.typeMatches(Collections.singleton(TaggingPreset.PresetType.WAY))) {
            return Collections.<OsmPrimitive>singleton(new Way());
        } else if (preset.typeMatches(Collections.singleton(TaggingPreset.PresetType.RELATION))) {
            return Collections.<OsmPrimitive>singleton(new Relation());
        } else if (preset.typeMatches(Collections.singleton(TaggingPreset.PresetType.CLOSEDWAY))) {
            Way w = new Way();
            w.addNode(new Node(new LatLon(0,0)));
            w.addNode(new Node(new LatLon(0,1)));
            w.addNode(new Node(new LatLon(1,1)));
            w.addNode(new Node(new LatLon(0,0)));
            return Collections.<OsmPrimitive>singleton(w);
        } else {
            return Collections.emptySet();
        }
    }
}
