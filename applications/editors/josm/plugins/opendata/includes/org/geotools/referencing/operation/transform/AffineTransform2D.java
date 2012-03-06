/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.referencing.operation.transform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.Matrix2;
import org.geotools.referencing.operation.matrix.Matrix3;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.Formattable;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.util.Cloneable;


/**
 * Transforms two-dimensional coordinate points using an affine transform. This class both
 * extends {@link AffineTransform} and implements {@link MathTransform2D}, so it can be
 * used as a bridge between Java2D and the referencing module.
 *
 * @since 2.5
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/operation/transform/AffineTransform2D.java $
 * @version $Id: AffineTransform2D.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class AffineTransform2D extends XAffineTransform
        implements MathTransform2D, LinearTransform, Formattable, Cloneable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5299837898367149069L;

    /**
     * The inverse transform. This field will be computed only when needed.
     */
    private transient AffineTransform2D inverse;

    /**
     * Constructs a new affine transform with the same coefficient than the specified transform.
     */
    public AffineTransform2D(final AffineTransform transform) {
        super(transform);
    }

    /**
     * Throws an {@link UnsupportedOperationException} when a mutable method
     * is invoked, since {@code AffineTransform2D} must be immutable.
     */
    @Override
    protected final void checkPermission() throws UnsupportedOperationException {
        super.checkPermission();
    }

    /**
     * Gets the dimension of input points, which is fixed to 2.
     */
    public final int getSourceDimensions() {
        return 2;
    }

    /**
     * Gets the dimension of output points, which is fixed to 2.
     */
    public final int getTargetDimensions() {
        return 2;
    }

    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     */
    public DirectPosition transform(final DirectPosition ptSrc, DirectPosition ptDst) {
        if (ptDst == null) {
            ptDst = new GeneralDirectPosition(2);
        } else {
            final int dimension = ptDst.getDimension();
            if (dimension != 2) {
                throw new MismatchedDimensionException(Errors.format(
                          ErrorKeys.MISMATCHED_DIMENSION_$3, "ptDst", dimension, 2));
            }
        }
        final double[] array = ptSrc.getCoordinate();
        transform(array, 0, array, 0, 1);
        ptDst.setOrdinate(0, array[0]);
        ptDst.setOrdinate(1, array[1]);
        return ptDst;
    }

    /**
     * Transforms the specified shape.
     *
     * @param  shape Shape to transform.
     * @return Transformed shape, or {@code shape} if this transform is the identity transform.
     */
    @Override
    public Shape createTransformedShape(final Shape shape) {
        return transform(this, shape, false);
    }

    /**
     * Returns this transform as an affine transform matrix.
     */
    public Matrix getMatrix() {
        return new Matrix3(this);
    }

    /**
     * Gets the derivative of this transform at a point. For an affine transform,
     * the derivative is the same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        return new Matrix2(getScaleX(), getShearX(),
                           getShearY(), getScaleY());
    }

    /**
     * Gets the derivative of this transform at a point. For an affine transform,
     * the derivative is the same everywhere.
     */
    public Matrix derivative(final DirectPosition point) {
        return derivative((Point2D) null);
    }

    /**
     * Creates the inverse transform of this object.
     *
     * @throws NoninvertibleTransformException if this transform can't be inverted.
     */
    public MathTransform2D inverse() throws NoninvertibleTransformException {
        if (inverse == null) {
            if (isIdentity()) {
                inverse = this;
            } else try {
                synchronized (this) {
                    inverse = new AffineTransform2D(createInverse());
                    inverse.inverse = this;
                }
            } catch (java.awt.geom.NoninvertibleTransformException exception) {
                throw new NoninvertibleTransformException(exception.getLocalizedMessage(), exception);
            }
        }
        return inverse;
    }

    /**
     * Returns a new affine transform which is a modifiable copy of this transform. We override
     * this method because it is {@linkplain AffineTransform#clone defined in the super-class}.
     * However this implementation do not returns a new {@code AffineTransform2D} instance because
     * the later is unmodifiable, which make exact cloning useless.
     */
    @Override
    public AffineTransform clone() {
        return new AffineTransform(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AffineTransform)) {
            return false;
        }

        AffineTransform a = (AffineTransform) obj;
        
        return Utilities.equals(getScaleX(), a.getScaleX()) &&
            Utilities.equals(getScaleY(), a.getScaleY()) &&
            Utilities.equals(getShearX(), a.getShearX()) &&
            Utilities.equals(getShearY(), a.getShearY()) &&
            Utilities.equals(getTranslateX(), a.getTranslateX()) &&
            Utilities.equals(getTranslateY(), a.getTranslateY());
    }
}
