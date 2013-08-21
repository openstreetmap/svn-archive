// License: GPL v2 or later. Copyright 2007 by Raphael Mack, Immanuel Scholz and others
package org.openstreetmap.josm.plugins.globalsat;

import static org.openstreetmap.josm.tools.I18n.tr;
import gnu.io.CommPortIdentifier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.openstreetmap.josm.Main;

/**
 * Main download dialog.
 *
 * @author Raphael Mack <ramack@raphael-mack.de>
 *
 */
public class GlobalsatImportDialog extends JPanel {

    // the JOptionPane that contains this dialog. required for the closeDialog() method.
    private JOptionPane optionPane;
    private JCheckBox delete;
    private JComboBox portCombo;
    private List<CommPortIdentifier> ports = new LinkedList<CommPortIdentifier>();

    public GlobalsatImportDialog() {
        GridBagConstraints c = new GridBagConstraints();
        JButton refreshBtn, configBtn;

        setLayout(new GridBagLayout());

        portCombo = new JComboBox();
        portCombo.setRenderer(new ListCellRenderer(){
                public java.awt.Component getListCellRendererComponent(JList list, Object o, int x, boolean a, boolean b){
                    String value = ((CommPortIdentifier)o).getName();
                    if(value == null){
                        value = "null";
                    }
                    return new JLabel(value);
                }
            });
        portCombo.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    Object i = portCombo.getSelectedItem();
                    if(i instanceof CommPortIdentifier){
                        GlobalsatPlugin.setPortIdent((CommPortIdentifier)i);
                        Main.pref.put("globalsat.portIdentifier", ((CommPortIdentifier)i).getName());
                    }
                }
            });

        refreshPorts();
        c.insets = new Insets(4,4,4,4);
        c.gridwidth = 1;
        c.weightx = 0.8;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel(tr("Port:")), c);

        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.5;
        add(portCombo, c);

        refreshBtn = new JButton(tr("Refresh"));
        refreshBtn.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    refreshPorts();
                }
            });
        refreshBtn.setToolTipText(tr("refresh the port list"));
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        add(refreshBtn, c);

        configBtn = new JButton(tr("Configure"));
        configBtn.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    System.out.println("configureing the device");
                    try{

                        GlobalsatConfigDialog dialog = new GlobalsatConfigDialog(GlobalsatPlugin.dg100().getConfig());
                        JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                        JDialog dlg = pane.createDialog(Main.parent, tr("Configure Device"));
                        dialog.setOptionPane(pane);
                        dlg.setVisible(true);
                        if(((Integer)pane.getValue()) == JOptionPane.OK_OPTION){
                            GlobalsatPlugin.dg100().setConfig(dialog.getConfig());
                        }
                        dlg.dispose();

                    }catch(GlobalsatDg100.ConnectionException ex){
                        JOptionPane.showMessageDialog(Main.parent, tr("Connection Error.") + " " + ex.toString());
                    }
                    System.out.println("configureing the device finised");
                }
            });
        configBtn.setToolTipText(tr("configure the connected DG100"));
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 1;
        add(configBtn, c);


        delete = new JCheckBox(tr("delete data after import"));
        delete.setSelected(Main.pref.getBoolean("globalsat.deleteAfterDownload", false));

        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        add(delete, c);
    }

    public void refreshPorts(){
        String sel = Main.pref.get("globalsat.portIdentifier");
        portCombo.setVisible(false);
        portCombo.removeAllItems();

        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        for(e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements(); ){
            CommPortIdentifier port = (CommPortIdentifier)e.nextElement();
            if(port.getPortType() == CommPortIdentifier.PORT_SERIAL){
                portCombo.addItem(port);
                if(sel != null && port.getName() == sel){
                    portCombo.setSelectedItem(port);
                    GlobalsatPlugin.setPortIdent(port);
                }
            }
        }
        portCombo.setVisible(true);
        GlobalsatPlugin.setPortIdent(getPort());

    }

    public boolean deleteFilesAfterDownload(){
        return delete.isSelected();
    }

    public CommPortIdentifier getPort(){
        return (CommPortIdentifier)portCombo.getSelectedItem();
    }
    /**
     * Has to be called after this dialog has been added to a JOptionPane.
     * @param optionPane
     */
    public void setOptionPane(JOptionPane optionPane) {
        this.optionPane = optionPane;
    }
}
