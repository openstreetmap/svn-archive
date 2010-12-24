package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;


import java.util.EnumMap;
import java.util.Iterator;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Col;

public class PanelCol extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup colourButtons = null;
	public JRadioButton offButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton whiteButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WhiteButton.png")));
	public JRadioButton redButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RedButton.png")));
	public JRadioButton orangeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OrangeButton.png")));
	public JRadioButton amberButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/AmberButton.png")));
	public JRadioButton yellowButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/YellowButton.png")));
	public JRadioButton greenButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/GreenButton.png")));
	public JRadioButton blueButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BlueButton.png")));
	public JRadioButton violetButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/VioletButton.png")));
	public JRadioButton blackButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BlackButton.png")));
	private ActionListener alColour = null;
	private EnumMap<Col, JRadioButton> colours = new EnumMap<Col, JRadioButton>(Col.class);

	public PanelCol(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getButton(offButton, 0, 0, 34, 16, "No colour", Col.UNKNOWN), null);
		this.add(getButton(whiteButton, 0, 16, 34, 16, "White", Col.WHITE), null);
		this.add(getButton(redButton, 0, 32, 34, 16, "Red", Col.RED), null);
		this.add(getButton(orangeButton, 0, 48, 34, 16, "Orange", Col.ORANGE), null);
		this.add(getButton(amberButton, 0, 64, 34, 16, "Amber", Col.AMBER), null);
		this.add(getButton(yellowButton, 0, 80, 34, 16, "Yellow", Col.YELLOW), null);
		this.add(getButton(greenButton, 0, 96, 34, 16, "Green", Col.GREEN), null);
		this.add(getButton(blueButton, 0, 112, 34, 16, "Blue", Col.BLUE), null);
		this.add(getButton(violetButton, 0, 128, 34, 16, "Violet", Col.VIOLET), null);
		this.add(getButton(blackButton, 0, 144, 34, 16, "Black", Col.BLACK), null);
		colourButtons = new ButtonGroup();
		colourButtons.add(offButton);
		colourButtons.add(whiteButton);
		colourButtons.add(redButton);
		colourButtons.add(orangeButton);
		colourButtons.add(amberButton);
		colourButtons.add(yellowButton);
		colourButtons.add(greenButton);
		colourButtons.add(blueButton);
		colourButtons.add(violetButton);
		colourButtons.add(blackButton);
		alColour = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Iterator<Col> it = colours.keySet().iterator();
				while (it.hasNext()) {
					Col col = it.next();
					JRadioButton button = colours.get(col);
					if (button.isSelected()) {
						switch (col) {
						case UNKNOWN: {
							break;
						}
						}
						button.setBorderPainted(true);
					} else button.setBorderPainted(false);
				}
/*				offButton.setBorderPainted(offButton.isSelected());
				whiteButton.setBorderPainted(whiteButton.isSelected());
				redButton.setBorderPainted(redButton.isSelected());
				orangeButton.setBorderPainted(orangeButton.isSelected());
				amberButton.setBorderPainted(amberButton.isSelected());
				yellowButton.setBorderPainted(yellowButton.isSelected());
				greenButton.setBorderPainted(greenButton.isSelected());
				blueButton.setBorderPainted(blueButton.isSelected());
				violetButton.setBorderPainted(violetButton.isSelected());
				blackButton.setBorderPainted(blackButton.isSelected());
*/			}
		};
		offButton.addActionListener(alColour);
		whiteButton.addActionListener(alColour);
		redButton.addActionListener(alColour);
		orangeButton.addActionListener(alColour);
		amberButton.addActionListener(alColour);
		yellowButton.addActionListener(alColour);
		greenButton.addActionListener(alColour);
		blueButton.addActionListener(alColour);
		violetButton.addActionListener(alColour);
		blackButton.addActionListener(alColour);
	}

	public void clearSelections() {
		colourButtons.clearSelection();
		alColour.actionPerformed(null);
	}

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip, Col col) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(tr(tip));
		colours.put(col, button);
		return button;
	}

}
