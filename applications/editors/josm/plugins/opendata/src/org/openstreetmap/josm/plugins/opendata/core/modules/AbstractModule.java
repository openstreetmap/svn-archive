//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.gui.preferences.SourceEditor.ExtendedSourceEntry;
import org.openstreetmap.josm.gui.preferences.SourceEntry;
import org.openstreetmap.josm.gui.preferences.SourceProvider;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public abstract class AbstractModule implements Module, OdConstants {

	protected final List<Class<? extends AbstractDataSetHandler>> handlers = new ArrayList<Class <? extends AbstractDataSetHandler>>();

	private final List<AbstractDataSetHandler> instanciatedHandlers = new ArrayList<AbstractDataSetHandler>();

	protected final ModuleInformation info;
	
	public AbstractModule(ModuleInformation info) {
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.modules.Module#getModuleInformation()
	 */
	@Override
	public ModuleInformation getModuleInformation() {
		return info;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.modules.Module#getHandlers()
	 */
	@Override
	public List<Class<? extends AbstractDataSetHandler>> getHandlers() {
		return handlers;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.modules.Module#getDisplayedName()
	 */
	@Override
	public String getDisplayedName() {
		return info.name;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.modules.Module#getMapPaintStyleSourceProvider()
	 */
	@Override
	public SourceProvider getMapPaintStyleSourceProvider() {
		final List<SourceEntry> sources = new ArrayList<SourceEntry>();
		for (AbstractDataSetHandler handler : getInstanciatedHandlers()) {
			ExtendedSourceEntry src;
			if (handler != null && (src = handler.getMapPaintStyle()) != null) {
				try {
					// Copy style sheet to disk to allow JOSM to load it at startup (even making the plugin "early" does not allow it)
					String path = OdPlugin.getInstance().getResourcesDirectory()+File.separator+src.url.replace(PROTO_RSRC, "").replace('/', File.separatorChar);
					
					int n = 0;
					byte[] buffer = new byte[4096];
					InputStream in = getClass().getResourceAsStream(src.url.substring(PROTO_RSRC.length()-1));
					new File(path.substring(0, path.lastIndexOf(File.separatorChar))).mkdirs();
					FileOutputStream out = new FileOutputStream(path);
					while ((n = in.read(buffer)) > 0) {
						out.write(buffer, 0, n);
					}
					out.close();
					in.close();

					// Add source pointing to the local file
					src.url = PROTO_FILE+path;
					sources.add(src);
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return sources.isEmpty() ? null : new SourceProvider() {
			@Override
			public Collection<SourceEntry> getSources() {
				return sources;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.modules.Module#getPresetSourceProvider()
	 */
	@Override
	public SourceProvider getPresetSourceProvider() {
		final List<SourceEntry> sources = new ArrayList<SourceEntry>();
		for (AbstractDataSetHandler handler : getInstanciatedHandlers()) {
			if (handler != null && handler.getTaggingPreset() != null) {
				sources.add(handler.getTaggingPreset());
			}
		}
		return sources.isEmpty() ? null : new SourceProvider() {
			@Override
			public Collection<SourceEntry> getSources() {
				return sources;
			}
		};
	}
	
	@Override
	public final List<AbstractDataSetHandler> getNewlyInstanciatedHandlers() {
		List<AbstractDataSetHandler> result = new ArrayList<AbstractDataSetHandler>();
		for (Class<? extends AbstractDataSetHandler> handlerClass : handlers) {
			if (handlerClass != null) {
				try {
					result.add(handlerClass.newInstance());
				} catch (InstantiationException e) {
					System.err.println(e.getMessage());
				} catch (IllegalAccessException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return result;
	}

	private final List<AbstractDataSetHandler> getInstanciatedHandlers() {
		if (instanciatedHandlers.isEmpty()) {
			instanciatedHandlers.addAll(getNewlyInstanciatedHandlers());
		}
		return instanciatedHandlers;
	}
}
