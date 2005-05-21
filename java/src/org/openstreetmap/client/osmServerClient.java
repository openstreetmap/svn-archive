/*
 * Copyright (C) 2004 Stephen Coast (steve@fractalus.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */
package org.openstreetmap.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.applet.SimpleXmlRpcClient;
import org.openstreetmap.applet.Node;
import org.openstreetmap.util.Logger;
import org.openstreetmap.util.gpspoint;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.OMLine;

public class osmServerClient {

    private String sUsername = "";
    private String sPassword = "";
    private String sLoginToken = "";
    private long loginTime = 0;
    SimpleXmlRpcClient xmlrpc;

   public osmServerClient() {
        try {
            xmlrpc = new SimpleXmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");
        }
        catch (Exception ex) {
            Logger.log(ex);
        }
    }
 
    private Object callServer(String method, Vector args) {
        try {
            long before = System.currentTimeMillis();
            Object result = xmlrpc.execute("openstreetmap." + method, args);
            float time = System.currentTimeMillis() - before;
            System.err.println("Calling " + method + " took " + time + "ms");
            return result;
        }
        catch (Exception ex) {
            Logger.log(ex);
        }
        return null;
    }
    
    private int int_callServer(String method, Vector args) {
        Integer i = (Integer) callServer(method, args);
        if (i == null) {
            return -1;
        }
        return i.intValue();
    }

    private String string_callServer(String method, Vector args) {
        return (String) callServer(method, args);
    }

    private Vector vector_callServer(String method, Vector args) {
        return (Vector) callServer(method, args);
    }

    private boolean boolean_callServer(String method, Vector args) {
        Boolean b = (Boolean) callServer(method, args);
        if (b == null) {
            return false;
        }
        return b.booleanValue();
    }

