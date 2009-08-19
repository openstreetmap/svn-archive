/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.batik.bridge.UpdateManager;
import org.openstreetmap.labelling.map.Evaluation;
import org.openstreetmap.labelling.map.Map;
import org.openstreetmap.labelling.map.MapFactory;
import org.openstreetmap.osm.util.OSMCircleElement;
import org.openstreetmap.osm.util.OSMGElement;
import org.openstreetmap.osm.util.OSMPathElement;
import org.openstreetmap.osm.util.OSMTextElement;
import org.openstreetmap.osm.util.OSMTextPathElement;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGCircleElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGTextElement;
import org.w3c.dom.svg.SVGTextPathElement;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGUseElement;

/**
 * @author sebi
 *
 */
public class OSMMapFactory
{
    private SVGDocument rendered;
    private UpdateManager updateManager;
    private OSMPositionGenerator posGen;
    private MapFactory mapFactory;
    
    private java.util.Map<Point2D, Vector<OSMTextElement>> areaFeatureLabels;
    
    public OSMMapFactory(SVGDocument rendered, UpdateManager updateManager)
    {
        this.rendered = rendered;
        this.updateManager = updateManager;
        this.posGen = new OSMPositionGenerator(rendered, updateManager);
        this.mapFactory = new MapFactory(posGen);
        
        areaFeatureLabels = new HashMap<Point2D, Vector<OSMTextElement>>();
        
        useMapFactory();
    }
    
    private void useMapFactory()
    {
        createLineFeatures();
        createPointFeatures();
        createAreaFeatures();
    }
    
    private void createLineFeatures()
    {
        NodeList nl = rendered.getElementsByTagName("textPath");
        
        for (int i = 0; i < nl.getLength(); ++i)
        {
            SVGTextPathElement el = (SVGTextPathElement)nl.item(i);
            OSMTextPathElement textPathElement = new OSMTextPathElement(el, updateManager);
            SVGPathElement pathEl = (SVGPathElement)rendered.getElementById(textPathElement.getHref().substring(1));
            OSMPathElement pathElement = new OSMPathElement(pathEl, updateManager);
            mapFactory.addLineFeature(new OSMLineFeature(textPathElement, pathElement));
        }
    }
    
    private void createPointFeatures()
    {
        java.util.Map<Point2D, OSMPointFeature> symbols = new HashMap<Point2D, OSMPointFeature>();
        
        NodeList nl_g = rendered.getElementsByTagName("g");
        
        int n = 1;
        
        for (int i = 0; i < nl_g.getLength(); ++i)
        {
            SVGGElement el = (SVGGElement)nl_g.item(i);
            OSMGElement gElement = new OSMGElement(el, updateManager);
            
            if (gElement.isPOISymbol())
            {
                symbols.put(gElement.getPOICoordinate(), new OSMPointFeature("pointFeature_" + n, gElement));
                ++n;
            }
        }
        
        NodeList nl_circle = rendered.getElementsByTagName("circle");
        for (int i = 0; i < nl_circle.getLength(); ++i)
        {
            SVGCircleElement el = (SVGCircleElement)nl_circle.item(i);
            OSMCircleElement circleElement = new OSMCircleElement(el, updateManager);
            
            if (circleElement.isPOISymbol())
            {
                symbols.put(circleElement.getPOICoordinate(), new OSMPointFeature("pointFeature_" + n, circleElement));
                ++n;
            }
        }
        
        NodeList nl_text = rendered.getElementsByTagName("text");
        
        Vector<OSMPointFeature> pFs = new Vector<OSMPointFeature>(symbols.values());
        
        for (int i = 0; i < nl_text.getLength(); ++i)
        {
            SVGTextElement el = (SVGTextElement)nl_text.item(i);
   
            if (el.getAttribute("k").equals("name"))
            {
                OSMTextElement textElement = new OSMTextElement(el, updateManager);
                Point2D p = textElement.getPoint();
                OSMPointFeature pF = symbols.get(p);
                if (pF != null)
                {
                    pF.addTextElement(textElement);
                }
                else
                {
                    Vector<OSMTextElement> v = areaFeatureLabels.get(p);
                    if (v == null)
                    {
                        v = new Vector<OSMTextElement>();
                    }
                    v.add(textElement);
                    areaFeatureLabels.put(p, v);
                }
            }
            else if (el.getAttribute("k").equals("addr:housenumber"))
            {
                OSMTextElement textElement = new OSMTextElement(el, updateManager);
                Point2D p = textElement.getPoint();
                Vector<OSMTextElement> v = areaFeatureLabels.get(p);
                if (v == null)
                {
                    v = new Vector<OSMTextElement>();
                }
                v.add(textElement);
                areaFeatureLabels.put(p, v);
            }
        }
        
        for (OSMPointFeature pF: pFs)
        {
            mapFactory.addPointFeature(pF);
        }
    }
    
    private void createAreaFeatures()
    {
        //TODO: extend this function!
        int n = 0;
        for (Vector<OSMTextElement> els : areaFeatureLabels.values())
        {
            OSMAreaFeature af = new OSMAreaFeature("areaFeature_" + n, null);
            for (OSMTextElement el : els)
            {
                af.addTextElement(el);
            }
            mapFactory.addAreaFeature(af);
            ++n;
        }
    }
    
    public Map getMap()
    {
        return mapFactory.getMap();
    }
    
    public Evaluation getEvaluation()
    {
        return mapFactory.getEvaluation();
    }
    
    public void render()
    {
        posGen.render();
    }
}
