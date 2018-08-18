// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 */
public class AlignWaysSelRefState extends AlignWaysState {

    @Override
    public void leftClick(AlignWaysMode alignWaysMode) {
        alignWaysMode.setCurrentState(alignWaysMode.getBothSelected());
    }

    @Override
    public void ctrlLClick(AlignWaysMode alignWaysMode) {
        // No state change, nothing to do
    }

    @Override
    public void setHelpText() {
        MainApplication.getMap().statusLine
                .setHelpText(tr("Click: select way segment to be aligned; Alt-click: Clear selection"));
    }

}
