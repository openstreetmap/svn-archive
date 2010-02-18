package org.openstreetmap.josm.plugins.tageditor;

import static org.openstreetmap.josm.tools.I18n.tr;

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

		DataSet.selListeners.add(this);
		registerAsMenuItem();
		setEnabled(false);
	}

	/**
	 * launch the editor
	 */
	protected void launchEditor() {
		if (!isEnabled())
			return;
		TagEditorDialog dialog = TagEditorDialog.getInstance();
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
