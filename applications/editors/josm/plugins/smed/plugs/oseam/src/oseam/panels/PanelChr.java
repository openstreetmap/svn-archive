package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.*;

public class PanelChr extends JPanel {

	private OSeaMAction dlg;
	public JLabel charLabel = new JLabel();
	public JTextField charBox = new JTextField();
	public JToggleButton noneButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/NoCharButton.png")));
	public JToggleButton fixedButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/FixedButton.png")));
	public JToggleButton flashButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/FlashButton.png")));
	public JToggleButton longFlashButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/LongFlashButton.png")));
	public JToggleButton quickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/QuickButton.png")));
	public JToggleButton veryQuickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/VeryQuickButton.png")));
	public JToggleButton ultraQuickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/UltraQuickButton.png")));
	public JToggleButton interruptedQuickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/InterruptedQuickButton.png")));
	public JToggleButton interruptedVeryQuickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/InterruptedVeryQuickButton.png")));
	public JToggleButton interruptedUltraQuickButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/InterruptedUltraQuickButton.png")));
	public JToggleButton isophasedButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/IsophasedButton.png")));
	public JToggleButton occultingButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/OccultingButton.png")));
	public JToggleButton morseButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/MorseButton.png")));
	public JToggleButton alternatingButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/AlternatingButton.png")));
	private EnumMap<Chr, JToggleButton> buttons = new EnumMap<Chr, JToggleButton>(Chr.class);
	private ActionListener alCharButton = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (noneButton.isSelected()) {
				if (dlg.panelMain.panelLit.panelCol.delButton.isSelected()) {
					dlg.panelMain.lightIcon.setIcon(null);
				}
			} else {
				dlg.panelMain.lightIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Green_120.png")));
			}
			JToggleButton source = (JToggleButton) e.getSource();
			EnumSet<Chr> combo = EnumSet.noneOf(Chr.class);
			for (Chr chr : buttons.keySet()) {
				JToggleButton button = buttons.get(chr);
				if (button.isSelected()) {
					combo.add(chr);
					button.setBorderPainted(true);
				} else {
					combo.remove(chr);
					button.setBorderPainted(false);
				}
			}
			if (SeaMark.ChrMAP.containsKey(combo)) {
				charBox.setText(SeaMark.ChrMAP.get(combo));
			} else {
				for (Chr chr : buttons.keySet()) {
					JToggleButton button = buttons.get(chr);
					if (button == source) {
						charBox.setText(SeaMark.ChrMAP.get(EnumSet.of(chr)));
						button.setSelected(true);
						button.setBorderPainted(true);
					} else {
						button.setSelected(false);
						button.setBorderPainted(false);
					}
				}
			}
		}
	};
	private ActionListener alCharBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = charBox.getText();
			EnumSet<Chr> set = EnumSet.noneOf(Chr.class);
			for (EnumSet<Chr> map : SeaMark.ChrMAP.keySet()) {
				if (str.equals(SeaMark.ChrMAP.get(map))) {
					set = map;
					break;
				}
			}
			for (Chr chr : buttons.keySet()) {
				JToggleButton button = buttons.get(chr);
				if (set.contains(chr)) {
					button.setSelected(true);
					button.setBorderPainted(true);
				} else {
					button.setSelected(false);
					button.setBorderPainted(false);
				}
			}
		}
	};

	public PanelChr(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getChrButton(noneButton, 0, 0, 44, 16, Messages.getString("NoChar"), Chr.UNKNOWN), null);
		this.add(getChrButton(fixedButton, 0, 16, 44, 16, Messages.getString("FChar"), Chr.FIXED), null);
		this.add(getChrButton(flashButton, 0, 32, 44, 16, Messages.getString("FlChar"), Chr.FLASH), null);
		this.add(getChrButton(longFlashButton, 0, 48, 44, 16, Messages.getString("LFlChar"), Chr.LONGFLASH), null);
		this.add(getChrButton(quickButton, 0, 64, 44, 16, Messages.getString("QChar"), Chr.QUICK), null);
		this.add(getChrButton(veryQuickButton, 0, 80, 44, 16, Messages.getString("VQChar"), Chr.VERYQUICK), null);
		this.add(getChrButton(ultraQuickButton, 0, 96, 44, 16, Messages.getString("UQChar"), Chr.ULTRAQUICK), null);
		this.add(getChrButton(alternatingButton, 44, 0, 44, 16, Messages.getString("AlChar"), Chr.ALTERNATING), null);
		this.add(getChrButton(isophasedButton, 44, 16, 44, 16, Messages.getString("IsoChar"), Chr.ISOPHASED), null);
		this.add(getChrButton(occultingButton, 44, 32, 44, 16, Messages.getString("OcChar"), Chr.OCCULTING), null);
		this.add(getChrButton(morseButton, 44, 48, 44, 16, Messages.getString("MoChar"), Chr.MORSE), null);
		this.add(getChrButton(interruptedQuickButton, 44, 64, 44, 16, Messages.getString("IQChar"), Chr.INTERRUPTEDQUICK), null);
		this.add(getChrButton(interruptedVeryQuickButton, 44, 80, 44, 16, Messages.getString("IVQChar"), Chr.INTERRUPTEDVERYQUICK),
				null);
		this.add(getChrButton(interruptedUltraQuickButton, 44, 96, 44, 16, Messages.getString("IUQChar"), Chr.INTERRUPTEDULTRAQUICK),
				null);
		charLabel.setBounds(new Rectangle(0, 113, 88, 20));
		charLabel.setHorizontalAlignment(SwingConstants.CENTER);
		charLabel.setText(Messages.getString("Character"));
		this.add(charLabel, null);
		charBox.setBounds(new Rectangle(20, 135, 50, 20));
		charBox.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(charBox, null);
		charBox.addActionListener(alCharBox);
	}

	public void enableAll(boolean state) {
		for (JToggleButton button : buttons.values()) {
			button.setEnabled(state);
		}
	}

	private JToggleButton getChrButton(JToggleButton button, int x, int y, int w, int h, String tip, Chr chr) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setBorderPainted(false);
		button.setToolTipText(tr(tip));
		button.addActionListener(alCharButton);
		buttons.put(chr, button);
		return button;
	}

}
