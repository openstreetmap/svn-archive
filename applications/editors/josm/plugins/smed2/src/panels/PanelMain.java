package panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PanelMain extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabs = null;
//	public PanelF panelF = null;
	public static JTextField messageBar = null;
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
//			item.saveSign(???);
		}
	};

	public PanelMain() {

		setLayout(null);
		setSize(new Dimension(480, 480));
		tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setBounds(new Rectangle(0, 0, 480, 420));

//		JPanel panelF = new PanelF();
//		tabs.addTab(null, new ImageIcon(getClass().getResource("/images/tabF.png")), panelF, Messages.getString("Ports"));

		add(tabs);
		
    messageBar = new JTextField();
    messageBar.setBounds(new Rectangle(10, 430, 350, 20));
    messageBar.setEditable(false);
    messageBar.setBackground(Color.WHITE);
    add(messageBar);
		saveButton = new JButton();
		saveButton.setBounds(new Rectangle(370, 430, 100, 20));
		saveButton.setText(tr("Save"));
		add(saveButton);
		saveButton.addActionListener(alSave);

	}
}
