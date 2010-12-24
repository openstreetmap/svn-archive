package oseam.panels;

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

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Cat;

public class PanelLights extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = new ButtonGroup();
	private JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
	private JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
	private JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
	private JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
	private JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
	private EnumMap<Cat, JRadioButton> categories = new EnumMap<Cat, JRadioButton>(Cat.class);
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Iterator<Cat> it = categories.keySet().iterator();
			while (it.hasNext()) {
				Cat cat = it.next();
				JRadioButton button = categories.get(cat);
				if (button.isSelected()) {
					dlg.mark.setCategory(cat);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getCatButton(houseButton, 0, 0, 34, 32, "LighthouseTip", Cat.LIGHT_HOUSE), null);
		this.add(getCatButton(majorButton, 0, 32, 34, 32, "MajorLightTip", Cat.LIGHT_MAJOR), null);
		this.add(getCatButton(minorButton, 0, 64, 34, 32, "MinorLightTip", Cat.LIGHT_MINOR), null);
		this.add(getCatButton(vesselButton, 0, 96, 34, 32, "LightVesselTip", Cat.LIGHT_VESSEL), null);
		this.add(getCatButton(floatButton, 0, 128, 34, 32, "LightFloatTip", Cat.LIGHT_FLOAT), null);
	}

	public void clearSelections() {

	}

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip, Cat cat) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		categories.put(cat, button);
		catButtons.add(button);
		return button;
	}

}
