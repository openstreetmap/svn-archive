/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.visitor.PropertyNameResolvingVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Utilities;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility functions for use when implementing working with data classes.
 * <p>
 * TODO: Move FeatureType manipulation to feature package
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/DataUtilities.java $
 *         http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/
 *         data/DataUtilities.java $
 */
public class DataUtilities {

    static Map<String, Class> typeMap = new HashMap<String, Class>();

    static Map<Class, String> typeEncode = new HashMap<Class, String>();

    static FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    static {
        typeEncode.put(String.class, "String");
        typeMap.put("String", String.class);
        typeMap.put("string", String.class);
        typeMap.put("\"\"", String.class);

        typeEncode.put(Integer.class, "Integer");
        typeMap.put("Integer", Integer.class);
        typeMap.put("int", Integer.class);
        typeMap.put("0", Integer.class);

        typeEncode.put(Double.class, "Double");
        typeMap.put("Double", Double.class);
        typeMap.put("double", Double.class);
        typeMap.put("0.0", Double.class);

        typeEncode.put(Float.class, "Float");
        typeMap.put("Float", Float.class);
        typeMap.put("float", Float.class);
        typeMap.put("0.0f", Float.class);

        typeEncode.put(Boolean.class, "Boolean");
        typeMap.put("Boolean", Boolean.class);
        typeMap.put("true", Boolean.class);
        typeMap.put("false", Boolean.class);

        typeEncode.put(Geometry.class, "Geometry");
        typeMap.put("Geometry", Geometry.class);

        typeEncode.put(Point.class, "Point");
        typeMap.put("Point", Point.class);

        typeEncode.put(LineString.class, "LineString");
        typeMap.put("LineString", LineString.class);

        typeEncode.put(Polygon.class, "Polygon");
        typeMap.put("Polygon", Polygon.class);

        typeEncode.put(MultiPoint.class, "MultiPoint");
        typeMap.put("MultiPoint", MultiPoint.class);

        typeEncode.put(MultiLineString.class, "MultiLineString");
        typeMap.put("MultiLineString", MultiLineString.class);

        typeEncode.put(MultiPolygon.class, "MultiPolygon");
        typeMap.put("MultiPolygon", MultiPolygon.class);

        typeEncode.put(GeometryCollection.class, "GeometryCollection");
        typeMap.put("GeometryCollection", GeometryCollection.class);

        typeEncode.put(Date.class, "Date");
        typeMap.put("Date", Date.class);
    }

