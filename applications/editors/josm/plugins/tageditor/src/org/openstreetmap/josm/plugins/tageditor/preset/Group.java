package org.openstreetmap.josm.plugins.tageditor.preset;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.tageditor.util.IndentWriter;

/**
 * Group represents a named group of preset items. Groups can be nested.
 * 
 *
 */
public class Group implements INameIconProvider {
	
	static private Logger logger = Logger.getLogger(Group.class.getName());
	
	private String name;
	private String iconName;
	private ImageIcon icon;
	private List<Item> items = null;
	
	public Group() {
		items = new ArrayList<Item>();
	}
	
	public Group(String name) {
		this();
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	
	public Icon getIcon() {
		if (icon == null) {
			// load the icon from the JOSM resources, use Main classloader
			// for loading 
			URL url = Main.class.getResource("/images/" + getIconName());
			if (url == null) {
				logger.warning("failed to create URL for resource 'images/" + getIconName() + "'");
				this.icon = null;
			} else {
				icon =new ImageIcon(url);
				Image i = icon.getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT);
				icon = new ImageIcon(i);

			}
		}
		return icon;
	}
	
	public void addItem(Item item) {
		item.setParent(this);
		items.add(item);
	}
	
	public void removeItem(Item item) {
		items.remove(item);
	}
	
	public List<Item> getItems() {
		return items; 
	}
	
	public void dump(IndentWriter writer) throws IOException {
		writer.indent();
		writer.write("<group ");
		writer.write(String.format("name=\"%s\" ", name));
		writer.write(String.format("iconName=\"%s\" ", iconName));
		writer.write(">");
		writer.write("\n");
		writer.incLevel();
		for(Item item: items) {
			item.dump(writer);
		}
		writer.decLevel();
		writer.writeLine("</group>");
	} 
}
