package panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import messages.Messages;
import smed.SmedAction;
import seamarks.SeaMark.*;

public class PanelRadar extends JPanel {

	private SmedAction dlg;
	private JToggleButton aisButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/AISButton.png")));
	private ActionListener alAis = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (aisButton.isSelected()) {
				radioCatBox.setVisible(true);
				aisButton.setBorderPainted(true);
			} else {
				radioCatBox.setSelectedIndex(0);
				radioCatBox.setVisible(false);
				aisButton.setBorderPainted(false);
			}
		}
	};
	private JComboBox radioCatBox;
	private EnumMap<Cat, Integer> radioCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alRadioCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : radioCats.keySet()) {
				int idx = radioCats.get(cat);
				if (dlg.node != null && (idx == radioCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setRadio(cat);
				}
			}
		}
	};
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
					dlg.panelMain.mark.setRadar(rtb);
				}
			}
			syncPanel();
		}
	};
	public JLabel groupLabel;
	public JTextField groupBox;
	private FocusListener flGroup = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconGroup(groupBox.getText());
		}
	};
	public JLabel periodLabel;
	public JTextField periodBox;
	private FocusListener flPeriod = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconPeriod(periodBox.getText());
		}
	};
	public JLabel seqLabel;
	public JTextField seqBox;
	private FocusListener flSeq = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconSequence(seqBox.getText());
		}
	};
	public JLabel rangeLabel;
	public JTextField rangeBox;
	private FocusListener flRange = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconRange(rangeBox.getText());
		}
	};
	public JLabel sector1Label;
	public JTextField sector1Box;
	private FocusListener flSector1 = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconSector1(sector1Box.getText());
		}
	};
	public JLabel sector2Label;
	public JTextField sector2Box;
	private FocusListener flSector2 = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setRaconSector2(sector2Box.getText());
		}
	};
	public JLabel sectorsLabel;

	public PanelRadar(SmedAction dia) {
		dlg = dia;
		setLayout(null);
		add(getRadButton(noRadButton, 0, 3, 27, 27, "NoRadar", Rtb.NORTB));
		add(getRadButton(reflButton, 0, 33, 27, 27, "RadarReflector", Rtb.REFLECTOR));
		add(getRadButton(ramarkButton, 0, 63, 27, 27, "Ramark", Rtb.RAMARK));
		add(getRadButton(raconButton, 0, 93, 27, 27, "Racon", Rtb.RACON));
		add(getRadButton(leadingButton, 0, 123, 27, 27, "LeadingRacon", Rtb.LEADING));
		
		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(30, 0, 100, 20));
		add(groupLabel);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(55, 20, 50, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(groupBox);
		groupBox.addFocusListener(flGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(130, 0, 100, 20));
		add(periodLabel);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(155, 20, 50, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(periodBox);
		periodBox.addFocusListener(flPeriod);

		seqLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		seqLabel.setBounds(new Rectangle(30, 40, 100, 20));
		add(seqLabel);
		seqBox = new JTextField();
		seqBox.setBounds(new Rectangle(55, 60, 50, 20));
		seqBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(seqBox);
		seqBox.addFocusListener(flSeq);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(130, 40, 100, 20));
		add(rangeLabel);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(155, 60, 50, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(rangeBox);
		rangeBox.addFocusListener(flRange);
		
		sectorsLabel = new JLabel(Messages.getString("VisibleSector"), SwingConstants.CENTER);
		sectorsLabel.setBounds(new Rectangle(75, 85, 100, 20));
		add(sectorsLabel);

		sector1Label = new JLabel(Messages.getString("Start"), SwingConstants.CENTER);
		sector1Label.setBounds(new Rectangle(30, 100, 100, 20));
		add(sector1Label);
		sector1Box = new JTextField();
		sector1Box.setBounds(new Rectangle(55, 120, 50, 20));
		sector1Box.setHorizontalAlignment(SwingConstants.CENTER);
		add(sector1Box);
		sector1Box.addFocusListener(flSector1);

		sector2Label = new JLabel(Messages.getString("End"), SwingConstants.CENTER);
		sector2Label.setBounds(new Rectangle(130, 100, 100, 20));
		add(sector2Label);
		sector2Box = new JTextField();
		sector2Box.setBounds(new Rectangle(155, 120, 50, 20));
		sector2Box.setHorizontalAlignment(SwingConstants.CENTER);
		add(sector2Box);
		sector2Box.addFocusListener(flSector2);

		aisButton.setBounds(new Rectangle(270, 3, 27, 27));
		aisButton.setBorder(BorderFactory.createLoweredBevelBorder());
		aisButton.setToolTipText("AIS");
		aisButton.addActionListener(alAis);
		add(aisButton);

		radioCatBox = new JComboBox();
		radioCatBox.setBounds(new Rectangle(210, 40, 150, 20));
		add(radioCatBox);
		radioCatBox.addActionListener(alRadioCatBox);
		addROItem("", Cat.NOROS);
		addROItem(Messages.getString("CircularBeacon"), Cat.ROS_OMNI);
		addROItem(Messages.getString("DirectionalBeacon"), Cat.ROS_DIRL);
		addROItem(Messages.getString("RotatingBeacon"), Cat.ROS_ROTP);
		addROItem(Messages.getString("ConsolBeacon"), Cat.ROS_CNSL);
		addROItem(Messages.getString("DirectionFinding"), Cat.ROS_RDF);
		addROItem(Messages.getString("QTGService"), Cat.ROS_QTG);
		addROItem(Messages.getString("AeronaticalBeacon"), Cat.ROS_AERO);
		addROItem(Messages.getString("Decca"), Cat.ROS_DECA);
		addROItem(Messages.getString("LoranC"), Cat.ROS_LORN);
		addROItem(Messages.getString("DGPS"), Cat.ROS_DGPS);
		addROItem(Messages.getString("Toran"), Cat.ROS_TORN);
		addROItem(Messages.getString("Omega"), Cat.ROS_OMGA);
		addROItem(Messages.getString("Syledis"), Cat.ROS_SYLD);
		addROItem(Messages.getString("Chiaka"), Cat.ROS_CHKA);
		addROItem(Messages.getString("PublicCommunication"), Cat.ROS_PCOM);
		addROItem(Messages.getString("CommercialBroadcast"), Cat.ROS_COMB);
		addROItem(Messages.getString("Facsimile"), Cat.ROS_FACS);
		addROItem(Messages.getString("TimeSignal"), Cat.ROS_TIME);
		addROItem(Messages.getString("AIS"), Cat.ROS_PAIS);
		addROItem(Messages.getString("S-AIS"), Cat.ROS_SAIS);
		radioCatBox.setVisible(false);
	}

	public void syncPanel() {
		boolean rad = ((dlg.panelMain.mark.getRadar() != Rtb.NORTB) && (dlg.panelMain.mark.getRadar() != Rtb.REFLECTOR));
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
			rads.get(rtb).setBorderPainted(dlg.panelMain.mark.getRadar() == rtb);
		}
		groupBox.setText(dlg.panelMain.mark.getRaconGroup());
		seqBox.setText(dlg.panelMain.mark.getRaconSequence());
		periodBox.setText(dlg.panelMain.mark.getRaconPeriod());
		rangeBox.setText(dlg.panelMain.mark.getRaconRange());
		sector1Box.setText(dlg.panelMain.mark.getRaconSector1());
		sector2Box.setText(dlg.panelMain.mark.getRaconSector2());
		aisButton.setSelected(dlg.panelMain.mark.getRadio() != Cat.NOROS);
		aisButton.setBorderPainted(aisButton.isSelected());
		radioCatBox.setVisible(dlg.panelMain.mark.getRadio() != Cat.NOROS);
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

	private void addROItem(String str, Cat cat) {
		radioCats.put(cat, radioCatBox.getItemCount());
		radioCatBox.addItem(str);
	}
}
