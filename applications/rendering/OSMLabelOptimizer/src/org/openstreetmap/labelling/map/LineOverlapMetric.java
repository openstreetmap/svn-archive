/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Vector;

/**
 * @author sebi
 *
 */
public class LineOverlapMetric extends PrecalculatedMetric
{
    private static final double EPS = 1e-10;
    
    public LineOverlapMetric(Collection<LineFeature> lineFeatures, Collection<Label> labels)
    {
        super(lineFeatures, labels);
    }
    
    private double calculateIntersection(Vector<Line2D> lines, Rectangle2D rect)
    {
        double val = 0.;
               
        double[][] rect_p = {
                {rect.getMinX(), rect.getMinY()}, // bottom left
                {rect.getMaxX(), rect.getMinY()}, // bottom right
                {rect.getMaxX(), rect.getMaxY()}, // top right
                {rect.getMinX(), rect.getMaxY()}  // top left
        };

        double[][] rect_v = {
                {rect_p[1][0] - rect_p[0][0], rect_p[1][1] - rect_p[0][1]}, // down side
                {rect_p[2][0] - rect_p[1][0], rect_p[2][1] - rect_p[1][1]}, // right side
                {rect_p[3][0] - rect_p[2][0], rect_p[3][1] - rect_p[2][1]}, // up side
                {rect_p[0][0] - rect_p[3][0], rect_p[0][1] - rect_p[3][1]}  // left side
        };
        
        Vector<double[]> intersections = new Vector<double[]> ();
        Line2D first_intersection = null;
        double[] first_intersection_v = new double[2];
        
        for (int i = 0; i < 4; ++i)
        {
            double[] v = rect_v[i];
            double[] p = rect_p[i];
            
            for (Line2D line : lines)
            {
                double[] p_ = {line.getP1().getX(), line.getP2().getY()};
                double[] v_ = {line.getP2().getX() - p_[0], line.getP2().getY() - p_[1]};
                
                /*
                 * Solve the equation 
                 * v[0] * t_1 - v_[0] * t_2 = p_[0] - p[0]
                 * v[1] * t_1 - v_[1] * t_2 = p_[1] - p[1] 
                 * for t_1, t_2.
                 */
                
                Double det = v_[0] * v[1] - v[0] *  v_[1];
                
                if (Math.abs(det) < EPS)
                {
                    // v and v_ are nearly parallel => no intersection
                    continue;
                }
                
                double det_ = 1/det;
                double p_0 = p_[0] - p[0];
                double p_1 = p_[1] - p[1];
                double t_1 = det_ * (v_[1] * p_0  - v_[0] * p_1);
                double t_2 = det_ * (- v[1] * p_0 + v[0] * p_1);
                
                if (t_1 < 0 || t_1 > 1 || t_2 < 0 || t_2 > 1)
                {
                    // we have no intersection
                    continue;
                }
                
                double x = p[0] + t_1 * v[0];
                double y = p[1] + t_1 * v[1]; 
                
                intersections.add(new double[] {x, y});
                
                if (first_intersection == null)
                {
                    first_intersection = line;
                    double sq = Math.sqrt(Math.pow(v_[0],2) + Math.pow(v_[1],2));
                    first_intersection_v[0] = v_[0] / sq;
                    first_intersection_v[1] = v_[1] / sq;
                }
            }
        }
        
        if (intersections.isEmpty())
        {
            return 0;
        }
        
        // unit vector in label baseline direction
        double[] b = {1, 0};
        double max_scalar_product = 0;
        
        if (intersections.size() == 1)
        {
            double[] v = first_intersection_v;
            max_scalar_product = Math.sqrt(Math.pow(v[0] * b[0], 2) + Math.pow(v[1] * b[1], 2));
        }
        
        
        for (double[] p : intersections)
        {
            for (double[] q : intersections)
            {
                if (p != q)
                {
                    double[] v_tmp = {p[0] - q[0], p[1] - q[1]};
                    double sq = Math.sqrt(Math.pow(v_tmp[0], 2) + Math.pow(v_tmp[1], 2));
                    double[] v_ = {v_tmp[0] / sq, v_tmp[1] / sq};
                    
                    double norm_scalar_product = Math.sqrt(Math.pow(v_[0] * b[0], 2) + Math.pow(v_[1] * b[1], 2));
                    
                    if (norm_scalar_product > max_scalar_product)
                    {
                        max_scalar_product = norm_scalar_product;
                    }
                    
                }
            }
        }
        
        val = 1 + 9 * max_scalar_product;
        return val;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#getWeight()
     */
    public double getWeight()
    {
        return 15;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PrecalculatedMetric#calculateValue(org.openstreetmap.labelling.map.LabelPosition, java.util.Vector, java.util.Vector)
     */
    @Override
    protected double calculateValue(LabelPosition pos, Collection<? extends Feature> features, Collection<? extends Label> labels)
    {
        double val = 0;
        Rectangle2D rect = pos.getBoundingBox();
        
        for (Feature f : features)
        {
            LineFeature line = (LineFeature)f;
            val = val + calculateIntersection(line.getLines(), rect);
        }
        
        return val;
    }

}
