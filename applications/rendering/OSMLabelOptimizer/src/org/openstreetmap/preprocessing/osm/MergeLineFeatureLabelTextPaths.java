/**
 * 
 */
package org.openstreetmap.preprocessing.osm;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.batik.bridge.UpdateManager;
import org.openstreetmap.osm.util.OSMDefsElement;
import org.openstreetmap.osm.util.OSMPathElement;
import org.openstreetmap.osm.util.OSMTextElement;
import org.openstreetmap.osm.util.OSMTextPathElement;
import org.openstreetmap.preprocessing.Preprocessor;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDefsElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGTextElement;
import org.w3c.dom.svg.SVGTextPathElement;

/**
 * @author sebi
 *
 */
public class MergeLineFeatureLabelTextPaths implements Preprocessor
{
    private SVGDocument rendered;
    private UpdateManager updateManager;
    
    private Map<String, LineFeatureLabelTextPaths> mappedTextPaths;
    
    public MergeLineFeatureLabelTextPaths(SVGDocument rendered, UpdateManager updateManager)
    {
        this.rendered = rendered;
        this.updateManager = updateManager;
        
        mappedTextPaths = new HashMap<String, LineFeatureLabelTextPaths>();
    }
    
    public void preprocess()
    {
        initMappedTextPaths();
        mergeTextPaths();
        createNewOSMTextPaths();
    }
    
    private void createNewOSMTextPaths()
    {
        for (LineFeatureLabelTextPaths lp : mappedTextPaths.values())
        {
            lp.createNewOSMTextPaths();
        }
    }
    
    private void mergeTextPaths()
    {
        for (LineFeatureLabelTextPaths lp : mappedTextPaths.values())
        {
            lp.mergeTextPaths();
        }
    }
    
    private void initMappedTextPaths()
    {
        NodeList nl = rendered.getElementsByTagName("textPath");
        for (int i = 0; i < nl.getLength(); ++i)
        {
            SVGTextPathElement el = (SVGTextPathElement)nl.item(i);
            
            OSMTextPathElement textPathElement = new OSMTextPathElement(el, updateManager);
            String labelText = textPathElement.getText();
            String href = textPathElement.getHref();
            
            SVGTextElement textEl = (SVGTextElement)el.getParentNode();
            OSMTextElement textElement = new OSMTextElement(textEl, updateManager);
            
            SVGPathElement pathEl = (SVGPathElement)(rendered.getElementById(href.substring(1)));
            OSMPathElement pathElement = new OSMPathElement(pathEl, updateManager);
            
            LineFeatureLabelTextPaths lp = null;
            if ((lp = mappedTextPaths.get(labelText)) == null)
            {
                lp = new LineFeatureLabelTextPaths();
            }
            lp.addTextPath(textPathElement, textElement, pathElement);
            mappedTextPaths.put(labelText, lp);
        }
    }

    private class LineFeatureLabelTextPaths
    {
        private Vector<LineFeatureLabelTextPath> textPaths;
        private Set<MergedLineFeatureLabelTextPath> mergedTextPaths;
        
        public LineFeatureLabelTextPaths()
        {
            textPaths = new Vector<LineFeatureLabelTextPath>();
            mergedTextPaths = new HashSet<MergedLineFeatureLabelTextPath>();
        }
        
        public void addTextPath(OSMTextPathElement textPath, OSMTextElement text, OSMPathElement path)
        {
            textPaths.add(new LineFeatureLabelTextPath(textPath, text, path));
        }
        
        public void createNewOSMTextPaths()
        {
            for (MergedLineFeatureLabelTextPath m: mergedTextPaths)
            {
                m.createNewOSMTextPath();
            }
        }
        
        public void mergeTextPaths()
        {
            for (LineFeatureLabelTextPath lf : textPaths)
            {
                for (LineFeatureLabelTextPath lf2 : textPaths)
                {
                    if (lf.isCompatibleWith(lf2))
                    {
                        MergedLineFeatureLabelTextPath m1_old = lf.merged;
                        MergedLineFeatureLabelTextPath m2_old = lf2.merged;
                        MergedLineFeatureLabelTextPath m = lf.mergeWith(lf2);
                        if (m != null)
                        {
                            mergedTextPaths.remove(m1_old);
                            mergedTextPaths.remove(m2_old);
                            mergedTextPaths.add(m);
                        }
                    }
                }
            }
        }
        
        private class LineFeatureLabelTextPath
        {
            private OSMTextPathElement textPath;
            private OSMTextElement text;
            private OSMPathElement path;
            private boolean reversedPath;
            private Point2D start;
            private Point2D end;
            private MergedLineFeatureLabelTextPath merged;
            
