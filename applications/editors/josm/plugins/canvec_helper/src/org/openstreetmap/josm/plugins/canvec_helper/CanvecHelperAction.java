// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;

class CanvecHelperAction extends JosmAction {
    private CanvecHelper parentTemp;
    CanvecHelperAction(CanvecHelper parent) {
        super("CanVec Helper", "layericon24", null, null, false);
        parentTemp = parent;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent action) {
        MainApplication.getLayerManager().addLayer(new CanvecLayer("canvec tile helper", parentTemp));
    }
}
