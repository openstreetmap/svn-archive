/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * @author tilusnet <tilusnet@gmail.com>
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
        Main.map.statusLine
                .setHelpText(tr("Ctrl-Click: select reference way segment; Alt-click: Clear selection"));
    }

}
