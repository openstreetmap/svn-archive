/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShpFileType.DBF;
import static org.geotools.data.shapefile.ShpFileType.PRJ;
import static org.geotools.data.shapefile.ShpFileType.SHP;
import static org.geotools.data.shapefile.ShpFileType.SHP_XML;
import static org.geotools.data.shapefile.ShpFileType.SHX;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;

import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.indexed.ShapeFIDReader;
import org.geotools.data.shapefile.prj.PrjFileReader;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.xml.ShpXmlFileReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.Classes;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A DataStore implementation which allows reading and writing from Shapefiles.
 * 
 * @author Ian Schneider
 * 
 * @todo fix file creation bug
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/ShapefileDataStore.java $
 */
public class ShapefileDataStore extends AbstractFileDataStore {
        
    // User-data keyword names for storing the original field name and its
    // instance number when the field name is replaced with a new name built
    // using the original+count convention.
    public static final String ORIGINAL_FIELD_NAME = "original";
    public static final String ORIGINAL_FIELD_DUPLICITY_COUNT = "count";
    
    /**
     * When true, the stack trace that got a lock that wasn't released is recorded and then
     * printed out when warning the user about this.
     */
    protected static final Boolean TRACE_ENABLED = "true".equalsIgnoreCase(System.getProperty("gt2.shapefile.trace"));

    protected ShpFiles shpFiles;
    protected URI namespace = null; // namespace provided by the constructor's
    // map
    protected SimpleFeatureType schema; // read only
    protected boolean useMemoryMappedBuffer = false; // windows is not up to use memory mapping in anger
    protected Charset dbfCharset;
    protected TimeZone dbfTimeZone = TimeZone.getDefault();
    
    private ServiceInfo info;

    Exception trace;
    
	/**
     * this sets the datastore's namespace during construction (so the schema -
     * FeatureType - will have the correct value) You can call this with
     * namespace = null, but I suggest you give it an actual namespace.
     * 
     * @param url
     * @param namespace
     * @param useMemoryMapped
     * @param dbfCharset
     */
    public ShapefileDataStore(URL url, URI namespace, boolean useMemoryMapped,
            boolean cacheMemoryMap, Charset dbfCharset) {
        this.namespace = namespace;
        shpFiles = new ShpFiles(url);
        if (!isLocal() || !shpFiles.exists(SHP)) {
            this.useMemoryMappedBuffer = false;
        } else {
            this.useMemoryMappedBuffer = useMemoryMapped;
        }
        shpFiles.setMemoryMapCacheEnabled(this.useMemoryMappedBuffer && cacheMemoryMap);
        this.dbfCharset = dbfCharset;
        if(TRACE_ENABLED) {
        	trace = new Exception();
        	trace.fillInStackTrace();
        }
    }

    /**
     * Access a ServiceInfo object for this shapefile.
     * 
     * @return ShapefileServiceInfo describing service.
     */
    public synchronized ServiceInfo getInfo(){
        if( info == null ){
            if( isLocal() ){
                info = new ShapefileFileServiceInfo( this );
            }
            else {
                info = new ShapefileURLServiceInfo( this );
            }
        }
        return info;
    }
        
    /**
     * Sets the {@link TimeZone} used to parse dates in the DBF file
     * @param dbftimeZone
     */
	public void setDbftimeZone(TimeZone dbftimeZone) {
		this.dbfTimeZone = dbftimeZone;
	}

    /**
     * Latch onto xmlURL if it is there, we may be able to get out of
     * calculating the bounding box!
     * 
     * <p>
     * This method is called by the createTypeEntry anonymous inner class
     * DefaultTypeEntry.
     * </p>
     * 
     * @param typeName
     *                DOCUMENT ME!
     * 
     * @return Map with xmlURL parsed, or an EMPTY_MAP.
     */
    protected Map createMetadata(String typeName) {
        String urlString = shpFiles.get(SHP_XML);
        if (urlString == null) {
            return Collections.EMPTY_MAP;
        }

        try {
            // System.out.println("found metadata = " + xmlURL);

            ShpXmlFileReader reader = new ShpXmlFileReader(shpFiles);

            Map map = new HashMap();
            map.put("shp.xml", reader.parse());
            // System.out.println("parsed ..." + xmlURL);

            return map;
        } catch (Throwable t) {
            LOGGER.warning("Could not parse " + urlString + ":"
                    + t.getLocalizedMessage());

            return Collections.EMPTY_MAP;
        }
    }

