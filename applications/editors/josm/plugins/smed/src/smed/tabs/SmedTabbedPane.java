package smed.tabs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;

import smed.io.SmedFile;
import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import smed.plug.manager.SmedPluginManagerImpl;
import smed.plug.util.SmedPluginLoader;

public class SmedTabbedPane extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

	static private List<SmedPluggable> plugins = null;
	static private JTabbedPane tabbedPane = null;
	
    public SmedTabbedPane() {
        // super(new GridLayout(1, 1));

        String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
        
		try {
            plugins = SmedPluginLoader.loadPlugins(new File(pluginDirName + "/splug"));

            if(plugins != null) {
            	ImageIcon icon = null;
            	if(tabbedPane == null) { 
            		tabbedPane = new JTabbedPane();
            		tabbedPane.addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent event) {
							System.out.println("hello world");
						}
            		});
            	}

            	JComponent panel;
            	int i = 0;
            	SmedFile splugDir = new SmedFile(pluginDirName + "/splug");
            	SmedPluginManager manager = new SmedPluginManagerImpl();
            
            	for(SmedPluggable p : plugins) {
            		p.setPluginManager(manager);
            		
            		if(splugDir.isVisible(p.getFileName())) {
            			panel = p.getComponent();
            			icon  = p.getIcon();
            			
            			tabbedPane.addTab(p.getName(),icon, panel, p.getInfo());
            			tabbedPane.setMnemonicAt(i, KeyEvent.VK_1 + i);
                    	
            			i++;
            		} else splugDir.setVisible(p.getFileName(),false);
            	}
            	
            	//Add the tabbed pane to this panel.
            	add(tabbedPane);

            	tabbedPane.setPreferredSize(new Dimension(400, 400));
            	
            	//The following line enables to use scrolling tabs.
            	tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        	}
        } catch (IOException e) {
            e.printStackTrace();
        } 

    } 
    
    public static List<SmedPluggable> getPlugins() { return plugins; }
    public static JTabbedPane getTabbedPane() { return tabbedPane; }

    /*
	@Override
	public void stateChanged(ChangeEvent event) {
		System.out.println("hello world");
		
	}
	*/
}
