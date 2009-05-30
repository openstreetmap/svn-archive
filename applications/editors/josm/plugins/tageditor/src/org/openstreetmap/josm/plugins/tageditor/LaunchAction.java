package org.openstreetmap.josm.plugins.tageditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class LaunchAction extends JosmAction implements SelectionChangedListener {

	protected void registerAsMenuItem() {
		JMenuBar menu = Main.main.menu;
		JMenu edit = null;
		JMenuItem item = new JMenuItem(this);

		for (int i = 0; i < menu.getMenuCount(); ++i) {
			if (menu.getMenu(i) != null
					&& tr("Edit").equals(menu.getMenu(i).getText())) {
				edit = menu.getMenu(i);
				break;
			}
		}

		if (edit != null) {
			edit.insertSeparator(edit.getItemCount());
			JMenuItem mitem = edit.insert(this, edit.getItemCount());
			mitem.setAccelerator(KeyStroke.getKeyStroke('T'));
		} else if (menu.getMenuCount() > 0) {
			edit = menu.getMenu(0);
			JMenuItem mitem = edit.insert(this, 0);
			mitem.setAccelerator(KeyStroke.getKeyStroke('T'));

		}

		item.setVisible(true);
	}

	public LaunchAction()  {
		super(
				tr("Edit tags"),
				null, //TODO: set "tag-editor" and add /images/tag-editor.png to distrib
				tr("Launches the tag editor dialog"),
				Shortcut.registerShortcut(
						"edit:launchtageditor",
						tr("Launches the tag editor dialog"),
						KeyEvent.VK_T,
						Shortcut.GROUP_EDIT),
						false // don't register, plugin will add the action to the menu
		);


		// register as dataset selection listener
		//
		DataSet.selListeners.add(this);

		// insert a menu item
		//
		registerAsMenuItem();

		// initially not enabled; becomes enabled when the selection becomes non-empty
		//
		setEnabled(false);

	}

	/**
	 * 
	 * @return  the top window of the JOSM GUI; can be null
	 */
	protected Window getTopWindow() {
		if (Main.contentPane == null)
			return null;
		Component c = Main.contentPane;
		while(c.getParent() != null) {
			c = c.getParent();
		}
		if (c instanceof Window)
			return (Window)c;
		else
			return null;
	}

	/**
	 * tries to center the tag editor dialog on the top window or, alternatively,
	 * on the screen
	 * 
	 * @param dialog the dialog to be placed on the screen
	 */
	protected void placeDialogOnScreen(TagEditorDialog dialog) {
		Window w = getTopWindow();
		if (w == null)
			// don't center
			return;

		GraphicsConfiguration gc = w.getGraphicsConfiguration();
		Rectangle screenBounds = null;
		if (gc != null) {
			screenBounds = gc.getBounds();
		}
		Rectangle winBounds = w.getBounds();
		Dimension d = dialog.getPreferredSize();

		Point p = new Point();
		if (d.width <= winBounds.width && d.height <= winBounds.height) {
			p.x = winBounds.x + ((winBounds.width - d.width)/2 );
			p.y = winBounds.y + ((winBounds.height - d.height)/2 );
		} else  {
			p.x = screenBounds.x + ((screenBounds.width - d.width)/2 );
			p.y = screenBounds.y + ((screenBounds.height - d.height)/2 );
		}

		dialog.setLocation(p);

	}

	/**
	 * launch the editor
	 */
	protected void launchEditor() {
		if (!isEnabled())
			return;
		TagEditorDialog dialog = TagEditorDialog.getInstance();
		placeDialogOnScreen(dialog);
		dialog.startEditSession();
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		launchEditor();
	}

	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		setEnabled(newSelection != null && newSelection.size() >0);
	}




}
