/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */

package com.innovant.josm.plugin.routing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

import com.innovant.josm.jrt.core.RoutingGraph;
import com.innovant.josm.jrt.core.RoutingGraph.Algorithm;
import com.innovant.josm.jrt.osm.OsmEdge;


/**
 * This class holds all the routing data and operations
 * @author juangui
 * @author Jose Vidal
 *
 */
public class RoutingModel {

	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(RoutingModel.class);

	/**
	 * Graph to calculate route
	 */
	public RoutingGraph routingGraph=null;

	/**
	 * List of nodes that the route has to traverse
	 */
	private List<Node> nodes=null;

	private List<OsmEdge> path=null;

	/**
	 * Flag to advise about changes in the selected nodes.
	 */
	private boolean changeNodes=false;

	/**
	 * Flag to advise about changes in ways.
	 */
	private boolean changeOneway=false;

	/**
	 * Default Constructor.
	 */
	public RoutingModel(DataSet data) {
		nodes = new ArrayList<Node>();
		System.out.println("gr " + data);
		routingGraph = new RoutingGraph(data);
	}

	/**
	 * Method that returns the selected nodes to calculate route.
	 * @return the selectedNodes
	 */
	public List<Node> getSelectedNodes() {
		return nodes;
	}

	/**
	 * Adds a node to the route node list.
	 * @param node the node to add.
	 */
	public void addNode(Node node) {
		nodes.add(node);
		this.changeNodes=true;
	}

	/**
	 * Removes a node from the route node list.
	 * @param index the index of the node to remove.
	 */
	public void removeNode(int index) {
		if (nodes.size()>index) {
			nodes.remove(index);
			this.changeNodes=true;
		}
	}

	/**
	 * Inserts a node in the route node list.
	 * @param index the index where the node will be inserted
	 * @param node the node to be inserted
	 */
	public void insertNode(int index, Node node) {
		if (nodes.size()>=index) {
			nodes.add(index, node);
			this.changeNodes=true;
		}
	}

	/**
	 * Reverse list of nodes
	 */
	public void reverseNodes() {
		List<Node> aux = new ArrayList<Node>();
		for (Node n : nodes) {
			aux.add(0,n);
		}
		nodes = aux;
		this.changeNodes=true;
	}

	/**
	 * Get the edges of the route.
	 * @return A list of edges forming the shortest path
	 */
	public List<OsmEdge> getRouteEdges() {
		if (this.changeNodes || path==null)
		{
			path=this.routingGraph.applyAlgorithm(nodes, Algorithm.ROUTING_ALG_DIJKSTRA);
			this.changeNodes=false;
			this.changeOneway=false;
		}
		return path;
	}

	/**
	 * Marks that some node or the node order has changed so the path should be computed again
	 */
	public void setNodesChanged() {
		this.changeNodes = true;
	}

	/**
	 * Marks that "Ignore oneway" option has changed so the path should be computed again
	 */
	public void setOnewayChanged() {
		this.changeOneway = true;
	}

	/**
	 * Marks that "Ignore oneway" option has changed so the path should be computed again
	 */
	public boolean getOnewayChanged() {
		return this.changeOneway;
	}

	/**
	 * Resets all data.
	 */
	public void reset() {
		nodes.clear();
		this.changeNodes=true;
	}

}
