package panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.openstreetmap.josm.Main;

import smed2.Smed2Action;

public class PanelMain extends JPanel {

	public static JTextField messageBar = null;
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		}
	};
	private JButton importButton = null;
	final JFileChooser ifc = new JFileChooser();
	private ActionListener alImport = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == importButton) {
        messageBar.setText("Select file");
        int returnVal = ifc.showOpenDialog(Main.parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          Smed2Action.panelS57.startImport(ifc.getSelectedFile());
         } else {
           messageBar.setText("");
         }
      }
		}
	};

	private JButton exportButton = null;
	final JFileChooser efc = new JFileChooser();
	private ActionListener alExport = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == exportButton) {
        messageBar.setText("Select file");
        int returnVal = efc.showOpenDialog(Main.parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          Smed2Action.panelS57.startExport(efc.getSelectedFile());
         } else {
           messageBar.setText("");
         }
      }
		}
	};

	public ButtonGroup sectionButtons = null;
	public JRadioButton eButton = new JRadioButton();
	public JRadioButton fButton = new JRadioButton();
	public JRadioButton jButton = new JRadioButton();
	public JRadioButton kButton = new JRadioButton();
	public JRadioButton lButton = new JRadioButton();
	public JRadioButton mButton = new JRadioButton();
	public JRadioButton nButton = new JRadioButton();
	public JRadioButton pButton = new JRadioButton();
	public JRadioButton qButton = new JRadioButton();
	public JRadioButton rButton = new JRadioButton();
	public JRadioButton sButton = new JRadioButton();
	public JRadioButton tButton = new JRadioButton();
	public JRadioButton uButton = new JRadioButton();
	private ActionListener alSection = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (eButton.isSelected()) {
				eButton.setBorderPainted(true);
			} else {
				eButton.setBorderPainted(false);
			}
			if (fButton.isSelected()) {
				fButton.setBorderPainted(true);
			} else {
				fButton.setBorderPainted(false);
			}
			if (jButton.isSelected()) {
				jButton.setBorderPainted(true);
			} else {
				jButton.setBorderPainted(false);
			}
			if (kButton.isSelected()) {
				kButton.setBorderPainted(true);
			} else {
				kButton.setBorderPainted(false);
			}
		}
	};

	public PanelMain() {

		setLayout(null);
		setSize(new Dimension(480, 480));
		
    messageBar = new JTextField();
    messageBar.setBounds(70, 430, 290, 20);
    messageBar.setEditable(false);
    messageBar.setBackground(Color.WHITE);
    add(messageBar);
		importButton = new JButton(new ImageIcon(getClass().getResource("/images/importButton.png")));
		importButton.setBounds(10, 430, 20, 20);
		add(importButton);
		importButton.addActionListener(alImport);
		exportButton = new JButton(new ImageIcon(getClass().getResource("/images/exportButton.png")));
		exportButton.setBounds(40, 430, 20, 20);
		add(exportButton);
		exportButton.addActionListener(alExport);
		saveButton = new JButton();
		saveButton.setBounds(370, 430, 100, 20);
		saveButton.setText(tr("Save"));
		add(saveButton);
		saveButton.addActionListener(alSave);

		add(getButton(eButton, 0, 0, 200, 20, "E Landmarks"), null);
		add(getButton(fButton, 0, 20, 200, 20, "F Ports"), null);
		add(getButton(jButton, 0, 40, 200, 20, "J Seabed"), null);
		add(getButton(kButton, 0, 60, 200, 20, "K Obstructions"), null);
		add(getButton(lButton, 0, 80, 200, 20, "L Obstructions"), null);
		add(getButton(mButton, 0, 100, 200, 20, "M Offshore Installations"), null);
		add(getButton(nButton, 0, 120, 200, 20, "N Areas & Limits"), null);
		add(getButton(pButton, 0, 140, 200, 20, "P Lights"), null);
		add(getButton(qButton, 0, 160, 200, 20, "Q Buoys & Beacons"), null);
		add(getButton(rButton, 0, 180, 200, 20, "R Fog Sigals"), null);
		add(getButton(sButton, 0, 200, 200, 20, "S Radio & Radar"), null);
		add(getButton(tButton, 0, 220, 200, 20, "T Services"), null);
		add(getButton(uButton, 0, 240, 200, 20, "U Small Craft Facilities"), null);
		sectionButtons = new ButtonGroup();
		sectionButtons.add(eButton);
		sectionButtons.add(fButton);
		sectionButtons.add(jButton);
		sectionButtons.add(kButton);
		sectionButtons.add(lButton);
		sectionButtons.add(mButton);
		sectionButtons.add(nButton);
		sectionButtons.add(pButton);
		sectionButtons.add(qButton);
		sectionButtons.add(rButton);
		sectionButtons.add(sButton);
		sectionButtons.add(tButton);
		sectionButtons.add(uButton);
		eButton.addActionListener(alSection);
		fButton.addActionListener(alSection);
		jButton.addActionListener(alSection);
		kButton.addActionListener(alSection);
		lButton.addActionListener(alSection);
		mButton.addActionListener(alSection);
		nButton.addActionListener(alSection);
		pButton.addActionListener(alSection);
		qButton.addActionListener(alSection);
		rButton.addActionListener(alSection);
		sButton.addActionListener(alSection);
		tButton.addActionListener(alSection);
		uButton.addActionListener(alSection);
	}
	
	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String title) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setText(title);
		return button;
	}

}
