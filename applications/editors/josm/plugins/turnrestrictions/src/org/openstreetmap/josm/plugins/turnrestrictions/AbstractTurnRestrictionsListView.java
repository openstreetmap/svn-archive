package org.openstreetmap.josm.plugins.turnrestrictions;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;

/**
 * The abstract base class for two views of turn restriction lists.
 * 
 * @see TurnRestrictionsInSelectionView
 * @see TurnRestrictionsInDatasetView
 *
 */
abstract class AbstractTurnRestrictionsListView extends JPanel {
	protected TurnRestrictionsListModel model;
	protected JList lstTurnRestrictions;
	
	public TurnRestrictionsListModel getModel(){
		return model;
	}
	
	public JList getList() {
		return lstTurnRestrictions;
	}
	
	public void addListSelectionListener(ListSelectionListener listener) {
		lstTurnRestrictions.addListSelectionListener(listener);
	}
	 
	public void removeListSelectionListener(ListSelectionListener listener) {
		lstTurnRestrictions.addListSelectionListener(listener);
	}

}
