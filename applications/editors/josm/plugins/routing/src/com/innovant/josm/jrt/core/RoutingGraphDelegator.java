/**
 * 
 */
package com.innovant.josm.jrt.core;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.GraphDelegator;

/**
 * @author jose
 *
 */
public class RoutingGraphDelegator<V, E> extends GraphDelegator<V, E> {

	/**
	 * Logger.
	 */
	static Logger logger = Logger.getLogger(RoutingGraphDelegator.class);
	
	public String name;
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoutingGraphDelegator(Graph<V, E> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public double getEdgeWeight(E arg0) {
		logger.debug("call getEdgeWeight");
		return super.getEdgeWeight(arg0);
	}
}
