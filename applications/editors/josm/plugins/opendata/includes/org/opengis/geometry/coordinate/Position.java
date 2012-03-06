/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.geometry.coordinate;

import static org.opengis.annotation.Specification.ISO_19107;

import org.opengis.annotation.UML;
import org.opengis.geometry.DirectPosition;


/**
 * A type consisting of either a {@linkplain DirectPosition direct position} or of a
 * {@linkplain Point point} from which a {@linkplain DirectPosition direct position}
 * shall be obtained. The use of this data type allows the identification of a position
 * either directly as a coordinate (variant direct) or indirectly as a {@linkplain Point point}
 * (variant indirect).
 *
 * @departure
 *   ISO 19111 defines {@code Position} like a C/C++ {@code union} of {@code DirectPosition} and
 *   {@code Point}. Since unions are not allowed in Java, GeoAPI defines {@code Position} as the
 *   base interface of the above. This leads to a slightly different semantic since ISO defines
 *   {@link #getDirectPosition} as conditional, while GeoAPI defines it as mandatory by allowing
 *   the method to return {@code this}.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/geometry/coordinate/Position.java $
 * @version <A HREF="http://www.opengeospatial.org/standards/as">ISO 19107</A>
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 1.0
 *
 * @issue http://jira.codehaus.org/browse/GEO-87
 */
@UML(identifier="GM_Position", specification=ISO_19107)
public interface Position {
}
