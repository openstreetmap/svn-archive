/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * @author sebi
 *
 */
public class MapFactory
{
    private Map map;
    private PositionGenerator posGen;
    private java.util.Map<String, PointFeature> pointFeatures;
    private java.util.Map<String, LineFeature> lineFeatures;
    private java.util.Map<String, AreaFeature> areaFeatures;
    private Collection<Label> labels;
    private Collection<Label> labelsWOLineFeatures;
    
    public MapFactory(PositionGenerator posGen)
    {
        this.map = new Map();
        this.posGen = posGen;
        this.pointFeatures = new HashMap<String, PointFeature> ();
        this.lineFeatures = new HashMap<String, LineFeature>();
        this.areaFeatures = new HashMap<String, AreaFeature>();
        this.labels = new HashSet<Label>();
        this.labelsWOLineFeatures = new HashSet<Label>();
    }
    
    public void addPointFeature(PointFeature pointFeature)
    {
        PointFeature pF = pointFeatures.get(pointFeature.getId());
        boolean alreadyCreated = pF != null;
        if (! alreadyCreated)
        {
            System.out.println(pointFeature.getLabels().size() > 0 ? pointFeature.getLabels().get(0).getText(): "no label");
            posGen.generatePointFeaturePositions(pointFeature);
            pointFeatures.put(pointFeature.getId(), pointFeature);
            map.addFeature(pointFeature);
            labels.addAll(pointFeature.getLabels());
            labelsWOLineFeatures.addAll(pointFeature.getLabels());
        }
    }
    
    public void addLineFeature(LineFeature lineFeature)
    {
        LineFeature lF = lineFeatures.get(lineFeature.getId());
        boolean alreadyCreated = lF != null;
        if (! alreadyCreated)
        {
            posGen.generateLineFeaturePositions(lineFeature);
            lineFeatures.put(lineFeature.getId(), lineFeature);
            map.addFeature(lineFeature);
            labels.addAll(lineFeature.getLabels());
        }
    }
    
    public void addAreaFeature(AreaFeature areaFeature)
    {
        AreaFeature aF = areaFeatures.get(areaFeature.getId());
        boolean alreadyCreated = aF != null;
        if (! alreadyCreated)
        {
            posGen.generateAreaFeaturePositions(areaFeature);
            areaFeatures.put(areaFeature.getId(), areaFeature);
            map.addFeature(areaFeature);
            labels.addAll(areaFeature.getLabels());
            labelsWOLineFeatures.addAll(areaFeature.getLabels());
        }
    }
    
    public Map getMap()
    {
        return map;
    }
    
    public Evaluation getEvaluation()
    {
        Evaluation eval = new Evaluation();
       
        eval.addMetric(createPointPosMetric());
        eval.addMetric(createPointOverlapMetric());
        /*
        eval.addMetric(createLineOverlapMetric());
        */
        eval.addMetric(createLabelOverlapMetric());
        eval.addMetric(createCenterednessLineLabelMetric());
        
        return eval;
    }

    /**
     * @return
     */
    private Metric createCenterednessLineLabelMetric()
    {
        return new CenterednessLineLabelMetric(lineFeatures.values());
    }

    /**
     * @return
     */
    private Metric createLabelOverlapMetric()
    {
        return new LabelOverlapMetric(labels);
    }

    /**
     * @return
     */
    private Metric createLineOverlapMetric()
    {
        return new LineOverlapMetric(lineFeatures.values(), labelsWOLineFeatures);
    }

    /**
     * @return
     */
    private Metric createPointOverlapMetric()
    {
        return new PointOverlapMetric(pointFeatures.values(), labels);
    }

    /**
     * @return
     */
    private Metric createPointPosMetric()
    {
        return new PointPosMetric(pointFeatures.values());
    }
}
