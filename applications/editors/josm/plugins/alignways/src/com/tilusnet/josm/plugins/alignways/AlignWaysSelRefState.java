/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.Main;

/**
 * @author tilusnet <tilusnet@gmail.com>
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
        Main.map.statusLine
                .setHelpText(tr("Click: select way segment to be aligned; Alt-click: Clear selection"));
    }

}
