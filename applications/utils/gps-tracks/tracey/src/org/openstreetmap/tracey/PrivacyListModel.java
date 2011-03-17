/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.tracey;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author Jonathan Bennett	
 */
public class PrivacyListModel extends AbstractListModel implements ComboBoxModel  {

	private Privacy privacy;

	public PrivacyListModel() {
		this.privacy = Privacy.PRIVATE;
	}

	public Privacy getSelectedItem() {
		return this.privacy;
	}

	public void setSelectedItem(Object newPrivacy) {
		this.privacy = (Privacy)newPrivacy;
	}

	public Privacy getElementAt(int index) {
		return Privacy.values()[index];
	}

	public int getSize() {
		return Privacy.values().length;
	}

}
