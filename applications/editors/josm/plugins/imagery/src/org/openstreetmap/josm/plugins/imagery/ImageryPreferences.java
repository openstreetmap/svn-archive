package org.openstreetmap.josm.plugins.imagery;

import java.awt.Color;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;

public class ImageryPreferences {
    public static final BooleanProperty PROP_REMOTE_CONTROL = new BooleanProperty("imagery.remotecontrol", true);
    public static final IntegerProperty PROP_FADE_AMOUNT = new IntegerProperty("imagery.fade_amount", 0);
    public static final IntegerProperty PROP_SHARPEN_LEVEL = new IntegerProperty("imagery.sharpen_level", 0);

    public static Color getFadeColor() {
        return Main.pref.getColor("imagery.fade", Color.white);
    }

    public static Color getFadeColorWithAlpha() {
        Color c = getFadeColor();
        return new Color(c.getRed(),c.getGreen(),c.getBlue(),PROP_FADE_AMOUNT.get()*255/100);
    }

    public static void setFadeColor(Color color) {
        Main.pref.putColor("imagery.fade", color);
    }
}
