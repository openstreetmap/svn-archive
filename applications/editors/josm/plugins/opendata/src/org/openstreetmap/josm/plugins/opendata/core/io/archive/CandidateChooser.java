// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class CandidateChooser extends ExtendedDialog {

    /**
     * This is the panel holding all candidate files
     */
    private final JPanel projPanel = new JPanel(new GridBagLayout());

    private final JComboBox<File> fileCombo;

    public CandidateChooser(Component parent, List<File> candidates) {
        this(parent, tr("File to load"), new String[] {tr("OK"), tr("Cancel")}, candidates);
    }

    private class Renderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                setText(((File) value).getName());
            }
            return this;
        }
    }

    protected CandidateChooser(Component parent, String title, String[] buttonTexts, List<File> candidates) {
        super(parent, title, buttonTexts);
        this.fileCombo = new JComboBox<>(candidates.toArray(new File[0]));
        this.fileCombo.setRenderer(new Renderer());
        addGui(candidates);
    }

    public void addGui(List<File> candidates) {
        projPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projPanel.setLayout(new GridBagLayout());
        projPanel.add(new JLabel(tr("File to load")), GBC.std().insets(5, 5, 0, 5));
        projPanel.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        /*for (File file : candidates) {
            JCheckBox cbox = new JCheckBox(file.getName());
            checkBoxes.put(cbox, file);
            projPanel.add(cbox, GBC.eop().fill(GBC.HORIZONTAL).insets(0, 5, 5, 5));
        }*/
        projPanel.add(fileCombo, GBC.eop().fill(GBC.HORIZONTAL).insets(0, 5, 5, 5));
        setContent(projPanel);
    }

    /*public List<File> getSelectedFiles() {
        List<File> result = new ArrayList<File>();
        for (JCheckBox cbox : checkBoxes.keySet()) {
            if (cbox.isSelected()) {
                result.add(checkBoxes.get(cbox));
            }
        }
        return result;
    }*/

    public File getSelectedFile() {
        return (File) fileCombo.getSelectedItem();
    }
}
