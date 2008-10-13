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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;

public class PopupFactory {
	
	private static JPopupMenu issuePopup;
	private static JPopupMenu fixedPopup;
	
	public static synchronized JPopupMenu createPopup(Node node) {
		if("0".equals(node.get("state"))) {
			return getIssuePopup();
		} else if("1".equals(node.get("state"))) {
			return getFixedPopup();
		} else {
			throw new RuntimeException("Unknown issue state");
		}
	}

	private static JPopupMenu getIssuePopup() {
		if(issuePopup == null) {
			issuePopup = new JPopupMenu();
			JMenuItem add = new JMenuItem();
			add.setAction(new AddCommentAction());
			add.setIcon(OsbPlugin.loadIcon("add_comment16.png"));
			issuePopup.add(add);
			JMenuItem close = new JMenuItem();
			close.setAction(new CloseIssueAction());
			close.setIcon(OsbPlugin.loadIcon("icon_valid16.png"));
			issuePopup.add(close);
		}
		return issuePopup;
	}
	
	private static JPopupMenu getFixedPopup() {
		if(fixedPopup == null) {
			fixedPopup = new JPopupMenu();
			JMenuItem add = new JMenuItem();
			AddCommentAction aca = new AddCommentAction();
			aca.setEnabled(false);
			add.setAction(aca);
			add.setIcon(OsbPlugin.loadIcon("add_comment16.png"));
			JMenuItem close = new JMenuItem();
			CloseIssueAction cia = new CloseIssueAction();
			cia.setEnabled(false);
			close.setAction(cia);
			close.setIcon(OsbPlugin.loadIcon("icon_valid16.png"));
			fixedPopup.add(add);
			fixedPopup.add(close);
		}
		return fixedPopup;
	}
}
