package oseam.seamarks.buoys;

import oseam.dialogs.OSeaMAction;

public class BuoyUkn extends Buoy {
	public BuoyUkn(OSeaMAction dia, String Msg) {
		super(dia);
		resetMask();
		dlg.cbM01TypeOfMark.setSelectedIndex(0);
		setErrMsg(Msg);
	}

	public void paintSign() {
		if (dlg.paintlock)
			return;
		super.paintSign();

		if (getErrMsg() != null)
			dlg.sM01StatusBar.setText(getErrMsg());

		setErrMsg(null);
	}

	public void setLightColour() {
		super.setLightColour("");
	}

	public void saveSign() {
	}
}