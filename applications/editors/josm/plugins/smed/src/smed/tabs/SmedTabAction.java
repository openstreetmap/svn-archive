package smed.tabs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

public class SmedTabAction extends JosmAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SmedTabAction() {
        super( "Seekarten Editor", "Smed","Seekarten Editor", Shortcut.registerShortcut(
                                "tools:Semmaps",
                                tr("Tool: {0}", "Seekarten Editor"), KeyEvent.VK_K, //$NON-NLS-1$ //$NON-NLS-2$
                                Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //Add content to the window.
        frame.add(new SmedTabbedPane(), BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


}
