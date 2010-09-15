package org.openstreetmap.josm.plugins.turnrestrictions.editor;

public interface NavigationControler {
    public enum BasicEditorFokusTargets {
        RESTRICION_TYPE,
        FROM,
        TO,
        VIA
    }   
    void gotoBasicEditor(); 
    void gotoAdvancedEditor();
    void gotoBasicEditor(BasicEditorFokusTargets focusTarget);  
}
