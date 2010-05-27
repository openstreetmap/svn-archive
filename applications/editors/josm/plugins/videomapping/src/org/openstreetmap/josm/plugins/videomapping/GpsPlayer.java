package org.openstreetmap.josm.plugins.videomapping;

import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.WayPoint;

//for GPS play control secure stepping througt list
public class GpsPlayer {
	private List<WayPoint> ls;
	private WayPoint prev,curr,next;
	

	public WayPoint getPrev() {
		return prev;
	}

	public WayPoint getCurr() {
		return curr;
	}

	public WayPoint getNext() {
		return next;
	}

	public GpsPlayer(List<WayPoint> l) {
		super();
		this.ls = l;
		prev=null;
		curr=ls.get(0);
		next=ls.get(1);
	}
	
	public void next() {		
		if(ls.indexOf(curr)+1<ls.size())
		{
			prev=curr;
			curr=next;
			if(ls.indexOf(curr)+1==ls.size()) next=null;
			else next=ls.get(ls.indexOf(curr)+1);
		}
		else next=null;
		
	}
	
	public void prev()
	{
		if(ls.indexOf(curr)>0)
		{			
			next =curr;
			curr=prev;
			prev=ls.get(ls.indexOf(curr)-1);;
		}
		else prev=null;		
	}
	
	public void jump(WayPoint p)
	{
		if(ls.contains(p))
		{
			curr=p;
			if(ls.indexOf(curr)>0)
			{
				prev=ls.get(ls.indexOf(curr)-1);
			}
			else prev=null;
			if(ls.indexOf(curr)<ls.size())
			{
				next=ls.get(ls.indexOf(curr)+1);
			}
			else next=null;
			
		}
	}
	
	public void jump(int t)
	{
		if ((ls.indexOf(curr)+t>0)&&(ls.indexOf(curr)<ls.size()))
		{
			jump(ls.get(ls.indexOf(curr)+t));
		}		
	}
	

}
