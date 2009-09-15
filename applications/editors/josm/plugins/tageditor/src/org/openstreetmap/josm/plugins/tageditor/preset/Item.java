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


public class Item  implements INameIconProvider {

	private static Logger logger = Logger.getLogger(Item.class.getName());

	private String name;
	private String iconName;
	private ImageIcon icon;
	private String label;
	private List<Tag> tags;
	private Group parent;

	public Item() {
		tags = new ArrayList<Tag>();
	}

	public Group getParent() {
		return parent;
	}

	public void setParent(Group parent) {
		this.parent = parent;
	}


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public Item(String name) {
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
				icon = null;
			} else {
				icon =new ImageIcon(url);
			}
			Image i = icon.getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT);
			icon = new ImageIcon(i);

		}
		return icon;
	}

	public void addTag(Tag tag) {
		tags.add(tag);
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void dump(IndentWriter writer) throws IOException {
		writer.indent();
		writer.write("<item ");
		writer.write(String.format("name=\"%s\" ", name));
		writer.write(String.format("label=\"%s\" ", label));
		writer.write(String.format("iconName=\"%s\" ", iconName));
		writer.write(">");
		writer.write("\n");
		writer.incLevel();
		for(Tag tag: tags) {
			tag.dump(writer);
		}
		writer.decLevel();
		writer.writeLine("</item>");
	}

	@Override
	public String toString() {
		StringBuilder builder  = new StringBuilder();
		builder.append("[")
		.append(getClass().getName())
		.append(":")
		.append("name=")
		.append(name)
		.append("]");

		return builder.toString();
	}
}
