//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

@SuppressWarnings("serial")
public class ProjectionChooser extends ExtendedDialog {

    /**
     * This is the panel holding all projection preferences
     */
    private final JPanel projPanel = new JPanel(new GridBagLayout());
    
    /**
     * Combobox with all projections available
     */
    private final JComboBox projectionCombo = new JComboBox(ProjectionPreference.getProjectionChoices().toArray());

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
