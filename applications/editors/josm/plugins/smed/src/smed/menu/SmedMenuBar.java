package smed.menu;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import smed.menu.file.HideAction;
import smed.plug.ifc.SmedPluggable;

public class SmedMenuBar extends JMenuBar {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

     JMenuBar menuBar;
     JMenu menu, submenu;
     JMenuItem menuItem;
     public List<SmedPluggable> plugins = null;
     HideAction hideAction = null;
     
     public SmedMenuBar() {
        menuBar = new JMenuBar();

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");

        menuItem = new JMenuItem("Hide",
                KeyEvent.VK_H);

        menuItem.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				DefaultListModel myModel = new DefaultListModel();

				for(SmedPluggable p : plugins) myModel.addElement (p.getName());
				
				hideAction = new HideAction(myModel);
			}
		});


        menu.add(menuItem);

        menuBar.add(menu);

        add(menuBar);
    }

}
