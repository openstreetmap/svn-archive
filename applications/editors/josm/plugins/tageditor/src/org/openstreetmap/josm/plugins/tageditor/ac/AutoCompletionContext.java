// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.ac;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;

public class AutoCompletionContext {

    private boolean selectionIncludesNodes = false;
    private boolean selectionIncludesWays = false;
    private boolean selectionIncludesRelations = false;
    private boolean selectionEmpty = false;

    public AutoCompletionContext(){
    }

    public void initFromJOSMSelection() {
        DataSet ds = Main.getLayerManager().getEditDataSet();
        selectionIncludesNodes = !ds.getSelectedNodes().isEmpty();
        selectionIncludesWays = !ds.getSelectedWays().isEmpty();
        selectionIncludesRelations = !ds.getSelectedRelations().isEmpty();
        selectionEmpty = ds.getSelected().isEmpty();
    }

    public boolean isSelectionEmpty() {
        return selectionEmpty;
    }

    public boolean isSelectionIncludesNodes() {
        return selectionIncludesNodes;
    }

    public void setSelectionIncludesNodes(boolean selectionIncludesNodes) {
        this.selectionIncludesNodes = selectionIncludesNodes;
    }

    public boolean isSelectionIncludesWays() {
        return selectionIncludesWays;
    }

    public void setSelectionIncludesWays(boolean selectionIncludesWays) {
        this.selectionIncludesWays = selectionIncludesWays;
    }

    public boolean isSelectionIncludesRelations() {
        return selectionIncludesRelations;
    }

    public void setSelectionIncludesRelations(boolean selectionIncludesRelations) {
        this.selectionIncludesRelations = selectionIncludesRelations;
    }
}
