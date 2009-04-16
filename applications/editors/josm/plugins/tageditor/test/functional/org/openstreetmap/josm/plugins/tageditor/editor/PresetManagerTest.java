package org.openstreetmap.josm.plugins.tageditor.editor;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.tageditor.editor.PresetManager;

public class PresetManagerTest extends JFrame {

	
	protected void build() {
		setSize(400,400);
		PresetManager manager = new PresetManager();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(manager, BorderLayout.NORTH);		
		
	}
	
	public PresetManagerTest() {
		build();
	}
	
	
	static public void main(String args[]) {
		PresetManagerTest test = new PresetManagerTest();
		test.setVisible(true);
	}
}
