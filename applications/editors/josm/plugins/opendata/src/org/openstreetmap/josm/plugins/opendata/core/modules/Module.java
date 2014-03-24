// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import java.util.List;

import org.openstreetmap.josm.gui.preferences.SourceProvider;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public interface Module {

	public String getDisplayedName();

	public List<Class<? extends AbstractDataSetHandler>> getHandlers();

	public List<AbstractDataSetHandler> getNewlyInstanciatedHandlers();

	public SourceProvider getMapPaintStyleSourceProvider();
	
	public SourceProvider getPresetSourceProvider();
	
	public ModuleInformation getModuleInformation();
}
