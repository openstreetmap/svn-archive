package oseam.seamarks;

import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;

public class MarkUkn extends SeaMark {
	public MarkUkn(OSeaMAction dia, Node node) {
		super(dia, node);
		dlg.panelMain.clearSelections();
	}

	public void parseMark() {}
	
	public void paintSign() {
/*		if (dlg.paintlock)
			return;
		super.paintSign();

		if (getErrMsg() != null)
			dlg.sM01StatusBar.setText(getErrMsg());

		setErrMsg(null);
*/	}

	public void setLightColour() {
		super.setLightColour("");
	}

	public void saveSign() {
	}
}
