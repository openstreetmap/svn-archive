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
import oseam.panels.PanelMain;
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Col;
import oseam.seamarks.SeaMark.Styl;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelHaz extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = null;
	public JRadioButton northButton = null;
	public JRadioButton southButton = null;
	public JRadioButton eastButton = null;
	public JRadioButton westButton = null;
	public JRadioButton isolButton = null;

	private ButtonGroup shapeButtons = null;
	public JRadioButton pillarButton = null;
	public JRadioButton sparButton = null;
	public JRadioButton floatButton = null;
	public JRadioButton beaconButton = null;
	public JRadioButton towerButton = null;

	public PanelHaz(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getNothButton(), null);
		this.add(getSouthButton(), null);
		this.add(getEastButton(), null);
		this.add(getWestButton(), null);
		this.add(getIsolButton(), null);
		catButtons = new ButtonGroup();
		catButtons.add(northButton);
		catButtons.add(southButton);
		catButtons.add(eastButton);
		catButtons.add(westButton);
		catButtons.add(isolButton);
		ActionListener alCat = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (northButton.isSelected()) {
					northButton.setBorderPainted(true);
					dlg.mark.setCategory(Cat.CARD_NORTH);
					dlg.mark.setColour(Col.BLACK_YELLOW);
				} else {
					northButton.setBorderPainted(false);
				}
				if (southButton.isSelected()) {
					southButton.setBorderPainted(true);
					dlg.mark.setCategory(Cat.CARD_SOUTH);
					dlg.mark.setColour(Col.YELLOW_BLACK);
				} else {
					southButton.setBorderPainted(false);
				}
				if (eastButton.isSelected()) {
					eastButton.setBorderPainted(true);
					dlg.mark.setCategory(Cat.CARD_EAST);
					dlg.mark.setColour(Col.BLACK_YELLOW_BLACK);
				} else {
					eastButton.setBorderPainted(false);
				}
				if (westButton.isSelected()) {
					westButton.setBorderPainted(true);
					dlg.mark.setCategory(Cat.CARD_WEST);
					dlg.mark.setColour(Col.YELLOW_BLACK_YELLOW);
				} else {
					westButton.setBorderPainted(false);
				}
				isolButton.setBorderPainted(isolButton.isSelected());
			}
		};
		northButton.addActionListener(alCat);
		southButton.addActionListener(alCat);
		eastButton.addActionListener(alCat);
		westButton.addActionListener(alCat);
		isolButton.addActionListener(alCat);

		this.add(getPillarButton(), null);
		this.add(getSparButton(), null);
		this.add(getFloatButton(), null);
		this.add(getBeaconButton(), null);
		this.add(getTowerButton(), null);
		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(floatButton);
		shapeButtons.add(beaconButton);
		shapeButtons.add(towerButton);
		ActionListener alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (pillarButton.isSelected()) {
				pillarButton.setBorderPainted(true);
				dlg.mark.setShape(Styl.PILLAR);
				} else {
					pillarButton.setBorderPainted(false);
				}
				if (sparButton.isSelected()) {
				sparButton.setBorderPainted(true);
				dlg.mark.setShape(Styl.SPAR);
				} else {
					sparButton.setBorderPainted(false);
				}
				if (floatButton.isSelected()) {
				floatButton.setBorderPainted(true);
				dlg.mark.setShape(Styl.FLOAT);
				} else {
					floatButton.setBorderPainted(false);
				}
				if (beaconButton.isSelected()) {
				beaconButton.setBorderPainted(true);
				dlg.mark.setShape(Styl.BEACON);
				} else {
					beaconButton.setBorderPainted(false);
				}
				if (towerButton.isSelected()) {
				towerButton.setBorderPainted(true);
				dlg.mark.setShape(Styl.TOWER);
				} else {
					towerButton.setBorderPainted(false);
				}
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
		beaconButton.addActionListener(alShape);
		towerButton.addActionListener(alShape);
	}

	private JRadioButton getNothButton() {
		if (northButton == null) {
			northButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/CardNButton.png")));
			northButton.setBounds(new Rectangle(0, 0, 52, 32));
	        northButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			northButton.setToolTipText(Messages.getString("NorthTip"));
		}
		return northButton;
	}

	private JRadioButton getSouthButton() {
		if (southButton == null) {
			southButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/CardSButton.png")));
			southButton.setBounds(new Rectangle(0, 32, 52, 32));
	        southButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			southButton.setToolTipText(Messages.getString("SouthTip"));
		}
		return southButton;
	}

	private JRadioButton getEastButton() {
		if (eastButton == null) {
			eastButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/CardEButton.png")));
			eastButton.setBounds(new Rectangle(0, 64, 52, 32));
	        eastButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			eastButton.setToolTipText(Messages.getString("EastTip"));
		}
		return eastButton;
	}

	private JRadioButton getWestButton() {
		if (westButton == null) {
			westButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/CardWButton.png")));
			westButton.setBounds(new Rectangle(0, 96, 52, 32));
	        westButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			westButton.setToolTipText(Messages.getString("WestTip"));
		}
		return westButton;
	}

	private JRadioButton getIsolButton() {
		if (isolButton == null) {
			isolButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/IsolButton.png")));
			isolButton.setBounds(new Rectangle(0, 128, 52, 32));
	        isolButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			isolButton.setToolTipText(Messages.getString("IsolTip"));
		}
		return isolButton;
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/PillarButton.png")));
			pillarButton.setBounds(new Rectangle(55, 0, 34, 32));
	        pillarButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			pillarButton.setToolTipText(Messages.getString("PillarTip"));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/SparButton.png")));
			sparButton.setBounds(new Rectangle(55, 32, 34, 32));
	        sparButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sparButton.setToolTipText(Messages.getString("SparTip"));
		}
		return sparButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/FloatButton.png")));
			floatButton.setBounds(new Rectangle(55, 64, 34, 32));
	        floatButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			floatButton.setToolTipText(Messages.getString("FloatTip"));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BeaconButton.png")));
			beaconButton.setBounds(new Rectangle(55, 96, 34, 32));
	        beaconButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			beaconButton.setToolTipText(Messages.getString("BeaconTip"));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/TowerButton.png")));
			towerButton.setBounds(new Rectangle(55, 128, 34, 32));
	        towerButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			towerButton.setToolTipText(Messages.getString("TowerTip"));
		}
		return towerButton;
	}

}
