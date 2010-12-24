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
	public JRadioButton regionAButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionAButton.png")));
	public JRadioButton regionBButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionBButton.png")));
	private ButtonGroup shapeButtons = null;
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	private ActionListener alShape = null;

	public PanelPrefStbd(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getButton(regionAButton, 0, 2, 34, 30, "RegionATip"), null);
		this.add(getButton(regionBButton, 0, 32, 34, 30, "RegionBTip"), null);
		this.add(getButton(pillarButton, 0, 64, 34, 32, "PillarTip"), null);
		this.add(getButton(sparButton, 0, 96, 34, 32, "SparTip"), null);
		this.add(getButton(coneButton, 0, 128, 34, 32, "ConeTip"), null);
		this.add(getButton(floatButton, 35, 0, 34, 32, "FloatTip"), null);
		this.add(getButton(beaconButton, 35, 32, 34, 32, "BeaconTip"), null);
		this.add(getButton(towerButton, 35, 64, 34, 32, "TowerTip"), null);

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

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		return button;
	}

}