    /**
     * A replacement for File.toURI().toURL().
     * <p>
     * The handling of file.toURL() is broken; the handling of file.toURI().toURL() is known to be
     * broken on a few platforms like mac. We have the urlToFile( URL ) method that is able to
     * untangle both these problems and we use it in the geotools library.
     * <p>
     * However occasionally we need to pick up a file and hand it to a third party library like EMF;
     * this method performs a couple of sanity checks which we can use to prepare a good URL
     * reference to a file in these situtations.
     * 
     * @param file
     * @return URL
     */
    public static URL fileToURL(File file) {
        try {
            URL url = file.toURI().toURL();
            String string = url.toExternalForm();
            if (string.contains("+")) {
                // this represents an invalid URL created using either
                // file.toURL(); or
                // file.toURI().toURL() on a specific version of Java 5 on Mac
                string = string.replace("+", "%2B");
            }
            if (string.contains(" ")) {
                // this represents an invalid URL created using either
                // file.toURL(); or
                // file.toURI().toURL() on a specific version of Java 5 on Mac
                string = string.replace(" ", "%20");
            }
            return new URL(string);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Takes a URL and converts it to a File. The attempts to deal with Windows UNC format specific
     * problems, specifically files located on network shares and different drives.
     * 
     * If the URL.getAuthority() returns null or is empty, then only the url's path property is used
     * to construct the file. Otherwise, the authority is prefixed before the path.
     * 
     * It is assumed that url.getProtocol returns "file".
     * 
     * Authority is the drive or network share the file is located on. Such as "C:", "E:",
     * "\\fooServer"
     * 
     * @param url
     *            a URL object that uses protocol "file"
     * @return a File that corresponds to the URL's location
     */
    public static File urlToFile(URL url) {
        if (!"file".equals(url.getProtocol())) {
            return null; // not a File URL
        }
        String string = url.toExternalForm();
        if (string.contains("+")) {
            // this represents an invalid URL created using either
            // file.toURL(); or
            // file.toURI().toURL() on a specific version of Java 5 on Mac
            string = string.replace("+", "%2B");
        }
        try {
            string = URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not decode the URL to UTF-8 format", e);
        }

        String path3;

        String simplePrefix = "file:/";
        String standardPrefix = "file://";
        String os = System.getProperty("os.name");

        if (os.toUpperCase().contains("WINDOWS") && string.startsWith(standardPrefix)) {
            // win32: host/share reference
            path3 = string.substring(standardPrefix.length() - 2);
        } else if (string.startsWith(standardPrefix)) {
            path3 = string.substring(standardPrefix.length());
        } else if (string.startsWith(simplePrefix)) {
            path3 = string.substring(simplePrefix.length() - 1);
        } else {
            String auth = url.getAuthority();
            String path2 = url.getPath().replace("%20", " ");
            if (auth != null && !auth.equals("")) {
                path3 = "//" + auth + path2;
            } else {
                path3 = path2;
            }
        }

        return new File(path3);
    }

    /**
     * Performs a deep copy of the provided object.
     * 
     * @param src Source object
     * @return copy of source object
     */
    public static Object duplicate(Object src) {
        // JD: this method really needs to be replaced with somethign better

        if (src == null) {
            return null;
        }

        //
        // The following are things I expect
        // Features will contain.
        // 
        if (src instanceof String || src instanceof Integer || src instanceof Double
                || src instanceof Float || src instanceof Byte || src instanceof Boolean
                || src instanceof Short || src instanceof Long || src instanceof Character
                || src instanceof Number) {
            return src;
        }

        if (src instanceof Date) {
            return new Date(((Date) src).getTime());
        }

        if (src instanceof URL || src instanceof URI) {
            return src; // immutable
        }

        if (src instanceof Object[]) {
            Object[] array = (Object[]) src;
            Object[] copy = new Object[array.length];

            for (int i = 0; i < array.length; i++) {
                copy[i] = duplicate(array[i]);
            }

            return copy;
        }

        if (src instanceof Geometry) {
            Geometry geometry = (Geometry) src;

            return geometry.clone();
        }

        if (src instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) src;
            return SimpleFeatureBuilder.copy(feature);
        }

        // 
        // We are now into diminishing returns
        // I don't expect Features to contain these often
        // (eveything is still nice and recursive)
        //
        Class<? extends Object> type = src.getClass();

        if (type.isArray() && type.getComponentType().isPrimitive()) {
            int length = Array.getLength(src);
            Object copy = Array.newInstance(type.getComponentType(), length);
            System.arraycopy(src, 0, copy, 0, length);

            return copy;
        }

        if (type.isArray()) {
            int length = Array.getLength(src);
            Object copy = Array.newInstance(type.getComponentType(), length);

            for (int i = 0; i < length; i++) {
                Array.set(copy, i, duplicate(Array.get(src, i)));
            }

            return copy;
        }

        if (src instanceof List) {
            List list = (List) src;
            List<Object> copy = new ArrayList<Object>(list.size());

            for (Iterator i = list.iterator(); i.hasNext();) {
                copy.add(duplicate(i.next()));
            }

            return Collections.unmodifiableList(copy);
        }

        if (src instanceof Map) {
            Map map = (Map) src;
            Map copy = new HashMap(map.size());

            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                copy.put(entry.getKey(), duplicate(entry.getValue()));
            }

            return Collections.unmodifiableMap(copy);
        }

        //
        // I have lost hope and am returning the orgional reference
        // Please extend this to support additional classes.
        //
        // And good luck getting Cloneable to work
        throw new IllegalAttributeException(null,"Do not know how to deep copy " + type.getName());
    }

