package org.openstreetmap.josm.plugins.validator.util;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.NameVisitor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Able to create a name and an icon for a collection of elements.
 * 
 * @author frsantos
 */
public class MultipleNameVisitor extends NameVisitor 
{
	/** The class name of the combined primitives */
	String multipleClassname;
	/** Size of the collection */
	int size;
	
	/**
	 * Visits a collection of primitives 
	 * @param data The collection of primitives 
	 */
	public void visit(Collection<? extends OsmPrimitive> data) 
	{
		size = data.size();
		multipleClassname = null;
		for (OsmPrimitive osm : data) 
		{
			osm.visit(this);
			if (multipleClassname == null)
				multipleClassname = className;
			else if (!multipleClassname.equals(className))
				multipleClassname = "object";
		}
	}

	@Override
	public JLabel toLabel() 
	{
		if( size == 1 )
			return super.toLabel();
		else
			return new JLabel( size + " " + trn(multipleClassname, multipleClassname + "s", size), ImageProvider.get("data", multipleClassname), JLabel.HORIZONTAL);
	}
	
	/**
	 * Gets the name of the items
	 * @return the name of the items
	 */
	public String getText()
	{
		if( size == 1 )
			return name;
		else
			return size + " " + trn(multipleClassname, multipleClassname + "s", size);
	}

	/**
	 * Gets the icon of the items
	 * @return the icon of the items
	 */
	public Icon getIcon()
	{
		if( size == 1 )
			return icon;
		else
			return ImageProvider.get("data", multipleClassname);
	}
}
