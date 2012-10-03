package smed.tabs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

import smed.menu.SmedMenuBar;
import smed.plug.ifc.SmedPluggable;

public class SmedTabAction extends JosmAction {

    /**
     *
     */
	private static final long serialVersionUID = 1L;
	private SmedTabbedPane smedTabs = new SmedTabbedPane();
	private SmedMenuBar smedMenu = new SmedMenuBar();
	private JFrame frame = null;
	private boolean isOpen = false;
	private JMenuItem osmItem =null;
	public static JTextField smedStatusBar = null;
	private static String editor =tr("SeaMap Editor");
	
    public SmedTabAction() {
        super( editor, "Smed", editor, null, true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowTabs();
            }
        });
        
        isOpen = true;
		if (osmItem == null) return;

		osmItem.setEnabled(false);
    }


    protected void createAndShowTabs() {
        //Create and set up the window.
        frame = new JFrame(editor);
        smedStatusBar = new JTextField();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(true);
        frame.setAlwaysOnTop(true);

        //Add content to the window.
        frame.setJMenuBar(smedMenu);
        frame.add(smedTabs, BorderLayout.CENTER);
        frame.add(smedStatusBar,BorderLayout.PAGE_END);

        //Display the window.
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
        	public void windowClosing(java.awt.event.WindowEvent e) {
        		osmItem.setEnabled(true);
        		
        		closeDialog();
        	}
        });
        frame.setSize(new Dimension(420, 470));
        // frame.pack();
        frame.setVisible(true);
    }


	public void closeDialog() {
		List<SmedPluggable> plugins = SmedTabbedPane.getPlugins();
		
		if(plugins != null) {
			for(SmedPluggable p : plugins) p.stop();
		}
		
		if(isOpen) {
			frame.setVisible(false);
			frame.dispose();
		}
		
		isOpen = false;
	}


	public void setOsmItem(JMenuItem item) {
		osmItem = item;		
	}

}
