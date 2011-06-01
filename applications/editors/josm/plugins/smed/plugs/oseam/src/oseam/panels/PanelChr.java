package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import java.util.EnumMap;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Chr;
import oseam.seamarks.SeaMark.Ent;

public class PanelChr	 extends JPanel {

	private OSeaMAction dlg;
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
	private EnumMap<Chr, JToggleButton> characters = new EnumMap<Chr, JToggleButton>(Chr.class);
	private ActionListener alCharacter = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Chr chr : characters.keySet()) {
				JToggleButton button = characters.get(chr);
				if (button.isSelected()) {
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};

	public PanelChr(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getChrButton(noneButton, 0, 0, 34, 16, "No character", Chr.UNKNOWN), null);
		this.add(getChrButton(fixedButton, 0, 16, 34, 16, "F. Fixed", Chr.FIXED), null);
		this.add(getChrButton(flashButton, 0, 32, 34, 16, "Fl. Flashing", Chr.FLASH), null);
		this.add(getChrButton(longFlashButton, 0, 48, 34, 16, "LFl. Long flash", Chr.LONGFLASH), null);
		this.add(getChrButton(quickButton, 0, 64, 34, 16, "Q. Quick flashing", Chr.QUICK), null);
		this.add(getChrButton(veryQuickButton, 0, 80, 34, 16, "VQ. Very quick flashing", Chr.VERYQUICK), null);
		this.add(getChrButton(ultraQuickButton, 0, 96, 34, 16, "UQ. Ultra quick flashing", Chr.ULTRAQUICK), null);
		this.add(getChrButton(alternatingButton, 34, 0, 34, 16, "Al. Alternating", Chr.ALTERNATING), null);
		this.add(getChrButton(isophasedButton, 34, 16, 34, 16, "Iso. Isophased flashing", Chr.ISOPHASED), null);
		this.add(getChrButton(occultingButton, 34, 32, 34, 16, "Oc. Occulting flash", Chr.OCCULTING), null);
		this.add(getChrButton(morseButton, 34, 48, 34, 16, "Mo. Morse", Chr.MORSE), null);
		this.add(getChrButton(interruptedQuickButton, 34, 64, 34, 16, "IQ. Interrupted quick flashing", Chr.INTERRUPTEDQUICK), null);
		this.add(getChrButton(interruptedVeryQuickButton, 34, 80, 34, 16, "IVQ. Interrupted very quick flashing", Chr.INTERRUPTEDVERYQUICK), null);
		this.add(getChrButton(interruptedUltraQuickButton, 34, 96, 34, 16, "IUQ. Interrupted ultra quick flashing", Chr.INTERRUPTEDULTRAQUICK), null);
	}

	public void clearSelections() {
		for (Chr chr : characters.keySet()) {
			JToggleButton button = characters.get(chr);
			button.setSelected(false);
		}
		noneButton.doClick();
	}

	public void enableAll(boolean state) {
		for (JToggleButton button : characters.values()) {
			button.setEnabled(state);
		}
	}

	private JToggleButton getChrButton(JToggleButton button, int x, int y, int w, int h, String tip, Chr chr) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(tr(tip));
		button.addActionListener(alCharacter);
		characters.put(chr, button);
		return button;
	}

}
