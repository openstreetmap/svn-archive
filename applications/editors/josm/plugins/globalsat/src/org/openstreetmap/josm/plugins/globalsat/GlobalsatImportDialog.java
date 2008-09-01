// License: GPL v2 or later. Copyright 2007 by Raphael Mack, Immanuel Scholz and others
package org.openstreetmap.josm.plugins.globalsat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;

import gnu.io.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadGpsTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.PluginProxy;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;

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
            portCombo = new JComboBox();
            portCombo.setRenderer(new ListCellRenderer(){
                    public java.awt.Component getListCellRendererComponent(JList list, Object o, int x, boolean a, boolean b){
                        return new JLabel(((CommPortIdentifier)o).getName());
                    }
                });
            portCombo.addActionListener(new ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e){
                        Main.pref.put("globalsat.portIdentifier", ((CommPortIdentifier)portCombo.getSelectedItem()).getName());
                    }
                });
            refreshPorts();
            delete = new JCheckBox(tr("delete data after import"));
            delete.setSelected(Main.pref.getBoolean("globalsat.deleteAfterDownload", false));
            setLayout(new GridBagLayout());
            add(portCombo);
            add(delete);
	}

    public void refreshPorts(){
        portCombo.removeAllItems();
        for(Enumeration e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements(); ){
            CommPortIdentifier port = (CommPortIdentifier)e.nextElement();
            if(port.getPortType() == CommPortIdentifier.PORT_SERIAL){
                portCombo.addItem(port);
            }
        }
        portCombo.setSelectedItem(Main.pref.get("globalsat.portIdentifier"));
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
