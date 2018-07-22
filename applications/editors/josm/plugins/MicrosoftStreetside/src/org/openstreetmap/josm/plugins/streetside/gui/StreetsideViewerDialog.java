// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerPanel;

/**
 * Toggle dialog that shows an image and some buttons.
 *
 * @author nokutu
 */

public final class StreetsideViewerDialog extends ToggleDialog
		 {

	private static final long serialVersionUID = -8983900297628236197L;

	private static final String BASE_TITLE = "360° Streetside Viewer";

	private static StreetsideViewerDialog instance;

	/**
	 * Object containing the shown image and that handles zoom and drag
	 */
	private StreetsideViewerPanel streetsideViewerPanel;

	private StreetsideViewerDialog() {
	  super(StreetsideViewerDialog.BASE_TITLE, "streetside-viewer", "Open Streetside Viewer window",
				null, 200, true, StreetsidePreferenceSetting.class);
		streetsideViewerPanel = new StreetsideViewerPanel();
		createLayout(streetsideViewerPanel, true, null);
	}

	/**
	 * Returns the unique instance of the class.
	 *
	 * @return The unique instance of the class.
	 */
	public static synchronized StreetsideViewerDialog getInstance() {
		if (StreetsideViewerDialog.instance == null) {
			StreetsideViewerDialog.instance = new StreetsideViewerDialog();
		}
		return StreetsideViewerDialog.instance;
	}

	/**
	 * @return true, iff the singleton instance is present
	 */
	public static boolean hasInstance() {
		return StreetsideViewerDialog.instance != null;
	}

	/**
	 * Destroys the unique instance of the class.
	 */
	public static synchronized void destroyInstance() {
		StreetsideViewerDialog.instance = null;
	}

	/**
	 * Creates the layout of the dialog.
	 *
	 * @param data
	 *            The content of the dialog
	 * @param buttons
	 *            The buttons where you can click
	 */
	public void createLayout(Component data, List<SideButton> buttons) {
		removeAll();
		createLayout(data, true, buttons);
		add(titleBar, BorderLayout.NORTH);
	}

	public StreetsideViewerPanel getStreetsideViewerPanel() {
		return streetsideViewerPanel;
	}
}