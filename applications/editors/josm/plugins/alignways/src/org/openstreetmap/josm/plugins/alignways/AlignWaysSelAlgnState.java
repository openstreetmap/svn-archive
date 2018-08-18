// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 */
public class AlignWaysSelAlgnState extends AlignWaysState {

    @Override
    public void leftClick(AlignWaysMode alignWaysMode) {
        // No state change, nothing to do
    }

    @Override
    public void ctrlLClick(AlignWaysMode alignWaysMode) {
        alignWaysMode.setCurrentState(alignWaysMode.getBothSelected());
    }

    @Override
    public void setHelpText() {
        MainApplication.getMap().statusLine
                .setHelpText(tr("Ctrl-Click: select reference way segment; Alt-click: Clear selection"));
    }

}
