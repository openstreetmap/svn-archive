//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.epsg31287;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.ProjectionPreference;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Epsg31287 extends Plugin {

    private JMenu mMenu;
    private Projection oldProj;
    private final String projCode;

    public Epsg31287(PluginInformation info) {
        super(info);
        projCode = ProjectionEPSG31287.getProjCode();
        refreshMenu();
    }

    public void toggleProjection()
    {
        Bounds b = (Main.map != null && Main.map.mapView != null) ? Main.map.mapView.getRealBounds() : null;

        try {
            // toggle projection
            if (Main.proj.toCode().equals(projCode)) {
                // use JOSM built in to fire Listeners
                ProjectionPreference.setProjection(oldProj.getClass().getName(), null);

                //Main.proj = oldProj;
                //UploadAction.unregisterUploadHook(uploadHook);
                //uploadHook = null;
            } else {
                oldProj = Main.proj;
                Main.proj = new ProjectionEPSG31287();
                //TODO use JOSM built in to fire Listeners, does not work currently due to classnotfound ex
                //     ProjectionPreference.setProjection(ProjectionEPSG31287.class.getName(), null);
                UploadAction.registerUploadHook(new EPSG31287UploadHook(this));
            }
            // toggle Menu
            refreshMenu();
            // show info
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Current projection is set to {0}", Main.proj.toString()) +
                    (Main.proj.toCode().equals(projCode) ?
                            tr("\nPlease adjust WMS layer manually by using known exact objects/traces/... before starting to map")
                            : ""),
                            tr("Info"),
                            JOptionPane.INFORMATION_MESSAGE
            );
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("The projection {0} could not be activated. Using Mercator", projCode),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );
            Main.proj = new Mercator();
        }
        if((b != null) && (oldProj != null) && (!Main.proj.getClass().getName().equals(oldProj.getClass().getName()) || Main.proj.hashCode() != oldProj.hashCode()))
        {
            Main.map.mapView.zoomTo(b);
            /* TODO - remove layers with fixed projection */
        }
    }

    public void refreshMenu() {
        MainMenu menu = Main.main.menu;

        if (mMenu == null)
            mMenu = menu.addMenu(marktr("EPSG31287"), KeyEvent.VK_S, menu.defaultMenuPos, null);
        else
            mMenu.removeAll();
        // toggle menu text based on current projection
        if (Main.proj.toCode().equals(projCode)) {
            mMenu.add(new JMenuItem(new
                    JosmAction(tr("set {0}",oldProj.toString())
                            ,"wmsmenu.png"
                            ,tr("set projection from {0} to {1}",projCode,oldProj.toString())
                            , null, false)
            {
                private static final long serialVersionUID = 7610502878925107647L;
                @Override
                public void actionPerformed(ActionEvent ev) {
                    toggleProjection();
                }
            }));
        } else {
            mMenu.add(new JMenuItem(new
                    JosmAction(tr("set {0}",projCode)
                            ,"wmsmenu_off.png"
                            ,tr("set projection from {0} to {1}",Main.proj.toString() ,projCode)
                            , null, false)
            {
                private static final long serialVersionUID = 7610502878925107646L;
                @Override
                public void actionPerformed(ActionEvent ev) {
                    toggleProjection();
                }
            }));
        }
    }

}
