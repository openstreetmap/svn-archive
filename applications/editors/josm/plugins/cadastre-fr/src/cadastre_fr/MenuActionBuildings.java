// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class MenuActionBuildings extends JosmAction {
    
    public static String name = "Grab buildings only";

    private static final long serialVersionUID = 1L;
   
    public MenuActionBuildings() {
        super(tr(name), "cadastre_small", tr("Grab building layer only"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        if (Main.map != null) {
            if (CadastrePlugin.isCadastreProjection()) {
                WMSLayer wmsLayer = WMSDownloadAction.getLayer();
                if (wmsLayer != null)
                    DownloadWMSVectorImage.download(wmsLayer, true);
            } else {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("To enable the cadastre WMS plugin, change\n"
                         + "the current projection to one of the cadastre\n"
                         + "projections and retry"));
            }
        } else
            new MenuActionNewLocation().actionPerformed(e);
    }

}
