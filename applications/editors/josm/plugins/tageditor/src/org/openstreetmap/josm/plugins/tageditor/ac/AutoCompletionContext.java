package org.openstreetmap.josm.plugins.tageditor.ac;

import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.plugins.tageditor.josm.CompatibilityUtil.getCurrentDataSet;

public class AutoCompletionContext {

	private boolean selectionIncludesNodes = false;
	private boolean selectionIncludesWays = false;
	private boolean selectionIncludesRelations = false;
	private boolean selectionEmpty = false;

	public AutoCompletionContext(){
	}

	public void initFromJOSMSelection() {
		selectionIncludesNodes = ! getCurrentDataSet().getSelectedNodes().isEmpty();
		selectionIncludesWays = !getCurrentDataSet().getSelectedWays().isEmpty();
		selectionIncludesRelations = !getCurrentDataSet().getSelectedRelations().isEmpty();
		selectionEmpty = (getCurrentDataSet().getSelected().size() == 0);
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