    /**
     * Returns a non-null default value for the class that is passed in. This is a helper class an
     * can't create a default class for any type but it does support:
     * <ul>
     * <li>String</li>
     * <li>Object - will return empty string</li>
     * <li>Number</li>
     * <li>Character</li>
     * <li>JTS Geometries</li>
     * </ul>
     * 
     * 
     * @param type
     * @return
     */
    public static Object defaultValue(Class type) {
        if (type == String.class || type == Object.class) {
            return "";
        }
        if (type == Integer.class) {
            return new Integer(0);
        }
        if (type == Double.class) {
            return new Double(0);
        }
        if (type == Long.class) {
            return new Long(0);
        }
        if (type == Short.class) {
            return new Short((short) 0);
        }
        if (type == Float.class) {
            return new Float(0.0f);
        }
        if (type == BigDecimal.class) {
            return BigDecimal.valueOf(0);
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf(0);
        }
        if (type == Character.class) {
            return new Character(' ');
        }
        if (type == Boolean.class) {
            return Boolean.FALSE;
        }
        if (type == Timestamp.class)
            return new Timestamp(System.currentTimeMillis());
        if (type == java.sql.Date.class)
            return new java.sql.Date(System.currentTimeMillis());
        if (type == java.sql.Time.class)
            return new java.sql.Time(System.currentTimeMillis());
        if (type == java.util.Date.class)
            return new java.util.Date();

        GeometryFactory fac = new GeometryFactory();
        Coordinate coordinate = new Coordinate(0, 0);
        Point point = fac.createPoint(coordinate);

        if (type == Point.class) {
            return point;
        }
        if (type == MultiPoint.class) {
            return fac.createMultiPoint(new Point[] { point });
        }
        if (type == LineString.class) {
            return fac.createLineString(new Coordinate[] { coordinate, coordinate, coordinate,
                    coordinate });
        }
        LinearRing linearRing = fac.createLinearRing(new Coordinate[] { coordinate, coordinate,
                coordinate, coordinate });
        if (type == LinearRing.class) {
            return linearRing;
        }
        if (type == MultiLineString.class) {
            return fac.createMultiLineString(new LineString[] { linearRing });
        }
        Polygon polygon = fac.createPolygon(linearRing, new LinearRing[0]);
        if (type == Polygon.class) {
            return polygon;
        }
        if (type == MultiPolygon.class) {
            return fac.createMultiPolygon(new Polygon[] { polygon });
        }

        throw new IllegalArgumentException(type + " is not supported by this method");
    }
    
    /**
     * Copies the provided fetaures into a List.
     * 
     * @param featureCollection
     * @return List of features copied into memory
     */
    public static List<SimpleFeature> list(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
        final ArrayList<SimpleFeature> list = new ArrayList<SimpleFeature>();
        try {
            featureCollection.accepts(new FeatureVisitor() {
                public void visit(Feature feature) {
                    list.add((SimpleFeature) feature);
                }
            }, null);
        } catch (IOException ignore) {
        }
        return list;
    }

