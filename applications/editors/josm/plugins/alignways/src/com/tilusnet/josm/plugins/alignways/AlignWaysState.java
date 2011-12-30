/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import org.openstreetmap.josm.Main;

/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public abstract class AlignWaysState {

    public abstract void leftClick(AlignWaysMode alignWaysMode);

    public abstract void ctrlLClick(AlignWaysMode alignWaysMode);

    public abstract void setHelpText();

    public void altLClick(AlignWaysMode alignWaysMode) {
        alignWaysMode.setCurrentState(alignWaysMode.getNoneSelected());
        Main.map.statusLine
        .setHelpText("Ctrl-Click: select reference way segment; Click: select way segment to be aligned");
    }

}
