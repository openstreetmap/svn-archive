package panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.*;

import messages.Messages;

import org.openstreetmap.josm.Main;

import s57.S57att.Att;
import s57.S57obj.Obj;
import s57.S57val.*;
import s57.S57map.*;
import render.Renderer;
import smed2.Smed2Action;

public class PanelMain extends JPanel {

	Smed2Action dlg;
	BufferedImage img;
	int w, h, z, f;
	JTextField wt, ht, zt, ft;
	public static JTextArea decode = null;
	public static JTextField messageBar = null;
	public JButton saveButton = null;
	private ActionListener alSave = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dumpMap();
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
//          xxx.startImport(ifc.getSelectedFile());
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
//          xxx.startExport(efc.getSelectedFile());
         } else {
           messageBar.setText("");
         }
      }
		}
	};

	public PanelMain(Smed2Action dia) {
		dlg = dia;
		setLayout(null);
		setSize(new Dimension(480, 480));
		
		w = h = z = f = 0;
		wt = new JTextField("0");
		wt.setBounds(10, 400, 40, 20);
    add(wt);
		ht = new JTextField("0");
		ht.setBounds(60, 400, 40, 20);
    add(ht);
		zt = new JTextField("0");
		zt.setBounds(110, 400, 40, 20);
    add(zt);
		ft = new JTextField("0");
		ft.setBounds(160, 400, 40, 20);
    add(ft);

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
	
	public void dumpMap() {
		img = new BufferedImage(Integer.parseInt(wt.getText()), Integer.parseInt(ht.getText()), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		Renderer.reRender(g2, Integer.parseInt(zt.getText()), Integer.parseInt(ft.getText()), dlg.map, dlg.rendering);
		try {
			ImageIO.write(img, "png", new File("/Users/mherring/Desktop/export.png"));
		} catch (Exception x) {
			System.out.println("Exception");
		}
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(new Color(0xb5d0d0));
		if (img != null) g2.clearRect(0, 0, img.getWidth(), img.getHeight());
		g2.drawImage(img, 0, 0, null);;
	}
	
	public void parseMark(Feature feature) {
		decode.setText("Selected object:\n");
		decode.append("\t" + tr("Type") + ": " + Messages.getString(feature.type.name()) + "\n");
		if (feature.atts.get(Att.OBJNAM) != null) {
			decode.append("\t" + tr("Name") + ": " + feature.atts.get(Att.OBJNAM).val + "\n");
		}
		decode.append("\tObjects:\n");
		for (Obj obj : feature.objs.keySet()) {
			decode.append("\t\t" + Messages.getString(obj.name()) + "\n");
			if (feature.objs.get(obj).size() != 0) {
				for (AttMap atts : feature.objs.get(obj).values()) {
					for (Att att : atts.keySet()) {
						AttVal<?> item = atts.get(att);
						switch (item.conv) {
						case E:
							decode.append("\t\t\t" + Messages.getString(att.name()) + ": " + Messages.getString(((Enum<?>)item.val).name()) + "\n");
							break;
						case L:
							decode.append("\t\t\t" + Messages.getString(att.name()) + ": ");
							Iterator<?> it = ((ArrayList<?>)item.val).iterator();
							while (it.hasNext()) {
								Object val = it.next();
								decode.append(Messages.getString(((Enum<?>)val).name()));
								if (it.hasNext()) {
									decode.append(", ");
								}
							}
							decode.append("\n");
							break;
						default:
							decode.append("\t\t\t" + Messages.getString(att.name()) + ": " + item.val + "\n");
						}
					}
				}
			}
		}
	}
	
	public void clearMark() {
		decode.setText(tr("No object selected"));
	}
	
}
