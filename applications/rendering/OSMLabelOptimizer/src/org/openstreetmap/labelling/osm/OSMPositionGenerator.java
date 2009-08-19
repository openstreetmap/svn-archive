/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.batik.bridge.UpdateManager;
import org.openstreetmap.labelling.map.AreaFeature;
import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.labelling.map.LineFeature;
import org.openstreetmap.labelling.map.LineLabelPosition;
import org.openstreetmap.labelling.map.PointFeature;
import org.openstreetmap.labelling.map.PointLabelPosition;
import org.openstreetmap.labelling.map.Position;
import org.openstreetmap.labelling.map.PositionGenerator;
import org.openstreetmap.osm.util.OSMTextPathElement;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGTextPathElement;

/**
 * @author sebi
 *
 */
public class OSMPositionGenerator implements PositionGenerator
{
    private SVGDocument svg;
    private UpdateManager updateManager;
    private Vector<OSMLabel> labels;
    
    public OSMPositionGenerator(SVGDocument svg, UpdateManager updateManager)
    {
        this.svg = svg;
        this.updateManager = updateManager;
        this.labels = new Vector<OSMLabel>();
    }
        
    
    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PositionGenerator#generateAreaFeaturePositions(org.openstreetmap.labelling.map.AreaFeature, java.text.AttributedString)
     */
    public void generateAreaFeaturePositions(AreaFeature af)
    {
        if (! (af instanceof OSMAreaFeature))
        {
            return;
        }
        
        OSMAreaFeature afOSM = (OSMAreaFeature)af;
        
        for (Label l : afOSM.getLabels())
        {
            OSMAreaFeatureLabel afLabel = (OSMAreaFeatureLabel)l;
            Vector<LabelPosition> llp = afLabel.getPositions();
            llp.add(new LabelPosition(afLabel, afLabel.getBB()));
            labels.add(afLabel);
        }
        
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PositionGenerator#generateLineFeaturePositions(org.openstreetmap.labelling.map.LineFeature, java.text.AttributedString)
     */
    public void generateLineFeaturePositions(LineFeature lf)
    {
        if (!(lf instanceof OSMLineFeature))
        {
            return;
        }
        
        OSMLineFeature OSMLf = (OSMLineFeature)lf;
        
        Vector<OSMTextPathElement> els = new Vector<OSMTextPathElement>();
        
        NodeList nl = svg.getElementsByTagName("textPath");
        
        for (int i = 0; i < nl.getLength(); ++i)
        {
            SVGTextPathElement el = (SVGTextPathElement)nl.item(i);
            OSMTextPathElement textPathElement = new OSMTextPathElement(el, updateManager);
            String id = textPathElement.getHref();
            if (id.equals(lf.getId()))
            {
                els.add(textPathElement);
            }
        }
        
        double textLength = OSMLf.getTextLength();
        String text = OSMLf.getText();
        
        long toCreateOrDelete = Math.round((lf.getLength() - 4*els.size()*textLength)/(4*els.size()*textLength));
        
        boolean create = toCreateOrDelete > 0;
        
        if (create)
            System.out.println("create Labels: " + text);
        else
            System.out.println("delete Labels: " + text);
        
        for (long i = 0; i < Math.abs(toCreateOrDelete); ++i)
        {
            OSMTextPathElement textPathElement = els.firstElement();
            final SVGTextPathElement tP = textPathElement.getSVGTextPathElement();

            if (create)
            {
                final Node tp_parent = tP.getParentNode();
                final Node tp_new = tp_parent.cloneNode(true);
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        tp_parent.getParentNode().appendChild(tp_new);
                    }
                };
                try
                {
                    updateManager.getUpdateRunnableQueue().invokeAndWait(r);
                }
                catch (InterruptedException ex)
                {
                    System.out.println(ex.getLocalizedMessage());
                    System.exit(1);
                }
                 
                SVGTextPathElement textPathNew = (SVGTextPathElement)tp_new.getChildNodes().item(1);
                OSMTextPathElement tPnew = new OSMTextPathElement(textPathNew, updateManager);
                els.add(tPnew);
            }
            else if (els.size() > 1)
            {
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        tP.getParentNode().getParentNode().removeChild(tP.getParentNode());
                    }
                };
                try
                {
                    updateManager.getUpdateRunnableQueue().invokeAndWait(r);
                }
                catch (InterruptedException ex)
                {
                    System.out.println(ex.getLocalizedMessage());
                    System.exit(1);
                }
                els.remove(0);
            }
        }
     
        Vector<Double> possibleValues = new Vector<Double>();
        double tpos = textLength/4;
        double l = lf.getLength();
        while (tpos < l)
        {
            possibleValues.add(tpos/l);
            tpos += textLength/4;
        }
        possibleValues.add(0.5);
        
        for (OSMTextPathElement textPathElement : els)
        {
            Vector<LabelPosition> llp = new Vector<LabelPosition>();
            OSMLineFeatureLabel label = new OSMLineFeatureLabel(lf, text, llp, textPathElement);

            for (Double d : possibleValues)
            {
                textPathElement.setStartOffset(d);
                try
                {
                    LineLabelPosition pos = new LineLabelPosition(label, textPathElement.getBB(), d);
                    llp.add(pos);
                }
                catch (DOMException e)
                {
                    System.out.println("Don't use " + d.toString() + " for label " + text);
                }
            }

            lf.addLabel(label);
            labels.add(label);
        }
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PositionGenerator#generatePointFeaturePositions(org.openstreetmap.labelling.map.PointFeature, java.text.AttributedString)
     */
    public void generatePointFeaturePositions(PointFeature pointFeature)
    {
        if (! (pointFeature instanceof OSMPointFeature))
        {
            return;
        }
        OSMPointFeature pfOSM = (OSMPointFeature)pointFeature;
        
        for (Label l : pfOSM.getLabels())
        {
            OSMPointFeatureLabel lOSM = (OSMPointFeatureLabel)l;
            Vector<LabelPosition> llp = l.getPositions();
            
            for (Position p : Position.values())
            {
                lOSM.setPointLabelPosition(p);
                PointLabelPosition plp = new PointLabelPosition(lOSM, lOSM.getBB(), p);
                llp.add(plp);
            }
            labels.add(lOSM);
        }

    }
    
    public void render()
    {
        for (OSMLabel l : labels)
        {
            l.render();
        }
    }
}
