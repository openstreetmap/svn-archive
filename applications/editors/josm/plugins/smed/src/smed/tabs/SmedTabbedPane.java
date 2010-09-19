package smed.tabs;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.Main;

import smed.plug.ifc.SmedPluggable;
import smed.plug.util.SmedPluginLoader;

public class SmedTabbedPane extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SmedTabbedPane() {
        super(new GridLayout(1, 1));

        List<SmedPluggable> plugins = null;
        String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
        try {
            plugins = SmedPluginLoader.loadPlugins(new File(pluginDirName + "/splug"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(plugins != null) {
            Icon icon = null;
            JTabbedPane tabbedPane = new JTabbedPane();

            JComponent panel;
            int i = 0;
            for(SmedPluggable p : plugins) {
                panel = p.getComponent();
                tabbedPane.addTab(p.getName(),icon, panel, p.getInfo());
                tabbedPane.setMnemonicAt(i, KeyEvent.VK_1 + i);

                i++;
            }

            //Add the tabbed pane to this panel.
            add(tabbedPane);

            //The following line enables to use scrolling tabs.
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }
}
