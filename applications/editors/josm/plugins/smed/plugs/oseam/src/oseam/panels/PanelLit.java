package oseam.panels;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.EnumMap;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelLit extends JPanel {

	private OSeaMAction dlg;
	public PanelSectors panelSector;
	public PanelCol panelCol;
	public PanelChr panelChr;
	public JLabel groupLabel;
	public JTextField groupBox;
	private FocusListener flGroup = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.GRP, 0, groupBox.getText());
		}
	};
	public JLabel periodLabel;
	public JTextField periodBox;
	private FocusListener flPeriod = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.PER, 0, periodBox.getText());
		}
	};
	public JLabel sequenceLabel;
	public JTextField sequenceBox;
	private FocusListener flSequence = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.SEQ, 0, sequenceBox.getText());
		}
	};
	public JLabel visibilityLabel;
	public JComboBox visibilityBox;
	public EnumMap<Vis, Integer> visibilities = new EnumMap<Vis, Integer>(Vis.class);
	private ActionListener alVisibility = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Vis vis : visibilities.keySet()) {
				int idx = visibilities.get(vis);
				if (idx == visibilityBox.getSelectedIndex())
					dlg.panelMain.mark.setLightAtt(Att.VIS, 0, vis);
			}
		}
	};
	public JLabel heightLabel;
	public JTextField heightBox;
	private FocusListener flHeight = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.HGT, 0, heightBox.getText());
		}
	};
	public JLabel rangeLabel;
	public JTextField rangeBox;
	private FocusListener flRange = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.RNG, 0, rangeBox.getText());
		}
	};
	public JLabel orientationLabel;
	public JTextField orientationBox;
	private FocusListener flOrientation = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.ORT, 0, orientationBox.getText());
		}
	};
	public JLabel multipleLabel;
	public JTextField multipleBox;
	private FocusListener flMultiple = new FocusAdapter() {
		public void focusLost(java.awt.event.FocusEvent e) {
			dlg.panelMain.mark.setLightAtt(Att.MLT, 0, multipleBox.getText());
		}
	};
	public JLabel categoryLabel;
	public JComboBox categoryBox;
	public EnumMap<Lit, Integer> categories = new EnumMap<Lit, Integer>(Lit.class);
	private ActionListener alCategory = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Lit lit : categories.keySet()) {
				int idx = categories.get(lit);
				if (idx == categoryBox.getSelectedIndex())
					dlg.panelMain.mark.setLightAtt(Att.LIT, 0, lit);
			}
			if (dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.DIR) {
				dlg.panelMain.mark.setLightAtt(Att.MLT, 0, "");
				multipleBox.setText("");
				orientationLabel.setVisible(true);
				orientationBox.setVisible(true);
				multipleLabel.setVisible(false);
				multipleBox.setVisible(false);
			} else if ((dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.VERT) || (dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.HORIZ)) {
				dlg.panelMain.mark.setLightAtt(Att.ORT, 0, "");
				orientationBox.setText("");
				orientationLabel.setVisible(false);
				orientationBox.setVisible(false);
				multipleLabel.setVisible(true);
				multipleBox.setVisible(true);
			} else {
				dlg.panelMain.mark.setLightAtt(Att.MLT, 0, "");
				multipleBox.setText("");
				dlg.panelMain.mark.setLightAtt(Att.ORT, 0, "");
				orientationBox.setText("");
				orientationLabel.setVisible(false);
				orientationBox.setVisible(false);
				multipleLabel.setVisible(false);
				multipleBox.setVisible(false);
			}
		}
	};
	public JLabel exhibitionLabel;
	public JComboBox exhibitionBox;
	public EnumMap<Exh, Integer> exhibitions = new EnumMap<Exh, Integer>(Exh.class);
	private ActionListener alExhibition = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Exh exh : exhibitions.keySet()) {
				int idx = exhibitions.get(exh);
				if (idx == exhibitionBox.getSelectedIndex())
					dlg.panelMain.mark.setLightAtt(Att.EXH, 0, exh);
			}
		}
	};
	private ButtonGroup typeButtons;
	public JRadioButton singleButton;
	public JRadioButton sectorButton;
	private ActionListener alType = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			singleButton.setBorderPainted(singleButton.isSelected());
			sectorButton.setBorderPainted(sectorButton.isSelected());
			if (sectorButton.isSelected()) {
				panelSector.setVisible(true);
			} else {
				panelSector.setVisible(false);
				while (dlg.panelMain.mark.getSectorCount() > 1)
					dlg.panelMain.mark.delLight(1);
			}
		}
	};

	public PanelLit(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		panelCol = new PanelCol(dlg, Ent.LIGHT);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		panelChr = new PanelChr(dlg);
		panelChr.setBounds(new Rectangle(34, 0, 88, 160));
		add(panelChr);
		add(panelCol);
		panelSector = new PanelSectors(dlg);
		panelSector.setVisible(false);

		typeButtons = new ButtonGroup();
		singleButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SingleButton.png")));
		add(getTypeButton(singleButton, 280, 125, 34, 30, "Single"));
		sectorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SectorButton.png")));
		add(getTypeButton(sectorButton, 315, 125, 34, 30, "Sectored"));

		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(123, 0, 65, 20));
		add(groupLabel);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(135, 20, 40, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(groupBox);
		groupBox.addFocusListener(flGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(123, 40, 65, 20));
		add(periodLabel);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(135, 60, 40, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(periodBox);
		periodBox.addFocusListener(flPeriod);

		heightLabel = new JLabel(Messages.getString("Height"), SwingConstants.CENTER);
		heightLabel.setBounds(new Rectangle(123, 80, 65, 20));
		add(heightLabel);
		heightBox = new JTextField();
		heightBox.setBounds(new Rectangle(135, 100, 40, 20));
		heightBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(heightBox);
		heightBox.addFocusListener(flHeight);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(123, 120, 65, 20));
		add(rangeLabel);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(135, 140, 40, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(rangeBox);
		rangeBox.addFocusListener(flRange);

		sequenceLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		sequenceLabel.setBounds(new Rectangle(188, 120, 80, 20));
		add(sequenceLabel);
		sequenceBox = new JTextField();
		sequenceBox.setBounds(new Rectangle(183, 140, 90, 20));
		sequenceBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(sequenceBox);
		sequenceBox.addFocusListener(flSequence);

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(185, 0, 165, 20));
		add(categoryLabel);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(185, 20, 165, 20));
		add(categoryBox);
		addCatItem("", Lit.UNKLIT);
		addCatItem(Messages.getString("VertDisp"), Lit.VERT);
		addCatItem(Messages.getString("HorizDisp"), Lit.HORIZ);
		addCatItem(Messages.getString("Directional"), Lit.DIR);
		addCatItem(Messages.getString("Upper"), Lit.UPPER);
		addCatItem(Messages.getString("Lower"), Lit.LOWER);
		addCatItem(Messages.getString("Rear"), Lit.REAR);
		addCatItem(Messages.getString("Front"), Lit.FRONT);
		addCatItem(Messages.getString("Aero"), Lit.AERO);
		addCatItem(Messages.getString("AirObstruction"), Lit.AIROBS);
		addCatItem(Messages.getString("FogDetector"), Lit.FOGDET);
		addCatItem(Messages.getString("Floodlight"), Lit.FLOOD);
		addCatItem(Messages.getString("Striplight"), Lit.STRIP);
		addCatItem(Messages.getString("Subsidiary"), Lit.SUBS);
		addCatItem(Messages.getString("Spotlight"), Lit.SPOT);
		addCatItem(Messages.getString("MoireEffect"), Lit.MOIRE);
		addCatItem(Messages.getString("Emergency"), Lit.EMERG);
		addCatItem(Messages.getString("Bearing"), Lit.BEAR);
		categoryBox.addActionListener(alCategory);

		visibilityLabel = new JLabel(Messages.getString("Visibility"), SwingConstants.CENTER);
		visibilityLabel.setBounds(new Rectangle(185, 40, 165, 20));
		add(visibilityLabel);
		visibilityBox = new JComboBox();
		visibilityBox.setBounds(new Rectangle(185, 60, 165, 20));
		add(visibilityBox);
		addVisibItem("", Vis.UNKVIS);
		addVisibItem(Messages.getString("Intensified"), Vis.INTEN);
		addVisibItem(Messages.getString("Unintensified"), Vis.UNINTEN);
		addVisibItem(Messages.getString("PartiallyObscured"), Vis.PARTOBS);
		visibilityBox.addActionListener(alVisibility);

		exhibitionLabel = new JLabel(Messages.getString("Exhibition"), SwingConstants.CENTER);
		exhibitionLabel.setBounds(new Rectangle(280, 80, 70, 20));
		add(exhibitionLabel);
		exhibitionBox = new JComboBox();
		exhibitionBox.setBounds(new Rectangle(280, 100, 70, 20));
		add(exhibitionBox);
		addExhibItem("", Exh.UNKEXH);
		addExhibItem(Messages.getString("24h"), Exh.H24);
		addExhibItem(Messages.getString("Day"), Exh.DAY);
		addExhibItem(Messages.getString("Night"), Exh.NIGHT);
		addExhibItem(Messages.getString("Fog"), Exh.FOG);
		exhibitionBox.addActionListener(alExhibition);

		orientationLabel = new JLabel(Messages.getString("Orientation"), SwingConstants.CENTER);
		orientationLabel.setBounds(new Rectangle(188, 80, 80, 20));
		orientationLabel.setVisible(false);
		add(orientationLabel);
		orientationBox = new JTextField();
		orientationBox.setBounds(new Rectangle(208, 100, 40, 20));
		orientationBox.setHorizontalAlignment(SwingConstants.CENTER);
		orientationBox.setVisible(false);
		add(orientationBox);
		orientationBox.addFocusListener(flOrientation);

		multipleLabel = new JLabel(Messages.getString("Multiplicity"), SwingConstants.CENTER);
		multipleLabel.setBounds(new Rectangle(188, 80, 80, 20));
		multipleLabel.setVisible(false);
		add(multipleLabel);
		multipleBox = new JTextField();
		multipleBox.setBounds(new Rectangle(208, 100, 40, 20));
		multipleBox.setHorizontalAlignment(SwingConstants.CENTER);
		multipleBox.setVisible(false);
		add(multipleBox);
		multipleBox.addFocusListener(flMultiple);
	}

	public void syncPanel() {
		orientationLabel.setVisible(false);
		orientationBox.setVisible(false);
		multipleLabel.setVisible(false);
		multipleBox.setVisible(false);
		groupBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.GRP, 0));
		periodBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.PER, 0));
		sequenceBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.SEQ, 0));
		heightBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.HGT, 0));
		rangeBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.RNG, 0));
		orientationBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.ORT, 0));
		orientationBox.setVisible(dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.DIR);
		multipleBox.setText((String)dlg.panelMain.mark.getLightAtt(Att.MLT, 0));
		multipleBox.setVisible((dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.VERT) || (dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == Lit.HORIZ));
		for (Vis vis : visibilities.keySet()) {
			int item = visibilities.get(vis);
			if (dlg.panelMain.mark.getLightAtt(Att.VIS, 0) == vis)
				visibilityBox.setSelectedIndex(item);
		}
		for (Lit lit : categories.keySet()) {
			int item = categories.get(lit);
			if (dlg.panelMain.mark.getLightAtt(Att.LIT, 0) == lit)
				categoryBox.setSelectedIndex(item);
		}
		for (Exh exh : exhibitions.keySet()) {
			int item = exhibitions.get(exh);
			if (dlg.panelMain.mark.getLightAtt(Att.EXH, 0) == exh)
				exhibitionBox.setSelectedIndex(item);
		}
		if (dlg.panelMain.mark.isSectored()) {
			singleButton.setBorderPainted(false);
			sectorButton.setBorderPainted(true);
			panelSector.setVisible(true);
		} else {
			singleButton.setBorderPainted(true);
			sectorButton.setBorderPainted(false);
			panelSector.setVisible(false);
			while (dlg.panelMain.mark.getSectorCount() > 1)
				dlg.panelMain.mark.delLight(dlg.panelMain.mark.getSectorCount() - 1);
		}
		panelCol.syncPanel();
		panelChr.syncPanel();
		panelSector.syncPanel();
	}

	private void addCatItem(String str, Lit lit) {
		categories.put(lit, categoryBox.getItemCount());
		categoryBox.addItem(str);
	}

	private void addVisibItem(String str, Vis vis) {
		visibilities.put(vis, visibilityBox.getItemCount());
		visibilityBox.addItem(str);
	}

	private void addExhibItem(String str, Exh exh) {
		exhibitions.put(exh, exhibitionBox.getItemCount());
		exhibitionBox.addItem(str);
	}

	private JRadioButton getTypeButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alType);
		typeButtons.add(button);
		return button;
	}

}
