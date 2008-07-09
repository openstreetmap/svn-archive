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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream provider for files.
 * 
 * @author nenik
 */
public class FileStreamProvider extends Convertor<File,NamedStream> {

    public FileStreamProvider() {
        super(File.class, NamedStream.class);
    }

    public @Override boolean accept(File source) {
        return source.canRead();
    }

    public @Override NamedStream convert(final File from) {
        return new NamedStream() {
            public @Override String getName() {
                return from.getName();
            }

            public @Override InputStream openStream() throws IOException {
                return new FileInputStream(from);
            }

            public @Override long sizeEstimate() {
                return from.length();
            }
        };
    }
}
