// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBuilder;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

public class ImageReloadAction extends AbstractAction {

	private static final long serialVersionUID = 7987479726049238315L;

	public ImageReloadAction(final String name) {
		 super(name, ImageProvider.get("reload", ImageSizes.SMALLICON));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(StreetsideMainDialog.getInstance().getImage()!=null){
			CubemapBuilder.getInstance().reload(CubemapBuilder.getInstance().getCubemap().getId());
		}
	}
}