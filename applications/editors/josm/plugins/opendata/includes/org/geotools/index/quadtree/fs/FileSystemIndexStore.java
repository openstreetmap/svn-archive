/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.index.quadtree.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.index.quadtree.IndexStore;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * DOCUMENT ME!
 * 
 * @author Tommaso Nolli
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/index/quadtree/fs/FileSystemIndexStore.java $
 */
public class FileSystemIndexStore implements IndexStore {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.index.quadtree");
    private File file;

    /**
     * Constructor. The byte order defaults to NEW_MSB_ORDER
     * 
     * @param file
     */
    public FileSystemIndexStore(File file) {
        this.file = file;
    }

    /**
     * Loads a quadrtee stored in a '.qix' file. <b>WARNING:</b> The resulting
     * quadtree will be immutable; if you perform an insert, an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @see org.geotools.index.quadtree.IndexStore#load()
     */
    public QuadTree load(IndexFile indexfile, boolean useMemoryMapping) throws StoreException {
        QuadTree tree = null;

        try {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Opening QuadTree "
                        + this.file.getCanonicalPath());
            }

            final FileInputStream fis = new FileInputStream(file);
            final FileChannel channel = fis.getChannel();

            IndexHeader header = new IndexHeader(channel);

            ByteOrder order = byteToOrder(header.getByteOrder());
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.order(order);
            channel.read(buf);
            buf.flip();

            tree = new QuadTree(buf.getInt(), buf.getInt(), indexfile) {
                public void insert(int recno, Envelope bounds) {
                    throw new UnsupportedOperationException(
                            "File quadtrees are immutable");
                }

                public boolean trim() {
                    return false;
                }

                public void close() throws StoreException {
                    super.close();
                    try {
                        channel.close();
                        fis.close();
                    } catch (IOException e) {
                        throw new StoreException(e);
                    }
                }
            };

            tree.setRoot(FileSystemNode.readNode(0, null, channel, order, useMemoryMapping));

            LOGGER.finest("QuadTree opened");
        } catch (IOException e) {
            throw new StoreException(e);
        }

        return tree;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param order
     * 
     */
    private static ByteOrder byteToOrder(byte order) {
        ByteOrder ret = null;

        switch (order) {
        case IndexHeader.NATIVE_ORDER:
            ret = ByteOrder.nativeOrder();

            break;

        case IndexHeader.LSB_ORDER:
        case IndexHeader.NEW_LSB_ORDER:
            ret = ByteOrder.LITTLE_ENDIAN;

            break;

        case IndexHeader.MSB_ORDER:
        case IndexHeader.NEW_MSB_ORDER:
            ret = ByteOrder.BIG_ENDIAN;

            break;
        }

        return ret;
    }
}
