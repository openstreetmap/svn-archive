// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 */
public class AlignWaysSelNoneState extends AlignWaysState {

    @Override
    public void leftClick(AlignWaysMode alignWaysMode) {
        // Reference way segment selected successfully
        alignWaysMode.setCurrentState(alignWaysMode.getAligneeSelected());

    }

    @Override
    public void ctrlLClick(AlignWaysMode alignWaysMode) {
        // Reference way segment selected successfully
        alignWaysMode.setCurrentState(alignWaysMode.getReferenceSelected());
    }

    @Override
    public void setHelpText() {
        MainApplication.getMap().statusLine
        .setHelpText(tr("Ctrl-click: select reference way segment; Click: select way segment to be aligned"));
    }

}
