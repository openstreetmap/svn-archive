// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.CoordinateFormat;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.gui.preferences.projection.SubPrefsOptions;
import org.openstreetmap.josm.tools.GBC;

/**
 * Projection chooser, ugly copy-paste of ProjectionPreference.
 * FIXME: to be refactored in core for reuse by plugins.
 */
public class ProjectionChooser extends ExtendedDialog {

    /**
     * Combobox with all projections available
     */
    private final JComboBox<ProjectionChoice> projectionCombo = new JComboBox<>(
            ProjectionPreference.getProjectionChoices().toArray(new ProjectionChoice[0]));

    /**
     * This variable holds the JPanel with the projection's preferences. If the
     * selected projection does not implement this, it will be set to an empty
     * Panel.
     */
    private JPanel projSubPrefPanel;
    private JPanel projSubPrefPanelWrapper = new JPanel(new GridBagLayout());

    private JLabel projectionCodeLabel;
    private Component projectionCodeGlue;
    private JLabel projectionCode = new JLabel();
    private JLabel projectionNameLabel;
    private Component projectionNameGlue;
    private JLabel projectionName = new JLabel();
    private JLabel bounds = new JLabel();
    
    /**
     * This is the panel holding all projection preferences
     */
    private final JPanel projPanel = new JPanel(new GridBagLayout());

    /**
     * The GridBagConstraints for the Panel containing the ProjectionSubPrefs.
     * This is required twice in the code, creating it here keeps both occurrences
     * in sync
     */
    private static final GBC projSubPrefPanelGBC = GBC.std().fill(GBC.BOTH).weight(1.0, 1.0);

    public ProjectionChooser(Component parent) {
        this(parent, tr("Projection method"), new String[] {tr("OK"), tr("Cancel")});
    }
    
    protected ProjectionChooser(Component parent, String title, String[] buttonTexts) {
        super(parent, title, buttonTexts);
        setMinimumSize(new Dimension(600, 200));
        addGui();
    }
    
    public void addGui() {
        projPanel.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
        projPanel.setLayout(new GridBagLayout());
        projPanel.add(new JLabel(tr("Projection method")), GBC.std().insets(5,5,0,5));
        projPanel.add(GBC.glue(5,0), GBC.std().fill(GBC.HORIZONTAL));
        projPanel.add(projectionCombo, GBC.eop().fill(GBC.HORIZONTAL).insets(0,5,5,5));
        projPanel.add(projectionCodeLabel = new JLabel(tr("Projection code")), GBC.std().insets(25,5,0,5));
        projPanel.add(projectionCodeGlue = GBC.glue(5,0), GBC.std().fill(GBC.HORIZONTAL));
        projPanel.add(projectionCode, GBC.eop().fill(GBC.HORIZONTAL).insets(0,5,5,5));
        projPanel.add(projectionNameLabel = new JLabel(tr("Projection name")), GBC.std().insets(25,5,0,5));
        projPanel.add(projectionNameGlue = GBC.glue(5,0), GBC.std().fill(GBC.HORIZONTAL));
        projPanel.add(projectionName, GBC.eop().fill(GBC.HORIZONTAL).insets(0,5,5,5));
        projPanel.add(new JLabel(tr("Bounds")), GBC.std().insets(25,5,0,5));
        projPanel.add(GBC.glue(5,0), GBC.std().fill(GBC.HORIZONTAL));
        projPanel.add(bounds, GBC.eop().fill(GBC.HORIZONTAL).insets(0,5,5,5));
        projPanel.add(projSubPrefPanelWrapper, GBC.eol().fill(GBC.HORIZONTAL).insets(20,5,5,5));

        selectedProjectionChanged((ProjectionChoice) projectionCombo.getSelectedItem());
        projectionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectionChoice pc = (ProjectionChoice) projectionCombo.getSelectedItem();
                selectedProjectionChanged(pc);
            }
        });
        setContent(projPanel);
    }
    
    private void selectedProjectionChanged(final ProjectionChoice pc) {
        // Don't try to update if we're still starting up
        int size = projPanel.getComponentCount();
        if(size < 1)
            return;

        final ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeta(pc);
            }
        };

        // Replace old panel with new one
        projSubPrefPanelWrapper.removeAll();
        projSubPrefPanel = pc.getPreferencePanel(listener);
        projSubPrefPanelWrapper.add(projSubPrefPanel, projSubPrefPanelGBC);
        projPanel.revalidate();
        projSubPrefPanel.repaint();
        updateMeta(pc);
    }
    
    private void updateMeta(ProjectionChoice pc) {
        pc.setPreferences(pc.getPreferences(projSubPrefPanel));
        Projection proj = pc.getProjection();
        projectionCode.setText(proj.toCode());
        projectionName.setText(proj.toString());
        Bounds b = proj.getWorldBoundsLatLon();
        CoordinateFormat cf = CoordinateFormat.getDefaultFormat();
        bounds.setText(b.getMin().lonToString(cf)+", "+b.getMin().latToString(cf)+" : "+b.getMax().lonToString(cf)+", "+b.getMax().latToString(cf));
        boolean showCode = true;
        boolean showName = false;
        if (pc instanceof SubPrefsOptions) {
            showCode = ((SubPrefsOptions) pc).showProjectionCode();
            showName = ((SubPrefsOptions) pc).showProjectionName();
        }
        projectionCodeLabel.setVisible(showCode);
        projectionCodeGlue.setVisible(showCode);
        projectionCode.setVisible(showCode);
        projectionNameLabel.setVisible(showName);
        projectionNameGlue.setVisible(showName);
        projectionName.setVisible(showName);
    }

    public Projection getProjection() {
        ProjectionChoice pc = (ProjectionChoice) projectionCombo.getSelectedItem();
        if (pc != null) {
            Main.info("Chosen projection: "+pc+" ("+pc.getProjection()+")");
            return pc.getProjection();
        } else {
            return null;
        }
    }
}
