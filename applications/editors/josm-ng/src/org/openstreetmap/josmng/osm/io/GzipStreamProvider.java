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

package org.openstreetmap.josmng.osm.io;

import org.openstreetmap.josmng.utils.Convertor;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * A gzip decompression filter.
 * 
 * @author nenik
 */
public class GzipStreamProvider extends Convertor<NamedStream,NamedStream> {

    private static final String EXT = ".gz";
    
    public GzipStreamProvider() {
        super(NamedStream.class, NamedStream.class);
    }

    public @Override boolean accept(NamedStream source) {
        return source.getName().endsWith(EXT);
    }

    public @Override NamedStream convert(final NamedStream from) {
        return new NamedStream() {
            public @Override String getName() {
                String origName = from.getName();
                return origName.substring(0, origName.length()-EXT.length());
            }

            public @Override InputStream openStream() throws IOException {
                return new GZIPInputStream(from.openStream(), 32768);
            }

            public @Override long sizeEstimate() {
                return from.sizeEstimate() * 10;
            }
        };
    }
}