    /**
     * Determine if the location of this shapefile is local or remote.
     * 
     * @return true if local, false if remote
     */
    public boolean isLocal() {
        return shpFiles.isLocal();
    }

    /**
     * Create a  FeatureReader<SimpleFeatureType, SimpleFeature> for the provided type name.
     * 
     * @param typeName
     *                The name of the FeatureType to create a reader for.
     * 
     * @return A new FeatureReader.
     * 
     * @throws IOException
     *                 If an error occurs during creation
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName)
            throws IOException {
        typeCheck(typeName);

        return getFeatureReader();
    }
    
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader() throws IOException {
        try {
            return createFeatureReader(getSchema().getTypeName(),
                    getAttributesReader(true, null), schema);
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema", se);
        }
    }

    /**
     * Just like the basic version, but adds a small optimization: if no
     * attributes are going to be read, don't uselessly open and read the dbf
     * file. Makes sure to consider also attributes in the query.
     * 
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName, Query query)
            throws IOException {
        String[] propertyNames = query.getPropertyNames();
        String defaultGeomName = schema.getGeometryDescriptor().getLocalName();
        
        // gather attributes needed by the query tool, they will be used by the
        // query filter
        FilterAttributeExtractor extractor = new FilterAttributeExtractor(schema);
        Filter filter = query.getFilter();
        filter.accept(extractor, null);
        String[] filterAttnames = extractor.getAttributeNames();

        // check if the geometry is the one and only attribute needed
        // to return attribute _and_ to run the query filter
        if ((propertyNames != null)
                && (propertyNames.length == 1)
                && propertyNames[0].equals(defaultGeomName)
                && (filterAttnames.length == 0 || (filterAttnames.length == 1 && filterAttnames[0]
                        .equals(defaultGeomName)))) {
            try {
                SimpleFeatureType newSchema = DataUtilities.createSubType(
                        schema, propertyNames);

                return createFeatureReader(typeName,
                        getAttributesReader(false, query), newSchema);
            } catch (SchemaException se) {
                throw new DataSourceException("Error creating schema", se);
            }
        }

        try {
            return createFeatureReader(getSchema().getTypeName(),
                    getAttributesReader(true, query), schema);
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema", se);
        }
    }

    /**
     * Builds the most appropriate geometry factory depending on the available query hints
     * @param query
     * @return
     */
    protected GeometryFactory getGeometryFactory(Hints hints) {
        // if no hints, use the default geometry factory
        if(hints == null)
            return new GeometryFactory();
        
        // grab a geometry factory... check for a special hint
        GeometryFactory geometryFactory = (GeometryFactory) hints.get(Hints.JTS_GEOMETRY_FACTORY);
        if (geometryFactory == null) {
            // look for a coordinate sequence factory
            CoordinateSequenceFactory csFactory = 
                (CoordinateSequenceFactory) hints.get(Hints.JTS_COORDINATE_SEQUENCE_FACTORY);

            if (csFactory != null) {
                geometryFactory = new GeometryFactory(csFactory);
            }
        }

        if (geometryFactory == null) {
            // fall back on the default one
            geometryFactory = new GeometryFactory();
        }
        return geometryFactory;
    }

    protected org.geotools.data.FIDFeatureReader createFeatureReader(String typeName,
            ShapefileAttributeReader reader, SimpleFeatureType readerSchema)
            throws SchemaException {

        return new org.geotools.data.FIDFeatureReader(reader,
                new ShapeFIDReader(readerSchema, reader), readerSchema);
    }

