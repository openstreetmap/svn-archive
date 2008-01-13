package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Check cyclic ways for errors
 * 
 * @author jrreid
 */
public class WronglyOrderedWays extends Test  {
    /** All ways, grouped by cells */
    Map<Point2D,List<Way>> _cellWays;
    /** The already detected errors */
    Bag<Way, Way> _errorWays;

	/**
	 * Constructor
	 */
	public WronglyOrderedWays() 
	{
		super(tr("Wrongly Ordered Ways."),
			  tr("This test checks the direction of water, land and coastline ways."));
	}

    @Override
    public void startTest() 
    {
        _cellWays = new HashMap<Point2D,List<Way>>(1000);
        _errorWays = new Bag<Way, Way>();
    }

    @Override
    public void endTest() 
    {
        _cellWays = null;
        _errorWays = null;
    }
    
	@Override
	public void visit(Way w) 
	{
		String errortype = "";
		
        if( w.deleted || w.incomplete )
            return;
        
        String natural = w.get("natural");
        if( natural == null)
            return;
        
        if(!natural.equals("coastline") ){
        	errortype = "Clockwise coastline";
        }else if(!natural.equals("water") ){
        	errortype = "Clockwise water";
        }else if(!natural.equals("land") ){
        	errortype = "Clockwise land";
        } else {
        	return;
        }
        
        /**
         * Test the directionality of the way
         * 
         * Checks if the node following the northern-most node is further 
         * west then the node previous
         * 
         * Only tests ways that the first and last node is the same currently
         * 
         */
        
        if(w.nodes.get(0) == w.nodes.get(w.nodes.size()-1)){
	        int maxnode = -1;
	        double maxlat = -90; 
	        
	        for (int node = 0; node < w.nodes.size(); node++){
	        	double lat = w.nodes.get(node).coor.lat();
	        	if(lat > maxlat){
	        		maxnode = node;
	        		maxlat = lat;
	        	}
	        }	        
	        
        	int nextnode;
        	int prevnode;
        	
        	// Determine the previous and next nodes in the loop
        	if(maxnode==0){
        		nextnode = 1;
    			prevnode = w.nodes.size()-1;
        	}else if(maxnode == w.nodes.size()-1){
        		nextnode = 0;
    			prevnode = maxnode - 1;
        	} else {
        		nextnode = maxnode + 1;
        		prevnode = maxnode - 1;
        	}
        	
        	double prevlon = w.nodes.get(prevnode).coor.lon();
        	double nextlon = w.nodes.get(nextnode).coor.lon();
        	
        	if(((natural.equals("coastline") || natural.equals("land")) && prevlon < nextlon) 
        			|| (natural.equals("water") && prevlon > nextlon)){	        
	        	List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
	        	primitives.add(w);
	        	errors.add( new TestError(this, Severity.WARNING, tr(errortype), primitives) );
	        	_errorWays.add(w,w);		
        	}        
        }
	}
}
