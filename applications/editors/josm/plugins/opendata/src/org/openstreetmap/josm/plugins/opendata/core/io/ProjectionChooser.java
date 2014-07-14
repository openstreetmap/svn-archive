// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.tools.GBC;

public class ProjectionChooser extends ExtendedDialog {

    /**
     * This is the panel holding all projection preferences
     */
    private final JPanel projPanel = new JPanel(new GridBagLayout());
    
    /**
     * Combobox with all projections available
     */
    private final JComboBox<ProjectionChoice> projectionCombo = new JComboBox<>(
            ProjectionPreference.getProjectionChoices().toArray(new ProjectionChoice[0]));

	public ProjectionChooser(Component parent) {
		this(parent, tr("Projection method"), new String[] {tr("OK"), tr("Cancel")});
	}
	
	protected ProjectionChooser(Component parent, String title,
			String[] buttonTexts) {
		super(parent, title, buttonTexts);
		addGui();
	}
	
	public void addGui() {
        projPanel.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
        projPanel.setLayout(new GridBagLayout());
        projPanel.add(new JLabel(tr("Projection method")), GBC.std().insets(5,5,0,5));
        projPanel.add(GBC.glue(5,0), GBC.std().fill(GBC.HORIZONTAL));
        projPanel.add(projectionCombo, GBC.eop().fill(GBC.HORIZONTAL).insets(0,5,5,5));
        setContent(projPanel);
	}
	
	public Projection getProjection() {
		ProjectionChoice choice = (ProjectionChoice) projectionCombo.getSelectedItem();
		return choice != null ? choice.getProjection() : null;
	}
}
