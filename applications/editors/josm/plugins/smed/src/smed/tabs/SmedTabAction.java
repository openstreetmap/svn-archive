package smed.tabs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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

    public SmedTabAction() {
        super( "Seekarten Editor", "Smed","Seekarten Editor", Shortcut.registerShortcut(
                                "tools:Semmaps",
                                tr("Tool: {0}", "Seekarten Editor"), KeyEvent.VK_K, //$NON-NLS-1$ //$NON-NLS-2$
                                Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowTabs();
            }
        });
    }


    protected void createAndShowTabs() {
        //Create and set up the window.
        JFrame frame = new JFrame("TabbedPaneDemo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);

        //Add content to the window.
        frame.setJMenuBar(smedMenu);
        frame.add(smedTabs, BorderLayout.CENTER);

        //Display the window.
        frame.setSize(new Dimension(420, 460));
        // frame.pack();
        frame.setVisible(true);
    }


}
