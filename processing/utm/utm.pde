
import com.bbn.openmap.proj.coords.UTMPoint;
import com.bbn.openmap.LatLonPoint;
import org.apache.xmlrpc.XmlRpcClient;

String USERNAME = "";
String PASSWORD = "";

Vector points = new Vector();
Vector pointIDs = new Vector();
Hashtable pointMap = new Hashtable();
Vector lines = new Vector();

LatLonPoint centreLL = new LatLonPoint(51.526447,-0.14746371);
LatLonPoint topLeftLL;
LatLonPoint bottomRightLL;
UTMPoint centreUTM;
UTMPoint topLeftUTM;
UTMPoint bottomRightUTM;

Vector screenLines = new Vector();
Vector screenPoints = new Vector();

void setup() {
  size(600,600);
  noLoop();
  smooth();
  getData();
}

void getData() {

  println(centreLL);
  
  centreUTM = UTMPoint.LLtoUTM(centreLL);
  println(centreUTM);
  
  topLeftUTM = new UTMPoint(centreUTM.northing+3000.0,centreUTM.easting-3000.0,centreUTM.zone_number,centreUTM.zone_letter);
  bottomRightUTM = new UTMPoint(centreUTM.northing-3000.0,centreUTM.easting+3000.0,centreUTM.zone_number,centreUTM.zone_letter);
  
  println(topLeftUTM);
  println(bottomRightUTM);
  
  topLeftLL = topLeftUTM.toLatLonPoint();
  bottomRightLL = bottomRightUTM.toLatLonPoint();

  println(topLeftLL);
  println(bottomRightLL);

  try {
  
    XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

    Vector params = new Vector();
    params.addElement (USERNAME);
    params.addElement (PASSWORD);
    String token = (String)xmlrpc.execute ("openstreetmap.login", params);
    println(token);

    params = new Vector();
    params.addElement (token);
    params.addElement (new Double(topLeftLL.getLatitude()));
    params.addElement (new Double(topLeftLL.getLongitude()));
    params.addElement (new Double(bottomRightLL.getLatitude()));
    params.addElement (new Double(bottomRightLL.getLongitude()));
    println(params);
    Vector result = (Vector)xmlrpc.execute ("openstreetmap.getNodes", params);
    println(result.size());
    
    LatLonPoint ll = new LatLonPoint(0, 0);
    float lat,lon;
    Iterator iter = result.iterator();
    while(iter.hasNext()) {
      Vector result2 = (Vector)iter.next();
      Iterator iter2 = result2.iterator();
      Integer id = (Integer)iter2.next();
      lat = ((Double)iter2.next()).floatValue();
      lon = ((Double)iter2.next()).floatValue();
      ll.setLatLon(lat,lon);
      UTMPoint a = UTMPoint.LLtoUTM(ll);
      points.add(a);
      pointIDs.add(id);
      pointMap.put(id,a);
    }
    println(points.size());
    println(pointIDs.size());
    println(pointMap.size());

    params = new Vector();
    params.addElement (token);
    params.addElement (pointIDs);
    result = (Vector)xmlrpc.execute ("openstreetmap.getLines", params);
    println(result.size());

    iter = result.iterator();
    while(iter.hasNext()) {
      Vector result2 = (Vector)iter.next();
      Iterator iter2 = result2.iterator();
      Integer id = (Integer)iter2.next();
      Integer a = (Integer)iter2.next();
      Integer b = (Integer)iter2.next();
      lines.add(new Line2d(a,b));
      // TODO if (!pointMap.contains(a)) get a from xmlrpc, same for b
    }
    println(lines.size());
            
  }
  catch (Exception e) {
    e.printStackTrace();
  }
  
}

class Line2d {
  Integer a,b;
  Line2d(Integer a,Integer b) {
    this.a=a; this.b=b;
  }
  String toString() {
    return "("+a+","+b+")";
  }
}

class Point2d {
  float x,y;
  Point2d(float x,float y) {
    this.x=x; this.y=y;
  }
  String toString() {
    return "("+x+","+y+")";
  }
}

class ScreenLine {
  Point2d a, b;
  ScreenLine(Point2d a,Point2d b) {
    this.a=a; this.b=b;
  }
  String toString() {
    return "("+a+","+b+")";
  }
}

void draw() {

  background(220);
  
  float sc = min(width/(bottomRightUTM.easting-topLeftUTM.easting),height/(topLeftUTM.northing-bottomRightUTM.northing);
  
  pushMatrix();
  scale(sc,-sc); // XXX -y scaling because UTM northings run counter to screenY
  translate(-topLeftUTM.easting,-topLeftUTM.northing);
  
  int skipped = 0;
  for (int i = 0; i < lines.size(); i++) {s
    Line2d l = (Line2d)lines.get(i);
    UTMPoint pa = (UTMPoint)pointMap.get(l.a);
    UTMPoint pb = (UTMPoint)pointMap.get(l.b);
    if (pa != null && pb != null) {
      screenLines.add(new ScreenLine(new Point2d(screenX(pa.easting,pa.northing),screenY(pa.easting,pa.northing)),new Point2d(screenX(pb.easting,pb.northing),screenY(pb.easting,pb.northing))));
    }
    else {
      skipped++;
    }
  }
  println(screenLines.size());
  println(skipped);
  
  for (int i = 0; i < points.size(); i++) {
    UTMPoint a = (UTMPoint)points.get(i);
    screenPoints.add(new Point2d(screenX(a.easting,a.northing),screenY(a.easting,a.northing)));
  }
  println(screenPoints.size());
  
  popMatrix();
  
  noFill();
  strokeWeight(6);
  stroke(0);
  for (int i = 0; i < screenLines.size(); i++) {
    ScreenLine l = (ScreenLine)screenLines.get(i);
    line(l.a.x,l.a.y,l.b.x,l.b.y);
  }
  strokeWeight(4);
  stroke(255);
  for (int i = 0; i < screenLines.size(); i++) {
    ScreenLine l = (ScreenLine)screenLines.get(i);
    line(l.a.x,l.a.y,l.b.x,l.b.y);
  }
  
  ellipseMode(CENTER);
  fill(255,0,0,40);
  noStroke();
  for (int i = 0; i < screenPoints.size(); i++) {
    Point2d a = (Point2d)screenPoints.get(i);
    ellipse(a.x,a.y,5,5);
  }
  
  
}

