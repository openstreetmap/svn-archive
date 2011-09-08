package oseam.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*	;

public class PanelMore extends JPanel {

	private OSeaMAction dlg;
	public PanelPat panelPat;

	public PanelMore(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		panelPat = new PanelPat(dlg);
		panelPat.setBounds(new Rectangle(0, 0, 100, 160));
		this.add(panelPat, null);

	}

	public void clearSelections() {
		panelPat.clearSelections();
	}

}
