package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.validator.PreferenceEditor;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Tests if there are segments that crosses in the same layer
 *
 * @author frsantos
 */
public class UnconnectedWays extends Test
{
	protected static int UNCONNECTED_WAYS = 1301;
	protected static final String PREFIX = PreferenceEditor.PREFIX + "." + UnconnectedWays.class.getSimpleName();

	Set<MyWaySegment> ways;
	Set<Node> endnodes; // nodes at end of way
	Set<Node> middlenodes; // nodes in middle of way
	Set<Node> othernodes; // nodes appearing at least twice

	double mindist;
	double minmiddledist;
	/**
	 * Constructor
	 */
	public UnconnectedWays()
	{
		super(tr("Unconnected ways."),
			  tr("This test checks if a way has an endpoint very near to another way"));
	}

	@Override
	public void startTest()
	{
		ways = new HashSet<MyWaySegment>();
		endnodes = new HashSet<Node>();
		middlenodes = new HashSet<Node>();
		othernodes = new HashSet<Node>();
		mindist = Main.pref.getDouble(PREFIX + ".node_way_distance", 10.0)/6378135.0;
		minmiddledist = Main.pref.getDouble(PREFIX + ".way_way_distance", 0.0)/6378135.0;
	}

	@Override
	public void endTest()
	{
		Map<Node, Way> map = new HashMap<Node, Way>();
		for(Node en : endnodes)
		{
			for(MyWaySegment s : ways)
			{
				if(s.nearby(en, mindist))
					map.put(en, s.w);
			}
		}
		if(map.size() > 0)
		{
			for(Map.Entry<Node, Way> error : map.entrySet())
			{
				errors.add(new TestError(this, Severity.WARNING,
				tr("Unconnected way end node near other way"), UNCONNECTED_WAYS,
				Arrays.asList(error.getKey(), error.getValue())));
			}
		}
		/* the following two use a shorter distance */
		if(minmiddledist > 0.0)
		{
			map.clear();
			for(Node en : middlenodes)
			{
				for(MyWaySegment s : ways)
				{
					if(s.nearby(en, minmiddledist))
						map.put(en, s.w);
				}
			}
			if(map.size() > 0)
			{
				for(Map.Entry<Node, Way> error : map.entrySet())
				{
					errors.add(new TestError(this, Severity.WARNING,
					tr("Unconnected way node near other way"), UNCONNECTED_WAYS,
					Arrays.asList(error.getKey(), error.getValue())));
				}
			}
			map.clear();
			for(Node en : othernodes)
			{
				for(MyWaySegment s : ways)
				{
					if(s.nearby(en, minmiddledist))
						map.put(en, s.w);
				}
			}
			if(map.size() > 0)
			{
				for(Map.Entry<Node, Way> error : map.entrySet())
				{
					errors.add(new TestError(this, Severity.WARNING,
					tr("Connected way end node near other way"), UNCONNECTED_WAYS,
					Arrays.asList(error.getKey(), error.getValue())));
				}
			}
		}
		ways = null;
		endnodes = null;
	}

	private class MyWaySegment
	{
		private Line2D line;
		public Way w;

		public MyWaySegment(Way w, Node n1, Node n2)
		{
			this.w = w;
			line = new Line2D.Double(n1.eastNorth.east(), n1.eastNorth.north(),
			n2.eastNorth.east(), n2.eastNorth.north());
		}

		public boolean nearby(Node n, double dist)
		{
			return !w.nodes.contains(n)
			&& line.ptSegDist(n.eastNorth.east(), n.eastNorth.north()) < dist;
		}
	}

	@Override
	public void visit(Way w)
	{
		if( w.deleted || w.incomplete )
			return;
		int size = w.nodes.size();
		if(size < 2)
			return;
		for(int i = 1; i < size; ++i)
		{
			if(i < size-1)
				addNode(w.nodes.get(i), middlenodes);
			ways.add(new MyWaySegment(w, w.nodes.get(i-1), w.nodes.get(i)));
		}
		addNode(w.nodes.get(0), endnodes);
		addNode(w.nodes.get(size-1), endnodes);
	}
	private void addNode(Node n, Set<Node> s)
	{
		Boolean m = middlenodes.contains(n);
		Boolean e = endnodes.contains(n);
		Boolean o = othernodes.contains(n);
		if(!m && !e && !o)
			s.add(n);
		else if(!o)
		{
			othernodes.add(n);
			if(e)
				endnodes.remove(n);
			else
				middlenodes.remove(n);
		}
	}
}
