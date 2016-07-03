// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.SourceEditor.ExtendedSourceEntry;
import org.openstreetmap.josm.gui.preferences.SourceEntry;
import org.openstreetmap.josm.gui.preferences.SourceProvider;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public abstract class AbstractModule implements Module {

    protected final List<Class<? extends AbstractDataSetHandler>> handlers = new ArrayList<>();

    private final List<AbstractDataSetHandler> instanciatedHandlers = new ArrayList<>();

    protected final ModuleInformation info;

    public AbstractModule(ModuleInformation info) {
        this.info = info;
    }

    @Override
    public ModuleInformation getModuleInformation() {
        return info;
    }

    @Override
    public List<Class<? extends AbstractDataSetHandler>> getHandlers() {
        return handlers;
    }

    @Override
    public String getDisplayedName() {
        return info.name;
    }

    @Override
    public SourceProvider getMapPaintStyleSourceProvider() {
        final List<SourceEntry> sources = new ArrayList<>();
        for (AbstractDataSetHandler handler : getInstanciatedHandlers()) {
            ExtendedSourceEntry src;
            if (handler != null && (src = handler.getMapPaintStyle()) != null) {
                // Copy style sheet to disk to allow JOSM to load it at startup
                // (even making the plugin "early" does not allow it)
                String path = OdPlugin.getInstance().getResourcesDirectory() + File.separator
                        + src.url.replace(OdConstants.PROTO_RSRC, "").replace('/', File.separatorChar);

                int n = 0;
                byte[] buffer = new byte[4096];
                try (InputStream in = getClass().getResourceAsStream(
                        src.url.substring(OdConstants.PROTO_RSRC.length()-1));
                     FileOutputStream out = new FileOutputStream(path)) {
                    String dir = path.substring(0, path.lastIndexOf(File.separatorChar));
                    if (new File(dir).mkdirs() && Main.isDebugEnabled()) {
                        Main.debug("Created directory: "+dir);
                    }
                    while ((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    // Add source pointing to the local file
                    src.url = OdConstants.PROTO_FILE+path;
                    sources.add(src);
                } catch (IOException e) {
                    Main.error(e.getMessage());
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

    @Override
    public SourceProvider getPresetSourceProvider() {
        final List<SourceEntry> sources = new ArrayList<>();
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
        List<AbstractDataSetHandler> result = new ArrayList<>();
        for (Class<? extends AbstractDataSetHandler> handlerClass : handlers) {
            if (handlerClass != null) {
                try {
                    result.add(handlerClass.newInstance());
                } catch (InstantiationException | IllegalAccessException t) {
                    Main.error("Cannot instantiate "+handlerClass+" because of "+t.getClass().getName()+": "+t.getMessage());
                }
            }
        }
        return result;
    }

    private List<AbstractDataSetHandler> getInstanciatedHandlers() {
        if (instanciatedHandlers.isEmpty()) {
            instanciatedHandlers.addAll(getNewlyInstanciatedHandlers());
        }
        return instanciatedHandlers;
    }
}