    /**
     * Returns the attribute reader, allowing for a pure shapefile reader, or a
     * combined dbf/shp reader.
     * 
     * @param readDbf -
     *                if true, the dbf fill will be opened and read
     * 
     * 
     * @throws IOException
     */
    protected ShapefileAttributeReader getAttributesReader(boolean readDbf, Query q)
            throws IOException {

        List<AttributeDescriptor> atts = (schema == null) ? readAttributes()
                : schema.getAttributeDescriptors();
        
        GeometryFactory geometryFactory;
        if(q != null) {
            geometryFactory = getGeometryFactory(q.getHints());
        } else {
            geometryFactory = new GeometryFactory();
        }

        ShapefileAttributeReader result;
        ShapefileReader shapeReader = openShapeReader(geometryFactory);
        if (!readDbf) {
            LOGGER.fine("The DBF file won't be opened since no attributes will be read from it");
            atts = new ArrayList(1);
            atts.add(schema.getGeometryDescriptor());
            result =new ShapefileAttributeReader(atts, shapeReader, null);
        } else {
            result = new ShapefileAttributeReader(atts, shapeReader, openDbfReader());
        }
        
        // setup the target bbox if any, and the generalization hints if available
        if(q != null) {
            Envelope bbox = new ReferencedEnvelope();
            bbox = (Envelope) q.getFilter().accept(
                ExtractBoundsFilterVisitor.BOUNDS_VISITOR, bbox);
            if(bbox != null && !bbox.isNull()) {
                result.setTargetBBox(bbox);
            }

            Hints hints = q.getHints();
            if(hints != null) {
                Number simplificationDistance = (Number) hints.get(Hints.GEOMETRY_DISTANCE);
                if(simplificationDistance != null) {
                    result.setSimplificationDistance(simplificationDistance.doubleValue());
                }
                
                if(Boolean.TRUE.equals(hints.get(Hints.FEATURE_2D))) {
                    shapeReader.setFlatGeometry(true);
                }
            }
            
            
        }
        
        
        return result;
    }

