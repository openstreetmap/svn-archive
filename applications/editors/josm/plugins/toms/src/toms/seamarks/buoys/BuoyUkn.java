//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;

import toms.dialogs.SmpDialogAction;

public class BuoyUkn extends Buoy {
	public BuoyUkn(SmpDialogAction dia, String Msg) {
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