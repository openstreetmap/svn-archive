package smed.menu;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import smed.menu.file.TabManager;
import smed.plug.ifc.SmedPluggable;
import smed.tabs.SmedTabbedPane;

public class SmedMenuBar extends JMenuBar {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

     JMenuBar menuBar;
     JMenu menu, submenu;
     JMenuItem menuItem;
     private List<SmedPluggable> plugins = null;
     
     public SmedMenuBar() {
        menuBar = new JMenuBar();

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
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
