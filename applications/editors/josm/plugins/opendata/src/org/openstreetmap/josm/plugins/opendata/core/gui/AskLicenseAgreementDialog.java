package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.tools.ImageProvider;

public class AskLicenseAgreementDialog extends ExtendedDialog {

	private final License license;
	private final JEditorPane htmlPane;
	private boolean summary;
	
	public AskLicenseAgreementDialog(License license) throws IOException {
		super(Main.parent, tr("License Agreement"), new String[] {tr("Accept"), "", tr("Refuse")});
		
		this.license = license;
		this.htmlPane = new JEditorPane();
		//htmlPane.setEditorKitForContentType(pdfEditorKit.getContentType(), pdfEditorKit);
		htmlPane.setEditable(false);
		if (license.getSummaryURL() != null) {
			htmlPane.setPage(license.getSummaryURL());
			summary = true;
		} else {
			htmlPane.setPage(license.getURL());
			summary = false;
		}
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		scrollPane.setPreferredSize(new Dimension(800, 600));
        
        setButtonIcons(new Icon[] {
                ImageProvider.get("ok"),
                ImageProvider.get("agreement24"),
                ImageProvider.get("cancel"),
                });
        setToolTipTexts(new String[] {
                tr("I understand and accept these terms and conditions"),
                tr("View the full text of this license"),
                tr("I refuse these terms and conditions. Cancel download.")});
        if (license.getIcon() != null) {
        	setIcon(license.getIcon());
        } else {
        	setIcon(JOptionPane.INFORMATION_MESSAGE);
        }
        setCancelButton(3);
        setMinimumSize(new Dimension(300, 200));
        setContent(scrollPane, false);
	}

	@Override
	protected void buttonAction(int buttonIndex, ActionEvent evt) {
		if (buttonIndex == 1) {
			try {
				if (summary) {
					buttons.get(1).setText(tr("View summary"));
					htmlPane.setPage(license.getURL());
				} else {
					buttons.get(1).setText(tr("View full text"));
					htmlPane.setPage(license.getSummaryURL());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			summary = !summary;
		} else {
			super.buttonAction(buttonIndex, evt);
		}
	}

	@Override
	public void setupDialog() {
		super.setupDialog();
		buttons.get(1).setEnabled(license.getSummaryURL() != null && license.getURL() != null);
		if (summary) {
			buttons.get(1).setText(tr("View full text"));
		} else {
			buttons.get(1).setText(tr("View summary"));
		}
	}
}
