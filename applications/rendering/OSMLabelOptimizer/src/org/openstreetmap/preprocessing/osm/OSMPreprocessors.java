/**
 * 
 */
package org.openstreetmap.preprocessing.osm;

import org.apache.batik.bridge.UpdateManager;
import org.openstreetmap.preprocessing.Preprocessors;
import org.w3c.dom.svg.SVGDocument;

/**
 * @author sebi
 *
 */
public class OSMPreprocessors extends Preprocessors
{
    private SVGDocument rendered;
    private UpdateManager updateManger;
    
    public OSMPreprocessors(SVGDocument rendered, UpdateManager updateManager)
    {
        this.rendered = rendered;
        this.updateManger = updateManager;
        
        initPreprocessors();
    }
    
    private void initPreprocessors()
    {
        addPreprocessor(new MergeLineFeatureLabelTextPaths(rendered, updateManger));
    }
}
