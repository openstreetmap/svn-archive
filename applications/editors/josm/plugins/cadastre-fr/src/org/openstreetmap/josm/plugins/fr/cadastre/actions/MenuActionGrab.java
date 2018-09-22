// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.DownloadWMSVectorImage;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action calling the wms grabber for cadastre.gouv.fr
 */
public class MenuActionGrab extends JosmAction {

    private static final String NAME = marktr("Cadastre grab");

    /**
     * Constructs a new {@code MenuActionGrab}.
     */
    public MenuActionGrab() {
        super(tr(NAME), "cadastre_small", tr("Download Image from French Cadastre WMS"),
                Shortcut.registerShortcut("cadastre:grab", tr("Cadastre: {0}", tr("Download Image from French Cadastre WMS")),
                KeyEvent.VK_F10, Shortcut.DIRECT), false, "cadastrefr/grab", true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (MainApplication.getMap() != null) {
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
