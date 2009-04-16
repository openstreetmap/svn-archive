package org.openstreetmap.josm.plugins.tageditor.preset;

import static org.junit.Assert.*;

import javax.swing.Icon;

import org.junit.Test;
import org.openstreetmap.josm.plugins.tageditor.preset.Group;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;

import junit.framework.TestCase;


public class ItemTest  {
	
	
	@Test
	public void constructor() {
		Item item = new Item();
		assertEquals(item.getTags().size(),0);
	}
	
	@Test
	public void accessorsParent() {
		Item item = new Item();
		Group group = new Group();
		item.setParent(group);
		assertEquals(item.getParent(),group);
	}
	
	@Test
	public void accessorsLabel() {
		Item item = new Item();
		assertNull(item.getLabel());
		
		item.setLabel("alabel");
		assertEquals(item.getLabel(), "alabel");
	}
	
	@Test
	public void accessorsName() {
		Item item = new Item();
		assertNull(item.getName());

		
		item.setName("aname");
		assertEquals(item.getName(), "aname");
	}

	@Test
	public void getIcon() {
		Item item = new Item();
		item.setIconName("presets/motorway.png");
		Icon icon = item.getIcon();
		
		assertNotNull(icon);

	}

}
