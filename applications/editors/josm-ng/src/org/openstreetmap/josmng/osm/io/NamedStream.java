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

import java.io.IOException;
import java.io.InputStream;

/**
 * A stream provider interface which can pass along (sometimes virtual)
 * file name of the stream. It is used as an intermediate conversion product
 * to allow file-extension based recognition and delay the stream creation.
 * 
 * @author nenik
 */
public abstract class NamedStream {
    public abstract String getName();
    public abstract InputStream openStream() throws IOException;
    public abstract long sizeEstimate();
}
