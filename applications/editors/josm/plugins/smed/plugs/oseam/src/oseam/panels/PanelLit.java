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
	private ActionListener alGroup = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = groupBox.getText();
		}
	};
	public JLabel periodLabel;
	public JTextField periodBox;
	private ActionListener alPeriod = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = periodBox.getText();
		}
	};
	public JLabel sequenceLabel;
	public JTextField sequenceBox;
	private ActionListener alSequence = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = sequenceBox.getText();
		}
	};
	public JLabel visibilityLabel;
	public JComboBox visibilityBox;
	public EnumMap<Vis, Integer> visibilities = new EnumMap<Vis, Integer>(Vis.class);
	private ActionListener alVisibility = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		}
	};
	public JLabel heightLabel;
	public JTextField heightBox;
	private ActionListener alHeight = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = heightBox.getText();
		}
	};
	public JLabel rangeLabel;
	public JTextField rangeBox;
	private ActionListener alRange = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = rangeBox.getText();
		}
	};
	public JLabel orientationLabel;
	public JTextField orientationBox;
	private ActionListener alOrientation = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = orientationBox.getText();
		}
	};
	public JLabel categoryLabel;
	public JComboBox categoryBox;
	private ActionListener alCategory = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		}
	};
	public JLabel exhibitionLabel;
	public JComboBox exhibitionBox;
	public EnumMap<Exh, Integer> exhibitions = new EnumMap<Exh, Integer>(Exh.class);
	private ActionListener alExhibition = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
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
				if (panelSector == null) {
					panelSector = new PanelSectors(dlg);
					panelSector.setAlwaysOnTop(true);
					panelSector.setLocation(450, 0);
				}
				if (panelSector.getSectorCount() == 0) {
					panelSector.addSector(1);
					dlg.mark.setSectored(true);
				}
				panelSector.setVisible(true);
			} else {
				dlg.mark.setSectored(false);
				panelSector.setVisible(false);
			}
		}
	};

	public PanelLit(OSeaMAction dia) {
		dlg = dia;
		panelChr = new PanelChr(dlg);
		panelChr.setBounds(new Rectangle(0, 0, 88, 160));
		panelCol = new PanelCol(dlg, Ent.LIGHT);
		panelCol.setBounds(new Rectangle(88, 0, 34, 160));
		this.setLayout(null);
		this.add(panelChr, null);
		this.add(panelCol, null);

		typeButtons = new ButtonGroup();
		singleButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SingleButton.png")));
		this.add(getTypeButton(singleButton, 280, 125, 34, 30, "Single"), null);
		sectorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SectorButton.png")));
		this.add(getTypeButton(sectorButton, 315, 125, 34, 30, "Sector"), null);

		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(123, 0, 65, 20));
		this.add(groupLabel, null);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(135, 20, 40, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(groupBox, null);
		groupBox.addActionListener(alGroup);

		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(123, 40, 65, 20));
		this.add(periodLabel, null);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(135, 60, 40, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(periodBox, null);
		periodBox.addActionListener(alPeriod);

		heightLabel = new JLabel(Messages.getString("Height"), SwingConstants.CENTER);
		heightLabel.setBounds(new Rectangle(123, 80, 65, 20));
		this.add(heightLabel, null);
		heightBox = new JTextField();
		heightBox.setBounds(new Rectangle(135, 100, 40, 20));
		heightBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(heightBox, null);
		heightBox.addActionListener(alHeight);

		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(123, 120, 65, 20));
		this.add(rangeLabel, null);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(135, 140, 40, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(rangeBox, null);
		rangeBox.addActionListener(alRange);

		sequenceLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		sequenceLabel.setBounds(new Rectangle(188, 120, 80, 20));
		this.add(sequenceLabel, null);
		sequenceBox = new JTextField();
		sequenceBox.setBounds(new Rectangle(183, 140, 90, 20));
		sequenceBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(sequenceBox, null);
		sequenceBox.addActionListener(alSequence);

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(185, 0, 165, 20));
		this.add(categoryLabel, null);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(185, 20, 165, 20));
		this.add(categoryBox, null);
		categoryBox.addActionListener(alCategory);
		categoryBox.addItem(Messages.getString("NoneSpecified"));
		categoryBox.addItem(Messages.getString("Vert2"));

		visibilityLabel = new JLabel(Messages.getString("Visibility"), SwingConstants.CENTER);
		visibilityLabel.setBounds(new Rectangle(185, 40, 165, 20));
		this.add(visibilityLabel, null);
		visibilityBox = new JComboBox();
		visibilityBox.setBounds(new Rectangle(185, 60, 165, 20));
		this.add(visibilityBox, null);
		visibilityBox.addActionListener(alVisibility);
		addVisibItem(Messages.getString("NoneSpecified"), Vis.UNKNOWN);
		addVisibItem(Messages.getString("Intensified"), Vis.INTEN);
		addVisibItem(Messages.getString("Unintensified"), Vis.UNINTEN);
		addVisibItem(Messages.getString("PartiallyObscured"), Vis.PARTOBS);

		exhibitionLabel = new JLabel(Messages.getString("Exhibition"), SwingConstants.CENTER);
		exhibitionLabel.setBounds(new Rectangle(280, 80, 70, 20));
		this.add(exhibitionLabel, null);
		exhibitionBox = new JComboBox();
		exhibitionBox.setBounds(new Rectangle(280, 100, 70, 20));
		this.add(exhibitionBox, null);
		exhibitionBox.addActionListener(alExhibition);
		addExhibItem(Messages.getString("NotSet"), Exh.UNKNOWN);
		addExhibItem(Messages.getString("24h"), Exh.H24);
		addExhibItem(Messages.getString("Day"), Exh.DAY);
		addExhibItem(Messages.getString("Night"), Exh.NIGHT);
		addExhibItem(Messages.getString("Fog"), Exh.FOG);

		orientationLabel = new JLabel(Messages.getString("Orientation"), SwingConstants.CENTER);
		orientationLabel.setBounds(new Rectangle(188, 80, 80, 20));
		this.add(orientationLabel, null);
		orientationBox = new JTextField();
		orientationBox.setBounds(new Rectangle(208, 100, 40, 20));
		orientationBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(orientationBox, null);
		orientationBox.addActionListener(alOrientation);
	}

	public void syncPanel() {
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
