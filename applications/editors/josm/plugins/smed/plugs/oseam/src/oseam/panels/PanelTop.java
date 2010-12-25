package oseam.panels;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Iterator;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Top;
import oseam.seamarks.SeaMark.Obj;

public class PanelTop extends JPanel {

	private OSeaMAction dlg;
	public PanelCol panelCol = null;
	private ButtonGroup topButtons = new ButtonGroup();
	public JRadioButton noTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NoTopButton.png")));
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
	public JRadioButton diamondDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DiamondDayButton.png")));
	public JRadioButton triangleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleDayButton.png")));
	public JRadioButton triangleInvDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleInvDayButton.png")));
	public JRadioButton squareDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SquareDayButton.png")));
	public JRadioButton mooringTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/MooringTopButton.png")));
	private EnumMap<Top, JRadioButton> tops = new EnumMap<Top, JRadioButton>(Top.class);
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Iterator<Top> it = tops.keySet().iterator();
			while (it.hasNext()) {
				Top top = it.next();
				JRadioButton button = tops.get(top);
				if (button.isSelected()) {
					dlg.mark.setTopMark(top);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
		}
	};

	public PanelTop(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg, Obj.TOPMARK);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		this.setLayout(null);
		this.add(panelCol, null);
		this.add(getTopButton(noTopButton, 40, 5, 27, 27, "NoTopTip", Top.NONE), null);
		this.add(getTopButton(canTopButton, 70, 5, 27, 27, "CanTopTip", Top.CAN), null);
		this.add(getTopButton(coneTopButton, 100, 5, 27, 27, "ConeTopTip", Top.CONE), null);
		this.add(getTopButton(sphereTopButton, 130, 5, 27, 27, "SphereTopTip", Top.SPHERE), null);
		this.add(getTopButton(XTopButton, 160, 5, 27, 27, "XTopTip", Top.X_SHAPE), null);
		this.add(getTopButton(northTopButton, 40, 35, 27, 27, "NorthTopTip", Top.NORTH), null);
		this.add(getTopButton(southTopButton, 70, 35, 27, 27, "SouthTopTip", Top.SOUTH), null);
		this.add(getTopButton(eastTopButton, 100, 35, 27, 27, "EastTopTip", Top.EAST), null);
		this.add(getTopButton(westTopButton, 130, 35, 27, 27, "WestTopTip", Top.WEST), null);
		this.add(getTopButton(spheres2TopButton, 160, 35, 27, 27, "Spheres2TopTip", Top.SPHERES2), null);
		this.add(getTopButton(boardDayButton, 40, 65, 27, 27, "BoardDayTip", Top.BOARD), null);
		this.add(getTopButton(diamondDayButton, 70, 65, 27, 27, "DiamondDayTip", Top.DIAMOND), null);
		this.add(getTopButton(triangleDayButton, 100, 65, 27, 27, "TriangleDayTip", Top.TRIANGLE), null);
		this.add(getTopButton(triangleInvDayButton, 130, 65, 27, 27, "TriangleInvDayTip", Top.TRIANGLE_INV), null);
		this.add(getTopButton(squareDayButton, 160, 65, 27, 27, "SquareDayTip", Top.SQUARE), null);
		this.add(getTopButton(mooringTopButton, 40, 95, 27, 27, "MooringTopTip", Top.MOORING), null);
	}

	public void clearSelections() {
		topButtons.clearSelection();
		alTop.actionPerformed(null);
		panelCol.clearSelections();
	}

	private JRadioButton getTopButton(JRadioButton button, int x, int y, int w, int h, String tip, Top top) {
			button.setBounds(new Rectangle(x, y, w, h));
			button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			button.setToolTipText(Messages.getString(tip));
			button.addActionListener(alTop);
			topButtons.add(button);
			tops.put(top, button);
		return button;
	}

}
