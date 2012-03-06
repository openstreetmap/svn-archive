/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.filter;

import org.opengis.filter.expression.PropertyName;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * Allows creation of additional Filter constructs.
 * <p>
 * Why do we need this? Because not all implementations are going
 * to be using geoapi Geometry. This allows the creation of complient
 * filters with SFSQL Geometry constructs. Consider this a bridge to
 * existing projects allowing GeoAPI to be used.
 * </p>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/filter/FilterFactory2.java $
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.1</A>
 * @author Jody Garnett, Refractions Research Inc.
 * @since GeoAPI 2.1
 */
public interface FilterFactory2 extends FilterFactory {
////////////////////////////////////////////////////////////////////////////////
//
//  FILTERS
//
////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves the value of a {@linkplain org.opengis.feature.Feature feature}'s property.
     * 
     * @param xpath XPath expression (subject to the restrictions of filter specificaiton)
     * @param namespaceContext Used to interpret any namespace prefixs in above xpath expression
     * @return PropertyName
     */
     
    PropertyName property(String xpath, NamespaceSupport namespaceContext);
}
