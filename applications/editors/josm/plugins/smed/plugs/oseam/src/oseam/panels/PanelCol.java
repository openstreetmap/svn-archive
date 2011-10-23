package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelCol extends JPanel {

	private OSeaMAction dlg;
	private Ent ent;
	private ButtonGroup colourButtons = new ButtonGroup();
	public JRadioButton delButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DelButton.png")));
	public JRadioButton addButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/AddButton.png")));
	public JRadioButton whiteButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WhiteButton.png")));
	public JRadioButton redButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RedButton.png")));
	public JRadioButton orangeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OrangeButton.png")));
	public JRadioButton amberButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/AmberButton.png")));
	public JRadioButton yellowButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/YellowButton.png")));
	public JRadioButton greenButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/GreenButton.png")));
	public JRadioButton blueButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BlueButton.png")));
	public JRadioButton violetButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/VioletButton.png")));
	public JRadioButton blackButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BlackButton.png")));
	public JRadioButton greyButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/GreyButton.png")));
	public JRadioButton brownButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BrownButton.png")));
	public JRadioButton magentaButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/MagentaButton.png")));
	public JRadioButton pinkButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PinkButton.png")));
	public EnumMap<Col, JRadioButton> colours = new EnumMap<Col, JRadioButton>(Col.class);
	private ActionListener alColour = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Col col : colours.keySet()) {
				JRadioButton button = colours.get(col);
				if (button.isSelected()) {
					if (dlg.mark != null) {
						if (ent == Ent.LIGHT) {
							dlg.mark.setColour(ent, col);
						} else {
							if (button == delButton) {
								if (stackCol.size() > 1) {
									JRadioButton btnI = stackCol.get(stackIdx);
									dlg.mark.subColour(ent, stackIdx);
									btnI.removeActionListener(alStack);
									stackColours.remove(btnI);
									stack.remove(btnI);
									stackCol.remove(stackIdx);
									if (stackCol.size() == stackIdx)
										stackIdx--;
								} else {
									dlg.mark.setColour(ent, Col.UNKNOWN);
								}
							} else if (button == addButton) {
								if (stackCol.size() != 0)
									stackIdx++;
								dlg.mark.addColour(ent, stackIdx, col);
								stackCol.add(stackIdx, new JRadioButton(new ImageIcon(getClass().getResource("/images/ColourButton.png"))));
								JRadioButton btnI = stackCol.get(stackIdx);
								btnI.setBorder(BorderFactory.createLoweredBevelBorder());
								stack.add(btnI);
								stackColours.add(btnI);
								btnI.addActionListener(alStack);
							} else {
								dlg.mark.setColour(ent, stackIdx, col);
							}
							int height = 60 / stackCol.size();
							for (int i = 0; stackCol.size() > i; i++) {
								JRadioButton btnI = stackCol.get(i);
								btnI.setBounds(2, (2 + (i * height)), 30, height);
								btnI.setBackground(dlg.mark.ColMAP.get(dlg.mark.getColour(ent, i)));
								if (stackIdx == i) {
									btnI.setBorderPainted(true);
								} else {
									btnI.setBorderPainted(false);
								}
							}
						}
					}
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};
	private JPanel stack;
	private ButtonGroup stackColours = new ButtonGroup();
	private ArrayList<JRadioButton> stackCol = new ArrayList<JRadioButton>();
	private int stackIdx = 0;
	private ActionListener alStack = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (int i = 0; stackCol.size() > i; i++) {
				JRadioButton button = stackCol.get(i);
				if (button.isSelected()) {
					stackIdx = i;
					button.setBorderPainted(true);
				} else {
					button.setBorderPainted(false);
				}
			}
		}
	};

	public PanelCol(OSeaMAction dia, Ent entity) {
		dlg = dia;
		ent = entity;
		this.setLayout(null);
		this.add(getColButton(delButton, 0, 0, 34, 16, Messages.getString("RemColour"), Col.UNKNOWN), null);
		this.add(getColButton(whiteButton, 0, 16, 34, 16, Messages.getString("White"), Col.WHITE), null);
		this.add(getColButton(redButton, 0, 32, 34, 16, Messages.getString("Red"), Col.RED), null);
		this.add(getColButton(orangeButton, 0, 48, 34, 16, Messages.getString("Orange"), Col.ORANGE), null);
		this.add(getColButton(amberButton, 0, 64, 34, 16, Messages.getString("Amber"), Col.AMBER), null);
		this.add(getColButton(yellowButton, 0, 80, 34, 16, Messages.getString("Yellow"), Col.YELLOW), null);
		this.add(getColButton(greenButton, 0, 96, 34, 16, Messages.getString("Green"), Col.GREEN), null);
		this.add(getColButton(blueButton, 0, 112, 34, 16, Messages.getString("Blue"), Col.BLUE), null);
		this.add(getColButton(violetButton, 0, 128, 34, 16, Messages.getString("Violet"), Col.VIOLET), null);
		if (ent != Ent.LIGHT) {
			this.add(getColButton(addButton, 0, 144, 34, 16, Messages.getString("AddColour"), Col.BLANK), null);
			this.add(getColButton(blackButton, 37, 0, 34, 16, Messages.getString("Black"), Col.BLACK), null);
			this.add(getColButton(greyButton, 37, 16, 34, 16, Messages.getString("Grey"), Col.GREY), null);
			this.add(getColButton(brownButton, 37, 32, 34, 16, Messages.getString("Brown"), Col.BROWN), null);
			this.add(getColButton(magentaButton, 37, 48, 34, 16, Messages.getString("Magenta"), Col.MAGENTA), null);
			this.add(getColButton(pinkButton, 37, 64, 34, 16, Messages.getString("Pink"), Col.PINK), null);

			stack = new JPanel();
			stack.setBorder(BorderFactory.createLineBorder(Color.black, 2));
			stack.setBounds(38, 87, 34, 64);
			stack.setLayout(null);
			this.add(stack);
		}
	}

	public void trimStack(int max) {
		while (stackCol.size() > max) {
			stackCol.get(stackCol.size() - 1).setSelected(true);
			delButton.doClick();
		}
	}

	public void clearSelections() {
		colourButtons.clearSelection();
		if (stackCol.size() == 0) {
			addButton.doClick();
		}
		do {
			delButton.doClick();
		} while (stackCol.size() > 1);
		alStack.actionPerformed(null);
	}

	public void enableAll(boolean state) {
		for (JRadioButton button : colours.values()) {
			button.setEnabled(state);
		}
	}

	private JRadioButton getColButton(JRadioButton button, int x, int y, int w, int h, String tip, Col col) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(tr(tip));
		button.addActionListener(alColour);
		colourButtons.add(button);
		colours.put(col, button);
		return button;
	}

}
