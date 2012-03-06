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
package org.geotools.resources.geometry;

import static java.lang.Math.abs;
import static java.lang.Math.hypot;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;


/**
 * Static utilities methods. Those methods operate on geometric
 * shapes from the {@code java.awt.geom} package.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/resources/geometry/ShapeUtilities.java $
 * @version $Id: ShapeUtilities.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class ShapeUtilities {
    /**
     * Valeur limite pour détecter si des points sont
     * colinéaires ou si des coordonnées sont identiques.
     */
    private static final double EPS = 1E-6;

    /**
     * Constante pour les calculs de paraboles. Cette constante indique que l'axe des
     * <var>x</var> de la parabole doit être parallèle à la droite joignant les points
     * P0 et P2.
     */
    public static final int PARALLEL = 0;

    /**
     * Constante pour les calculs de paraboles. Cette constante indique que l'axe des
     * <var>x</var> de la parabole doit être horizontale, quelle que soit la pente de
     * la droite joignant les points P0 et P2.
     */
    public static final int HORIZONTAL = 1;

    /**
     * Interdit la création d'objets de cette classe.
     */
    private ShapeUtilities() {
    }

    /**
     * Retourne le point de contrôle d'une courbe quadratique passant par les trois points spécifiés.
     * Il peut exister une infinité de courbes quadratiques passant par trois points. On peut voir
     * les choses en disant qu'une courbe quadratique correspond à une parabole produite par une
     * équation de la forme <code>y=ax²+bx+c</code>, mais que l'axe des <var>x</var> de cette
     * équation n'est pas nécessairement horizontal. La direction de cet axe des <var>x</var> dépend
     * du paramètre {@code orientation} spécifié à cette méthode. La valeur {@link #HORIZONTAL}
     * signifie que l'axe des <var>x</var> de la parabole sera toujours horizontal. La courbe
     * quadratique produite ressemblera alors à une parabole classique telle qu'on en voit dans les
     * ouvrages de mathématiques élémentaires. La valeur {@link #PARALLEL} indique plutôt que l'axe
     * des <var>x</var> de la parabole doit être parallèle à la droite joignant les points
     * {@code (x0,y0)} et {@code (x2,y2)}. Ce dernier type produira le même résultat que
     * {@link #HORIZONTAL} si {@code y0==y2}.
     *
     * @param  x0 <var>x</var> value of the first  point.
     * @param  y0 <var>y</var> value of the first  point.
     * @param  x1 <var>x</var> value of the second point.
     * @param  y1 <var>y</var> value of the second point.
     * @param  x2 <var>x</var> value of the third  point.
     * @param  y2 <var>y</var> value of the third  point.
     * @param  orientation Orientation de l'axe des <var>x</var> de la parabole: {@link #PARALLEL}
     *         ou {@link #HORIZONTAL}.
     * @param  dest Where to store the control point.
     * @return Le point de contrôle d'une courbe quadratique passant par les trois points spécifiés.
     *         La courbe commencera au point {@code (x0,y0)} et se terminera au point {@code (x2,y2)}.
     *         Si deux points ont des coordonnées presque identiques, ou si les trois points sont
     *         colinéaires, alors cette méthode retourne {@code null}.
     * @throws IllegalArgumentException si l'argument {@code orientation} n'est pas une des
     *         constantes valides.
     */
    public static Point2D parabolicControlPoint(final double x0, final double y0,
                                                      double x1,       double y1,
                                                      double x2,       double y2,
                                                final int orientation, final Point2D dest)
        throws IllegalArgumentException
    {
        /*
         * Applique une translation de façon à ce que (x0,y0)
         * devienne l'origine du système d'axes. Il ne faudra
         * plus utiliser (x0,y0) avant la fin de ce code.
         */
        x1 -= x0;
        y1 -= y0;
        x2 -= x0;
        y2 -= y0;
        switch (orientation) {
            case PARALLEL: {
                /*
                 * Applique une rotation de façon à ce que (x2,y2)
                 * tombe sur l'axe des x, c'est-à-dire que y2=0.
                 */
                final double rx2 = x2;
                final double ry2 = y2;
                x2 = hypot(x2,y2);
                y2 = (x1*rx2 + y1*ry2) / x2; // use 'y2' as a temporary variable for 'x1'
                y1 = (y1*rx2 - x1*ry2) / x2;
                x1 = y2;
                y2 = 0;
                /*
                 * Calcule maintenant les coordonnées du point
                 * de contrôle selon le nouveau système d'axes.
                 */
                final double x = 0.5;                       // Really "x/x2"
                final double y = (y1*x*x2) / (x1*(x2-x1));  // Really "y/y2"
                final double check = abs(y);
                if (!(check <= 1/EPS)) return null; // Deux points ont les mêmes coordonnées.
                if (!(check >=   EPS)) return null; // Les trois points sont colinéaires.
                /*
                 * Applique une rotation inverse puis une translation pour
                 * ramener le système d'axe dans sa position d'origine.
                 */
                x1 = (x*rx2 - y*ry2) + x0;
                y1 = (y*rx2 + x*ry2) + y0;
                break;
            }
            case HORIZONTAL: {
                final double a = (y2 - y1*x2/x1) / (x2-x1); // Really "a*x2"
                final double check = abs(a);
                if (!(check <= 1/EPS)) return null; // Deux points ont les mêmes coordonnées.
                if (!(check >=   EPS)) return null; // Les trois points sont colinéaires.
                final double b = y2/x2 - a;
                x1 = (1 + b/(2*a))*x2 - y2/(2*a);
                y1 = y0 + b*x1;
                x1 += x0;
                break;
            }
            default: throw new IllegalArgumentException();
        }
        if (dest != null) {
            dest.setLocation(x1,y1);
            return dest;
        } else {
            return new Point2D.Double(x1,y1);
        }
    }

    /**
     * Tente de remplacer la forme géométrique {@code path} par une des formes standards
     * de Java2D. Par exemple, si {@code path} ne contient qu'un simple segment de droite
     * ou une courbe quadratique, alors cette méthode retournera un objet {@link Line2D} ou
     * {@link QuadCurve2D} respectivement.
     *
     * @param  path Forme géométrique à simplifier (généralement un objet {@link GeneralPath}).
     * @return Forme géométrique standard, ou {@code path} si aucun remplacement n'est proposé.
     */
    public static Shape toPrimitive(final Shape path) {
        final float[] buffer = new float[6];
        final PathIterator it = path.getPathIterator(null);
        if (!it.isDone() && it.currentSegment(buffer) == PathIterator.SEG_MOVETO && !it.isDone()) {
            final float x1 = buffer[0];
            final float y1 = buffer[1];
            final int code = it.currentSegment(buffer);
            if (it.isDone()) {
                switch (code) {
                    case PathIterator.SEG_LINETO:  return new       Line2D.Float(x1,y1, buffer[0],buffer[1]);
                    case PathIterator.SEG_QUADTO:  return new  QuadCurve2D.Float(x1,y1, buffer[0],buffer[1], buffer[2],buffer[3]);
                    case PathIterator.SEG_CUBICTO: return new CubicCurve2D.Float(x1,y1, buffer[0],buffer[1], buffer[2],buffer[3], buffer[4],buffer[5]);
                }
            }
        }
        return path;
    }
}
