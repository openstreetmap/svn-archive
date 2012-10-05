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
	private SmedPluginManager manager = null;
	private int activeIndex = -1;
	static private SmedPluggable curPlugin = null;
	
    public SmedTabbedPane() {
        // super(new GridLayout(1, 1));

        String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
        
		try {
            plugins = SmedPluginLoader.loadPlugins(new File(pluginDirName + "/splug"));

            if(plugins != null) {

            	if(tabbedPane == null) tabbedPane = new JTabbedPane();

            	ImageIcon icon = null;

            	JComponent panel;
            	int i = 0;
            	SmedFile splugDir = new SmedFile(pluginDirName + "/splug");
            	manager = new SmedPluginManagerImpl();
            
            	for(SmedPluggable p : plugins) {
            		p.setPluginManager(manager);
            		
            		if(splugDir.isVisible(p.getFileName())) {
            			panel = p.getComponent();
            			icon  = p.getIcon();
            			
            			tabbedPane.addTab(p.getName(),icon, panel, p.getInfo());
            			tabbedPane.setMnemonicAt(i, KeyEvent.VK_1 + i);
            			if(i == 0) { 
            				curPlugin = p;
            				activeIndex = 0;
            			}
            			p.setIndex(i);
                    	
            			i++;
            		} else splugDir.setVisible(p.getFileName(),false);
            	}
            	
            	//Add the tabbed pane to this panel.
            	add(tabbedPane);

            	tabbedPane.setPreferredSize(new Dimension(410, 400));
            	
            	//The following line enables to use scrolling tabs.
            	tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            	
            	// add ChangeListener
            	tabbedPane.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent event) {
						JTabbedPane pane = (JTabbedPane) event.getSource();
						
						for(SmedPluggable p : plugins) {
							if(p.getIndex() == activeIndex) p.lostFocus();
						}
						
						activeIndex = pane.getSelectedIndex();
						for(SmedPluggable p : plugins) {
							if(p.getIndex() == activeIndex) { 
								p.hasFocus();
								curPlugin = p;
							}
						}
					}
            	});

        	}
        } catch (IOException e) {
            e.printStackTrace();
        } 

    } 
    
    public static List<SmedPluggable> getPlugins() { return plugins; }
    public static JTabbedPane getTabbedPane() { return tabbedPane; }
    public static SmedPluggable getCurPlugin() { return curPlugin; }
}
