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
import oseam.seamarks.SeaMark.Shp;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelPrefStbd extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup regionButtons = null;
	public JRadioButton regionAButton = null;
	public JRadioButton regionBButton = null;
	private ButtonGroup shapeButtons = null;
	public JRadioButton pillarButton = null;
	public JRadioButton sparButton = null;
	public JRadioButton coneButton = null;
	public JRadioButton floatButton = null;
	public JRadioButton beaconButton = null;
	public JRadioButton towerButton = null;
	private ActionListener alShape = null;

	public PanelPrefStbd(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getRegionAButton(), null);
		this.add(getRegionBButton(), null);
		this.add(getPillarButton(), null);
		this.add(getSparButton(), null);
		this.add(getConeButton(), null);
		this.add(getFloatButton(), null);
		this.add(getBeaconButton(), null);
		this.add(getTowerButton(), null);

		regionButtons = new ButtonGroup();
		regionButtons.add(regionAButton);
		regionButtons.add(regionBButton);
		ActionListener alRegion = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				regionAButton.setBorderPainted(regionAButton.isSelected());
				regionBButton.setBorderPainted(regionBButton.isSelected());
				dlg.mark.paintSign();
			}
		};
		regionAButton.addActionListener(alRegion);
		regionBButton.addActionListener(alRegion);

		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(coneButton);
		shapeButtons.add(floatButton);
		shapeButtons.add(beaconButton);
		shapeButtons.add(towerButton);
		alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (pillarButton.isSelected()) {
					dlg.mark.setShape(Shp.PILLAR);
					pillarButton.setBorderPainted(true);
				} else {
					pillarButton.setBorderPainted(false);
				}
				if (sparButton.isSelected()) {
					dlg.mark.setShape(Shp.SPAR);
					sparButton.setBorderPainted(true);
				} else {
					sparButton.setBorderPainted(false);
				}
				if (coneButton.isSelected()) {
					dlg.mark.setShape(Shp.CONE);
					coneButton.setBorderPainted(true);
				} else {
					coneButton.setBorderPainted(false);
				}
				if (floatButton.isSelected()) {
					dlg.mark.setShape(Shp.FLOAT);
					floatButton.setBorderPainted(true);
				} else {
					floatButton.setBorderPainted(false);
				}
				if (beaconButton.isSelected()) {
					dlg.mark.setShape(Shp.BEACON);
					beaconButton.setBorderPainted(true);
				} else {
					beaconButton.setBorderPainted(false);
				}
				if (towerButton.isSelected()) {
					dlg.mark.setShape(Shp.TOWER);
					towerButton.setBorderPainted(true);
				} else {
					towerButton.setBorderPainted(false);
				}
				if (dlg.mark != null)
					dlg.mark.paintSign();
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		coneButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
		beaconButton.addActionListener(alShape);
		towerButton.addActionListener(alShape);
	}

	public void clearSelections() {
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getRegionAButton() {
		if (regionAButton == null) {
			regionAButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionAButton.png")));
			regionAButton.setBounds(new Rectangle(0, 2, 34, 30));
			regionAButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			regionAButton.setToolTipText(Messages.getString("RegionATip"));
		}
		return regionAButton;
	}

	private JRadioButton getRegionBButton() {
		if (regionBButton == null) {
			regionBButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionBButton.png")));
			regionBButton.setBounds(new Rectangle(0, 32, 34, 30));
			regionBButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			regionBButton.setToolTipText(Messages.getString("RegionBTip"));
		}
		return regionBButton;
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
			pillarButton.setBounds(new Rectangle(0, 64, 34, 32));
			pillarButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			pillarButton.setToolTipText(Messages.getString("PillarTip"));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
			sparButton.setBounds(new Rectangle(0, 96, 34, 32));
			sparButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sparButton.setToolTipText(Messages.getString("SparTip"));
		}
		return sparButton;
	}

	private JRadioButton getConeButton() {
		if (coneButton == null) {
			coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
			coneButton.setBounds(new Rectangle(0, 128, 34, 32));
			coneButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			coneButton.setToolTipText(Messages.getString("ConeTip"));
		}
		return coneButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
			floatButton.setBounds(new Rectangle(35, 0, 34, 32));
			floatButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			floatButton.setToolTipText(Messages.getString("FloatTip"));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
			beaconButton.setBounds(new Rectangle(35, 32, 34, 32));
			beaconButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			beaconButton.setToolTipText(Messages.getString("BeaconTip"));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
			towerButton.setBounds(new Rectangle(35, 64, 34, 32));
			towerButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			towerButton.setToolTipText(Messages.getString("TowerTip"));
		}
		return towerButton;
	}

}
