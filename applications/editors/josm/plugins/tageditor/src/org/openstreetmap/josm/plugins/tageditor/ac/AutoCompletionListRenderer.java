package org.openstreetmap.josm.plugins.tageditor.ac;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * This is the table cell renderer for the list of auto completion list items.
 * 
 * It uses an instance of {@Link JLabel} to render an {@link AutoCompletionListItem}.
 * 
 *
 */
public class AutoCompletionListRenderer implements TableCellRenderer {


	static public final String RES_OSM_ICON = "/resources/osm.gif";
	static public final String RES_SELECTION_ICON = "/resources/selection.gif";
	public static final Color BG_COLOR_SELECTED = new Color(143,170,255);

	/** the renderer component */
	private final JLabel renderer;

	/** the icon used to decorate items of priority
	 *  {@link AutoCompletionItemPritority#IS_IN_STANDARD}
	 */
	private Icon iconStandard;

	/** the icon used to decorate items of priority
	 *  {@link AutoCompletionItemPritority#IS_IN_SELECTION}
	 */
	private Icon iconSelection;

	/**
	 * constructor
	 */
	public AutoCompletionListRenderer() {
		renderer = new JLabel();
		renderer.setOpaque(true);

		loadIcons();
	}

	/**
	 * loads the icons
	 */
	protected void loadIcons() {
		java.net.URL imgURL = getClass().getResource(RES_OSM_ICON);
		if (imgURL != null) {
			iconStandard = new ImageIcon(imgURL);
		} else {
			System.err.println("Could not load icon: " + RES_OSM_ICON);
			iconStandard = null;
		}

		imgURL = getClass().getResource(RES_SELECTION_ICON);
		if (imgURL != null) {
			iconSelection = new ImageIcon(imgURL);
		} else {
			System.err.println("Could not load icon: " + RES_SELECTION_ICON);
			iconSelection = null;
		}
	}


	/**
	 * prepares the renderer for rendering a specific icon
	 * 
	 * @param item the item to be rendered
	 */
	protected void prepareRendererIcon(AutoCompletionListItem item) {
		if (item.getPriority().equals(AutoCompletionItemPritority.IS_IN_STANDARD)) {
			if (iconStandard != null) {
				renderer.setIcon(iconStandard);
			}
		} else if (item.getPriority().equals(AutoCompletionItemPritority.IS_IN_SELECTION)) {
			if (iconSelection != null) {
				renderer.setIcon(iconSelection);
			}
		}
	}

	/**
	 * resets the renderer
	 */
	protected void resetRenderer() {
		renderer.setIcon(null);
		renderer.setText("");
		renderer.setOpaque(true);
		renderer.setBackground(Color.WHITE);
		renderer.setForeground(Color.BLACK);
	}

	/**
	 * prepares background and text colors for a selected item
	 */
	protected void renderSelected() {
		renderer.setBackground(BG_COLOR_SELECTED);
		renderer.setForeground(Color.WHITE);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		resetRenderer();
		// set icon and text
		//
		if (value instanceof AutoCompletionListItem) {
			AutoCompletionListItem item = (AutoCompletionListItem)value;
			prepareRendererIcon(item);
			renderer.setText(item.getValue());
		} else if (value != null) {
			renderer.setText(value.toString());
		} else {
			renderer.setText("<null>");
		}

		// prepare background and foreground for a selected item
		//
		if (isSelected) {
			renderSelected();
		}


		return renderer;

	}



}
