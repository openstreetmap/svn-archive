/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/**
 * @author cdaller
 *
 */
public class PluginUpdateFrame extends JFrame {
    private JTabbedPane tabPane;
    //private ManagePanel manager;
    private InstallPanel updater;
    private InstallPanel installer;



    /**
     * @param title
     * @param descriptions 
     * @throws HeadlessException
     */
    public PluginUpdateFrame(String title, List<SiteDescription> descriptions) throws HeadlessException {
        super(title);
        init(descriptions);
    }
    
    public void init(List<SiteDescription> descriptions) {
        
        /* Setup panes */
        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBorder(new EmptyBorder(12,12,12,12));
        setContentPane(content);

        tabPane = new JTabbedPane();
        //tabPane.addTab(tr("Manage"),  manager = new ManagePanel(this));
        tabPane.addTab(tr("Update"), updater = new InstallPanel(this, descriptions));
        //tabPane.addTab(tr("Install"),installer = new InstallPanel(this));

        content.add(BorderLayout.CENTER,tabPane);
        
        JPanel buttonPannel = new JPanel();
        // <FIXXME date="23.06.2007" author="cdaller">
        // TODO i18n!
        JButton okButton = new JButton(tr("Install"));
        JButton cancelButton = new JButton(tr("Cancel"));
        // </FIXXME> 
        buttonPannel.add(okButton);
        buttonPannel.add(cancelButton);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updater.install();
                setVisible(false);
                dispose();
            }   
        });
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }   
        });
        content.add(BorderLayout.SOUTH,buttonPannel);
        
        pack();
    }

}
