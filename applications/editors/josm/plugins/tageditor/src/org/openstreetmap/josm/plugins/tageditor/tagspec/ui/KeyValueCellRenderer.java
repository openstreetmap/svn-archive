package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class KeyValueCellRenderer extends JLabel implements TableCellRenderer  {

	private static Logger logger = Logger.getLogger(KeyValueCellRenderer.class.getName());
	public static final Color BG_COLOR_SELECTED = new Color(143,170,255);


	protected void init() {
		setFont(new Font("Courier",Font.PLAIN,12));
		setOpaque(true);
	}

	public KeyValueCellRenderer() {
		init();
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {

		if (isSelected) {
			setBackground(BG_COLOR_SELECTED);
		} else  {
			setBackground(Color.WHITE);
		}
		setText((String)value);
		setIcon(null);
		return this;
	}
}
