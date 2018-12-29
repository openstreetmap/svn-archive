/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.tools.GBC;

/**
 * @author Nzara
 *
 */
public class PreferenceSettingsGui {

	private class ModeChoice {
		public Preferences.GuiMode guiMode;
		public String name;

		ModeChoice(Preferences.GuiMode guiMode, String name) {
			this.guiMode = guiMode;
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	private ModeChoice[] choices = { 
			new ModeChoice(Preferences.GuiMode.Auto, tr("Auto")),
			new ModeChoice(Preferences.GuiMode.Simple, tr("Basic")),
			new ModeChoice(Preferences.GuiMode.Expert, tr("Extended"))
			};

	private final String guiAuto = tr("Auto");
	private final String guiBasic = tr("Basic");
	private final String guiExtended = tr("Extended");
			
	private String[] guiPrefs = {guiAuto, guiBasic, guiExtended};
	private JComboBox<String> guiPrefsCombo = new JComboBox<>(guiPrefs);
	private JPanel panel;

	public JPanel getGui() {
		if (panel == null)
			build();
		int guiCode = Preferences.getGuiCode();
		if (guiCode == 1) guiPrefsCombo.setSelectedItem(guiExtended);
		if (guiCode == 2) guiPrefsCombo.setSelectedItem(guiBasic);
		return panel;
	}

	public boolean save() {
		String s = (String) guiPrefsCombo.getSelectedItem();
		if (s==guiAuto) Preferences.setGuiCode(0);
		if (s==guiBasic) Preferences.setGuiCode(2);
		if (s==guiExtended) Preferences.setGuiCode(1);
		
		return true;
	}

	private void build() {
		panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createTitledBorder(tr("User Interface")));
		JLabel l = new JLabel(tr("Interactions:"));
		panel.add(l, GBC.std());
		panel.add(guiPrefsCombo, GBC.eop());
		guiPrefsCombo.setBackground(Color.red);
		guiPrefsCombo.setAlignmentX(Component.RIGHT_ALIGNMENT);
	}

}
