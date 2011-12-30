/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

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
                .setHelpText("Click: select way segment to be aligned; Alt-click: Clear selection");
    }

}
