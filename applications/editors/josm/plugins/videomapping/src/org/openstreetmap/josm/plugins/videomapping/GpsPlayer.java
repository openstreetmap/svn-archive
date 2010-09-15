package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Point;
import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
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
	private WayPoint start;
	private Timer t;
	private TimerTask ani; //for moving trough the list
	private boolean autoCenter;
	

	public WayPoint getPrev() {
		return prev;
	}

	public WayPoint getCurr() {
		return curr;
	}

	public WayPoint getNext() {
		return next;
	}
	
	public WayPoint getWaypoint(long relTime)
	{
		int pos = Math.round(relTime/1000);//TODO ugly quick hack	
		return ls.get(pos);
	}

	public GpsPlayer(List<WayPoint> l) {
		super();
		this.ls = l;
		//set start position
		start=ls.get(0);
		prev=null;
		curr=ls.get(0);
		next=ls.get(1);
	}
	
	// one secure step forward
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
	
	//one secure step backward
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
	
	//select the given waypoint as center
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
	
	//walk k waypoints forward/backward
	public void jumpRel(int k)
	{

		if ((ls.indexOf(curr)+k>0)&&(ls.indexOf(curr)<ls.size())) //check range
		{
			jump(ls.get(ls.indexOf(curr)+k));
		}
		Main.map.mapView.repaint(); //seperate modell and view logic...
	}
	
	//select the k-th waypoint
	public void jump(int k)
	{
		if (k>0)
		{
			if ((ls.indexOf(curr)+k>0)&&(ls.indexOf(curr)<ls.size())) //check range
			{
				jump(ls.get(k));
			}
			Main.map.mapView.repaint();
		}
	}
	
	//go to the position at the timecode e.g.g "14:00:01";
	public void jump(Time GPSAbsTime)
	{
		jump(getWaypoint(GPSAbsTime.getTime()-start.getTime().getTime())); //TODO replace Time by Date?
	}
	
	//go to the position at 
	public void jump(Date GPSDate)
	{
		long s,m,h,diff;
		//calculate which waypoint is at the offset
		System.out.println(start.getTime());
		System.out.println(start.getTime().getHours()+":"+start.getTime().getMinutes()+":"+start.getTime().getSeconds());
		s=GPSDate.getSeconds()-start.getTime().getSeconds();
		m=GPSDate.getMinutes()-start.getTime().getMinutes();
		h=GPSDate.getHours()-start.getTime().getHours();
		diff=s*1000+m*60*1000+h*60*60*1000; //TODO ugly hack but nothing else works right
		jump(getWaypoint(diff)); 
	}
	
	//gets only points on the line of the GPS track (between waypoints) nearby the point m
	private Point getInterpolated(Point m)
	{
		Point leftP,rightP,highP,lowP;
		boolean invalid = false; //when we leave this segment
		Point p1 = Main.map.mapView.getPoint(getCurr().getEastNorth());
		Point p2 = getEndpoint();
		//determine which point is what
		leftP=getLeftPoint(p1, p2);
		rightP=getRightPoint(p1,p2);
		highP=getHighPoint(p1, p2);
		lowP=getLowPoint(p1, p2);
		if(getNext()!=null)
		{
			//we might switch to one neighbor segment
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
			if(!invalid)
			{
				float slope = getSlope(highP, lowP);
				m.y = highP.y+Math.round(slope*(m.x-highP.x));
			}
		}
		else
		{
			//currently we are at the end
			if(m.x>rightP.x)
			{
				m=rightP; //we can't move anywhere
			}
			else
			{
				prev(); //walk back to the segment before
			}			
		}
		return m;
	}
	
	//returns a point on the p% of the current selected segment
	private Point getInterpolated(float percent)
	{

		int dX,dY;
		Point p;
		Point leftP,rightP;
		Point c = Main.map.mapView.getPoint(getCurr().getEastNorth());
		Point p1 = Main.map.mapView.getPoint(getCurr().getEastNorth());
		Point p2 = getEndpoint();		
		//determine which point is what
		leftP=getLeftPoint(p1, p2);
		rightP=getRightPoint(p1,p2);
		//we will never go over the segment
		percent=percent/100;
		dX=Math.round((rightP.x-leftP.x)*percent);
		dY=Math.round((rightP.y-leftP.y)*percent);
		//move in the right direction
		p=new Point(rightP.x-dX,rightP.y-dY);

		return p;
	}

	//gets further infos for a point between two Waypoints
	public WayPoint getInterpolatedWaypoint(Point m)
	{	int a,b,length,lengthSeg;
		long timeSeg;
		float ratio;
		Time base;
		Point p2;
		
		Point curr =Main.map.mapView.getPoint(getCurr().getEastNorth());
		m =getInterpolated(m); //get the right position
		//get the right time
		p2=getEndpoint();
		if (getNext()!=null)
		{
			timeSeg=getNext().getTime().getTime()-getCurr().getTime().getTime();
		}
		else
		{
			timeSeg=-(getPrev().getTime().getTime()-getCurr().getTime().getTime());
		}
		WayPoint w =new WayPoint(Main.map.mapView.getLatLon(m.x, m.y));
		//calc total traversal length
		lengthSeg = getTraversalLength(p2, curr);
		length = getTraversalLength(p2, m);
		length=lengthSeg-length;
		//calc time difference
		ratio=(float)length/(float)lengthSeg;
		long inc=(long) (timeSeg*ratio);		
		long old = getCurr().getTime().getTime();
		old=old+inc;
		Date t = new Date(old);
		w.time = t.getTime()/1000; //TODO need better way to set time and sync it
		SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss:S");
		/*System.out.print(length+"px ");
		System.out.print(ratio+"% ");
		System.out.print(inc+"ms ");
		System.out.println(df.format(t.getTime()));+*/
		System.out.println(df.format(w.getTime()));
		//TODO we have to publish the new date to the node...
		return w;
	}

	//returns a point and time for the current segment
	private WayPoint getInterpolatedWaypoint(float percentage)
	{
		Point p = getInterpolated(percentage);
		WayPoint w =new WayPoint(Main.map.mapView.getLatLon(p.x, p.y));
		return w;
	}
	
	//returns n points on the current segment
	public List<WayPoint> getInterpolatedLine(int interval)
	{
		List<WayPoint> ls;
		Point p2;
		float step;
		int length;
		
		step=100/(float)interval;
		ls=new LinkedList<WayPoint>();
		for(float i=step;i<100;i+=step)
		{
			ls.add(getInterpolatedWaypoint(i));
		}
		return ls;
	}
	
	private Point getLeftPoint(Point p1,Point p2)
	{
		if(p1.x<p2.x) return p1; else return p2;
	}
	
	private Point getRightPoint(Point p1, Point p2)
	{
		if(p1.x>p2.x) return p1; else return p2;
	}
	
	private Point getHighPoint(Point p1, Point p2)
	{
		if(p1.y<p2.y)return p1; else return p2;
	}
	
	private Point getLowPoint(Point p1, Point p2)
	{
		if(p1.y>p2.y)return p1; else return p2;
	}

	private Point getEndpoint() {
		if(getNext()!=null)
		{
			return Main.map.mapView.getPoint(getNext().getEastNorth());
		}
		else
		{
			return Main.map.mapView.getPoint(getPrev().getEastNorth());
		}
		
	}

	private float getSlope(Point highP, Point lowP) {
		float slope=(float)(highP.y-lowP.y) / (float)(highP.x - lowP.x);
		return slope;
	}

	private int getTraversalLength(Point p2, Point curr) {
		int a;
		int b;
		int lengthSeg;
		a=Math.abs(curr.x-p2.x);
		b=Math.abs(curr.y-p2.y);
		lengthSeg= (int) Math.sqrt(Math.pow(a, 2)+Math.pow(b, 2));
		return lengthSeg;
	}

	//returns time in ms relatie to startpoint
	public long getRelativeTime()
	{
		return getRelativeTime(curr);
	}
	
	public long getRelativeTime(WayPoint p)
	{
		return p.getTime().getTime()-start.getTime().getTime(); //TODO assumes timeintervall is constant!!!!
	}
	
	
	//jumps to a specific time
	public void jump(long relTime) {
		int pos = Math.round(relTime/1000);//TODO ugly quick hack	
		jump(pos);
		//if (autoCenter) Main.map.mapView.
	}
	
	//toggles walking along the track
	public void play()
	{ /*
		if (t==null)
		{
			//start
			t= new Timer();
			ani=new TimerTask() {			
				@Override
				//some cheap animation stuff
				public void run() {				
					next();
					if(autoCenter) Main.map.mapView.zoomTo(getCurr().getEastNorth());
					Main.map.mapView.repaint();
				}
			};
			t.schedule(ani,1000,1000);			
		}
		else
		{
			//stop
			ani.cancel();
			ani=null;
			t.cancel();
			t=null;					
		}*/
	}

	public long getLength() {
		return ls.size()*1000; //FIXME this is a poor hack
	}
	
	public void setAutoCenter(boolean b)
	{
		this.autoCenter=b;
	}
	
	public List<WayPoint> getTrack()
	{
		return ls;
	}



	
}
