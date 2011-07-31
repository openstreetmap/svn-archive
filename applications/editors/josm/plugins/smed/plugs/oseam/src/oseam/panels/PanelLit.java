package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Ent;

public class PanelLit extends JPanel {

	private OSeaMAction dlg;
	public PanelSectors panelSector;
	public PanelCol panelCol;
	public PanelChr panelChr;
	public JLabel groupLabel;
	public JTextField groupBox;
	public JLabel periodLabel;
	public JTextField periodBox;
	public JLabel sequenceLabel;
	public JTextField sequenceBox;
	public JLabel visibilityLabel;
	public JComboBox visibilityBox;
	public JLabel heightLabel;
	public JTextField heightBox;
	public JLabel rangeLabel;
	public JTextField rangeBox;
	public JLabel orientationLabel;
	public JTextField orientationBox;
	public JLabel categoryLabel;
	public JComboBox categoryBox;
	public JLabel exhibitionLabel;
	public JComboBox exhibitionBox;
	private ButtonGroup typeButtons;
	public JRadioButton singleButton;
	public JRadioButton sectorButton;
	private ActionListener alType;
	private ActionListener alGroupBox;
	private ActionListener alPeriodBox;
	private ActionListener alSequenceBox;
	private ActionListener alVisibilityBox;
	private ActionListener alHeightBox;
	private ActionListener alRangeBox;
	private ActionListener alOrientationBox;
	private ActionListener alCategoryBox;
	private ActionListener alExhibitionBox;

