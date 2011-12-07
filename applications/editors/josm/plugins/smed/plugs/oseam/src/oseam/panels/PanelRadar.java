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
	public JRadioButton reflButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadarReflectorButton.png")));
	public JRadioButton ramarkButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RamarkButton.png")));
	public JRadioButton raconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RaconButton.png")));
	public JRadioButton leadingButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LeadingRaconButton.png")));
	private EnumMap<Rtb, JRadioButton> rads = new EnumMap<Rtb, JRadioButton>(Rtb.class);
	private ActionListener alRad = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Rtb rtb : rads.keySet()) {
				JRadioButton button = rads.get(rtb);
				if (button.isSelected()) {
					dlg.mark.setRadar(rtb);
				}
			}
			syncPanel();
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
			dlg.mark.setRaconSector1(sector1Box.getText());
		}
	};
	public JLabel sector2Label;
	public JTextField sector2Box;
	private ActionListener alSector2 = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setRaconSector2(sector2Box.getText());
		}
	};
	public JLabel sectorsLabel;

	public PanelRadar(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getRadButton(noRadButton, 0, 3, 27, 27, "NoRadar", Rtb.NONE), null);
		this.add(getRadButton(reflButton, 0, 33, 27, 27, "RadarReflector", Rtb.REFLECTOR), null);
		this.add(getRadButton(ramarkButton, 0, 63, 27, 27, "Ramark", Rtb.RAMARK), null);
		this.add(getRadButton(raconButton, 0, 93, 27, 27, "Racon", Rtb.RACON), null);
		this.add(getRadButton(leadingButton, 0, 123, 27, 27, "LeadingRacon", Rtb.LEADING), null);

		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(30, 0, 100, 20));
		this.add(groupLabel, null);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(55, 20, 50, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(groupBox, null);
		groupBox.addActionListener(alGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(130, 0, 100, 20));
		this.add(periodLabel, null);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(155, 20, 50, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(periodBox, null);
		periodBox.addActionListener(alPeriod);

		seqLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		seqLabel.setBounds(new Rectangle(30, 40, 100, 20));
		this.add(seqLabel, null);
		seqBox = new JTextField();
		seqBox.setBounds(new Rectangle(55, 60, 50, 20));
		seqBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(seqBox, null);
		seqBox.addActionListener(alSeq);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(130, 40, 100, 20));
		this.add(rangeLabel, null);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(155, 60, 50, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		rangeBox.addActionListener(alRange);
		
		sectorsLabel = new JLabel(Messages.getString("VisibleSector"), SwingConstants.CENTER);
		sectorsLabel.setBounds(new Rectangle(75, 85, 100, 20));
		this.add(sectorsLabel, null);

		sector1Label = new JLabel(Messages.getString("Start"), SwingConstants.CENTER);
		sector1Label.setBounds(new Rectangle(30, 100, 100, 20));
		this.add(sector1Label, null);
		sector1Box = new JTextField();
		sector1Box.setBounds(new Rectangle(55, 120, 50, 20));
		sector1Box.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(sector1Box, null);
		sector1Box.addActionListener(alSector1);

		sector2Label = new JLabel(Messages.getString("End"), SwingConstants.CENTER);
		sector2Label.setBounds(new Rectangle(130, 100, 100, 20));
		this.add(sector2Label, null);
		sector2Box = new JTextField();
		sector2Box.setBounds(new Rectangle(155, 120, 50, 20));
		sector2Box.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(sector2Box, null);
		sector2Box.addActionListener(alSector2);
	}

	public void syncPanel() {
		boolean rad = ((dlg.mark.getRadar() != Rtb.NONE) && (dlg.mark.getRadar() != Rtb.REFLECTOR));
		groupLabel.setVisible(rad);
		groupBox.setVisible(rad);
		periodLabel.setVisible(rad);
		periodBox.setVisible(rad);
		seqLabel.setVisible(rad);
		seqBox.setVisible(rad);
		rangeLabel.setVisible(rad);
		rangeBox.setVisible(rad);
		sector1Label.setVisible(rad);
		sector1Box.setVisible(rad);
		sector2Label.setVisible(rad);
		sector2Box.setVisible(rad);
		sectorsLabel.setVisible(rad);
		for (Rtb rtb : rads.keySet()) {
			rads.get(rtb).setBorderPainted(dlg.mark.getRadar() == rtb);
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
