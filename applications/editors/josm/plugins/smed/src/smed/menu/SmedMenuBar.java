package smed.menu;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
				System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
			}
		});

        
        menu.add(menuItem);

        menuBar.add(menu);
		
		add(menuBar);
	}

}
