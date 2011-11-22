package org.openstreetmap.josm.plugins.piclayer;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;

@SuppressWarnings("serial")
public class PicToggleButton extends IconToggleButton {


    public PicToggleButton(Action action, String btnName, String visibilityKey, boolean defVisibility) {
        super(action);
        this.btnName = btnName;
        this.visibilityKey = visibilityKey;
        this.defVisibility = defVisibility;
    }

    private final String visibilityKey;
    protected final String btnName;
    private boolean defVisibility;

    public String getVisibilityKey() {
        return visibilityKey;
    }
    public String getBtnName() {
        return btnName;
    }
    public boolean getDefVisibility() {
        return defVisibility;
    }

    public void readVisible() {
        setVisible(Main.pref.getBoolean(getVisibilityKey(), getDefVisibility()));
    }

}
