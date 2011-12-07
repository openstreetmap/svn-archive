package oseam.panels;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.EnumMap;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelFog extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup fogButtons = new ButtonGroup();
	public JRadioButton noFogButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton yesFogButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogYesButton.png")));
	public JRadioButton hornButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogHornButton.png")));
	public JRadioButton sirenButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogSirenButton.png")));
	public JRadioButton diaButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogDiaButton.png")));
	public JRadioButton bellButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogBellButton.png")));
	public JRadioButton whisButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogWhisButton.png")));
	public JRadioButton gongButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogGongButton.png")));
	public JRadioButton explosButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FogExplosButton.png")));
	private EnumMap<Fog, JRadioButton> fogs = new EnumMap<Fog, JRadioButton>(Fog.class);
	private ActionListener alFog = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Fog fog : fogs.keySet()) {
				JRadioButton button = fogs.get(fog);
				if (button.isSelected()) {
					dlg.mark.setFogSound(fog);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};
	public JLabel groupLabel;
	public JTextField groupBox;
	private ActionListener alGroup = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setFogGroup(groupBox.getText());
		}
	};
	public JLabel periodLabel;
	public JTextField periodBox;
	private ActionListener alPeriod = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setFogPeriod(periodBox.getText());
		}
	};
	public JLabel seqLabel;
	public JTextField seqBox;
	private ActionListener alSeq = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setFogSequence(seqBox.getText());
		}
	};
	public JLabel rangeLabel;
	public JTextField rangeBox;
	private ActionListener alRange = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.mark.setFogRange(rangeBox.getText());
		}
	};

	public PanelFog(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getFogButton(noFogButton, 0, 2, 27, 27, "NoFog", Fog.NONE), null);
		this.add(getFogButton(yesFogButton, 0, 32, 27, 27, "FogSignal", Fog.UNKNOWN), null);
		this.add(getFogButton(hornButton, 0, 62, 27, 27, "Horn", Fog.HORN), null);
		this.add(getFogButton(sirenButton, 0, 92, 27, 27, "Siren", Fog.SIREN), null);
		this.add(getFogButton(gongButton, 0, 122, 27, 27, "Gong", Fog.SIREN), null);
		this.add(getFogButton(diaButton, 30, 2, 27, 27, "Diaphone", Fog.DIA), null);
		this.add(getFogButton(bellButton, 30, 32, 27, 27, "Bell", Fog.BELL), null);
		this.add(getFogButton(whisButton, 30, 62, 27, 27, "Whistle", Fog.WHIS), null);
		this.add(getFogButton(explosButton, 30, 92, 27, 27, "Explosion", Fog.EXPLOS), null);

		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(75, 0, 100, 20));
		this.add(groupLabel, null);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(100, 20, 50, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(groupBox, null);
		groupBox.addActionListener(alGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(75, 40, 100, 20));
		this.add(periodLabel, null);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(100, 60, 50, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(periodBox, null);
		periodBox.addActionListener(alPeriod);

		seqLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		seqLabel.setBounds(new Rectangle(75, 80, 100, 20));
		this.add(seqLabel, null);
		seqBox = new JTextField();
		seqBox.setBounds(new Rectangle(100, 100, 50, 20));
		seqBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(seqBox, null);
		seqBox.addActionListener(alSeq);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(75, 120, 100, 20));
		this.add(rangeLabel, null);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(100, 140, 50, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(rangeBox, null);
		rangeBox.addActionListener(alRange);

	}

	public void syncPanel() {
		for (Fog fog : fogs.keySet()) {
			JRadioButton button = fogs.get(fog);
			button.setBorderPainted(dlg.mark.getFogSound() == fog);
		}
		groupBox.setText(dlg.mark.getFogGroup());
		seqBox.setText(dlg.mark.getFogSequence());
		periodBox.setText(dlg.mark.getFogPeriod());
		rangeBox.setText(dlg.mark.getFogRange());
	}

	private JRadioButton getFogButton(JRadioButton button, int x, int y, int w, int h, String tip, Fog fog) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alFog);
		fogButtons.add(button);
		fogs.put(fog, button);
		return button;
	}

}
