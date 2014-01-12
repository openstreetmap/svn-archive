package org.openstreetmap.josm.plugins.importvec;

import org.openstreetmap.josm.Main;

public class Settings {

    public static void setScaleNumerator(double value) {
        Main.pref.putDouble("importvec.scalenum", value);
    }
    public static void setScaleDivisor(double value) {
        if (value == 0)
            throw new IllegalArgumentException("Scale divisor cannot be 0");
        Main.pref.putDouble("importvec.scalediv", value);
    }
    public static void setCurveSteps(long value) {
        if (value < 1)
            throw new IllegalArgumentException("Curve steps cannot less than 1");
        Main.pref.putLong("importvec.curvesteps", value);
    }
    
    public static double getScaleNumerator() {
        return Main.pref.getDouble("importvec.scalenum", 1);
    }
    public static double getScaleDivisor() {
        return Main.pref.getDouble("importvec.scalediv", 1);
    }
    public static double getCurveSteps() {
        return Main.pref.getDouble("importvec.curvesteps", 4);
    }
}
