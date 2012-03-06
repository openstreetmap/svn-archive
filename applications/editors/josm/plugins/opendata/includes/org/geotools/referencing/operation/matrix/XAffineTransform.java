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
package org.geotools.referencing.operation.matrix;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Utility methods for affine transforms. This class provides two kind of services:
 *
 * <ul>
 *   <li><p>A set of public static methods working on any {@link AffineTransform}.</p></li>
 *   <li><p>An abstract base class that override all mutable {@link AffineTransform} methods
 *       in order to check for permission before changing the transform's state.
 *       If {@link #checkPermission} is defined to always throw an exception,
 *       then {@code XAffineTransform} is immutable.</p></li>
 * </ul>
 *
 * @since 2.3
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/operation/matrix/XAffineTransform.java $
 * @version $Id: XAffineTransform.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 * @author Simone Giannecchini
 */
public class XAffineTransform extends AffineTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5215291166450556451L;

    /**
     * Constructs a new {@code XAffineTransform} that is a
     * copy of the specified {@code AffineTransform} object.
     */
    public XAffineTransform(final AffineTransform tr) {
        super(tr);
    }

    /**
     * Checks if the caller is allowed to change this {@code XAffineTransform} state.
     * If this method is defined to thrown an exception in all case, then this
     * {@code XAffineTransform} is immutable.
     * <p>
     * The default implementation throws the exception in all case, thus making this
     * instance immutable.
     *
     * @throws UnsupportedOperationException if this affine transform is immutable.
     */
    protected void checkPermission() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                  Errors.format(ErrorKeys.UNMODIFIABLE_AFFINE_TRANSFORM));
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before translating this transform.
     */
    @Override
    public void translate(double tx, double ty) {
        checkPermission();
        super.translate(tx, ty);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before rotating this transform.
     */
    @Override
    public void rotate(double theta) {
        checkPermission();
        super.rotate(theta);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before rotating this transform.
     */
    @Override
    public void rotate(double theta, double x, double y) {
        checkPermission();
        super.rotate(theta, x, y);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before scaling this transform.
     */
    @Override
    public void scale(double sx, double sy) {
        checkPermission();
        super.scale(sx, sy);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before shearing this transform.
     */
    @Override
    public void shear(double shx, double shy) {
        checkPermission();
        super.shear(shx, shy);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToIdentity() {
        checkPermission();
        super.setToIdentity();
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToTranslation(double tx, double ty) {
        checkPermission();
        super.setToTranslation(tx, ty);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToRotation(double theta) {
        checkPermission();
        super.setToRotation(theta);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToRotation(double theta, double x, double y) {
        checkPermission();
        super.setToRotation(theta, x, y);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToScale(double sx, double sy) {
        checkPermission();
        super.setToScale(sx, sy);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setToShear(double shx, double shy) {
        checkPermission();
        super.setToShear(shx, shy);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setTransform(AffineTransform Tx) {
        checkPermission();
        super.setTransform(Tx);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before setting this transform.
     */
    @Override
    public void setTransform(double m00, double m10,
                             double m01, double m11,
                             double m02, double m12) {
        checkPermission();
        super.setTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before concatenating this transform.
     */
    @Override
    public void concatenate(AffineTransform Tx) {
        checkPermission();
        super.concatenate(Tx);
    }

    /**
     * Checks for {@linkplain #checkPermission permission} before concatenating this transform.
     */
    @Override
    public void preConcatenate(AffineTransform Tx) {
        checkPermission();
        super.preConcatenate(Tx);
    }

    /**
     * Checks whether or not this {@code XAffineTransform} is the identity by
     * using the provided {@code tolerance}.
     *
     * @param tolerance The tolerance to use for this check.
     * @return {@code true} if the transform is identity, {@code false} otherwise.
     *
     * @since 2.3.1
     */
    public boolean isIdentity(double tolerance) { // NO_UCD
    	return isIdentity(this, tolerance);
    }

    /**
     * Returns {@code true} if the specified affine transform is an identity transform up to the
     * specified tolerance. This method is equivalent to computing the difference between this
     * matrix and an identity matrix (as created by {@link AffineTransform#AffineTransform()
     * new AffineTransform()}) and returning {@code true} if and only if all differences are
     * smaller than or equal to {@code tolerance}.
     * <p>
     * This method is used for working around rounding error in affine transforms resulting
     * from a computation, as in the example below:
     *
     * <blockquote><pre>
     * [ 1.0000000000000000001  0.0                      0.0 ]
     * [ 0.0                    0.999999999999999999999  0.0 ]
     * [ 0.0                    0.0                      1.0 ]
     * </pre></blockquote>
     *
     * @param tr The affine transform to be checked for identity.
     * @param tolerance The tolerance value to use when checking for identity.
     * return {@code true} if this tranformation is close enough to the
     *        identity, {@code false} otherwise.
     *
     * @since 2.3.1
     */
    public static boolean isIdentity(final AffineTransform tr, double tolerance) {
        if (tr.isIdentity()) {
            return true;
        }
        tolerance = Math.abs(tolerance);
        return Math.abs(tr.getScaleX() - 1) <= tolerance &&
               Math.abs(tr.getScaleY() - 1) <= tolerance &&
               Math.abs(tr.getShearX())     <= tolerance &&
               Math.abs(tr.getShearY())     <= tolerance &&
               Math.abs(tr.getTranslateX()) <= tolerance &&
               Math.abs(tr.getTranslateY()) <= tolerance;
    }

    /**
     * Transforms the given shape. This method is similar to
     * {@link #createTransformedShape createTransformedShape} except that:
     * <p>
     * <ul>
     *   <li>It tries to preserve the shape kind when possible. For example if the given shape
     *       is an instance of {@link RectangularShape} and the given transform do not involve
     *       rotation, then the returned shape may be some instance of the same class.</li>
     *   <li>It tries to recycle the given object if {@code overwrite} is {@code true}.</li>
     * </ul>
     *
     * @param transform Affine transform to use.
     * @param shape     The shape to transform.
     * @param overwrite If {@code true}, this method is allowed to overwrite {@code shape} with the
     *                  transform result. If {@code false}, then {@code shape} is never modified.
     *
     * @return The direct transform of the given shape. May or may not be the same instance than
     *         the given shape.
     *
     * @see #createTransformedShape
     *
     * @since 2.5
     */
    public static Shape transform(final AffineTransform transform, Shape shape, boolean overwrite) {
        final int type = transform.getType();
        if (type == TYPE_IDENTITY) {
            return shape;
        }
        // If there is only scale, flip, quadrant rotation or translation,
        // then we can optimize the transformation of rectangular shapes.
        if ((type & (TYPE_GENERAL_ROTATION | TYPE_GENERAL_TRANSFORM)) == 0) {
            // For a Rectangle input, the output should be a rectangle as well.
            if (shape instanceof Rectangle2D) {
                final Rectangle2D rect = (Rectangle2D) shape;
                return transform(transform, rect, overwrite ? rect : null);
            }
            // For other rectangular shapes, we restrict to cases whithout
            // rotation or flip because we don't know if the shape is symetric.
            if ((type & (TYPE_FLIP & TYPE_MASK_ROTATION)) == 0) {
                if (shape instanceof RectangularShape) {
                    RectangularShape rect = (RectangularShape) shape;
                    if (!overwrite) {
                        rect = (RectangularShape) rect.clone();
                    }
                    final Rectangle2D frame = rect.getFrame();
                    rect.setFrame(transform(transform, frame, frame));
                    return rect;
                }
            }
        }
        // TODO: Check for Path2D instance instead of GeneralPath
        //       when we will be allowed to compile for Java 6.
        if (shape instanceof GeneralPath) {
            final GeneralPath path = (GeneralPath) shape;
            if (overwrite) {
                path.transform(transform);
            } else {
                shape = path.createTransformedShape(transform);
            }
        } else if (shape instanceof Area) {
            final Area area = (Area) shape;
            if (overwrite) {
                area.transform(transform);
            } else {
                shape = area.createTransformedArea(transform);
            }
        } else {
            final GeneralPath path = new GeneralPath(shape);
            path.transform(transform);
            shape = path;
            // TODO: use the line below instead of the above 3 lines when we will
            //       be allowed to compile for Java 6:
//          shape = new Path2D.Double(shape, transform);
        }
        return shape;
    }

    /**
     * Returns a rectangle which entirely contains the direct
     * transform of {@code bounds}. This operation is equivalent to:
     *
     * <blockquote><code>
     * {@linkplain #createTransformedShape createTransformedShape}(bounds).{@linkplain
     * Rectangle2D#getBounds2D() getBounds2D()}
     * </code></blockquote>
     *
     * @param transform Affine transform to use.
     * @param bounds    Rectangle to transform. This rectangle will not be modified except
     *                  if {@code dest} is the same reference.
     * @param dest      Rectangle in which to place the result.
     *                  If null, a new rectangle will be created.
     *
     * @return The direct transform of the {@code bounds} rectangle.
     *
     * @see org.geotools.referencing.CRS#transform(
     *      org.opengis.referencing.operation.MathTransform2D, Rectangle2D, Rectangle2D)
     */
    public static Rectangle2D transform(final AffineTransform transform,
                                        final Rectangle2D     bounds,
                                        final Rectangle2D     dest)
    {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        final Point2D.Double point = new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i & 1) == 0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i & 2) == 0 ? bounds.getMinY() : bounds.getMaxY();
            transform.transform(point, point);
            if (point.x < xmin) xmin = point.x;
            if (point.x > xmax) xmax = point.x;
            if (point.y < ymin) ymin = point.y;
            if (point.y > ymax) ymax = point.y;
        }
        if (dest != null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }
}
