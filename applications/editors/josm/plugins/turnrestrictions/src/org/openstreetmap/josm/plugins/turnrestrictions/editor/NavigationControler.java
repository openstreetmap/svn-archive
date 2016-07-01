// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

public interface NavigationControler {
    enum BasicEditorFokusTargets {
        RESTRICION_TYPE,
        FROM,
        TO,
        VIA
    }

    void gotoBasicEditor();

    void gotoAdvancedEditor();

    void gotoBasicEditor(BasicEditorFokusTargets focusTarget);
}
