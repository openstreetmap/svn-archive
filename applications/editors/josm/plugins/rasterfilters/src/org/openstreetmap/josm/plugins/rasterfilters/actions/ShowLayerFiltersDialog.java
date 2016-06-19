package org.openstreetmap.josm.plugins.rasterfilters.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.plugins.rasterfilters.gui.FiltersDialog;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * The action that is called when user click on 'Choose filters' button
 *
 * and sets image on that button
 *
 * @author Nipel-Crumple
 *
 */
public final class ShowLayerFiltersDialog extends AbstractAction implements LayerAction {

	private List<FiltersDialog> dialogs = new ArrayList<FiltersDialog>();

	/**
	 * Constructs a new {@code ShowLayerFiltersDialog}.
	 */
	public ShowLayerFiltersDialog() {
		putValue(NAME, tr("Filters"));
		putValue(SHORT_DESCRIPTION, tr("Choose Filter"));
		putValue(SMALL_ICON, ImageProvider.get("josm_filters_48.png"));
	}

	public void addFiltersDialog(FiltersDialog dialog) {
		dialogs.add(dialog);
	}

	public void removeFiltersDialog(FiltersDialog dialog) {
		dialogs.remove(dialog);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Layer layer = Main.getLayerManager().getActiveLayer();

		if (layer instanceof ImageryLayer) {
			for (FiltersDialog temp : dialogs) {

				if (temp.getLayer().equals(layer)) {
					try {
						temp.createAndShowGUI();
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
					break;
				}

			}
		}
	}

	public FiltersDialog getDialogByLayer(Layer layer) {
		for (FiltersDialog dialog : dialogs) {

			if (dialog.getLayer().equals(layer)) {
				return dialog;
			}

		}

		return null;
	}

	@Override
	public boolean supportLayers(List<Layer> layers) {
		return true;
	}

	@Override
	public Component createMenuComponent() {
		return new JMenuItem(this);
	}
}