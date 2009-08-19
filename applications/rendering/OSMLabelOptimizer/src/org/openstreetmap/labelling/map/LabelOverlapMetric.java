/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * @author sebi
 *
 */
public class LabelOverlapMetric implements Metric
{
    private java.util.Map<Label, LabelPosition> currentPositions;
    private Set<LabelPosition> activePositions;
    private java.util.Map<LabelPosition, Set<LabelPosition>> intersections;
    private int value;
    private int oldValue;
    private boolean revertable;
    private Label lastChanged;
    private LabelPosition newActivePos;
    private LabelPosition oldActivePos;
    
    public LabelOverlapMetric(Collection<Label> labels)
    {
        currentPositions = new HashMap<Label, LabelPosition>();
        activePositions = new HashSet<LabelPosition> ();
        intersections = new HashMap<LabelPosition, Set<LabelPosition>>();
        value = 0;
        oldValue = 0;
        revertable = false;
        for (Label l : labels)
        {
            l.addMetric(this);
            activePositions.add(l.getLabelPosition());
            currentPositions.put(l, l.getLabelPosition());
        }
        calculateIntersections();
        countIntersections();
    }
    
    private void calculateIntersections()
    {
        Set<Label> labels = currentPositions.keySet();
        for (Label l1 : labels)
        {
            Vector<LabelPosition> pos1 = l1.getPositions();
            for (LabelPosition p1 : pos1)
            {
                for (Label l2 : labels)
                {
                    if (l1 != l2)
                    {
                        Vector<LabelPosition> pos2 = l2.getPositions();
                        for (LabelPosition p2 : pos2)
                        {
                            Set<LabelPosition> alreadyInserted = null;

                            if (intersections.containsKey(p1))
                            {    
                                alreadyInserted = intersections.get(p1);
                            }

                            Rectangle2D bb1 = p1.getBoundingBox();
                            Rectangle2D bb2 = p2.getBoundingBox();

                            if (bb1.intersects(bb2))
                            {
                                if (alreadyInserted == null)
                                {
                                    alreadyInserted = new HashSet<LabelPosition> ();
                                }

                                alreadyInserted.add(p2);
                                intersections.put(p1, alreadyInserted);                       
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void countIntersections()
    {
        for (LabelPosition pos : activePositions)
        {
            value = value + numberOfActiveIntersectionsWith(pos);
        }
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#getValue()
     */
    public double getValue()
    {
        //System.out.println("value: " + value);
        return value;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#getWeight()
     */
    public double getWeight()
    {
        return 40;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#recalculate(org.openstreetmap.labelling.map.Label)
     */
    public void recalculate(Label changedLabel)
    {
        oldValue = value;
        revertable = true;
        lastChanged = changedLabel;
        LabelPosition newPos = changedLabel.getLabelPosition();
        LabelPosition pos = currentPositions.get(changedLabel);
        oldActivePos = pos;
        newActivePos = newPos;
        currentPositions.put(changedLabel, newPos);
        
        activePositions.remove(pos);
        
        int n1 = numberOfActiveIntersectionsWith(pos);
        //System.out.println("n1: " + n1);
        value = value - n1;
        
        for (LabelPosition pos_tmp : activePositions)
        {
            Set<LabelPosition> inters = intersections.get(pos_tmp);

            if (inters != null && inters.contains(pos) && ! inters.contains(newPos))
            {
                value = value - 1;
            }
            else if (inters != null && ! inters.contains(pos) && inters.contains(newPos))
            {
                value = value + 1;
            }
        }

        int n2 = numberOfActiveIntersectionsWith(newPos);
        //System.out.println("n2: " + n2);
        value = value + n2;
       
        activePositions.add(newPos);
    }
    
    public void revert()
    {
        if (revertable)
        {
            value = oldValue;
            revertable = false;
            currentPositions.put(lastChanged, oldActivePos);
            activePositions.remove(newActivePos);
            activePositions.add(oldActivePos);
        }
    }
    
    private int numberOfActiveIntersectionsWith(LabelPosition pos)
    {
        int r = 0;
        
        Set<LabelPosition> lp = intersections.get(pos);
        if (lp == null)
        {
            return r;
        }
        
        for (LabelPosition pos_tmp : lp)
        {
            
            if (activePositions.contains(pos_tmp))
            {
                r = r + 1;
            }
        }
        
        return r;
    }

}
