package oseam.panels;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelRadar extends JPanel {

	private OSeaMAction dlg;

	public PanelRadar(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
	}

	public void clearSelections() {

	}

}
