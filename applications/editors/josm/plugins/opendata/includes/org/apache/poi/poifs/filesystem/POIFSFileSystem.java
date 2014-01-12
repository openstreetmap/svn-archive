
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.Property;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.storage.BlockAllocationTableReader;
import org.apache.poi.poifs.storage.BlockList;
import org.apache.poi.poifs.storage.HeaderBlockReader;
import org.apache.poi.poifs.storage.RawDataBlockList;
import org.apache.poi.poifs.storage.SmallBlockTableReader;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This is the main class of the POIFS system; it manages the entire
 * life cycle of the filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class POIFSFileSystem
{
	private static final POILogger _logger =
		POILogFactory.getLogger(POIFSFileSystem.class);

    private PropertyTable _property_table;
    private List<POIFSDocument> _documents;
    private DirectoryNode _root;

    /**
     * What big block size the file uses. Most files
     *  use 512 bytes, but a few use 4096
     */
    private POIFSBigBlockSize bigBlockSize = 
       POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;

    /**
     * Constructor, intended for writing
     */
    public POIFSFileSystem()
    {
        _property_table = new PropertyTable();
        _documents      = new ArrayList<POIFSDocument>();
        _root           = null;
    }

    /**
     * Create a POIFSFileSystem from an <tt>InputStream</tt>.  Normally the stream is read until
     * EOF.  The stream is always closed.<p/>
     *
     * Some streams are usable after reaching EOF (typically those that return <code>true</code>
     * for <tt>markSupported()</tt>).  In the unlikely case that the caller has such a stream
     * <i>and</i> needs to use it after this constructor completes, a work around is to wrap the
     * stream in order to trap the <tt>close()</tt> call.  A convenience method (
     * <tt>createNonClosingInputStream()</tt>) has been provided for this purpose:
     * <pre>
     * InputStream wrappedStream = POIFSFileSystem.createNonClosingInputStream(is);
     * HSSFWorkbook wb = new HSSFWorkbook(wrappedStream);
     * is.reset();
     * doSomethingElse(is);
     * </pre>
     * Note also the special case of <tt>ByteArrayInputStream</tt> for which the <tt>close()</tt>
     * method does nothing.
     * <pre>
     * ByteArrayInputStream bais = ...
     * HSSFWorkbook wb = new HSSFWorkbook(bais); // calls bais.close() !
     * bais.reset(); // no problem
     * doSomethingElse(bais);
     * </pre>
     *
     * @param stream the InputStream from which to read the data
     *
     * @exception IOException on errors reading, or on invalid data
     */

    public POIFSFileSystem(InputStream stream)
        throws IOException
    {
        this();
        boolean success = false;

        HeaderBlockReader header_block_reader;
        RawDataBlockList data_blocks;
        try {
            // read the header block from the stream
            header_block_reader = new HeaderBlockReader(stream);
            bigBlockSize = header_block_reader.getBigBlockSize();

            // read the rest of the stream into blocks
            data_blocks = new RawDataBlockList(stream, bigBlockSize);
            success = true;
        } finally {
            closeInputStream(stream, success);
        }


        // set up the block allocation table (necessary for the
        // data_blocks to be manageable
        new BlockAllocationTableReader(header_block_reader.getBigBlockSize(),
                                       header_block_reader.getBATCount(),
                                       header_block_reader.getBATArray(),
                                       header_block_reader.getXBATCount(),
                                       header_block_reader.getXBATIndex(),
                                       data_blocks);

        // get property table from the document
        PropertyTable properties =
            new PropertyTable(
                              header_block_reader.getPropertyStart(),
                              data_blocks);

        // init documents
        processProperties(
        		SmallBlockTableReader.getSmallDocumentBlocks(
        		      bigBlockSize, data_blocks, properties.getRoot(),
        				header_block_reader.getSBATStart()
        		),
        		data_blocks,
        		properties.getRoot().getChildren(),
        		null,
        		header_block_reader.getPropertyStart()
        );

        // For whatever reason CLSID of root is always 0.
        getRoot().setStorageClsid(properties.getRoot().getStorageClsid());
    }
    /**
     * @param stream the stream to be closed
     * @param success <code>false</code> if an exception is currently being thrown in the calling method
     */
    private void closeInputStream(InputStream stream, boolean success) {

        if(stream.markSupported() && !(stream instanceof ByteArrayInputStream)) {
            String msg = "POIFS is closing the supplied input stream of type ("
                    + stream.getClass().getName() + ") which supports mark/reset.  "
                    + "This will be a problem for the caller if the stream will still be used.  "
                    + "If that is the case the caller should wrap the input stream to avoid this close logic.  "
                    + "This warning is only temporary and will not be present in future versions of POI.";
            _logger.log(POILogger.WARN, msg);
        }
        try {
            stream.close();
        } catch (IOException e) {
            if(success) {
                throw new RuntimeException(e);
            }
            // else not success? Try block did not complete normally
            // just print stack trace and leave original ex to be thrown
            e.printStackTrace();
        }
    }

    /**
     * get the root entry
     *
     * @return the root entry
     */

    public DirectoryNode getRoot()
    {
        if (_root == null)
        {
            _root = new DirectoryNode(_property_table.getRoot(), this, null);
        }
        return _root;
    }


    /**
     * add a new POIFSDocument
     *
     * @param document the POIFSDocument being added
     */

    void addDocument(final POIFSDocument document)
    {
        _documents.add(document);
        _property_table.addProperty(document.getDocumentProperty());
    }

    /**
     * add a new DirectoryProperty
     *
     * @param directory the DirectoryProperty being added
     */

    void addDirectory(final DirectoryProperty directory)
    {
        _property_table.addProperty(directory);
    }

    /**
     * remove an entry
     *
     * @param entry to be removed
     */

    void remove(EntryNode entry)
    {
        _property_table.removeProperty(entry.getProperty());
        if (entry.isDocumentEntry())
        {
            _documents.remove((( DocumentNode ) entry).getDocument());
        }
    }

    private void processProperties(final BlockList small_blocks,
                                   final BlockList big_blocks,
                                   final Iterator<Property> properties,
                                   final DirectoryNode dir,
                                   final int headerPropertiesStartAt)
        throws IOException
    {
        while (properties.hasNext())
        {
            Property      property = properties.next();
            String        name     = property.getName();
            DirectoryNode parent   = (dir == null)
                                     ? (( DirectoryNode ) getRoot())
                                     : dir;

            if (property.isDirectory())
            {
                DirectoryNode new_dir =
                    ( DirectoryNode ) parent.createDirectory(name);

                new_dir.setStorageClsid( property.getStorageClsid() );

                processProperties(
                    small_blocks, big_blocks,
                    (( DirectoryProperty ) property).getChildren(),
                    new_dir, headerPropertiesStartAt);
            }
            else
            {
                int           startBlock = property.getStartBlock();
                int           size       = property.getSize();
                POIFSDocument document   = null;

                if (property.shouldUseSmallBlocks())
                {
                    document =
                        new POIFSDocument(name,
                                          small_blocks.fetchBlocks(startBlock, headerPropertiesStartAt),
                                          size);
                }
                else
                {
                    document =
                        new POIFSDocument(name,
                                          big_blocks.fetchBlocks(startBlock, headerPropertiesStartAt),
                                          size);
                }
                parent.createDocument(document);
            }
        }
    }
}   // end public class POIFSFileSystem

