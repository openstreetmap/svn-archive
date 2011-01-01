package oseam.panels;

import javax.swing.JPanel;

import oseam.dialogs.OSeaMAction;

public class PanelRadar extends JPanel {

	private OSeaMAction dlg;

	public PanelRadar(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
	}

	public void clearSelections() {

	}

}
