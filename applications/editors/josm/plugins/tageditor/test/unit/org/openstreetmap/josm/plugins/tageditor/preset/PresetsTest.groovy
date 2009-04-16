package org.openstreetmap.josm.plugins.tageditor.preset

import org.junit.Test
import static org.junit.Assert.*



public class PresetsTest{

	@Test
	void Presets() {
		def presets = new Presets()
		assertNotNull presets
	}

	@Test
	void loadPresets_URL(){
		def url = new URL("file:///C:\\data\\projekte\\eclipse-3.4.1-ws\\osm-tag-editor-plugin\\test\\unit\\data\\simple-presets.xml")
		def presets = Presets.loadPresets(url)
		assertNotNull presets
		assertEquals  presets.groups.size(),1  
		Group group = presets.groups.get(0)
		assertEquals group.name, "Group1"
		assertEquals group.iconName, "presets/group1.png"
		assertEquals group.items.size(), 1
		
		Item item = group.items.get(0)
	    assertEquals item.name, "Item1"
	    assertEquals item.iconName, "presets/item1.png"
	    assertEquals item.label, "Label for Item1"
	}

}
 