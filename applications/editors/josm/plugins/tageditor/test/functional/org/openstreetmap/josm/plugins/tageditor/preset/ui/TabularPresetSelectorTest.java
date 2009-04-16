package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;
import org.openstreetmap.josm.plugins.tageditor.preset.Presets;
import org.openstreetmap.josm.plugins.tageditor.preset.io.PresetIOException;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.IPresetSelectorListener;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.TabularPresetSelector;


public class TabularPresetSelectorTest extends JFrame implements IPresetSelectorListener {

	private static Logger logger = Logger.getLogger(TabularPresetSelectorTest.class.getName());
	protected void build() {
		setSize(600,600);
		getContentPane().setLayout(new BorderLayout());
		final TabularPresetSelector selector = new TabularPresetSelector();
		getContentPane().add(selector, BorderLayout.CENTER);
		
		selector.addPresetSelectorListener(this);
	}
	
	public TabularPresetSelectorTest() {
		build();
	}
	

	
	@Override
	public void itemSelected(Item item) {
		logger.info("preset item selected: " + item);
	}
 
	static public void main(String args[]) {
		new TabularPresetSelectorTest().setVisible(true);
	}
}
 