    /**
     * Convenience method for opening a ShapefileReader.
     * 
     * @return A new ShapefileReader.
     * 
     * @throws IOException
     *                 If an error occurs during creation.
     */
    protected ShapefileReader openShapeReader(GeometryFactory gf) throws IOException {
        try {
            return new ShapefileReader(shpFiles, true, useMemoryMappedBuffer, gf);
        } catch (ShapefileException se) {
            throw new DataSourceException("Error creating ShapefileReader", se);
        }
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     * 
     * @return A new DbaseFileReader
     * 
     * @throws IOException
     *                 If an error occurs during creation.
     */
    protected DbaseFileReader openDbfReader() throws IOException {

        if (shpFiles.get(ShpFileType.DBF) == null) {
            return null;
        }

        if (isLocal() && !shpFiles.exists(DBF)) {
            return null;
        }

        try {
            return new DbaseFileReader(shpFiles, useMemoryMappedBuffer,
                    dbfCharset, dbfTimeZone);
        } catch (IOException e) {
            // could happen if dbf file does not exist
            return null;
        }
    }

    /**
     * Convenience method for opening an index file.
     * 
     * @return An IndexFile
     * 
     * @throws IOException
     */
    protected IndexFile openIndexFile() throws IOException {
        if (shpFiles.get(SHX) == null) {
            return null;
        }

        if (isLocal() && !shpFiles.exists(SHX)) {
            return null;
        }

        try {
            return new IndexFile(shpFiles, this.useMemoryMappedBuffer);
        } catch (IOException e) {
            // could happen if shx file does not exist remotely
            return null;
        }
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     * 
     * @return A new DbaseFileReader
     * 
     * @throws IOException
     *                 If an error occurs during creation.
     * @throws FactoryException
     *                 DOCUMENT ME!
     */
    protected PrjFileReader openPrjReader() throws IOException,
            FactoryException {

        if (shpFiles.get(PRJ) == null) {
            return null;
        }

        if (isLocal() && !shpFiles.exists(PRJ)) {
            return null;
        }

        try {
            return new PrjFileReader(shpFiles);
        } catch (IOException e) {
            // could happen if prj file does not exist remotely
            return null;
        }
    }

    /**
     * Get an array of type names this DataStore holds.<BR/>ShapefileDataStore
     * will always return a single name.
     * 
     * @return An array of length one containing the single type held.
     */
    public String[] getTypeNames() {
        return new String[] { getCurrentTypeName(), };
    }

    /**
     * Create the type name of the single FeatureType this DataStore represents.<BR/>
     * For example, if the urls path is file:///home/billy/mytheme.shp, the type
     * name will be mytheme.
     * 
     * @return A name based upon the last path component of the url minus the
     *         extension.
     */
    protected String createFeatureTypeName() {
        return shpFiles.getTypeName();
    }

    protected String getCurrentTypeName() {
        return (schema == null) ? createFeatureTypeName() : schema
                .getTypeName();
    }

    /**
     * A convenience method to check if a type name is correct.
     * 
     * @param requested
     *                The type name requested.
     * 
     * @throws IOException
     *                 If the type name is not available
     */
    protected void typeCheck(String requested) throws IOException {
        if (!getCurrentTypeName().equals(requested)) {
            throw new IOException("No such type : " + requested);
        }
    }

    /**
     * Create a FeatureWriter for the given type name.
     * 
     * @param typeName
     *                The typeName of the FeatureType to write
     * @param transaction
     *                DOCUMENT ME!
     * 
     * @return A new FeatureWriter.
     * 
     * @throws IOException
     *                 If the typeName is not available or some other error
     *                 occurs.
     */
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> createFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        return null;
    }

    /**
     * Obtain the FeatureType of the given name. ShapefileDataStore contains
     * only one FeatureType.
     * 
     * @param typeName
     *                The name of the FeatureType.
     * 
     * @return The FeatureType that this DataStore contains.
     * 
     * @throws IOException
     *                 If a type by the requested name is not present.
     */
    public SimpleFeatureType getSchema(String typeName) throws IOException {
        typeCheck(typeName);
        return getSchema();
    }

    public SimpleFeatureType getSchema() throws IOException {
        if (schema == null) {

            List<AttributeDescriptor> types = readAttributes();

            SimpleFeatureType parent = null;
            GeometryDescriptor geomDescriptor = (GeometryDescriptor) types.get(0);            
			Class<?> geomBinding = geomDescriptor.getType().getBinding();

            if ((geomBinding == Point.class) || (geomBinding == MultiPoint.class)) {
                parent = BasicFeatureTypes.POINT;
            } else if ((geomBinding == Polygon.class)
                    || (geomBinding == MultiPolygon.class)) {
                parent = BasicFeatureTypes.POLYGON;
            } else if ((geomBinding == LineString.class)
                    || (geomBinding == MultiLineString.class)) {
                parent = BasicFeatureTypes.LINE;
            }

            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setDefaultGeometry( geomDescriptor.getLocalName() );
            builder.addAll(types);
            builder.setName(createFeatureTypeName());
            if (namespace != null) {
                builder.setNamespaceURI(namespace);
            } else {
                builder.setNamespaceURI(BasicFeatureTypes.DEFAULT_NAMESPACE);
            }
            builder.setAbstract(false);
            if (parent != null) {
                builder.setSuperType(parent);
            }
            schema = builder.buildFeatureType();
        }
        return schema;
    }

    /**
     * Create the AttributeDescriptor contained within this DataStore.
     * 
     * @return List of new AttributeDescriptor
     * @throws IOException
     *                 If AttributeType reading fails
     */
    protected List<AttributeDescriptor> readAttributes() throws IOException {
        ShapefileReader shp = openShapeReader(new GeometryFactory());
        DbaseFileReader dbf = openDbfReader();
        CoordinateReferenceSystem crs = null;

        PrjFileReader prj = null;
        try {
            prj = openPrjReader();

            if (prj != null) {
                crs = prj.getCoodinateSystem();
            }
        } catch (FactoryException fe) {
        }

        AttributeTypeBuilder build = new AttributeTypeBuilder();
        List<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        try {
            Class<?> geometryClass = JTSUtilities.findBestGeometryClass(shp
                    .getHeader().getShapeType());
            build.setName(Classes.getShortName( geometryClass ));
            build.setNillable(true);
            build.setCRS(crs);
            build.setBinding(geometryClass);

            GeometryType geometryType = build.buildGeometryType();
            attributes.add(build.buildDescriptor(
                    BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, geometryType));
            Set<String> usedNames = new HashSet<String>(); // record names in
            // case of
            // duplicates
            usedNames.add(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME);

            // take care of the case where no dbf and query wants all =>
            // geometry only
            if (dbf != null) {
                DbaseFileHeader header = dbf.getHeader();
                for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
                    Class attributeClass = header.getFieldClass(i);
                    String name = header.getFieldName(i);
                    if (usedNames.contains(name)) {
                        String origional = name;
                        int count = 1;
                        name = name + count;
                        while (usedNames.contains(name)) {
                            count++;
                            name = origional + count;
                        }
                    	build.addUserData(ORIGINAL_FIELD_NAME, origional);
                    	build.addUserData(ORIGINAL_FIELD_DUPLICITY_COUNT, count);
                    }
                    usedNames.add(name);
                    int length = header.getFieldLength(i);

                    build.setNillable(true);
                    build.setLength(length);
                    build.setBinding(attributeClass);
                    attributes.add(build.buildDescriptor(name));
                }
            }
            return attributes;
        } finally {

            try {
                if (prj != null) {
                    prj.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
            try {
                if (dbf != null) {
                    dbf.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
            try {
                if (shp != null) {
                    shp.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    /**
     * Gets the bounding box of the file represented by this data store as a
     * whole (that is, off all of the features in the shapefile)
     * 
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     * 
     * @throws DataSourceException
     *                 DOCUMENT ME!
     */
    protected ReferencedEnvelope getBounds() throws DataSourceException {
        // This is way quick!!!
        ReadableByteChannel in = null;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);
            FileReader reader = new FileReader() {
            };

            in = shpFiles.getReadChannel(SHP, reader);
            try {
                in.read(buffer);
                buffer.flip();

                ShapefileHeader header = new ShapefileHeader();
                header.read(buffer, true);

                ReferencedEnvelope bounds = new ReferencedEnvelope(schema
                        .getCoordinateReferenceSystem());
                bounds.include(header.minX(), header.minY());
                bounds.include(header.minX(), header.minY());

                Envelope env = new Envelope(header.minX(), header.maxX(),
                        header.minY(), header.maxY());

                if (schema != null) {
                    return new ReferencedEnvelope(env, schema.getCoordinateReferenceSystem());
                }
                return new ReferencedEnvelope(env, null);
            } finally {
                in.close();
            }

        } catch (IOException ioe) {
            // What now? This seems arbitrarily appropriate !
            throw new DataSourceException("Problem getting Bbox", ioe);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    protected ReferencedEnvelope getBounds(Query query) throws IOException {
        if (query.getFilter().equals(Filter.INCLUDE)) {
            return getBounds();
        }

        return null; // too expensive

        // TODO should we just return the layer? matches the javadocs
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public SimpleFeatureSource getFeatureSource(final String typeName)
            throws IOException {
        final SimpleFeatureType featureType = getSchema(typeName);

        if (isWriteable) {
            if (getLockingManager() != null) {
                return new ShapefileFeatureLocking(this, getSupportedHints(), featureType);
            }
            else {
                return new ShapefileFeatureStore(this, getSupportedHints(), featureType);
            }
        }
        return new ShapefileFeatureSource(this, getSupportedHints(), featureType);
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getCount(org.geotools.data.Query)
     */
    public int getCount(Query query) throws IOException {
        if (query.getFilter() == Filter.INCLUDE) {
            IndexFile file = openIndexFile();
            if (file != null) {
                try {
                    return file.getRecordCount();
                } finally {
                    file.close();
                }
            }

            // no Index file so use the number of shapefile records
            ShapefileReader reader = openShapeReader(new GeometryFactory());
            int count = -1;

            try {
                count = reader.getCount(count);
            } catch (IOException e) {
                throw e;
            } finally {
                reader.close();
            }

            return count;

        }

        return super.getCount(query);
    }

    @Override
    public String toString() {
        return "Shapefile datastore for :" + shpFiles.get(SHP);
    }
    @Override
    public void dispose() {
        super.dispose();
        if(shpFiles != null) {
	        shpFiles.dispose();
	        shpFiles = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	if(shpFiles != null && trace != null) {
    		LOGGER.log(Level.SEVERE, "Undisposed of shapefile, you should call dispose() on all shapefile stores", trace);
    	}
    	dispose();
    }
    
    @Override
    protected Set getSupportedHints() {
        Set<Hints.Key> hints = new HashSet<Hints.Key>();
        hints.add( Hints.FEATURE_DETACHED );
        hints.add( Hints.JTS_GEOMETRY_FACTORY );
        hints.add( Hints.JTS_COORDINATE_SEQUENCE_FACTORY );
        hints.add( Hints.GEOMETRY_DISTANCE);
        return hints;
    }
    
}

