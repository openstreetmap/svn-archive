package org.openstreetmap.josm.plugins.importvec;

import org.openstreetmap.josm.spi.preferences.Config;

public class Settings {

    public static void setScaleNumerator(double value) {
        Config.getPref().putDouble("importvec.scalenum", value);
    }
    public static void setScaleDivisor(double value) {
        if (value == 0)
            throw new IllegalArgumentException("Scale divisor cannot be 0");
        Config.getPref().putDouble("importvec.scalediv", value);
    }
    public static void setCurveSteps(long value) {
        if (value < 1)
            throw new IllegalArgumentException("Curve steps cannot less than 1");
        Config.getPref().putLong("importvec.curvesteps", value);
    }
    
    public static double getScaleNumerator() {
        return Config.getPref().getDouble("importvec.scalenum", 1);
    }
    public static double getScaleDivisor() {
        return Config.getPref().getDouble("importvec.scalediv", 1);
    }
    public static double getCurveSteps() {
        return Config.getPref().getDouble("importvec.curvesteps", 4);
    }
}
