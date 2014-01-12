// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class MenuActionBoundaries extends JosmAction {
    
    public static String name = "Administrative boundary";

    private static final long serialVersionUID = 1L;
    private WMSLayer wmsLayer = null;
   
    public MenuActionBoundaries() {
        super(tr(name), "cadastre_small", tr("Extract commune boundary"), null, false);
    }

    public void actionPerformed(ActionEvent arg0) {
        wmsLayer = WMSDownloadAction.getLayer();
        if (wmsLayer != null) {
            if (wmsLayer.isRaster()) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("Only on vectorized layers"), tr("Error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            DownloadSVGTask.download(wmsLayer);
        }
    }

}
