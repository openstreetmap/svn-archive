/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.tracey;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractListModel;

/**
 *
 * @author Jonathan Bennett
 */
public class TraceListModel extends AbstractListModel implements Iterable<File> {

	private ArrayList<File> delegate;

	TraceListModel() {
		this.delegate = new ArrayList<File>();
	}

	public void addElement(File file) {
		int index = delegate.size();
		delegate.add(file);
		fireIntervalAdded(this, index, index);
	}

	public void insertElementAt(File file, int index) {
		delegate.add(index, file);
		fireIntervalAdded(this, index, index);
    }


	public File get(int index) {
		return delegate.get(index);
	}

	public File getElementAt(int index) {
		return delegate.get(index);
	}

	public void remove(File element) {
		delegate.remove(element);
	}

	public void removeElementAt(int index) {
		delegate.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public void clear() {
		delegate.clear();
		fireContentsChanged(this, 0, 0);
	}

	public int getSize() {
		return delegate.size();
	}

	public Iterator<File> iterator() {
		return delegate.iterator();
	}

	/**
	* Returns a string that displays and identifies this
	* object's properties.
	*
	* @return a String representation of this object
	*/

	@Override
	public String toString() {
		return delegate.toString();
    }

}
