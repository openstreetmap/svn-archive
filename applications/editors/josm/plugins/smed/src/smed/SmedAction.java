/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package smed;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.*;

import messages.Messages;

import panels.PanelMain;

public class SmedAction extends JosmAction implements SelectionChangedListener {

	private static final long serialVersionUID = 1L;
	private static String editor = tr("SeaMap Editor");
	public static JFrame editFrame = null;
	private boolean isOpen = false;
	public static PanelMain panelMain = null;
	public DataSet data = null;

	public OsmPrimitive node = null;
	private Collection<? extends OsmPrimitive> selection = null;

	public SmedAction() {
		super(editor, "Smed", editor, null, true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen)
					createFrame();
				else
					editFrame.toFront();
				isOpen = true;
			}
		});
	}

	protected void createFrame() {
		editFrame = new JFrame(editor);
		editFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeDialog();
			}
		});
		editFrame.setSize(new Dimension(420, 420));
		editFrame.setLocation(100, 200);
		editFrame.setResizable(true);
		editFrame.setAlwaysOnTop(true);
		editFrame.setVisible(true);
		editFrame.setLayout(null);
		panelMain = new PanelMain(this);
		panelMain.setBounds(10, 10, 400, 400);
		node = null;
		panelMain.syncPanel();
		editFrame.add(panelMain);
		DataSet.addSelectionListener(this);

		// System.out.println("hello");
	}

	public void closeDialog() {
		if (isOpen) {
			editFrame.setVisible(false);
			editFrame.dispose();
			data = null;
		}
		isOpen = false;
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		OsmPrimitive nextNode = null;
		selection = newSelection;

		for (OsmPrimitive osm : selection) {
			nextNode = (OsmPrimitive) osm;
			if (selection.size() == 1) {
				if (nextNode.compareTo(node) != 0) {
					node = nextNode;
					panelMain.mark.parseMark(node);
				}
			} else {
				node = null;
				panelMain.mark.clrMark();
				PanelMain.messageBar.setText(Messages.getString("OneNode"));
			}
		}
		if (nextNode == null) {
			node = null;
			panelMain.mark.clrMark();
			PanelMain.messageBar.setText(Messages.getString("SelectNode"));
		}
	}

}
