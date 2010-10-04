package smed.menu;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
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

     JMenuBar menuBar;
     JMenu menu, submenu;
     JMenuItem menuItem;
     
     public SmedMenuBar() {
    	ResourceBundle keyEvents = ResourceBundle.getBundle("smed.keys.Events");
    	
        menuBar = new JMenuBar();

        menu = new JMenu(tr("File"));
        // menu.setMnemonic(KeyEvent.VK_F);
        menu.setMnemonic((Integer) keyEvents.getObject("SmedMenuBar.001"));
        
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");

        menuItem = new JMenuItem("Tabmanager",
                KeyEvent.VK_T);

        menuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) { new TabManager(); } 
			});


        menu.add(menuItem);

        menuBar.add(menu);

        add(menuBar);
    }

}
