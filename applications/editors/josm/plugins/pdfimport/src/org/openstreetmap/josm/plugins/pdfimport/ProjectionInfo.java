// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.gui.preferences.projection.SingleProjectionChoice;

public final class ProjectionInfo {
    private static Map<String, ProjectionChoice> allCodesPC = new HashMap<>();
    private static Map<String, Projection> allCodes = new HashMap<>();

    static {
        for (ProjectionChoice pc : ProjectionPreference.getProjectionChoices()) {
            for (String code : pc.allCodes()) {
                allCodesPC.put(code, pc);
            }
        }
    }

    private ProjectionInfo() {
        // Hide default constructor for utilities classes
    }

    public static Projection getProjectionByCode(String code) {
        try {
            ProjectionChoice pc = new SingleProjectionChoice(code.toString(), code.toString(), code);
            Projection p = pc.getProjection();
            return p;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

//        Projection p = allCodes.get(code);
//        if (p != null) return p;
//        ProjectionChoice pc = allCodesPC.get(code);
//        if (pc == null) return null;
//        Collection<String> pref = pc.getPreferencesFromCode(code);
//        pc.setPreferences(pref);
//        p = pc.getProjection();
//        allCodes.put(code, p);
//        return p;
    }
}
