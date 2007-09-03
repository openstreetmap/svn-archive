// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.BookmarkList;
import org.openstreetmap.josm.tools.GBC;

/**
 * Bookmark selector.
 * 
 * Provides selection, creation and deletion of bookmarks.
 * Extracted from old DownloadAction.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class BookmarkSelection implements DownloadSelection {

	private Preferences.Bookmark tempBookmark= null;
	private BookmarkList bookmarks; 
	
	public void addGui(final DownloadDialog gui) {
		
		JPanel dlg = new JPanel(new GridBagLayout());
		gui.tabpane.addTab("Bookmarks", dlg);

		bookmarks = new BookmarkList();
		bookmarks.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Preferences.Bookmark b = (Preferences.Bookmark)bookmarks.getSelectedValue();
				if (b != null) {
					gui.minlat = b.latlon[0];
					gui.minlon = b.latlon[1];
					gui.maxlat = b.latlon[2];
					gui.maxlon = b.latlon[3];
					gui.boundingBoxChanged(BookmarkSelection.this);
				}
			}
		});
		//wc.addListMarker(bookmarks);
		dlg.add(new JScrollPane(bookmarks), GBC.eol().fill());

		JPanel buttons = new JPanel(new GridLayout(1,2));
		JButton add = new JButton(tr("Add"));
		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				if (tempBookmark == null) {
					JOptionPane.showMessageDialog(Main.parent, tr("Please enter the desired coordinates first."));
					return;
				}
				tempBookmark.name = JOptionPane.showInputDialog(Main.parent,tr("Please enter a name for the location."));
				if (tempBookmark.name != null && !tempBookmark.name.equals("")) {
					((DefaultListModel)bookmarks.getModel()).addElement(tempBookmark);
					bookmarks.save();
				}
			}
		});
		buttons.add(add);
		JButton remove = new JButton(tr("Remove"));
		remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel = bookmarks.getSelectedValue();
				if (sel == null) {
					JOptionPane.showMessageDialog(Main.parent,tr("Select a bookmark first."));
					return;
				}
				((DefaultListModel)bookmarks.getModel()).removeElement(sel);
				bookmarks.save();
			}
		});
		buttons.add(remove);
		dlg.add(buttons, GBC.eop().fill(GBC.HORIZONTAL));
	}		

	public void boundingBoxChanged(DownloadDialog gui) {
		tempBookmark = new Preferences.Bookmark();
		tempBookmark.latlon[0] = gui.minlat;
		tempBookmark.latlon[1] = gui.minlon;
		tempBookmark.latlon[2] = gui.maxlat;
		tempBookmark.latlon[3] = gui.maxlon;
		bookmarks.clearSelection();
	}


}