	public PanelLit(OSeaMAction dia) {
		dlg = dia;
		panelChr = new PanelChr(dlg);
		panelChr.setBounds(new Rectangle(0, 0, 88, 160));
		panelCol = new PanelCol(dlg, Ent.LIGHT);
		panelCol.setBounds(new Rectangle(88, 0, 34, 160));
		panelCol.blackButton.setVisible(false);
		this.setLayout(null);
		this.add(panelChr, null);
		this.add(panelCol, null);

		alType = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				singleButton.setBorderPainted(singleButton.isSelected());
				sectorButton.setBorderPainted(sectorButton.isSelected());
				if (sectorButton.isSelected()) {
					if (panelSector == null) {
						panelSector = new PanelSectors(dlg.mark.light);
						panelSector.setAlwaysOnTop(true);
						panelSector.setLocation(450, 0);
					}
					if (panelSector.getSectorCount() == 0) {
						panelSector.addSector(1);
						panelSector.light.setSectored(true);
					}
					panelSector.setVisible(true);
				} else {
					dlg.mark.light.setSectored(false);
					panelSector.setVisible(false);
				}
			}
		};
		typeButtons = new ButtonGroup();
		singleButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SingleButton.png")));
		this.add(getTypeButton(singleButton, 280, 125, 34, 30, "Single"), null);
		sectorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SectorButton.png")));
		this.add(getTypeButton(sectorButton, 315, 125, 34, 30, "Sector"), null);

		alGroupBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = groupBox.getText();
			}
		};
		groupLabel = new JLabel(Messages.getString("Group"), SwingConstants.CENTER);
		groupLabel.setBounds(new Rectangle(123, 0, 65, 20));
		this.add(groupLabel, null);
		groupBox = new JTextField();
		groupBox.setBounds(new Rectangle(135, 20, 40, 20));
		groupBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(groupBox, null);
		groupBox.addActionListener(alGroupBox);

		alPeriodBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = periodBox.getText();
			}
		};
		periodLabel = new JLabel(Messages.getString("Period"), SwingConstants.CENTER);
		periodLabel.setBounds(new Rectangle(123, 40, 65, 20));
		this.add(periodLabel, null);
		periodBox = new JTextField();
		periodBox.setBounds(new Rectangle(135, 60, 40, 20));
		periodBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(periodBox, null);
		periodBox.addActionListener(alPeriodBox);

		alHeightBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = heightBox.getText();
			}
		};
		heightLabel = new JLabel(Messages.getString("Height"), SwingConstants.CENTER);
		heightLabel.setBounds(new Rectangle(123, 80, 65, 20));
		this.add(heightLabel, null);
		heightBox = new JTextField();
		heightBox.setBounds(new Rectangle(135, 100, 40, 20));
		heightBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(heightBox, null);
		heightBox.addActionListener(alHeightBox);

		alRangeBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = rangeBox.getText();
			}
		};
		rangeLabel = new JLabel(Messages.getString("Range"), SwingConstants.CENTER);
		rangeLabel.setBounds(new Rectangle(123, 120, 65, 20));
		this.add(rangeLabel, null);
		rangeBox = new JTextField();
		rangeBox.setBounds(new Rectangle(135, 140, 40, 20));
		rangeBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(rangeBox, null);
		rangeBox.addActionListener(alRangeBox);

		alSequenceBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = sequenceBox.getText();
			}
		};
		sequenceLabel = new JLabel(Messages.getString("Sequence"), SwingConstants.CENTER);
		sequenceLabel.setBounds(new Rectangle(188, 120, 80, 20));
		this.add(sequenceLabel, null);
		sequenceBox = new JTextField();
		sequenceBox.setBounds(new Rectangle(183, 140, 90, 20));
		sequenceBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(sequenceBox, null);
		sequenceBox.addActionListener(alSequenceBox);

		alCategoryBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
			}
		};
		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(185, 0, 165, 20));
		this.add(categoryLabel, null);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(185, 20, 165, 20));
		this.add(categoryBox, null);
		categoryBox.addActionListener(alCategoryBox);
		categoryBox.addItem(Messages.getString("NoneSpecified"));
		categoryBox.addItem(Messages.getString("Vert2"));

		alVisibilityBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
			}
		};
		visibilityLabel = new JLabel(Messages.getString("Visibility"), SwingConstants.CENTER);
		visibilityLabel.setBounds(new Rectangle(185, 40, 165, 20));
		this.add(visibilityLabel, null);
		visibilityBox = new JComboBox();
		visibilityBox.setBounds(new Rectangle(185, 60, 165, 20));
		this.add(visibilityBox, null);
		visibilityBox.addActionListener(alVisibilityBox);
		visibilityBox.addItem(Messages.getString("NoneSpecified"));
		visibilityBox.addItem(Messages.getString("Intensified"));
		visibilityBox.addItem(Messages.getString("Unintensified"));
		visibilityBox.addItem(Messages.getString("PartiallyObscured"));

		alExhibitionBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
			}
		};
		exhibitionLabel = new JLabel(Messages.getString("Exhibition"), SwingConstants.CENTER);
		exhibitionLabel.setBounds(new Rectangle(280, 80, 70, 20));
		this.add(exhibitionLabel, null);
		exhibitionBox = new JComboBox();
		exhibitionBox.setBounds(new Rectangle(280, 100, 70, 20));
		this.add(exhibitionBox, null);
		exhibitionBox.addActionListener(alExhibitionBox);
		exhibitionBox.addItem("-");
		exhibitionBox.addItem(Messages.getString("24h"));
		exhibitionBox.addItem(Messages.getString("Day"));
		exhibitionBox.addItem(Messages.getString("Night"));
		exhibitionBox.addItem(Messages.getString("Fog"));

		alOrientationBox = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String str = orientationBox.getText();
			}
		};
		orientationLabel = new JLabel(Messages.getString("Orientation"), SwingConstants.CENTER);
		orientationLabel.setBounds(new Rectangle(188, 80, 80, 20));
		this.add(orientationLabel, null);
		orientationBox = new JTextField();
		orientationBox.setBounds(new Rectangle(208, 100, 40, 20));
		orientationBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(orientationBox, null);
		orientationBox.addActionListener(alOrientationBox);
	}

	private JRadioButton getTypeButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alType);
		typeButtons.add(button);
		return button;
	}

	public void clearSelections() {

	}

}
