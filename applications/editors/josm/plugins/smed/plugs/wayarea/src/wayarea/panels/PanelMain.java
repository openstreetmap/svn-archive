package wayarea.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import wayarea.Messages;
import wayarea.dialogs.WayAreaAction;

public class PanelMain extends JPanel {

	private WayAreaAction dlg;
	private JTabbedPane tabs = null;
	public PanelF panelF = null;
	public PanelJ panelJ = null;
	public PanelK panelK = null;
	public PanelL panelL = null;
	public PanelM panelM = null;
	public PanelN panelN = null;
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
//			mark.saveSign(dlg.node);
		}
	};

	public PanelMain(WayAreaAction dia) {

		dlg = dia;
		setLayout(null);
		tabs = new JTabbedPane(JTabbedPane.LEFT);
		
		JPanel panelF = new PanelF();
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelF, "Ports");

		JPanel panelJ = new PanelJ();
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelJ, "Seabed");

		JPanel panelK = new PanelK();
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelK, "Obstructions");

		JPanel panelL = new PanelL();;
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelL, "Offshore Installations");

		JPanel panelM = new PanelM();
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelM, "Tracks & Routes");

		JPanel panelN = new PanelN();;
		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/ports.png")), panelN, "Areas & Limits");
		
		tabs.setBounds(new Rectangle(0, 0, 400, 325));
		add(tabs);

		saveButton = new JButton();
		saveButton.setBounds(new Rectangle(285, 330, 100, 20));
		saveButton.setText(tr("Save"));
		add(saveButton);
		saveButton.addActionListener(alSave);
	}

	public void syncPanel() {
	}

}
