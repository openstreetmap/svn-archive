package oseam.panels;

import javax.swing.JPanel;

import java.awt.Rectangle;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Ent;

public class PanelLit extends JPanel {

	private OSeaMAction dlg;
	public PanelCol panelCol = null;
	public PanelChr panelChr = null;

	public PanelLit(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg, Ent.LIGHT);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		panelCol.blackButton.setVisible(false);
		panelChr = new PanelChr(dlg);
		panelChr.setBounds(new Rectangle(36, 0, 68, 160));
		this.setLayout(null);
		this.add(panelCol, null);
		this.add(panelChr, null);
	}

	public void clearSelections() {

	}

}
