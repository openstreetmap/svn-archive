// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import java.util.List;

import org.openstreetmap.josm.gui.preferences.SourceProvider;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public interface Module {

    String getDisplayedName();

    List<Class<? extends AbstractDataSetHandler>> getHandlers();

    List<AbstractDataSetHandler> getNewlyInstanciatedHandlers();

    SourceProvider getMapPaintStyleSourceProvider();

    SourceProvider getPresetSourceProvider();

    ModuleInformation getModuleInformation();
}
