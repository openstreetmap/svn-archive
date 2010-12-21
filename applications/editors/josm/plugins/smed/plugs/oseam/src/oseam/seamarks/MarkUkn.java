package oseam.seamarks;

import oseam.dialogs.OSeaMAction;

public class MarkUkn extends SeaMark {
	public MarkUkn(OSeaMAction dia) {
		super(dia);
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
