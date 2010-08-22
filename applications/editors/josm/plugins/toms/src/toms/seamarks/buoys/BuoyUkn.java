//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import org.openstreetmap.josm.data.osm.Node;

import toms.dialogs.SmpDialogAction;
import toms.seamarks.SeaMark;

public class BuoyUkn extends Buoy {
	public BuoyUkn(SmpDialogAction dia, String Msg) {
		super(dia);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");

		if (dlg.cbM01Kennung != null) {
			dlg.cbM01Kennung.removeAllItems();
			dlg.cbM01Kennung.addItem("Not set");
		}

		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(false);
		dlg.tbM01Region.setEnabled(false);

		setErrMsg(Msg);
		setName("");
		setTopMark(false);
		setFired(false);
		setValid(false);
		setBuoyIndex(0);
	}

	public void paintSign() {
		super.paintSign();

		if (dlg.lM01Icon01 != null)
			dlg.lM01Icon01.setIcon(null);
		if (getErrMsg() != null)
			dlg.sM01StatusBar.setText(getErrMsg());

		setErrMsg(null);

		dlg.tfM01Name.setText(getName());
		dlg.tfM01Name.setEnabled(false);

		dlg.cM01Fired.setEnabled(false);
		dlg.cM01TopMark.setEnabled(false);

		dlg.bM01Save.setEnabled(false);

	}

	public void setLightColour() {
		super.setLightColour("");
	}

	public void saveSign() {
	}
}