    /**
     * Create a derived FeatureType
     * 
     * <p>
     * </p>
     * 
     * @param featureType
     * @param properties
     *            - if null, every property of the feature type in input will be used
     * @param override
     * 
     * 
     * @throws SchemaException
     */
    public static SimpleFeatureType createSubType(SimpleFeatureType featureType,
            String[] properties, CoordinateReferenceSystem override) throws SchemaException {
        URI namespaceURI = null;
        if (featureType.getName().getNamespaceURI() != null) {
            try {
                namespaceURI = new URI(featureType.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return createSubType(featureType, properties, override, featureType.getTypeName(),
                namespaceURI);

    }

    public static SimpleFeatureType createSubType(SimpleFeatureType featureType,
            String[] properties, CoordinateReferenceSystem override, String typeName, URI namespace)
            throws SchemaException {

        if ((properties == null) && (override == null)) {
            return featureType;
        }

        if (properties == null) {
            properties = new String[featureType.getAttributeCount()];
            for (int i = 0; i < properties.length; i++) {
                properties[i] = featureType.getDescriptor(i).getLocalName();
            }
        }

        String namespaceURI = namespace != null ? namespace.toString() : null;
        boolean same = featureType.getAttributeCount() == properties.length
                && featureType.getTypeName().equals(typeName)
                && Utilities.equals(featureType.getName().getNamespaceURI(), namespaceURI);

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            AttributeDescriptor type = featureType.getDescriptor(i);
            same = type.getLocalName().equals(properties[i])
                    && (((override != null) && type instanceof GeometryDescriptor) ? assertEquals(
                            override, ((GeometryDescriptor) type).getCoordinateReferenceSystem())
                            : true);
        }

        if (same) {
            return featureType;
        }

        AttributeDescriptor[] types = new AttributeDescriptor[properties.length];

        for (int i = 0; i < properties.length; i++) {
            types[i] = featureType.getDescriptor(properties[i]);

            if ((override != null) && types[i] instanceof GeometryDescriptor) {
                AttributeTypeBuilder ab = new AttributeTypeBuilder();
                ab.init(types[i]);
                ab.setCRS(override);
                types[i] = ab.buildDescriptor(types[i].getLocalName(), ab.buildGeometryType());
            }
        }

        if (typeName == null)
            typeName = featureType.getTypeName();
        if (namespace == null && featureType.getName().getNamespaceURI() != null)
            try {
                namespace = new URI(featureType.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(typeName);
        tb.setNamespaceURI(namespace);
        tb.addAll(types);

        return tb.buildFeatureType();
    }

    private static boolean assertEquals(Object o1, Object o2) {
        return o1 == null && o2 == null ? true : (o1 != null ? o1.equals(o2) : false);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param featureType
     *            DOCUMENT ME!
     * @param properties
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SchemaException
     *             DOCUMENT ME!
     */
    public static SimpleFeatureType createSubType(SimpleFeatureType featureType, String[] properties)
            throws SchemaException {
        if (properties == null) {
            return featureType;
        }

        boolean same = featureType.getAttributeCount() == properties.length;

        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            same = featureType.getDescriptor(i).getLocalName().equals(properties[i]);
        }

        if (same) {
            return featureType;
        }

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(featureType.getName());

        for (int i = 0; i < properties.length; i++) {
            tb.add(featureType.getDescriptor(properties[i]));
        }
        return tb.buildFeatureType();
    }

    /**
     * Factory method to produce Comparator based on provided Query SortBy information.
     * <p>
     * This method handles:
     * <ul>
     * <li>{@link SortBy#NATURAL_ORDER}: As sorting by FeatureID
     * </ul>
     * 
     * @param sortBy
     * @return Comparator suitable for use with Arrays.sort( SimpleFeature[], comparator )
     */
    public static Comparator<SimpleFeature> sortComparator(SortBy sortBy) {
        if (sortBy == null) {
            sortBy = SortBy.NATURAL_ORDER;
        }
        if (sortBy == SortBy.NATURAL_ORDER) {
            return new Comparator<SimpleFeature>() {
                public int compare(SimpleFeature f1, SimpleFeature f2) {
                    return f1.getID().compareTo(f2.getID());
                }
            };
        } else if (sortBy == SortBy.REVERSE_ORDER) {
            return new Comparator<SimpleFeature>() {
                public int compare(SimpleFeature f1, SimpleFeature f2) {
                    int compare = f1.getID().compareTo(f2.getID());
                    return -compare;
                }
            };
        } else {
            final PropertyName PROPERTY = sortBy.getPropertyName();
            return new Comparator<SimpleFeature>() {
                @SuppressWarnings("unchecked")
                public int compare(SimpleFeature f1, SimpleFeature f2) {
                    Object value1 = PROPERTY.evaluate(f1, Comparable.class);
                    Object value2 = PROPERTY.evaluate(f2, Comparable.class);
                    if (value1 == null || value2 == null) {
                        return 0; // cannot perform comparison
                    }
                    if (value1 instanceof Comparable && value1.getClass().isInstance(value2)) {
                        return ((Comparable<Object>) value1).compareTo(value2);
                    } else {
                        return 0; // cannot perform comparison
                    }
                }
            };
        }
    }

    /**
     * Takes two {@link Query}objects and produce a new one by mixing the restrictions of both of
     * them.
     * 
     * <p>
     * The policy to mix the queries components is the following:
     * 
     * <ul>
     * <li>
     * typeName: type names MUST match (not checked if some or both queries equals to
     * <code>Query.ALL</code>)</li>
     * <li>
     * handle: you must provide one since no sensible choice can be done between the handles of both
     * queries</li>
     * <li>
     * maxFeatures: the lower of the two maxFeatures values will be used (most restrictive)</li>
     * <li>
     * attributeNames: the attributes of both queries will be joined in a single set of attributes.
     * IMPORTANT: only <b><i>explicitly</i></b> requested attributes will be joint, so, if the
     * method <code>retrieveAllProperties()</code> of some of the queries returns <code>true</code>
     * it does not means that all the properties will be joined. You must create the query with the
     * names of the properties you want to load.</li>
     * <li>
     * filter: the filtets of both queries are or'ed</li>
     * <li>
     * <b>any other query property is ignored</b> and no guarantees are made of their return values,
     * so client code shall explicitly care of hints, startIndex, etc., if needed.</li>
     * </ul>
     * </p>
     * 
     * @param firstQuery
     *            Query against this DataStore
     * @param secondQuery
     *            DOCUMENT ME!
     * @param handle
     *            DOCUMENT ME!
     * 
     * @return Query restricted to the limits of definitionQuery
     * 
     * @throws NullPointerException
     *             if some of the queries is null
     * @throws IllegalArgumentException
     *             if the type names of both queries do not match
     */
    public static Query mixQueries(Query firstQuery, Query secondQuery, String handle) {
        if ((firstQuery == null) && (secondQuery == null)) {
            // throw new NullPointerException("Cannot combine two null queries");
            return Query.ALL;
        }
        if (firstQuery == null || firstQuery.equals(Query.ALL)) {
            return secondQuery;
        } else if (secondQuery == null || secondQuery.equals(Query.ALL)) {
            return firstQuery;
        }
        if ((firstQuery.getTypeName() != null) && (secondQuery.getTypeName() != null)) {
            if (!firstQuery.getTypeName().equals(secondQuery.getTypeName())) {
                String msg = "Type names do not match: " + firstQuery.getTypeName() + " != "
                        + secondQuery.getTypeName();
                throw new IllegalArgumentException(msg);
            }
        }

        // mix versions, if possible
        String version;
        if (firstQuery.getVersion() != null) {
            if (secondQuery.getVersion() != null
                    && !secondQuery.getVersion().equals(firstQuery.getVersion()))
                throw new IllegalArgumentException(
                        "First and second query refer different versions");
            version = firstQuery.getVersion();
        } else {
            version = secondQuery.getVersion();
        }

        // none of the queries equals Query.ALL, mix them
        // use the more restrictive max features field
        int maxFeatures = Math.min(firstQuery.getMaxFeatures(), secondQuery.getMaxFeatures());

        // join attributes names
        String[] propNames = joinAttributes(firstQuery.getPropertyNames(), secondQuery
                .getPropertyNames());

        // join filters
        Filter filter = firstQuery.getFilter();
        Filter filter2 = secondQuery.getFilter();

        if ((filter == null) || filter.equals(Filter.INCLUDE)) {
            filter = filter2;
        } else if ((filter2 != null) && !filter2.equals(Filter.INCLUDE)) {
            filter = ff.and(filter, filter2);
        }
        Integer start = 0;
        if (firstQuery.getStartIndex() != null) {
            start = firstQuery.getStartIndex();
        }
        if (secondQuery.getStartIndex() != null) {
            start += secondQuery.getStartIndex();
        }
        // collect all hints
        Hints hints = new Hints();
        if(firstQuery.getHints() != null) {
            hints.putAll(firstQuery.getHints());
        } 
        if(secondQuery.getHints() != null) {
            hints.putAll(secondQuery.getHints());
        }
        // build the mixed query
        String typeName = firstQuery.getTypeName() != null ? firstQuery.getTypeName() : secondQuery
                .getTypeName();

        Query mixed = new Query(typeName, filter, maxFeatures, propNames, handle);
        mixed.setVersion(version);
        mixed.setHints(hints);
        if (start != 0) {
            mixed.setStartIndex(start);
        }
        return mixed;
    }

    /**
     * This method changes the query object so that all propertyName references are resolved to
     * simple attribute names against the schema of the feature source.
     * <p>
     * For example, this method ensures that propertyName's such as "gml:name" are rewritten as
     * simply "name".
     *</p>
     */
    public static Query resolvePropertyNames(Query query, SimpleFeatureType schema) {
        Filter resolved = resolvePropertyNames(query.getFilter(), schema);
        if (resolved == query.getFilter()) {
            return query;
        }
        Query newQuery = new Query(query);
        newQuery.setFilter(resolved);
        return newQuery;
    }

    /** Transform provided filter; resolving property names */
    public static Filter resolvePropertyNames(Filter filter, SimpleFeatureType schema) {
        if (filter == null || filter == Filter.INCLUDE || filter == Filter.EXCLUDE) {
            return filter;
        }
        return (Filter) filter.accept(new PropertyNameResolvingVisitor(schema), null);
    }

    /**
     * Creates a set of attribute names from the two input lists of names, maintaining the order of
     * the first list and appending the non repeated names of the second.
     * <p>
     * In the case where both lists are <code>null</code>, <code>null</code> is returned.
     * </p>
     * 
     * @param atts1
     *            the first list of attribute names, who's order will be maintained
     * @param atts2
     *            the second list of attribute names, from wich the non repeated names will be
     *            appended to the resulting list
     * 
     * @return Set of attribute names from <code>atts1</code> and <code>atts2</code>
     */
    private static String[] joinAttributes(String[] atts1, String[] atts2) {
        String[] propNames = null;

        if (atts1 == null && atts2 == null) {
            return null;
        }

        List<String> atts = new LinkedList<String>();

        if (atts1 != null) {
            atts.addAll(Arrays.asList(atts1));
        }

        if (atts2 != null) {
            for (int i = 0; i < atts2.length; i++) {
                if (!atts.contains(atts2[i])) {
                    atts.add(atts2[i]);
                }
            }
        }

        propNames = new String[atts.size()];
        atts.toArray(propNames);

        return propNames;
    }

    /**
     * Manually calculates the bounds of a feature collection.
     * 
     * @param collection
     * @return
     */
    public static Envelope bounds(
            FeatureCollection<? extends FeatureType, ? extends Feature> collection) {
        FeatureIterator<? extends Feature> i = collection.features();
        try {
            ReferencedEnvelope bounds = new ReferencedEnvelope(collection.getSchema()
                    .getCoordinateReferenceSystem());
            if (!i.hasNext()) {
                bounds.setToNull();
                return bounds;
            }

            bounds.init(((SimpleFeature) i.next()).getBounds());
            return bounds;
        } finally {
            i.close();
        }
    }
}
