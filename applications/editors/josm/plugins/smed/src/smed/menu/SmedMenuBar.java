package smed.menu;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import smed.Smed;
import smed.list.JCheckBoxList;
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
				HideAction hideAction = new HideAction();
				JDialog dialogHide = hideAction.getHideDialog();
				JCheckBoxList hideList = hideAction.getHideList();
				
				JCheckBox jCheckBox = new JCheckBox();
				jCheckBox.setBounds(new Rectangle(5, 10, 300, 20));
				jCheckBox.setText("hello world");

				List<JCheckBox> hideCBList = new ArrayList<JCheckBox>();
				
				/*
				for(SmedPluggable p : Smed.getPlugins() ) {
					
				}
				*/
				
				hideList.add(jCheckBox);
				
				
				dialogHide.setVisible(true);
			}
		});


        menu.add(menuItem);

        menuBar.add(menu);

        add(menuBar);
    }

}
