package smed.menu;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import smed.menu.file.TabManager;

public class SmedMenuBar extends JMenuBar {

     /**
      *
      */
	 private static final long serialVersionUID = 1L;
    
	 private String[] cmd = new String[4];
	 
     private JMenuBar menuBar;
     private JMenu menuFile, menuHelp, submenu;
     private JMenuItem menuItemFile, menuItemOnLineHelp;
     
     public SmedMenuBar() {
    	 cmd[0] = "cmd.exe";
    	 cmd[1] = "/C";
    	 cmd[2] = "start";
    	 cmd[3] = "http://www.openseamap.org/";

    	 ResourceBundle keyEvents = ResourceBundle.getBundle("smed.keys.Events");
    	
        menuBar = new JMenuBar();

        menuFile = new JMenu(tr("File"));
        // menu.setMnemonic(KeyEvent.VK_F);
        menuFile.setMnemonic((Integer) keyEvents.getObject("SmedMenuBar.001"));
        
        menuFile.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");

        menuItemFile = new JMenuItem("Tabmanager", KeyEvent.VK_T);
        menuItemFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) { new TabManager(); } 
			});
        
        menuFile.add(menuItemFile);
        
        menuHelp = new JMenu(tr("Help"));
        menuHelp.setMnemonic(KeyEvent.VK_H);
        
        menuItemOnLineHelp = new JMenuItem(tr("Online Help"), KeyEvent.VK_O);
        menuItemOnLineHelp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {  
			try {
				Desktop.getDesktop().browse( new URI("http://www.openseamap.org/") ); 
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			}

			});
        
        menuHelp.add(menuItemOnLineHelp);
        
        menuBar.add(menuFile);
        menuBar.add(menuHelp);

        add(menuBar);
    }

}
