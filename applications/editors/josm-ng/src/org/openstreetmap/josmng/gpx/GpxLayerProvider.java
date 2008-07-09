/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.gpx;

import java.io.IOException;
import org.openstreetmap.josmng.osm.io.NamedStream;
import org.openstreetmap.josmng.utils.Convertor;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.view.Layer;

/**
 * A convertor providing GPX layer from .gpx files.
 * 
 * @author nenik
 */
public class GpxLayerProvider extends Convertor<NamedStream,Layer> {
    public GpxLayerProvider() {
        super(NamedStream.class, Layer.class);
    }
    
    public @Override boolean accept(NamedStream source) {
        return source.getName().endsWith(".gpx");
    }

    public @Override Layer convert(NamedStream from) {
        try {
            return new GpxLayer(Main.main.getMapView(), from.getName(), from.openStream());
        } catch (IOException ex) {
            return null;
        }
    }

}
