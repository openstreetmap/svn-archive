package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*	;

public class PanelMore extends JPanel {

	private OSeaMAction dlg;
	public JLabel infoLabel;
	public JTextField infoBox;
	private ActionListener alInfo = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel sourceLabel;
	public JTextField sourceBox;
	private ActionListener alSource = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel elevLabel;
	public JTextField elevBox;
	private ActionListener alElev = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel heightLabel;
	public JTextField heightBox;
	private ActionListener alHeight = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel statusLabel;
	public JComboBox statusBox;
	private ActionListener alStatus = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel constrLabel;
	public JComboBox constrBox;
	private ActionListener alConstr = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel visLabel;
	public JComboBox visBox;
	private ActionListener alVis = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public JLabel conspLabel;
	public JComboBox conspBox;
	private ActionListener alConsp = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark == null)
				return;
		}
	};
	public PanelPat panelPat;
	private ButtonGroup regionButtons = new ButtonGroup();
	public JRadioButton regionAButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionAButton.png")));
	public JRadioButton regionBButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionBButton.png")));
	public JRadioButton regionCButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RegionCButton.png")));
	private ActionListener alRegion = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (regionAButton.isSelected()) {
				dlg.mark.setRegion(Reg.A);
				switch (dlg.mark.getCategory()) {
				case LAM_PORT:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.setPattern(Ent.BODY, Pat.NONE);
					break;
				case LAM_PPORT:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.setPattern(Ent.BODY, Pat.HORIZ);
					break;
				case LAM_STBD:
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					dlg.mark.setPattern(Ent.BODY, Pat.NONE);
					break;
				case LAM_PSTBD:
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.setPattern(Ent.BODY, Pat.HORIZ);
					break;
				}
				regionAButton.setBorderPainted(true);
			} else {
				regionAButton.setBorderPainted(false);
			}
			if (regionBButton.isSelected()) {
				dlg.mark.setRegion(Reg.B);
				switch (dlg.mark.getCategory()) {
				case LAM_PORT:
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					dlg.mark.setPattern(Ent.BODY, Pat.NONE);
					break;
				case LAM_PPORT:
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.setPattern(Ent.BODY, Pat.HORIZ);
					break;
				case LAM_STBD:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.setPattern(Ent.BODY, Pat.NONE);
					break;
				case LAM_PSTBD:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.setPattern(Ent.BODY, Pat.HORIZ);
					break;
				}
				regionBButton.setBorderPainted(true);
			} else {
				regionBButton.setBorderPainted(false);
			}
			if (regionCButton.isSelected()) {
				dlg.mark.setRegion(Reg.C);
				dlg.mark.setPattern(Ent.BODY, Pat.HORIZ);
				switch (dlg.mark.getCategory()) {
				case LAM_PORT:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.WHITE);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.WHITE);
					break;
				case LAM_PPORT:
				case LAM_PSTBD:
					dlg.mark.setColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.RED);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					break;
				case LAM_STBD:
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.WHITE);
					dlg.mark.addColour(Ent.BODY, Col.GREEN);
					dlg.mark.addColour(Ent.BODY, Col.WHITE);
					break;
				}
				regionCButton.setBorderPainted(true);
			} else {
				regionCButton.setBorderPainted(false);
			}
			dlg.mark.paintSign();
		}
	};

	public PanelMore(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		panelPat = new PanelPat(dlg);
		panelPat.setBounds(new Rectangle(0, 0, 110, 160));
		this.add(panelPat, null);
		this.add(getRegionButton(regionAButton, 110, 0, 34, 30, "RegionA"), null);
		this.add(getRegionButton(regionBButton, 110, 32, 34, 30, "RegionB"), null);
		this.add(getRegionButton(regionCButton, 110, 64, 34, 30, "RegionC"), null);

		elevLabel = new JLabel(Messages.getString("Elevation"), SwingConstants.CENTER);
		elevLabel.setBounds(new Rectangle(140, 0, 90, 20));
		this.add(elevLabel, null);
		elevBox = new JTextField();
		elevBox.setBounds(new Rectangle(160, 20, 50, 20));
		this.add(elevBox, null);
		elevBox.addActionListener(alElev);

		heightLabel = new JLabel(Messages.getString("StructureHeight"), SwingConstants.CENTER);
		heightLabel.setBounds(new Rectangle(230, 0, 100, 20));
		this.add(heightLabel, null);
		heightBox = new JTextField();
		heightBox.setBounds(new Rectangle(250, 20, 50, 20));
		this.add(heightBox, null);
		heightBox.addActionListener(alHeight);

		sourceLabel = new JLabel(Messages.getString("Source"), SwingConstants.CENTER);
		sourceLabel.setBounds(new Rectangle(110, 120, 140, 20));
		this.add(sourceLabel, null);
		sourceBox = new JTextField();
		sourceBox.setBounds(new Rectangle(110, 100, 140, 20));
		this.add(sourceBox, null);
		sourceBox.addActionListener(alSource);

		infoLabel = new JLabel(Messages.getString("Information"), SwingConstants.CENTER);
		infoLabel.setBounds(new Rectangle(110, 120, 140, 20));
		this.add(infoLabel, null);
		infoBox = new JTextField();
		infoBox.setBounds(new Rectangle(110, 140, 140, 20));
		this.add(infoBox, null);
		infoBox.addActionListener(alInfo);

/*		statusLabel = new JLabel(Messages.getString("Status"), SwingConstants.CENTER);
		statusLabel.setBounds(new Rectangle(150, 0, 100, 20));
		this.add(statusLabel, null);
		statusBox = new JComboBox();
		statusBox.setBounds(new Rectangle(150, 20, 100, 20));
		this.add(statusBox, null);
		statusBox.addActionListener(alStatus);
*/
		}

	public void clearSelections() {
		panelPat.clearSelections();
	}

	private JRadioButton getRegionButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alRegion);
		regionButtons.add(button);
		return button;
	}

}
