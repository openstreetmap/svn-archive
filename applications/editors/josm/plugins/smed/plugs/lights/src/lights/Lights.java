package lights;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JPanel;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.Dimension;


import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class Lights implements SmedPluggable {

	public SmedPluginManager manager = null;

	private int index = -1;
	private String msg = "";

	private JPanel jPanel = null;

	ButtonGroup bgFired = null;
	JRadioButton rbFired1 = null;
	JRadioButton rbFiredN = null;
	JFrame sectors = null;
	JTable table = null;

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Lights";
	}

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		this.manager = manager;
	}

	@Override
	public String getInfo() {
		return "Light data editor";
	}

	@Override
	public JComponent getComponent() {
		manager.showVisualMessage(msg);
		return getJPanel();
	}

	@Override
	public String getFileName() {
		return "Lights.jar";
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public boolean hasFocus() {
		manager.showVisualMessage(msg);
		return true;
	}

	@Override
	public boolean lostFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setSize(new Dimension(400, 300));
			rbFired1 = new JRadioButton("Single", true);
			rbFired1.setBounds(new Rectangle(100, 0, 70, 30));
			rbFiredN = new JRadioButton("Sectored", false);
			rbFiredN.setBounds(new Rectangle(180, 0, 80, 30));
			bgFired = new ButtonGroup();
			bgFired.add(rbFired1);
			bgFired.add(rbFiredN);
			jPanel.add(rbFired1, null);
			jPanel.add(rbFiredN, null);

			ActionListener alFired = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (rbFiredN.isSelected()) {
						if (sectors == null) {
							sectors = new JFrame("Sector Table");
							sectors.setSize(500, 100);
							sectors.setLocation(500, 0);
							table = new JTable(2, 10);
							sectors.add(table, null);
						}
						sectors.setVisible(true);
					}
				}
			};
			rbFired1.addActionListener(alFired);
			rbFiredN.addActionListener(alFired);
		}
		return jPanel;
	}

}
