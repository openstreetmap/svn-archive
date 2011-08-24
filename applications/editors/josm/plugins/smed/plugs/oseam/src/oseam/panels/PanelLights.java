package oseam.panels;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelLights extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
	public JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
	public JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
	public JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
	public JRadioButton trafficButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TrafficButton.png")));
	public JRadioButton warningButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WarningButton.png")));
	private EnumMap<Cat, JRadioButton> categories = new EnumMap<Cat, JRadioButton>(Cat.class);
	private EnumMap<Cat, Obj> objects = new EnumMap<Cat, Obj>(Cat.class);
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : categories.keySet()) {
				JRadioButton button = categories.get(cat);
				if (button.isSelected()) {
					dlg.mark.setCategory(cat);
					dlg.mark.setObject(objects.get(cat));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getCatButton(houseButton, 0, 0, 34, 32, "Lighthouse", Cat.LIGHT_HOUSE, Obj.LNDMRK), null);
		this.add(getCatButton(majorButton, 35, 0, 34, 32, "MajorLight", Cat.LIGHT_MAJOR, Obj.LITMAJ), null);
		this.add(getCatButton(minorButton, 70, 0, 34, 32, "MinorLight", Cat.LIGHT_MINOR, Obj.LITMIN), null);
		this.add(getCatButton(vesselButton, 105, 0, 34, 32, "LightVessel", Cat.LIGHT_VESSEL, Obj.LITVES), null);
		this.add(getCatButton(floatButton, 140, 0, 34, 32, "LightFloat", Cat.LIGHT_FLOAT, Obj.LITFLT), null);
		this.add(getCatButton(trafficButton, 35, 32, 34, 32, "SSTraffic", Cat.SIGNAL_STATION, Obj.SIGSTA), null);
		this.add(getCatButton(warningButton, 105, 32, 34, 32, "SSWarning", Cat.SIGNAL_STATION, Obj.SIGSTA), null);
	}

	public void clearSelections() {

	}

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip, Cat cat, Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		catButtons.add(button);
		categories.put(cat, button);
		objects.put(cat, obj);
		return button;
	}

}
