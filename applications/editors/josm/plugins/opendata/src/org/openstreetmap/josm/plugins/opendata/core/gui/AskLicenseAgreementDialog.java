package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;

public class AskLicenseAgreementDialog extends ViewLicenseDialog {
	
	public AskLicenseAgreementDialog(License license) throws IOException {
		super(license, Main.parent, tr("License Agreement"), new String[] {tr("Accept"), "", tr("Refuse")});
		
        setToolTipTexts(new String[] {
                tr("I understand and accept these terms and conditions"),
                tr("View the full text of this license"),
                tr("I refuse these terms and conditions. Cancel download.")});
	}
}
