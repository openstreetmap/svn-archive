package org.openstreetmap.josm.plugins.tageditor.ac;

import org.openstreetmap.josm.Main;

public class AutoCompletionContext {
	
	private boolean selectionIncludesNodes = false;
	private boolean selectionIncludesWays = false;
	private boolean selectionIncludesRelations = false;
	private boolean selectionEmpty = false; 
	
	public AutoCompletionContext(){
	}

	public void initFromJOSMSelection() {
		selectionIncludesNodes = ! Main.ds.getSelectedNodes().isEmpty();
		selectionIncludesWays = !Main.ds.getSelectedWays().isEmpty();
		selectionIncludesRelations = !Main.ds.getSelectedRelations().isEmpty();
		selectionEmpty = (Main.ds.getSelected().size() == 0);
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
