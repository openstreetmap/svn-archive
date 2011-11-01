package oseam.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelRadar extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup radarButtons = new ButtonGroup();
	public JRadioButton noRadButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton reflButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton raconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton ramarkButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton leadingButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	private EnumMap<Rtb, JRadioButton> rads = new EnumMap<Rtb, JRadioButton>(Rtb.class);
	private ActionListener alRad = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Rtb rtb : rads.keySet()) {
				JRadioButton button = rads.get(rtb);
				if (button.isSelected()) {
					dlg.mark.setRaType(rtb);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			dlg.mark.paintSign();
		}
	};
	public JLabel groupLabel;
	public JTextField groupBox;
	private ActionListener alGroup = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconGroup(groupBox.getText());
		}
	};
	public JLabel periodLabel;
	public JTextField periodBox;
	private ActionListener alPeriod = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconPeriod(periodBox.getText());
		}
	};
	public JLabel seqLabel;
	public JTextField seqBox;
	private ActionListener alSeq = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconSequence(seqBox.getText());
		}
	};
	public JLabel rangeLabel;
	public JTextField rangeBox;
	private ActionListener alRange = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconRange(rangeBox.getText());
		}
	};
	public JLabel sector1Label;
	public JTextField sector1Box;
	private ActionListener alSector1 = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconSector1(rangeBox.getText());
		}
	};
	public JLabel sector2Label;
	public JTextField sector2Box;
	private ActionListener alSector2 = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconSector2(rangeBox.getText());
		}
	};

	public PanelRadar(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getRadButton(noRadButton, 0, 3, 27, 27, "NoRadar", Rtb.NONE), null);
		this.add(getRadButton(reflButton, 0, 33, 27, 27, "RadarReflector", Rtb.REFLECTOR), null);
		this.add(getRadButton(raconButton, 0, 63, 27, 27, "Racon", Rtb.RACON), null);
		this.add(getRadButton(ramarkButton, 0, 93, 27, 27, "Ramark", Rtb.RAMARK), null);
		this.add(getRadButton(leadingButton, 0, 123, 27, 27, "LeadingBeacon", Rtb.LEADING), null);

		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(75, 0, 100, 20));
		this.add(groupLabel, null);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(100, 20, 50, 20));
		this.add(groupBox, null);
		groupBox.addActionListener(alGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(75, 40, 100, 20));
		this.add(periodLabel, null);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(100, 60, 50, 20));
		this.add(periodBox, null);
		periodBox.addActionListener(alPeriod);

		seqLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		seqLabel.setBounds(new Rectangle(75, 80, 100, 20));
		this.add(seqLabel, null);
		seqBox = new JTextField();
		seqBox.setBounds(new Rectangle(100, 100, 50, 20));
		this.add(seqBox, null);
		seqBox.addActionListener(alSeq);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(75, 120, 100, 20));
		this.add(rangeLabel, null);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(100, 140, 50, 20));
		this.add(rangeBox, null);
		rangeBox.addActionListener(alRange);

		sector1Label = new JLabel(Messages.getString("SectorStart"), SwingConstants.CENTER);
		sector1Label.setBounds(new Rectangle(175, 40, 100, 20));
		this.add(sector1Label, null);
		sector1Box = new JTextField();
		sector1Box.setBounds(new Rectangle(200, 60, 50, 20));
		this.add(sector1Box, null);
		sector1Box.addActionListener(alSector1);

		sector2Label = new JLabel(Messages.getString("SectorEnd"), SwingConstants.CENTER);
		sector2Label.setBounds(new Rectangle(175, 80, 100, 20));
		this.add(sector2Label, null);
		sector2Box = new JTextField();
		sector2Box.setBounds(new Rectangle(200, 100, 50, 20));
		this.add(sector2Box, null);
		sector2Box.addActionListener(alSector1);
	}

	public void syncPanel() {
		for (Rtb rtb : rads.keySet()) {
			JRadioButton button = rads.get(rtb);
			if (dlg.mark.getRaType() == rtb) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
		groupBox.setText(dlg.mark.getRaconGroup());
		seqBox.setText(dlg.mark.getRaconSequence());
		periodBox.setText(dlg.mark.getRaconPeriod());
		rangeBox.setText(dlg.mark.getRaconRange());
		sector1Box.setText(dlg.mark.getRaconSector1());
		sector2Box.setText(dlg.mark.getRaconSector2());
	}

	private JRadioButton getRadButton(JRadioButton button, int x, int y, int w, int h, String tip, Rtb rtb) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alRad);
		radarButtons.add(button);
		rads.put(rtb, button);
		return button;
	}

}
