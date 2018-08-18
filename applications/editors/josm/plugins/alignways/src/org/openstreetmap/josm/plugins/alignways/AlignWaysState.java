// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MainApplication;

/**
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 *
 */
public abstract class AlignWaysState {

    public abstract void leftClick(AlignWaysMode alignWaysMode);

    public abstract void ctrlLClick(AlignWaysMode alignWaysMode);

    public abstract void setHelpText();

    public void altLClick(AlignWaysMode alignWaysMode) {
        alignWaysMode.setCurrentState(alignWaysMode.getNoneSelected());
        MainApplication.getMap().statusLine
        .setHelpText(tr("Ctrl-Click: select reference way segment; Click: select way segment to be aligned"));
    }

}
