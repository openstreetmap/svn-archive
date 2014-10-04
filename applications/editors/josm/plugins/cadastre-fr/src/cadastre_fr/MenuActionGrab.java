// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action calling the wms grabber for cadastre.gouv.fr
 */
public class MenuActionGrab extends JosmAction {

    public static String name = marktr("Cadastre grab");

    public MenuActionGrab() {
        super(tr(name), "cadastre_small", tr("Download Image from French Cadastre WMS"),
                Shortcut.registerShortcut("cadastre:grab", tr("Cadastre: {0}", tr("Download Image from French Cadastre WMS")),
                KeyEvent.VK_F10, Shortcut.DIRECT), false, "cadastrefr/grab", true);
    }

    public void actionPerformed(ActionEvent e) {
        if (Main.map != null) {
            if (CadastrePlugin.isCadastreProjection()) {
                WMSLayer wmsLayer = WMSDownloadAction.getLayer();
                if (wmsLayer != null)
                    DownloadWMSVectorImage.download(wmsLayer);
            } else {
                CadastrePlugin.askToChangeProjection();
            }
        } else
            new MenuActionNewLocation().actionPerformed(e);
    }

}
