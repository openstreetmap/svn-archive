// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;

import org.openstreetmap.josm.Main;

/** The error severity */
public enum Severity {

    /** clean override */ 
    CLEAN(tr("overridden with odbl=clean"), "clean",           
        Main.pref.getColor(marktr("license check info"), Color.GREEN)),

    /** Error messages */
    FIRST(tr("data loss"), "error",                     
        Main.pref.getColor(marktr("license check error"), Color.RED)),

    /** Warning messages */
    NORMAL(tr("possible data loss"), "warning", 
        Main.pref.getColor(marktr("license check warning"), Color.ORANGE)),

    /** Other messages */
    HARMLESS(tr("harmless data loss"), "other",           
        Main.pref.getColor(marktr("license check notice"), Color.YELLOW)),

    /** Other messages */
    NONE(tr("no data loss"), "other",           
        Main.pref.getColor(marktr("license check info"), Color.GRAY));

    /** Description of the severity code */
    private final String message;

    /** Associated icon */
    private final String icon;

    /** Associated color */
    private final Color color;

    /**
     * Constructor
     *
     * @param message Description
     * @param icon Associated icon
     * @param color The color of this severity
     */
    Severity(String message, String icon, Color color)
    {
        this.message = message;
        this.icon = icon;
        this.color = color;
    }

    @Override
    public String toString()
    {
        return message;
    }

    /**
     * Gets the associated icon
     * @return the associated icon
     */
    public String getIcon()
    {
        return icon;
    }

    /**
     * Gets the associated color
     * @return The associated color
     */
    public Color getColor()
    {
        return color;
    }


}