    public synchronized int addNewStreet(String sStreetName, double lat1, double lon1, double lat2, double lon2) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(sStreetName);
        params.addElement(new Double(lat1));
        params.addElement(new Double(lon1));
        params.addElement(new Double(lat2));
        params.addElement(new Double(lon2));
        return int_callServer("addNewStreet", params);
    }

    public synchronized boolean addStreetSegment(int street_uid, double lat1, double lon1, double lat2, double lon2) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Integer(street_uid));
        params.addElement(new Double(lat1));
        params.addElement(new Double(lon1));
        params.addElement(new Double(lat2));
        params.addElement(new Double(lon2));
        return boolean_callServer("addStreetSegment", params);
    }

    public synchronized boolean deletePoint(double lat, double lon) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Double(lon));
        params.addElement(new Double(lat));
        return boolean_callServer("dropPoint", params);
    }

    public synchronized boolean deletePointsInArea(double lon1, double lat1, double lon2, double lat2) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Double(lon1));
        params.addElement(new Double(lat1));
        params.addElement(new Double(lon2));
        params.addElement(new Double(lat2));
        return boolean_callServer("dropPointsInArea", params);
    }

    public synchronized boolean login(String user, String pass) {
        Logger.log("trying to login with '" + user + "' , '" + pass + "'...");
        Vector params = new Vector();
        params.addElement(user);
        params.addElement(pass);
        String token = string_callServer("login", params);
        if (token.equals("ERROR")) {
            return false;
        }
        sUsername = user;
        sPassword = pass;
        sLoginToken = token;
        loginTime = System.currentTimeMillis() + (1000 * 60 * 9);  // set logout time for 9 mins hence
        return true;
    }

    public synchronized boolean loggedIn() {
        if (loginTime > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    // Petter Reinholdtsen's sticky username/password patch
    /**
     * Make it possible to get the last username/password, for use when
     * reconnecting the applet to the server.
     */
    public synchronized String getUsername() {
        return sUsername;
    }

    public synchronized String getPassword() {
        return sPassword;
    }
    // EOF patch

    public synchronized Vector getStreets(LatLonPoint llp1, LatLonPoint llp2) {
        Logger.log("getting streets...");
        Vector params = new Vector();
        params.addElement("applet");
        params.addElement(new Double((double) llp1.getLatitude()));
        params.addElement(new Double((double) llp1.getLongitude()));
        params.addElement(new Double((double) llp2.getLatitude()));
        params.addElement(new Double((double) llp2.getLongitude()));
        return vector_callServer("getStreets", params);
    }

    public synchronized Vector getPoints(LatLonPoint llp1, LatLonPoint llp2) {
        Vector params = new Vector();
        params.addElement("applet");
        params.addElement(new Double((double) llp1.getLatitude()));
        params.addElement(new Double((double) llp1.getLongitude()));
        params.addElement(new Double((double) llp2.getLatitude()));
        params.addElement(new Double((double) llp2.getLongitude()));
        Vector results = vector_callServer("getPoints", params);
        Logger.log("reading points...");
        Vector gpsPoints = new Vector();
        Enumeration enum = results.elements();
        while (enum.hasMoreElements()) {
            float lat = (float) ((Double) enum.nextElement()).doubleValue();
            float lon = (float) ((Double) enum.nextElement()).doubleValue();
            gpsPoints.add(new gpspoint(lat, lon, 0, 0));
        }
        Logger.log("done getting points");
        return gpsPoints;
    }

    public synchronized Hashtable getNodes(LatLonPoint llp1, LatLonPoint llp2) {
        Logger.log("grabbing nodes...");
        Vector params = new Vector();
        params.addElement("applet");
        params.addElement(new Double((double) llp1.getLatitude()));
        params.addElement(new Double((double) llp1.getLongitude()));
        params.addElement(new Double((double) llp2.getLatitude()));
        params.addElement(new Double((double) llp2.getLongitude()));
        Vector results = vector_callServer("getNodes", params);
        Hashtable htNodes = new Hashtable();
        Enumeration enum = results.elements();
        while (enum.hasMoreElements()) {
            int uid = ((Integer) enum.nextElement()).intValue();
            double lat = ((Double) enum.nextElement()).doubleValue();
            double lon = ((Double) enum.nextElement()).doubleValue();
            Node n = new Node(uid, lat, lon);
            htNodes.put("" + uid, n);
        }
        return htNodes;
    }

    public synchronized int addNode(double latitude, double longitude) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Double(latitude));
        params.addElement(new Double(longitude));
        int i = int_callServer("newNode", params);
        Logger.log("added node " + i);
        return i;
    }

    public synchronized boolean moveNode(int nUID, double latitude, double longitude) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Integer(nUID));
        params.addElement(new Double(latitude));
        params.addElement(new Double(longitude));
        return boolean_callServer("moveNode", params);
    }

    public synchronized boolean deleteNode(int nUID) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Integer(nUID));
        return boolean_callServer("deleteNode", params);
    }

    public synchronized int newLine(int nUIDa, int nUIDb) {
        Vector params = new Vector();
        params.addElement(sLoginToken);
        params.addElement(new Integer(nUIDa));
        params.addElement(new Integer(nUIDb));
        return int_callServer("newLine", params);
    }

    public synchronized Vector getLines(Hashtable htNodes) {
        Logger.log("grabbing lines...");
        Vector v = new Vector();
        Vector params = new Vector();
        params.addElement("applet");
        Vector uids = new Vector();
        Enumeration enum = htNodes.elements();
        while (enum.hasMoreElements()) {
            uids.addElement(new Integer(((Node) enum.nextElement()).getUID()));
        }
        params.addElement(uids);
        Vector results = vector_callServer("getLines", params);
        Logger.log("reading Lines...");
        enum = results.elements();
        while (enum.hasMoreElements()) {
            Integer ia = (Integer) enum.nextElement();
            Integer ib = (Integer) enum.nextElement();
            int na = ia.intValue();
            int nb = ib.intValue();
            Node nodeA = (Node) htNodes.get("" + na);
            Node nodeB = (Node) htNodes.get("" + nb);
            if (nodeA != null && nodeB != null) {
                LatLonPoint llpA = nodeA.getLatLon();
                LatLonPoint llpB = nodeB.getLatLon();
                v.add(new OMLine(llpA.getLatitude(), llpA.getLongitude(), llpB.getLatitude(), llpB.getLongitude(), OMLine.STRAIGHT_LINE));
                Logger.log("adding line between " + llpA + ", " + llpB);
            }
        }
        Logger.log("done getting lines!");
        return v;
    }

}
