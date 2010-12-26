package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Shp;

import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Iterator;

public class PanelSaw extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup shapeButtons = new ButtonGroup();
	private JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	private JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	private JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	private JRadioButton barrelButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BarrelButton.png")));
	private JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	private EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	private ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Iterator<Shp> it = shapes.keySet().iterator();
			while (it.hasNext()) {
				Shp shp = it.next();
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelSaw(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getShapeButton(pillarButton, 0, 0, 34, 32, "PillarTip", Shp.PILLAR), null);
		this.add(getShapeButton(sparButton, 0, 32, 34, 32, "SparTip", Shp.SPAR), null);
		this.add(getShapeButton(sphereButton, 0, 64, 34, 32, "SphereTip", Shp.SPHERE), null);
		this.add(getShapeButton(barrelButton, 0, 96, 34, 32, "BarrelTip", Shp.BARREL), null);
		this.add(getShapeButton(floatButton, 0, 128, 34, 32, "FloatTip", Shp.FLOAT), null);
	}

	public void clearSelections() {
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		return button;
	}

}
