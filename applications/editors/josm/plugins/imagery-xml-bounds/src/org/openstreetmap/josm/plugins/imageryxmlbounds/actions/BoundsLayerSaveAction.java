// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;

import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerSaveAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 *
 * @author Don-vip
 *
 */
@SuppressWarnings("serial")
public class BoundsLayerSaveAction extends LayerSaveAction {

	public static class SaveBoundsAction extends SaveActionBase {

		public SaveBoundsAction() {
	        super(tr("Save"), "save", tr("Save the current data."), null);
	        putValue("help", ht("/Action/Save"));
		}

		@Override public File getFile(Layer layer) {
	        File f = layer.getAssociatedFile();
	        if (f != null && ! f.exists()) {
	            f = null;
	        }
	        return f == null ? BoundsLayerSaveAsAction.SaveBoundsAsAction.openFileDialog(layer) : f;
	    }
	}

	protected XmlBoundsLayer layer;

	public BoundsLayerSaveAction(XmlBoundsLayer layer) {
		super(layer);
	}

    @Override
	public void actionPerformed(ActionEvent e) {
        new SaveBoundsAction().doSave(layer);
    }
}
