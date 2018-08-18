/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;

import messages.Messages;
import s57.S57att.Att;
import s57.S57map.AttMap;
import s57.S57map.Feature;
import s57.S57obj.Obj;
import s57.S57val.AttVal;
import scedit.SCeditAction;

public class PanelMain extends JPanel {

    BufferedImage img;
    int w, h, z, f;
    JTextField wt, ht, zt, ft;
    public static JTextArea decode = null;
    public static JTextField messageBar = null;
    public JButton saveButton = null;
    private ActionListener alSave = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
    };
    private JButton importButton = null;
    JFileChooser ifc = new JFileChooser(Config.getPref().get("nceditplugin.encinpfile"));
    private ActionListener alImport = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (e.getSource() == importButton) {
                SCeditAction.panelS57.setVisible(true);
        setStatus("Select S-57 ENC file for import", Color.yellow);
                int returnVal = ifc.showOpenDialog(MainApplication.getMainFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        Config.getPref().put("smed2plugin.encinpfile", ifc.getSelectedFile().getPath());
                        SCeditAction.panelS57.startImport(ifc.getSelectedFile());
                    } catch (IOException e1) {
                        SCeditAction.panelS57.setVisible(false);
                        setStatus("IO Exception", Color.red);
                    }
                } else {
                    SCeditAction.panelS57.setVisible(false);
                    clrStatus();
                }
            }
        }
    };

    private JButton exportButton = null;
    final JFileChooser efc = new JFileChooser();
    private ActionListener alExport = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (e.getSource() == exportButton) {
                SCeditAction.panelS57.setVisible(true);
        setStatus("Select S-57 ENC file for export", Color.yellow);
        int returnVal = efc.showOpenDialog(MainApplication.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                        SCeditAction.panelS57.startExport(efc.getSelectedFile());
                    } catch (IOException e1) {
                        SCeditAction.panelS57.setVisible(false);
                        setStatus("IO Exception", Color.red);
                    }
         } else {
                     SCeditAction.panelS57.setVisible(false);
                    clrStatus();
         }
      }
        }
    };

    public PanelMain() {
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(new Color(0xb5d0d0));
        if (img != null) g2.clearRect(0, 0, img.getWidth(), img.getHeight());
        g2.drawImage(img, 0, 0, null);
    }

    public static void setStatus(String text, Color bg) {
        messageBar.setBackground(bg);
        messageBar.setText(text);
    }

    public static void clrStatus() {
        messageBar.setBackground(Color.white);
        messageBar.setText("");
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
                            decode.append("\t\t\t" + Messages.getString(att.name()) + ": " +
                                    Messages.getString(((Enum<?>) ((ArrayList<?>) item.val).get(0)).name()) + "\n");
                            break;
                        case L:
                            decode.append("\t\t\t" + Messages.getString(att.name()) + ": ");
                            boolean first = true;
                            for (Object val : (ArrayList<?>) item.val) {
                                if (!first) {
                                    decode.append(", ");
                                } else {
                                    first = false;
                                }
                                decode.append(Messages.getString(((Enum<?>) val).name()));
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
