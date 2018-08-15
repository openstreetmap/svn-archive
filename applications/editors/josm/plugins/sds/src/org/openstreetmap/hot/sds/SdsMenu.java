// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.tools.Logging;

@SuppressWarnings("serial")
public class SdsMenu extends JMenu implements LayerChangeListener, ActiveLayerChangeListener {

    private JMenuItem saveItem;
    private JMenuItem loadItem;
    private JMenuItem prefsItem;
    private JMenuItem aboutItem;
    private JMenu menu;

    public SdsMenu(final SeparateDataStorePlugin thePlugin) {
        MainMenu mm = MainApplication.getMenu();
        menu = mm.addMenu("SDS", tr("SDS"), KeyEvent.VK_S, mm.getDefaultMenuPos(), null);
        saveItem = new JMenuItem(new SdsSaveAction());
        menu.add(saveItem);
        loadItem = new JMenuItem(new SdsLoadAction(thePlugin));
        menu.add(loadItem);
        menu.addSeparator();
        prefsItem = new JMenuItem(new SdsPreferencesAction());
        menu.add(prefsItem);
        menu.addSeparator();
        aboutItem = new JMenuItem(new SdsAboutAction());
        menu.add(aboutItem);

        MainApplication.getLayerManager().addLayerChangeListener(this);
        MainApplication.getLayerManager().addActiveLayerChangeListener(this);
        setEnabledState();
    }

    void setEnabledState() {
        boolean en = MainApplication.getLayerManager().getActiveLayer() instanceof OsmDataLayer;
        loadItem.setEnabled(en);
        saveItem.setEnabled(en);
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        setEnabledState();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) { }

    @Override
    public void layerAdded(LayerAddEvent e) {
        setEnabledState();
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        setEnabledState();
    }

    private static class SdsAboutAction extends JosmAction {

        SdsAboutAction() {
            super(tr("About"), "sds", tr("Information about SDS."), null, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel about = new JPanel();

            JTextArea l = new JTextArea();
            l.setLineWrap(true);
            l.setWrapStyleWord(true);
            l.setEditable(false);
            l.setText(
                "Separate Data Store\n\nThis plugin provides access to a \"Separate Data Store\" server. " +
                "Whenever data is loaded from the OSM API, " +
                "it queries the SDS for additional tags that have been stored for the objects just loaded, " +
                "and adds these tags. When you upload data to JOSM, SDS tags will again be separated and, " +
                "instead of sending them to OSM, they will be uploaded to SDS." +
                "\n\n" +
                "This depends on SDS tags starting with a special prefix, which can be configured in the SDS preferences." +
                "\n\n" +
                "Using the SDS server will usually require an account to be set up there, which is completely independent of your OSM account.");

            l.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            l.setOpaque(false);
            l.setPreferredSize(new Dimension(500, 300));
            JScrollPane sp = new JScrollPane(l);
            sp.setBorder(null);
            sp.setOpaque(false);

            about.add(sp);

            about.setPreferredSize(new Dimension(500, 300));

            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), about, tr("About SDS..."),
                    JOptionPane.INFORMATION_MESSAGE, null);
        }
    }

    private static final class SdsPreferencesAction extends JosmAction implements Runnable {

        private SdsPreferencesAction() {
            super(tr("Preferences..."), "preference", tr("Open a preferences dialog for SDS."),
                    null, true);
            putValue("help", ht("/Action/Preferences"));
        }

        /**
         * Launch the preferences dialog.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            run();
        }

        @Override
        public void run() {
            PreferenceDialog pd = new PreferenceDialog(MainApplication.getMainFrame());
            // unusual reflection mechanism to cater for older JOSM versions where
            // the selectPreferencesTabByName method was not public
            try {
                Method sptbn = pd.getClass().getMethod("selectPreferencesTabByName", String.class);
                sptbn.invoke(pd, "sds");
            } catch (Exception ex) {
                Logging.trace(ex);
            }
            pd.setVisible(true);
        }
    }
}
