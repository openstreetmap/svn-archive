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
	public JLabel col1Label = new JLabel();
	public JLabel col2Label = new JLabel();
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
			String str = charBox.getText();
			dlg.panelMain.mark.setLightAtt(Att.CHR, 0, str);
			if (!str.contains("Al")) {
				col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.COL, 0)));
				dlg.panelMain.mark.setLightAtt(Att.ALT, 0, Col.UNKNOWN);
			} else {
				col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.ALT, 0)));
			}
		}
	};
	private ActionListener alCharBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			String str = charBox.getText();
			dlg.panelMain.mark.setLightAtt(Att.CHR, 0, str);
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
			if (!str.contains("Al")) {
				col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.COL, 0)));
				dlg.panelMain.mark.setLightAtt(Att.ALT, 0, Col.UNKNOWN);
			} else {
				col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.ALT, 0)));
			}
		}
	};

	public PanelChr(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getChrButton(noneButton, 0, 0, 44, 16, Messages.getString("NoChar"), Chr.UNKNOWN));
		add(getChrButton(fixedButton, 0, 16, 44, 16, Messages.getString("FChar"), Chr.FIXED));
		add(getChrButton(flashButton, 0, 32, 44, 16, Messages.getString("FlChar"), Chr.FLASH));
		add(getChrButton(longFlashButton, 0, 48, 44, 16, Messages.getString("LFlChar"), Chr.LFLASH));
		add(getChrButton(quickButton, 0, 64, 44, 16, Messages.getString("QChar"), Chr.QUICK));
		add(getChrButton(veryQuickButton, 0, 80, 44, 16, Messages.getString("VQChar"), Chr.VQUICK));
		add(getChrButton(ultraQuickButton, 0, 96, 44, 16, Messages.getString("UQChar"), Chr.UQUICK));
		add(getChrButton(alternatingButton, 44, 0, 44, 16, Messages.getString("AlChar"), Chr.ALTERNATING));
		add(getChrButton(isophasedButton, 44, 16, 44, 16, Messages.getString("IsoChar"), Chr.ISOPHASED));
		add(getChrButton(occultingButton, 44, 32, 44, 16, Messages.getString("OcChar"), Chr.OCCULTING));
		add(getChrButton(morseButton, 44, 48, 44, 16, Messages.getString("MoChar"), Chr.MORSE));
		add(getChrButton(interruptedQuickButton, 44, 64, 44, 16, Messages.getString("IQChar"), Chr.IQUICK));
		add(getChrButton(interruptedVeryQuickButton, 44, 80, 44, 16, Messages.getString("IVQChar"), Chr.IVQUICK));
		add(getChrButton(interruptedUltraQuickButton, 44, 96, 44, 16, Messages.getString("IUQChar"), Chr.IUQUICK));
		charLabel.setBounds(new Rectangle(0, 113, 88, 20));
		charLabel.setHorizontalAlignment(SwingConstants.CENTER);
		charLabel.setText(Messages.getString("Character"));
		add(charLabel);
		col1Label.setBounds(new Rectangle(10, 135, 10, 20));
		col1Label.setOpaque(true);
		add(col1Label);
		col2Label.setBounds(new Rectangle(70, 135, 10, 20));
		col2Label.setOpaque(true);
		add(col2Label);
		charBox.setBounds(new Rectangle(20, 135, 50, 20));
		charBox.setHorizontalAlignment(SwingConstants.CENTER);
		add(charBox);
		charBox.addActionListener(alCharBox);
	}

	public void syncPanel() {
		String str = (String)dlg.panelMain.mark.getLightAtt(Att.CHR, 0);
		charBox.setText(str);
		EnumSet<Chr> set = EnumSet.noneOf(Chr.class);
		for (EnumSet<Chr> map : SeaMark.ChrMAP.keySet()) {
			if (dlg.node != null && str.equals(SeaMark.ChrMAP.get(map))) {
				set = map;
				break;
			}
		}
		if (!str.contains("Al")) {
			col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.COL, 0)));
		} else {
			col2Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.ALT, 0)));
		}
		col1Label.setBackground(SeaMark.ColMAP.get(dlg.panelMain.mark.getLightAtt(Att.COL, 0)));
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
