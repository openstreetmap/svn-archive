package org.openstreetmap.josm.plugins.piclayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;

enum PicActions {MOVE_PICTURE, MOVE_POINT, TRANSFORM_POINT, SCALEX, SCALEY, SCALEXY, SHEAR, ROTATE}

@SuppressWarnings("serial")
public class ActionVisibilityChangeMenu extends JMenu {
	public ActionVisibilityChangeMenu() {
		super(tr("Change visibility of controls"));

		add(new SwitchVisibilityMenuItem("Move Picture", "piclayer.actionvisibility.move", PicLayerPlugin.movePictureButton, true));
		add(new SwitchVisibilityMenuItem("Move Point", "piclayer.actionvisibility.movepoint", PicLayerPlugin.movePointButton, true));
		add(new SwitchVisibilityMenuItem("Transform Point", "piclayer.actionvisibility.transformpoint", PicLayerPlugin.transformPointButton, true));
		add(new SwitchVisibilityMenuItem("Rotate", "piclayer.actionvisibility.rotate", PicLayerPlugin.rotatePictureButton, false));
		add(new SwitchVisibilityMenuItem("Scale X", "piclayer.actionvisibility.scalex", PicLayerPlugin.scalexPictureButton, false));
		add(new SwitchVisibilityMenuItem("Scale Y", "piclayer.actionvisibility.scaley", PicLayerPlugin.scaleyPictureButton, false));
		add(new SwitchVisibilityMenuItem("Scale", "piclayer.actionvisibility.scale", PicLayerPlugin.scalexyPictureButton, false));
		add(new SwitchVisibilityMenuItem("Shear", "piclayer.actionvisibility.shear", PicLayerPlugin.shearPictureButton, false));
	}
}

@SuppressWarnings("serial")
class SwitchVisibilityMenuItem extends JCheckBoxMenuItem {
	public SwitchVisibilityMenuItem(String name, final String key, final IconToggleButton button, final boolean def) {
		super();
		setSelected(Main.pref.getBoolean(key, def));
		button.setVisible(isSelected());
		setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean val = !Main.pref.getBoolean(key, def);
				Main.pref.put(key, val);
				SwitchVisibilityMenuItem.this.setSelected(val);
				button.setVisible(val);
			}
		});
		setText(name);
	}
}