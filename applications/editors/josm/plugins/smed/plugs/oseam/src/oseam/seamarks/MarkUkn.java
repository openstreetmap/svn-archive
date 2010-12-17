package oseam.seamarks;

import oseam.dialogs.OSeaMAction;

public class MarkUkn extends SeaMark {
	public MarkUkn(OSeaMAction dia, String Msg) {
		super(dia);
//		dlg.cbM01TypeOfMark.setSelectedIndex(0);
		setErrMsg(Msg);
	}

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
