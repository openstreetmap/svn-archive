package oseam.panels;

import javax.swing.BorderFactory;
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

public class PanelLights extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = null;
	private JRadioButton houseButton = null;
	private JRadioButton majorButton = null;
	private JRadioButton minorButton = null;
	private JRadioButton vesselButton = null;
	private JRadioButton floatButton = null;

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getHouseButton(), null);
		this.add(getMajorButton(), null);
		this.add(getMinorButton(), null);
		this.add(getVesselButton(), null);
		this.add(getFloatButton(), null);
		catButtons = new ButtonGroup();
		catButtons.add(houseButton);
		catButtons.add(majorButton);
		catButtons.add(minorButton);
		catButtons.add(vesselButton);
		catButtons.add(floatButton);
		ActionListener alCat = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				houseButton.setBorderPainted(houseButton.isSelected());
				majorButton.setBorderPainted(majorButton.isSelected());
				minorButton.setBorderPainted(minorButton.isSelected());
				vesselButton.setBorderPainted(vesselButton.isSelected());
				floatButton.setBorderPainted(floatButton.isSelected());
				if (dlg.mark != null) dlg.mark.paintSign();
			}
		};
		houseButton.addActionListener(alCat);
		majorButton.addActionListener(alCat);
		minorButton.addActionListener(alCat);
		vesselButton.addActionListener(alCat);
		floatButton.addActionListener(alCat);
	}

	public void clearSelections() {
		
	}

	private JRadioButton getHouseButton() {
		if (houseButton == null) {
			houseButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LighthouseButton.png")));
			houseButton.setBounds(new Rectangle(0, 0, 34, 32));
	        houseButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			houseButton.setToolTipText(Messages.getString("LighthouseTip"));
		}
		return houseButton;
	}

	private JRadioButton getMajorButton() {
		if (majorButton == null) {
			majorButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LightMajorButton.png")));
			majorButton.setBounds(new Rectangle(0, 32, 34, 32));
	        majorButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			majorButton.setToolTipText(Messages.getString("MajorLightTip"));
		}
		return majorButton;
	}

	private JRadioButton getMinorButton() {
		if (minorButton == null) {
			minorButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LightMinorButton.png")));
			minorButton.setBounds(new Rectangle(0, 64, 34, 32));
	        minorButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			minorButton.setToolTipText(Messages.getString("MinorLightTip"));
		}
		return minorButton;
	}

	private JRadioButton getVesselButton() {
		if (vesselButton == null) {
			vesselButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LightVesselButton.png")));
			vesselButton.setBounds(new Rectangle(0, 96, 34, 32));
	        vesselButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			vesselButton.setToolTipText(Messages.getString("LightVesselTip"));
		}
		return vesselButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/LightFloatButton.png")));
			floatButton.setBounds(new Rectangle(0, 128, 34, 32));
	        floatButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			floatButton.setToolTipText(Messages.getString("LightFloatTip"));
		}
		return floatButton;
	}

}
