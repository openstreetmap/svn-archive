/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.geometry;

import java.awt.geom.Rectangle2D;

import org.opengis.annotation.Extension;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.cs.AxisDirection;


/**
 * Represents a two-dimensional {@linkplain Envelope envelope}.
 * This interface combines the ideas of {@link GeographicBoundingBox} with
 * those of {@link Envelope}. It provides convenience methods to assist
 * in accessing the formal properties of this object. Those methods
 * (for example {@link #getMinX()}) match common usage in existing libraries
 * like {@linkplain Rectangle2D Java2D}.
 * <p>
 * This object contains no additional information beyond that provided
 * by {@link Envelope}.
 *
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux (Geomatys)
 * @since GeoAPI 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/geometry/BoundingBox.java $
 */
@Extension
public interface BoundingBox extends Envelope {

    /**
     * Provides the minimum ordinate along the first axis.
     * This is equivalent to <code>{@linkplain #getMinimum getMinimum}(0)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#EAST East}.
     *
     * @return The minimum ordinate along the first axis.
     */
    double getMinX();

    /**
     * Provides the maximum ordinate along the first axis.
     * This is equivalent to <code>{@linkplain #getMaximum getMaximum}(0)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#EAST East}.
     *
     * @return The maximum ordinate along the first axis.
     */
    double getMaxX();

    /**
     * Provides the minimum ordinate along the second axis.
     * This is equivalent to <code>{@linkplain #getMinimum getMinimum}(1)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#NORTH North}.
     *
     * @return The minimum ordinate along the second axis.
     */
    double getMinY();

    /**
     * Provides the maximum ordinate along the second axis.
     * This is equivalent to <code>{@linkplain #getMaximum getMaximum}(1)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#NORTH North}.
     *
     * @return The maximum ordinate along the second axis.
     */
    double getMaxY();

    /**
     * Provides the difference between {@linkplain #getMinX minimum} and
     * {@linkplain #getMaxX maximum} ordinate along the first axis.
     * This is equivalent to <code>{@linkplain #getLength getLength}(0)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#EAST East}.
     *
     * @return The span along the first axis.
     */
    double getWidth();

    /**
     * Provides the difference between {@linkplain #getMinX minimum} and
     * {@linkplain #getMaxX maximum} ordinate along the second axis.
     * This is equivalent to <code>{@linkplain #getLength getLength}(1)</code>.
     * There is no guarantee that this axis is oriented toward
     * {@linkplain AxisDirection#NORTH North}.
     *
     * @return The span along the second axis.
     */
    double getHeight();

    /**
     * Returns {@code true} if {@linkplain #getSpan spans} along all dimension are zero
     * or negative.
     *
     * @return {@code true} if this bounding box is empty.
     */
    boolean isEmpty();

    /**
     * Includes the provided bounding box, expanding as necesary.
     *
     * @param bounds The bounds to add to this geographic bounding box.
     */
    void include(BoundingBox bounds);

    /**
     * Includes the provided coordinates, expanding as necessary. Note that there is no
     * guarantee that the (<var>x</var>, <var>x</var>) values are oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}),
     * since it depends on the {@linkplain #getCoordinateReferenceSystem envelope CRS}.
     *
     * @param x The first ordinate value.
     * @param y The second ordinate value.
     */
    void include(double x, double y);
}
