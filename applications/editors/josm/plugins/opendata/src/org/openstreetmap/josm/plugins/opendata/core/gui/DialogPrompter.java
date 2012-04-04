//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.gui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.ExtendedDialog;

public abstract class DialogPrompter implements Runnable {
	
	private int value = -1;

	protected abstract ExtendedDialog buildDialog();
	
	@Override
	public final void run() {
		ExtendedDialog dlg = buildDialog();
		if (dlg != null) {
			value = dlg.showDialog().getValue();
		}
	}
	
	public final DialogPrompter promptInEdt() {
		if (SwingUtilities.isEventDispatchThread()) {
			run();
		} else {
			try {
				SwingUtilities.invokeAndWait(this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return this;
	}

	public final int getValue() {
		return value;
	}
}
