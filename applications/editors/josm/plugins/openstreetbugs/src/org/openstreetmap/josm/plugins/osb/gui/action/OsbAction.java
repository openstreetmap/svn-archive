/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.data.osm.Node;

public abstract class OsbAction extends AbstractAction {

	private static List<OsbActionObserver> observers = new ArrayList<OsbActionObserver>(); 
	
	private static Node selectedNode;
	
	public OsbAction(String name) {
		super(name);
	}

	public static Node getSelectedNode() {
		return selectedNode;
	}

	public static void setSelectedNode(Node selectedNode) {
		OsbAction.selectedNode = selectedNode;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			doActionPerformed(e);
			for (OsbActionObserver obs : observers) {
				obs.actionPerformed(this);
			}
		} catch (Exception e1) {
			System.err.println("Couldn't execute action " + getClass().getSimpleName());
			e1.printStackTrace();
		}
	}
	
	protected abstract void doActionPerformed(ActionEvent e) throws Exception;
	
	public static void addActionObserver(OsbActionObserver obs) {
		observers.add(obs);
	}
	
	public static void removeActionObserver(OsbActionObserver obs) {
		observers.remove(obs);
	}
}
