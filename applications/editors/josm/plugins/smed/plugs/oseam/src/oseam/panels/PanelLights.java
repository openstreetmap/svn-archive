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
	private ButtonGroup objButtons = new ButtonGroup();
	public JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
	public JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
	public JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
	public JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
	public JRadioButton trafficButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TrafficButton.png")));
	public JRadioButton warningButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WarningButton.png")));
	private EnumMap<Obj, JRadioButton> objects = new EnumMap<Obj, JRadioButton>(Obj.class);
	private ActionListener alObj = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Obj obj : objects.keySet()) {
				JRadioButton button = objects.get(obj);
				if (button.isSelected()) {
					dlg.mark.setObject(obj);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getObjButton(houseButton, 0, 0, 34, 32, "Lighthouse", Obj.LNDMRK), null);
		this.add(getObjButton(majorButton, 35, 0, 34, 32, "MajorLight", Obj.LITMAJ), null);
		this.add(getObjButton(minorButton, 70, 0, 34, 32, "MinorLight", Obj.LITMIN), null);
		this.add(getObjButton(vesselButton, 105, 0, 34, 32, "LightVessel", Obj.LITVES), null);
		this.add(getObjButton(floatButton, 140, 0, 34, 32, "LightFloat", Obj.LITFLT), null);
		this.add(getObjButton(trafficButton, 50, 35, 34, 32, "SSTraffic", Obj.SISTAT), null);
		this.add(getObjButton(warningButton, 90, 35, 34, 32, "SSWarning", Obj.SISTAW), null);
	}

	public void clearSelections() {

	}

	private JRadioButton getObjButton(JRadioButton button, int x, int y, int w, int h, String tip,Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alObj);
		objButtons.add(button);
		objects.put(obj, button);
		return button;
	}

}
