package panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import s57.S57att.Att;
import s57.S57obj.Obj;
import s57.S57val;
import seamap.SeaMap;
import seamap.SeaMap.*;
import smed2.S57en;
import smed2.Smed2Action;

public class PanelMain extends JPanel {

	public static JTextArea decode = null;
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

		decode = new JTextArea();
		decode.setBounds(0, 0, 480, 420);
		decode.setTabSize(1);
		add(decode);
	}
	
	public void parseMark(Feature feature) {
		decode.setText("Selected feature:\n");
		decode.append("\tType: " + S57en.ObjEN.get(feature.type) + "\n");
		if (feature.atts.get(Att.OBJNAM) != null) {
			decode.append("\tName: " + feature.atts.get(Att.OBJNAM).val + "\n");
		}
		decode.append("\tObjects:\n");
		for (Obj obj : feature.objs.keySet()) {
			decode.append("\t\t" + S57en.ObjEN.get(obj) + "\n");
			if (feature.objs.get(obj).size() != 0) {
				for (AttMap atts : feature.objs.get(obj).values()) {
					for (Att att : atts.keySet()) {
						AttItem item = atts.get(att);
						switch (item.conv) {
						case E:
							decode.append("\t\t\t" + S57en.AttEN.get(att) + ": " + S57en.enums.get(att).get(item.val) + "\n");
							break;
						case L:
							decode.append("\t\t\t" + S57en.AttEN.get(att) + ": ");
							Iterator it = ((ArrayList)item.val).iterator();
							while (it.hasNext()) {
								Object val = it.next();
								decode.append((String)S57en.enums.get(att).get(val));
								if (it.hasNext()) {
									decode.append(", ");
								}
							}
							decode.append("\n");
							break;
						default:
							decode.append("\t\t\t" + S57en.AttEN.get(att) + ": " + item.val + "\n");
						}
					}
				}
			}
		}
	}
	
	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String title) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setText(title);
		return button;
	}

}
