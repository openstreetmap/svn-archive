package org.openstreetmap.josm.plugins.tageditor.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.openstreetmap.josm.plugins.tageditor.preset.Item;

public class PresetItemListCellRenderer extends JLabel implements
ListCellRenderer {

	private static final Logger logger = Logger.getLogger(PresetItemListCellRenderer.class.getName());
	private static final Font DEFAULT_FONT =  new Font("SansSerif",Font.PLAIN,10);
	public static final Color BG_COLOR_SELECTED = new Color(143,170,255);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		Item item = (Item)value;
		if (item == null) {
			setText(tr("(none)"));
			setIcon(null);
		} else {
			if (isSelected) {
				setBackground(BG_COLOR_SELECTED);
			} else {
				setBackground(Color.WHITE);
			}
			setIcon(item.getIcon());
			StringBuilder sb = new StringBuilder();
			sb.append(item.getParent().getName())
			.append("/")
			.append(item.getName());
			setText(sb.toString());
			setOpaque(true);
			setFont(DEFAULT_FONT);
		}
		return this;
	}

}