            public LineFeatureLabelTextPath(OSMTextPathElement textPath, OSMTextElement text, OSMPathElement path)
            {
                this.textPath = textPath;
                this.text = text;
                this.path = path;
                this.reversedPath = textPath.getHref().matches(".*_reverse_.*");
                start = path.getStart();
                end = path.getEnd();
                merged = null;
            }
            
            public boolean isCompatibleWith(LineFeatureLabelTextPath lf)
            {
                //boolean comp = reversedPath == lf.reversedPath;
                boolean comp = (textPath.getFontSize().equals(lf.textPath.getFontSize()));
                comp = comp && (text.getCssString().equals(lf.text.getCssString()));
                comp = comp && (start.equals(lf.end) || end.equals(lf.start));
                return comp;
            }
            
            public MergedLineFeatureLabelTextPath mergeWith(LineFeatureLabelTextPath lf)
            {
                MergedLineFeatureLabelTextPath mp = null;
                if (merged == null && lf.merged == null)
                {
                    mp = new MergedLineFeatureLabelTextPath();
                    if (mp.addTextPath(this) && mp.addTextPath(lf))
                    {
                        merged = mp;
                        lf.merged = mp;
                    }
                }
                else if (merged != null && lf.merged != null && merged != lf.merged)
                {
                    Vector<LineFeatureLabelTextPath> tmp = merged.getMergedPaths();
                    if (tmp.get(0) == this)
                    {
                        for (LineFeatureLabelTextPath p: tmp)
                        {
                            if (lf.merged.addTextPath(p))
                            {
                                mp = lf.merged;
                                p.merged = lf.merged;
                            }
                        }
                    }
                    else if (tmp.get(tmp.size() - 1) == this)
                    {
                        int z = tmp.size() - 1;
                        for (int i = z; i >= 0; --i)
                        {
                            LineFeatureLabelTextPath p = tmp.get(i);
                            if (lf.merged.addTextPath(p))
                            {
                                mp = lf.merged;
                                p.merged = lf.merged;
                            }
                        }
                    }
                }
                else if (merged != null || lf.merged != null)
                {
                    if (merged != null)
                    {
                        if (merged.addTextPath(lf))
                        {
                            mp = merged;
                            lf.merged = mp;
                        }
                    }
                    if (lf.merged != null)
                    {
                        if (lf.merged.addTextPath(this))
                        {
                            mp = lf.merged;
                            merged = mp;
                        }
                    }
                }

                return mp;
            }
            
        }
        
        private class MergedLineFeatureLabelTextPath
        {
            private Vector<LineFeatureLabelTextPath> mergedPaths;
            private Point2D start;
            private Point2D end;
            
            public MergedLineFeatureLabelTextPath()
            {
                mergedPaths = new Vector<LineFeatureLabelTextPath>();
                start = null;
                end = null;
            }
            
            public Vector<LineFeatureLabelTextPath> getMergedPaths()
            {
                return mergedPaths;
            }
            
            public void createNewOSMTextPath()
            {
                String newId = getNewId();
                System.out.println(mergedPaths.get(0).textPath.getText());
                System.out.println(newId);
                
                SVGDefsElement defsEl = (SVGDefsElement)rendered.getElementById("defs-ways");
                OSMDefsElement defsElement = new OSMDefsElement(defsEl, updateManager);
                
                OSMPathElement path = defsElement.addNewPathElement(newId);
                
                for (LineFeatureLabelTextPath l: mergedPaths)
                {
                    path.addPath(l.path);
                }
                
                for (LineFeatureLabelTextPath l: mergedPaths)
                {
                    l.textPath.setHref(newId);
                }
            }
            
            private String getNewId()
            {
                String id = "way_connected_";
                
                for (LineFeatureLabelTextPath l: mergedPaths)
                {
                    id = id.concat(l.reversedPath?"reversed_":"normal_");
                    id = id.concat((new Long(l.textPath.getId())).toString());
                    if (l != mergedPaths.get(mergedPaths.size() - 1))
                    {
                        id = id.concat("_");
                    }
                }
                
                return id;
            }
            
            public boolean addTextPath(LineFeatureLabelTextPath lf)
            {
                boolean m = false;
                
                Point2D startN = lf.start;
                Point2D endN = lf.end;
                
                if (start == null || end == null)
                {
                    this.start = startN;
                    this.end = endN;
                    mergedPaths.add(lf);
                    m = true;
                }
                else if (this.start.equals(lf.end))
                {
                    this.start = startN;
                    mergedPaths.insertElementAt(lf, 0);
                    m = true;
                }
                else if (this.end.equals(lf.start))
                {
                    this.end = endN;
                    mergedPaths.add(lf);
                    m = true;
                }
                
                return m;
            }            
        }
    }
}
