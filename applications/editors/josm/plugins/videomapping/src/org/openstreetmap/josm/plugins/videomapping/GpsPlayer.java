package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Point;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.WayPoint;

//for GPS play control, secure stepping through list and interpolation work in current projection
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
			if(ls.indexOf(curr)==0) prev=null;else 	prev=ls.get(ls.indexOf(curr)-1);
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
			if(ls.indexOf(curr)+1<ls.size())
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
	
	//gets only points on the line of the GPS track between waypoints nearby the point m
	private Point getInterpolated(Point m)
	{
		Point leftP,rightP,highP,lowP;
		boolean invalid = false;
		Point p1 = Main.map.mapView.getPoint(getCurr().getEastNorth());
		if(getNext()!=null) //TODO outsource certain dub routines
		{
			Point p2 = Main.map.mapView.getPoint(getNext().getEastNorth());
			//determine which point is what
			if(p1.x<p2.x)
			{
				leftP=p1;
				rightP=p2;
			}
			else
			{
				leftP=p2;
				rightP=p1;
			}
			if(p1.y<p2.y)
			{
				highP=p1;
				lowP=p2;
			}
			else
			{
				highP=p2;
				lowP=p1;
			}
			//we might switch to neighbor segment
			if(m.x<leftP.x)
			{
				Point c = Main.map.mapView.getPoint(getCurr().getEastNorth());
				Point n = Main.map.mapView.getPoint(getNext().getEastNorth());
				if(n.x<c.x)	next(); else prev();
				invalid=true;
				m=leftP;
				System.out.println("entering left segment");
			}
			if(m.x>rightP.x)
			{
				Point c = Main.map.mapView.getPoint(getCurr().getEastNorth());
				Point n = Main.map.mapView.getPoint(getNext().getEastNorth());
				if(n.x>c.x)	next(); else prev();
				invalid=true;
				m=rightP;
				System.out.println("entering right segment");
			}
			//highP = Main.map.mapView.getPoint(l.getCurr().getEastNorth());
			//lowP = Main.map.mapView.getPoint(l.getNext().getEastNorth());
			if(!invalid)
			{
				float slope=(float)(highP.y-lowP.y) / (float)(highP.x - lowP.x);
				m.y = highP.y+Math.round(slope*(m.x-highP.x));
			}
		}
		else
		{
			//currently we are at the end
			Point p2 = Main.map.mapView.getPoint(getPrev().getEastNorth());
			if (p1.x>p2.x)
			{
				leftP=p2;
				rightP=p1;
			}
			else
			{
				leftP=p1;
				rightP=p2;
			}
			if(m.x>rightP.x)
			{
				m=rightP; //we can't move anywhere
			}
			else
			{
				prev();
			}
			
			
		}
		//System.out.println((m));
		return m;
	}
	
	//gets further infos for a point between two Waypoints
	public WayPoint getInterpolatedWaypoint(Point m)
	{	int a,b,length,lengthSeg;
		long timeSeg;
		float ratio;
		Time base;
		Point p2;
		
		Point curr =Main.map.mapView.getPoint(getCurr().getEastNorth());
		m =getInterpolated(m);
		if (getNext()!=null)
		{
			p2 =Main.map.mapView.getPoint(getNext().getEastNorth());
			timeSeg=getNext().getTime().getTime()-getCurr().getTime().getTime();
		}
		else
		{
			p2 =Main.map.mapView.getPoint(getPrev().getEastNorth());
			timeSeg=-(getPrev().getTime().getTime()-getCurr().getTime().getTime());
		}
		WayPoint w =new WayPoint(Main.map.mapView.getLatLon(m.x, m.y));
		//calc total traversal length
		a=Math.abs(curr.x-p2.x);
		b=Math.abs(curr.y-p2.y);
		lengthSeg= (int) Math.sqrt(Math.pow(a, 2)+Math.pow(b, 2));
		a=Math.abs(m.x-p2.x);
		b=Math.abs(m.y-p2.y);
		length= (int) Math.sqrt(Math.pow(a, 2)+Math.pow(b, 2));
		length=lengthSeg-length;
		ratio=(float)length/(float)lengthSeg;
		long inc=(long) (timeSeg*ratio);
		SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss:S");
		long old = getCurr().getTime().getTime();
		old=old+inc;
		System.out.print(length+"px ");
		System.out.print(ratio+"% ");
		System.out.print(inc+"ms ");
		System.out.println(df.format(old));
		Date t = new Date(old);
		//TODO we have to publish the new date to the node...
		return w;
	}
	
	public List<WayPoint> getInterpolatedLine(int interval)
	{
		Point p2;
		Point curr =Main.map.mapView.getPoint(getCurr().getEastNorth());
		if (getNext()!=null)
		{
			p2 =Main.map.mapView.getPoint(getNext().getEastNorth());
		}
		else
		{
			p2 =Main.map.mapView.getPoint(getPrev().getEastNorth());
		}
		int a=Math.abs(curr.x-p2.x);
		int b=Math.abs(curr.y-p2.y);
		int length= (int) Math.sqrt(Math.pow(a, 2)+Math.pow(b, 2));
		float step=length/interval;
		//TODO here we go
		return null;
	}
	

}
