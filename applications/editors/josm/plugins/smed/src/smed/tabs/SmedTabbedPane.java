package smed.tabs;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
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

    @SuppressWarnings("null")
    public SmedTabbedPane() {
        super(new GridLayout(1, 1));

        List<SmedPluggable> plugins = null;
        String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
        try {
            plugins = SmedPluginLoader.loadPlugins(new File(pluginDirName + "/splug"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Icon icon = null;
        JTabbedPane tabbedPane = new JTabbedPane();

        JComponent panel1;
        if(plugins == null) {
            panel1 = makeTextPanel("Panel #1");
            tabbedPane.addTab("Tab 1", icon , panel1, "Does nothing");
        } else {
            panel1 = new JPanel();
            plugins.get(0).start(panel1);
            tabbedPane.addTab(plugins.get(0).getName(), icon , panel1, "say hello");
        }

        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);


        JComponent panel2 = makeTextPanel("Panel #2");
        tabbedPane.addTab("Tab 2", icon, panel2, "Does twice as much nothing");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        JComponent panel3 = makeTextPanel("Panel #3");
        tabbedPane.addTab("Tab 3", icon, panel3, "Still does nothing");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        JComponent panel4 = makeTextPanel( "Panel #4 (has a preferred size of 410 x 50).");
        panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Tab 4", icon, panel4, "Does nothing at all");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

        //Add the tabbed pane to this panel.
        add(tabbedPane);

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    private JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);

        return panel;
    }
}
