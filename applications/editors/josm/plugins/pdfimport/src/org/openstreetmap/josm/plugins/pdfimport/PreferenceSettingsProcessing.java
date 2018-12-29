/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.tools.GBC;

/**
 * @author Nzara
 *
 */
public class PreferenceSettingsProcessing extends PathOptimizerConfig{
	
	public PreferenceSettingsProcessing() {
		super();
		panel.setBorder(BorderFactory.createTitledBorder(tr("Processing defaults")));
	}
	public JPanel getGui() {
		return panel;
	}
	
}
