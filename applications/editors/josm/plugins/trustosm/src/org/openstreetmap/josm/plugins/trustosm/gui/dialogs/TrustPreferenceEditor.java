package org.openstreetmap.josm.plugins.trustosm.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.trustosm.TrustOSMplugin;
import org.openstreetmap.josm.tools.GBC;

public class TrustPreferenceEditor implements PreferenceSetting {

	private final JCheckBox showSignedDeleted = new JCheckBox(tr("Show deleted tags and notes if they were signed before"));
	private final JRadioButton defaultHomedir = new JRadioButton(tr("Use default (and maybe existing) GnuPG directory ({0}) to store new keys and configs.", "~/.gnupg"));
	private final JRadioButton separateHomedir = new JRadioButton(tr("Use separate GnuPG directory ({0}) to store new keys and configs.", TrustOSMplugin.getGpgPath()));

	@Override
	public void addGui(final PreferenceTabbedPane gui) {
		// TODO Auto-generated method stub
		JPanel p = gui.createPreferenceTab("trustosm", tr("Trust OSM Settings"), tr("Change GPG and privacy settings of the trustosm plugin."));
		JTabbedPane tabs = new JTabbedPane();
		p.add(tabs, GBC.eol().fill(GBC.BOTH));


		JPanel gpgsettings = new JPanel(new GridBagLayout());

		gpgsettings.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		gpgsettings.add(new JLabel(tr("Use separate GnuPG configuration directory?")), GBC.eol().insets(20,0,0,0));
		ButtonGroup gpgdirGroup = new ButtonGroup();
		gpgdirGroup.add(separateHomedir);
		gpgdirGroup.add(defaultHomedir);

		if (Main.pref.getBoolean("trustosm.gpg.separateHomedir")) {
			separateHomedir.setSelected(true);
		} else
			defaultHomedir.setSelected(true);

		gpgsettings.add(separateHomedir, GBC.eol().insets(40,0,0,0));
		gpgsettings.add(defaultHomedir, GBC.eol().insets(40,0,0,0));


		gpgsettings.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
		JScrollPane scrollpane = new JScrollPane(gpgsettings);
		scrollpane.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
		tabs.add(tr("GnuPG"), scrollpane);


		JPanel dialogsettings = new JPanel(new GridBagLayout());
		tabs.add(tr("Dialog"), dialogsettings);
		dialogsettings.add(showSignedDeleted, GBC.eol().fill(GBC.HORIZONTAL).insets(20,0,0,5));

	}

	@Override
	public boolean ok() {
		Main.pref.put("trustosm.gpg.showSignedDeleted", showSignedDeleted.isSelected());
		Main.pref.put("trustosm.gpg.separateHomedir", separateHomedir.isSelected());
		//		TrustOSMplugin.gpg.refreshGpg();
		return false;
	}


}
