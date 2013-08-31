package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelTop extends JPanel {

	private OSeaMAction dlg;
	public PanelPat panelPat = null;
	private ButtonGroup topButtons = new ButtonGroup();
	public JRadioButton noTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/OffButton.png")));
	public JRadioButton canTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanTopButton.png")));
	public JRadioButton coneTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeTopButton.png")));
	public JRadioButton sphereTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereTopButton.png")));
	public JRadioButton XTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/XTopButton.png")));
	public JRadioButton northTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NorthTopButton.png")));
	public JRadioButton southTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SouthTopButton.png")));
	public JRadioButton eastTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/EastTopButton.png")));
	public JRadioButton westTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WestTopButton.png")));
	public JRadioButton spheres2TopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/Spheres2TopButton.png")));
	public JRadioButton boardDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BoardDayButton.png")));
	public JRadioButton rhombusDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DiamondDayButton.png")));
	public JRadioButton triangleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleDayButton.png")));
	public JRadioButton triangleInvDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleInvDayButton.png")));
	public JRadioButton squareDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SquareDayButton.png")));
	public JRadioButton circleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CircleDayButton.png")));
	private EnumMap<Top, JRadioButton> tops = new EnumMap<Top, JRadioButton>(Top.class);
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Top top : tops.keySet()) {
				JRadioButton button = tops.get(top);
				if (button.isSelected()) {
					dlg.panelMain.mark.setTopmark(top);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};

	public PanelTop(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		panelPat = new PanelPat(dlg, Ent.TOPMARK);
		panelPat.setBounds(new Rectangle(160, 0, 110, 160));
		add(panelPat);
		add(getTopButton(noTopButton, 0, 5, 27, 27, "NoTop", Top.NOTOP));
		add(getTopButton(canTopButton, 30, 5, 27, 27, "CanTop", Top.CYL));
		add(getTopButton(coneTopButton, 60, 5, 27, 27, "ConeTop", Top.CONE));
		add(getTopButton(sphereTopButton, 90, 5, 27, 27, "SphereTop", Top.SPHERE));
		add(getTopButton(XTopButton, 120, 5, 27, 27, "XTop", Top.X_SHAPE));
		add(getTopButton(northTopButton, 0, 35, 27, 27, "NorthTop", Top.NORTH));
		add(getTopButton(southTopButton, 30, 35, 27, 27, "SouthTop", Top.SOUTH));
		add(getTopButton(eastTopButton, 60, 35, 27, 27, "EastTop", Top.EAST));
		add(getTopButton(westTopButton, 90, 35, 27, 27, "WestTop", Top.WEST));
		add(getTopButton(spheres2TopButton, 120, 35, 27, 27, "Spheres2Top", Top.SPHERES2));
		add(getTopButton(boardDayButton, 0, 65, 27, 27, "BoardDay", Top.BOARD));
		add(getTopButton(rhombusDayButton, 30, 65, 27, 27, "DiamondDay", Top.RHOMBUS));
		add(getTopButton(triangleDayButton, 60, 65, 27, 27, "TriangleDay", Top.TRIANGLE));
		add(getTopButton(triangleInvDayButton, 90, 65, 27, 27, "TriangleInvDay", Top.TRIANGLE_INV));
		add(getTopButton(squareDayButton, 120, 65, 27, 27, "SquareDay", Top.SQUARE));
		add(getTopButton(circleDayButton, 120, 95, 27, 27, "CircleDay", Top.CIRCLE));
	}

	public void enableAll(boolean state) {
		for (JRadioButton button : tops.values()) {
			button.setEnabled(state);
		}
	}

	public void syncPanel() {
		for (Top top : tops.keySet()) {
			JRadioButton button = tops.get(top);
			if (dlg.panelMain.mark.getTopmark() == top) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
		panelPat.syncPanel();
	}

	private JRadioButton getTopButton(JRadioButton button, int x, int y, int w, int h, String tip, Top top) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alTop);
		topButtons.add(button);
		tops.put(top, button);
		return button;
	}

}
