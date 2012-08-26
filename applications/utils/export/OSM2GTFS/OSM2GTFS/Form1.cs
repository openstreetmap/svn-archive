///
/// OSM2GTFS - Given an OpenStreetMap XML file, look for bus route relations
/// and generate GTFS files.  Intended for first-time creation of GTFS files.
/// *
/// Author: Mike N July 2012
/// License: Public Domain
///


using System;
using System.Collections.Generic;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Xml;
using System.Text;
using System.Linq;
using System.Text.RegularExpressions;
using System.IO;
using System.Windows.Forms;
using System.Threading;
using System.Xml.Serialization;

namespace OSM2GTFS
{

    /// <summary>
    /// Process a .OSM XML file
    /// </summary>
    public partial class Form1 : Form
    {
        public delegate void LogMessageDelegate(string status, bool error);

        private Dictionary<Int64, Relation> routeMasters;
        private Dictionary<Int64, Relation> routes;
        private Dictionary<Int64, Way> ways;
        private Dictionary<Int64, Coord> nodes;
        private Dictionary<Int64, StopDistance> stopPositions;

        private Dictionary<Int64, int> StopOsmIdToStopID; // Retain Stop ID across edits once assigned

        bool terminating = false;
        private Thread exportThread = null;

        private string gtfsFolder;



        public Form1()
        {
            InitializeComponent();
        }



        private Dictionary<string, string> GetTags(XmlNode parentNode)
        {
            var tagList = new Dictionary<string, string>();
            var osmTags = parentNode.SelectNodes("tag");
            foreach (XmlNode osmTag in osmTags)
            {
                tagList.Add(osmTag.Attributes["k"].InnerText, osmTag.Attributes["v"].InnerText);
            }

            return tagList;
        }



        private void LoadOSMFile(string xmlFilename)
        {
            XmlDocument osmXMLDoc;
            XmlNode rootNode;



            ways = new Dictionary<Int64, Way>();

            osmXMLDoc = new XmlDocument();
            osmXMLDoc.Load(xmlFilename);

            rootNode = osmXMLDoc.SelectSingleNode("/osm");

            XmlNodeList xmlNodes = osmXMLDoc.SelectNodes("/osm/way");
            foreach (XmlNode xmlNode in xmlNodes)
            {
                Int64 wayID = Convert.ToInt64(xmlNode.Attributes["id"].InnerText);

                var wayNodeList = new List<Int64>();
                var osmNodeIDs = xmlNode.SelectNodes("nd");
                foreach(XmlNode osmNodeID in osmNodeIDs) {
                    wayNodeList.Add(Convert.ToInt64(osmNodeID.Attributes["ref"].InnerText));
                }

                if (wayNodeList.Count > 0)
                {
                    var tagList = GetTags(xmlNode);

                    var way = new Way(wayID, wayNodeList, tagList);
                    ways.Add(wayID, way);
                }
            }
            
            LogMessage("Finished Reading: " + ways.Count + " ways", false);

            nodes = new Dictionary<long, Coord>();
            xmlNodes = osmXMLDoc.SelectNodes("/osm/node");
            foreach (XmlNode xmlNode in xmlNodes)
            {
                var tagList = GetTags(xmlNode);
                Int64 nodeID = Convert.ToInt64(xmlNode.Attributes["id"].InnerText);
                nodes.Add(nodeID, new Coord(nodeID, xmlNode.Attributes["lat"].InnerText, xmlNode.Attributes["lon"].InnerText, tagList));
            }
            LogMessage("Finished Reading: " + nodes.Count + " nodes", false);


            routeMasters = new Dictionary<long, Relation>();
            routes = new Dictionary<long, Relation>();

            xmlNodes = osmXMLDoc.SelectNodes("/osm/relation");

            // Get route relations
            foreach (XmlNode xmlNode in xmlNodes)
            {
                Int64 relationID = Convert.ToInt64(xmlNode.Attributes["id"].InnerText);
                var tagList = GetTags(xmlNode);

                var members = new List<RelationMember>();
                var osmTags = xmlNode.SelectNodes("member");
                foreach (XmlNode osmTag in osmTags)
                {
                    var member = new RelationMember(osmTag.Attributes["type"].InnerText, Convert.ToInt64(osmTag.Attributes["ref"].InnerText), 
                        osmTag.Attributes["role"].InnerText);
                    members.Add(member);
                }

                // See if this is a relation of interest
                if (tagList.ContainsKey("type") &&
                    tagList.ContainsKey("route") || tagList.ContainsKey("route_master"))
                {
                    switch (tagList["type"])
                    {
                        case "route":
                            if ( (tagList["route"] == "bus") || (tagList["route"] == "trolleybus") ) 
                            {
                                routes.Add(relationID, new Relation(relationID, members, tagList));
                            }
                            break;

                        case "route_master":
                            var routeMaster = new Relation(relationID, members, tagList);
                            routeMasters.Add(relationID, routeMaster);
                            
                            break;
                    }
                }

            }
            LogMessage("Finished Reading: " + routeMasters.Count + " routes, " + routes.Count + " route segments", false);

        }

        // Calculate the distance between
        // Node 'stopPosition' and the segment p1 --> p2, where p1 -> p2 is a subset of a way.
        private double FindDistanceToSegment(Coord stopPosition, Coord p1, Coord p2, out Coord closest)
        {
            double dx = p2.Lon - p1.Lon;
            double dy = p2.Lat - p1.Lat;
            if ((dx == 0) && (dy == 0))
            {
                // It's a point not a line segment.
                closest = p1;
                dx = stopPosition.Lon - p1.Lon;
                dy = stopPosition.Lat - p1.Lat;
                return Math.Sqrt(dx * dx + dy * dy);
            }

            // Calculate the t that minimizes the distance.
            double t = ((stopPosition.Lon - p1.Lon) * dx + (stopPosition.Lat - p1.Lat) * dy) / (dx * dx + dy * dy);

            var tagList = new Dictionary<string, string>();
            tagList["public_transport"] = "stop_position";
            tagList["name"] = "calculated gtfs stop position";
            // See if this represents one of the segment's
            // end points or a point in the middle.
            if (t < 0)
            {
                closest = new Coord(-1, p1.Lat, p1.Lon, tagList);
                dx = stopPosition.Lon - p1.Lon;
                dy = stopPosition.Lat - p1.Lat;
            }
            else if (t > 1)
            {
                closest = new Coord(-1, p2.Lat, p2.Lon, tagList);
                dx = stopPosition.Lon - p2.Lon;
                dy = stopPosition.Lat - p2.Lat;
            }
            else
            {
                closest = new Coord(-1, p1.Lat + t * dy, p1.Lon + t * dx, tagList);
                dx = stopPosition.Lon - closest.Lon;
                dy = stopPosition.Lat - closest.Lat;
            }

            return Math.Sqrt(dx * dx + dy * dy);
        }

        int newStopNodeID = -1;  // Negative numbers for node creation

        private void FindStopPosition(Coord stopNode)
        {
            double minDistance = 999.0;  // Smallest distance seen so far
            StopDistance nearestStopPosition = null;
            // Brute force - examine every segment of every way in all route relations to find closest - some are even looked at multiple times
            foreach (Relation route in routes.Values)
            {
                foreach (RelationMember relatiomMember in route.Members)
                {
                    if (IsRoutePath(relatiomMember))
                    {
                        var way = ways[relatiomMember.RelationRef];
                        for (int i = 0; i < way.NodeList.Count - 1; i++)
                        {
                            var p1 = nodes[way.NodeList[i]];
                            var p2 = nodes[way.NodeList[i+1]];

                            Coord closest;
                            var dist = FindDistanceToSegment(stopNode, p1, p2, out closest);
                            if (dist < minDistance)
                            {
                                minDistance = dist;
                                nearestStopPosition = new StopDistance(stopNode, way, i, closest, dist);
                            }
                        }
                    }
                }
            }

            if (nearestStopPosition == null) return;

            // Insert new virtual stop_position node into way
            nearestStopPosition.StopPosition.NodeID = newStopNodeID;
            Way wayToModify = nearestStopPosition.Way;
            wayToModify.NodeList.Insert(nearestStopPosition.WayPosition, newStopNodeID);
            nodes.Add(newStopNodeID, nearestStopPosition.StopPosition);
            stopPositions.Add(newStopNodeID, nearestStopPosition); // Store for later access
            newStopNodeID--;

        }


        /// <summary>
        /// Assign virtual stop positions to each platform
        /// </summary>
        private void FindStopPositions()
        {
            // Create a temporary static list of stop nodes - 
            // global node list will be modified when adding stop positions
            var stopNodeList = new List<Coord>();
            foreach (Coord node in nodes.Values)
            {
                if (IsStop(node))
                {
                    stopNodeList.Add(node);
                }

            }

            foreach (Coord node in stopNodeList)
            {
                FindStopPosition(node);
            }
        }



        private bool IsRoutePath(RelationMember relationMember)
        {
            return (relationMember.RelationType == "way" &&
                (relationMember.Role == "forward" || relationMember.Role == "backward"));

        }


        private bool SortRouteWays(Relation route)
        {
            LinkedList<RelationMember> linkedWays = new LinkedList<RelationMember>();

            List<RelationMember> memberList = new List<RelationMember>(); // Temporary working copy
            foreach (RelationMember member in route.Members) memberList.Add(member);  // Clone

            while (memberList.Count > 0)
            {
                RelationMember relationMember = memberList[0];
                if (IsRoutePath(relationMember))
                {
                    if (linkedWays.Count == 0)
                    {
                        // First one
                        linkedWays.AddFirst(relationMember);
                        memberList.RemoveAt(0);
                    }
                    else
                    {
                        // See if one of the remaining ways connects to an end of the linked list
                        bool foundLink  = false;
                        var lastWay = ways[linkedWays.Last.Value.RelationRef];
                        var firstWay = ways[linkedWays.First.Value.RelationRef];
                        for (int i = 0; i < memberList.Count; i++)
                        {
                            var wayMember = memberList[i];
                            if (!ways.ContainsKey(wayMember.RelationRef)) {
                                MessageBox.Show("missing way");  // Debugger convenience breakpoint line
                            }
                            var way = ways[wayMember.RelationRef];

                            if ( (way.FirstNode == firstWay.FirstNode) || (way.FirstNode == firstWay.LastNode) ||
                                (way.LastNode == firstWay.FirstNode) || (way.LastNode == firstWay.LastNode))
                            {
                                linkedWays.AddFirst(wayMember);
                                memberList.RemoveAt(i);
                                foundLink = true;
                                break;
                            }

                            if ((way.FirstNode == lastWay.FirstNode) || (way.FirstNode == lastWay.LastNode) ||
                                (way.LastNode == lastWay.FirstNode) || (way.LastNode == lastWay.LastNode))
                            {
                                linkedWays.AddLast(wayMember);
                                memberList.RemoveAt(i);
                                foundLink = true;
                                break;
                            }
                        }
                        if (!foundLink)
                        {
                            // Error - unable to link anything to front or rear of linked list
                            LogMessage("Error in relation " + route.TagList["name"] + "; route break near way " + firstWay.TagList["name"] + "/" + firstWay.WayID +
                                " or way " + lastWay.TagList["name"] + "/" + lastWay.WayID, true);
                            return false;
                        }
                    }
                }
                else
                {
                    // Not a 'road'
                    memberList.RemoveAt(0);
                }
            }

            List<RelationMember> sortedWays = new List<RelationMember>();
            SaveLinkedWays(route, linkedWays, sortedWays);

            route.SortedWays = sortedWays;
            return true;
        }

        // At this point the ways are linked, but may be in either forward or reverse order.
        private void SaveLinkedWays(Relation route, LinkedList<RelationMember> linkedWays, List<RelationMember> sortedWays)
        {
            bool firstToLast = true;

            if (linkedWays.Count == 0)
            {
                LogMessage(string.Format("Note: No ways found in route {0} ", route.TagList["name"]), false);
                return;
            }

            var firstWayMember = linkedWays.First.Value;
            var secondWayMember = linkedWays.First.Next.Value;

            var firstWay = ways[firstWayMember.RelationRef];
            var secondWay = ways[secondWayMember.RelationRef];

            if (firstWay.FirstRoleNode(firstWayMember.Role) == secondWay.LastRoleNode(secondWayMember.Role)) firstToLast = false;

            if (firstToLast)
            {
                var member = linkedWays.First;

                sortedWays.Add(member.Value);

                while (member.Next != null)
                {
                    member = member.Next;
                    sortedWays.Add(member.Value);
                }
            }
            else
            {
                var member = linkedWays.Last;

                sortedWays.Add(member.Value);

                while (member.Previous != null)
                {
                    member = member.Previous;
                    sortedWays.Add(member.Value);
                }

            }
        }

        private Int64 LastPathNodeID(Relation route)
        {
            var lastWayMember = route.SortedWays[route.SortedWays.Count - 1];
            var lastWay = ways[lastWayMember.RelationRef];

            if (lastWayMember.Role == "forward")
            {
                return (lastWay.LastNode);
            }
            else
            {
                return (lastWay.FirstNode);
            }
        }

        private Int64 FirstPathNodeID(Relation route)
        {
            var firstWayMember = route.SortedWays[0];
            var firstWay = ways[firstWayMember.RelationRef];

            if (firstWayMember.Role == "forward")
            {
                return (firstWay.FirstNode);
            }
            else
            {
                return (firstWay.LastNode);
            }
        }


        private void SortRouteMasterMembers(Relation routeMaster)
        {

            var sortedRouteMembers = new List<RelationMember>();
            var workMembers = new List<RelationMember>(); // Temporary work list
            for (int i = 0; i < routeMaster.Members.Count; i++)
            {
                workMembers.Add(routeMaster.Members[i]);
            }

            Relation firstRoute = null;
            for (int i = 0; i < workMembers.Count; i++)
            {
                Relation route = routes[workMembers[i].RelationRef];
                if (route.TagList.ContainsKey("from"))
                {
                    if (route.TagList["from"] == "Transit Center")
                    {
                        firstRoute = route;
                        sortedRouteMembers.Add(workMembers[i]);
                        workMembers.RemoveAt(i); // has been assigned
                        break;
                    }
                }

            }
            if (firstRoute == null)
            {
                // Couldn't find a definite beginning .... use route order in OSM route_master data
                return; 
            }

            while (workMembers.Count > 0)
            {
                // Find route segment that follows the end of current list
                RelationMember lastRouteMember = sortedRouteMembers[sortedRouteMembers.Count-1];
                Relation lastRoute = routes[lastRouteMember.RelationRef];
                Int64 lastNode =  LastPathNodeID(lastRoute);
                bool segmentFound = false;
                for (int i = 0; i < workMembers.Count; i++)
                {
                    Relation possibleNextRoute = routes[workMembers[i].RelationRef];
                    if (FirstPathNodeID(possibleNextRoute) == lastNode)
                    {
                        // Found next segment
                        sortedRouteMembers.Add(workMembers[i]);
                        workMembers.RemoveAt(i); // has been assigned
                        segmentFound = true;
                        break;
                    }
                }

                if (!segmentFound)
                {
                    LogMessage("Possible broken route segment", false);
                    return;
                }

            }

            routeMaster.Members = sortedRouteMembers;
        }


        /// <summary>
        /// TODO:  ADAPTATION NOTE: This hard codes the starting point for route sortation.  In case of
        /// unexpected results (stops are out of order), adapt for your situation.
        /// </summary>
        private void SortAllRouteMasterMembers()
        {
            foreach (Relation routeMaster in routeMasters.Values)
            {
                SortRouteMasterMembers(routeMaster);

            }

        }


        /// <summary>
        /// Ensure that route ways are ordered from first to last to form a continuous link.  
        /// Although the JOSM "Public Transport" plugin
        /// or some other tool *may* have sorted them, they are subject to being broken.
        /// </summary>
        private bool SortAllRouteWays()
        {
            bool allGood = true;
            foreach (Relation route in routes.Values)
            {
                allGood = allGood && SortRouteWays(route);
            }

            if (allGood)
            {
                SortAllRouteMasterMembers();
            }

            return allGood;
        }


        private Coord GetCoord(Int64 nodeID)
        {
            return nodes[nodeID];
        }

        private double GetAngle(Way way, Way followWay)
        {
            Coord first = GetCoord(way.NodeList[way.NodeList.Count - 2]);
            Coord middle = GetCoord(way.LastNode);
            Coord last = GetCoord(followWay.NodeList[1]);

            double firstAngle = Math.Atan2(middle.Lat - first.Lat, middle.Lon - first.Lon) * 180/Math.PI;
            double secondAngle = Math.Atan2(last.Lat - middle.Lat, last.Lon - middle.Lon) * 180 / Math.PI;

            return firstAngle - secondAngle;
        }

        public static bool IsStop(Coord node)
        {
            if (node.TagList.ContainsKey("public_transport"))
            {
                if (node.TagList["public_transport"] == "platform")
                {
                    return true;
                }
            }

            if (node.TagList.ContainsKey("highway"))
            {
                if (node.TagList["highway"] == "bus_stop")
                {
                    return true;
                }
            }

            return false;
        }

        const string StopAssignmentsFileName = @"..\StopAssignments.csv";

        /// <summary>
        /// Save stop ID assignments for future edit session in case new stops are created in the OSM file
        /// </summary>
        private void SaveOldStopAssignments()
        {

            var filePath = Path.Combine(gtfsFolder, StopAssignmentsFileName);
            if (File.Exists(filePath))
            {
                var bakFilename = filePath + ".bak";
                if (File.Exists(bakFilename)) File.Delete(bakFilename);
                File.Move(filePath, bakFilename);
            }


            using (var outfile = new StreamWriter(filePath))
            {
                foreach (var osmID in StopOsmIdToStopID.Keys)
                {
                    outfile.WriteLine("{0},{1}", osmID, StopOsmIdToStopID[osmID]);
                }
                outfile.Close();
            }
        }

        /// <summary>
        /// Save stop ID assignments
        /// </summary>
        private void LoadOldStopAssignments()
        {
            StopOsmIdToStopID = new Dictionary<long, int>();
            var filePath = Path.Combine(gtfsFolder, StopAssignmentsFileName);

            if (!File.Exists(filePath)) return; // No old data

            using (var infile = new StreamReader(filePath))
            {
                while (!infile.EndOfStream)
                {
                    var line = infile.ReadLine();
                    if (line.Length > 2)
                    {
                        var ids = line.Split(',');
                        StopOsmIdToStopID.Add(Convert.ToInt64(ids[0]), Convert.ToInt32(ids[1]));
                    }
                }
                infile.Close();
            }

        }


        /// <summary>
        /// Save stops to GTFS file, assign stop IDs  (First retrieving any previous assignments)
        /// </summary>
        /// <param name="dir"></param>
        private void SaveStops(string dir)
        {
            LoadOldStopAssignments();

            var StopsFileName = "stops.txt";

            TimeScheduleForm.BackupFile(dir, StopsFileName); 

            var filePath = Path.Combine(dir, StopsFileName);
            using (var outfile = new StreamWriter(filePath))
            {
                outfile.WriteLine("stop_id,stop_name,stop_lat,stop_lon,location_type");
                int stopID = 1;

                if (StopOsmIdToStopID.Count > 0)
                {
                    stopID = StopOsmIdToStopID.Values.Max<int>() + 1;  // Start numbering after highest assigned so far
                }

                // Iterate through all nodes to get stops, rather than use routes which may share stops
                foreach (Coord node in nodes.Values)
                {
                    if (IsStop(node)) 
                    {
                        int assignedStopID = stopID;
                        if (StopOsmIdToStopID.ContainsKey(node.NodeID))
                        {
                            assignedStopID = StopOsmIdToStopID[node.NodeID];
                        }
                        else stopID++;

                        string name = assignedStopID.ToString(); // Create some name for un-named stops
                        if (node.TagList.ContainsKey("name")) name = node.TagList["name"];
                        int locationType = 0; // Ordinary stop
                        outfile.WriteLine("{0},\"{1}\",{2},{3},{4}", assignedStopID, name, node.Lat, node.Lon, locationType);
                        node.StopID = assignedStopID;
                        if (!StopOsmIdToStopID.ContainsKey(node.NodeID)) StopOsmIdToStopID.Add(node.NodeID, assignedStopID);
                    }
                }

                outfile.Close();
            }

            SaveOldStopAssignments();

        }


        private void SaveMasterRoutes(string dir)
        {
            var RoutesFileName = "routes.txt";

            TimeScheduleForm.BackupFile(dir, RoutesFileName); 


            var filePath = Path.Combine(dir, RoutesFileName);
            using (var outfile = new StreamWriter(filePath))
            {
                outfile.WriteLine("route_id,agency_id,route_short_name,route_long_name,route_type,route_url"); // ,route_bikes_allowed");

                // Iterate through all nodes to get stops, rather than use routes which may share stops
                foreach (Relation routeMaster in routeMasters.Values)
                {
                    string routeRefID = routeMaster.TagList["ref"];
                    if (routeRefID == "Trolley") routeRefID = "TRLY";

                    string url = "";
                    if (routeMaster.TagList.ContainsKey("url")) url = routeMaster.TagList["url"];

                    string longName = "";
                    if (routeMaster.TagList.ContainsKey("name")) longName = routeMaster.TagList["name"];
                    // To suppress GTFS warnings - strip out short name
                    if (longName.Contains(routeRefID))
                    {
                        // Strip out short name from long name
                        longName = longName.Replace(routeRefID, "");
                    }

                    string agency = "";
                    if (routeMaster.TagList.ContainsKey("operator")) agency = routeMaster.TagList["operator"];

                    outfile.WriteLine("{0},{1},\"{2}\",\"{3}\",{4},{5}", routeRefID, agency, routeRefID, longName, 3, url);
                    routeMaster.RouteID = routeRefID;
                }

                outfile.Close();
            }

        }

        /// <summary>
        /// Not all vehicles stop at all stops along their route - be sure
        /// this stop is contained in the relation
        /// </summary>
        /// <param name="route"></param>
        /// <param name="stop"></param>
        /// <returns></returns>
        private bool RouteContainsStop(Relation route, Coord stopNode, out RelationMember stopMember)
        {
            stopMember = null;
            foreach (RelationMember relationMember in route.Members)
            {
                if (relationMember.RelationType == "node")
                {
                    var node = nodes[relationMember.RelationRef];
                    if (IsStop(node))
                    {
                        if (node.NodeID == stopNode.NodeID)
                        {
                            stopMember = relationMember;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void SortStop(Coord stopPosition, Relation route)
        {
            var stopInfo = stopPositions[stopPosition.NodeID];
            RelationMember stopMember;
            if (RouteContainsStop(route, stopInfo.StopNode, out stopMember))
            {
                route.SortedStops.Add(stopMember);
            }
            else
            {
                // Diagnose(stopPosition, route);
            }

        }

        private void Diagnose(Coord stopPosition, Relation route)
        {
            // This can be a normal case if 2 routes pass by in different directions.   
            // Need to check for stop on right side of the street before logging message

            var stopInfo = stopPositions[stopPosition.NodeID];
            var stopNode= nodes[stopPosition.NodeID];

            // Create 2 fake ways to calculate the angle:
            var way1List = new List<Int64>();
            way1List.Add(stopNode.NodeID);
            way1List.Add(stopInfo.StopPosition.NodeID);
            var way2List = new List<Int64>();
            way2List.Add(stopInfo.StopPosition.NodeID);

            // *** Unfinished

            //string stopName = "Un named";
            //if (stopNode.TagList.ContainsKey("name")) stopName = stopNode.TagList["name"];
            //LogMessage("Note: Route " + route.TagList["ref"] + " does not stop at Stop ID " + stopNode.NodeID + " (" + stopName + ")", false);
        }

        private void DumpShape(StreamWriter outfile, int shapeID, Way way, string role, Relation route,
            Relation routeMaster,
            ref int shapeSequence, ref double distanceMeters, ref Coord lastNode)
        {
            int increment = 1;   // To control direction
            int index = 0;
            if (role == "backward")
            {
                increment = -1;
                index = way.NodeList.Count - 1;
            }

            for (int i = 0; i < way.NodeList.Count; i++)
            {
                var node = nodes[way.NodeList[index]];

                Int64 lastNodeID = 0;
                if (lastNode != null) lastNodeID = lastNode.NodeID;
                if (node.NodeID != lastNodeID)  // Repeated node test (from adjoining way)
                {

                    if (lastNode != null)
                    {
                        var currentSegment = node.GreatCircleDistance(lastNode);
                        distanceMeters += currentSegment;
                    }
                    lastNode = node;

                    if (node.NodeID < 0)
                    {
                        // This is a virtual stop position created here.  Save the stop since it is next in route order
                        SortStop(node, route);
                    }

                    // Save nodes for both the road and virtual stop positions in exported shapes - 
                    // This could allow better matching of the stop position to the roadway

                    outfile.WriteLine("{0},{1},{2},{3},{4}", shapeID, node.Lon, node.Lat, shapeSequence, distanceMeters);
                    shapeSequence++;
                }
                index += increment;
            }


        }


        /// <summary>
        /// Saved as one shape per route master
        /// </summary>
        /// <param name="dir"></param>
        private void SaveShapes(string dir)
        {
            var fileName = "shapes.txt";
            var filePath = Path.Combine(dir, fileName);
            using (var outfile = new StreamWriter(filePath))
            {
                outfile.WriteLine("shape_id,shape_pt_lon,shape_pt_lat,shape_pt_sequence,shape_dist_traveled");
                int shapeID = 1;
                int shapeSequence = 1;

                // Iterate through all nodes to get stops, rather than use routes which may share stops
                foreach (Relation routeMaster in routeMasters.Values)
                {
                    routeMaster.StopDistances = new List<double>();

                    double distanceMeters = 0.0;
                    Coord lastNode = null;
                    foreach (RelationMember routeMember in routeMaster.Members)
                    {
                        // Interested in only route relation members
                        if (routeMember.RelationType == "relation")
                        {
                            var route = routes[routeMember.RelationRef];
                            route.SortedStops = new List<RelationMember>(); // Sorted stops list created as ways are transversed
                            // Iterate over member ways
                            foreach (RelationMember relationMember in route.SortedWays)
                            {
                                if (IsRoutePath(relationMember))
                                {
                                    if (!ways.ContainsKey(relationMember.RelationRef))
                                    {
                                        // Debug assistance breakpoint here
                                        MessageBox.Show("missing relation ref");
                                    }

                                    var way = ways[relationMember.RelationRef];
                                    DumpShape(outfile, shapeID, way, relationMember.Role, route, routeMaster, 
                                        ref shapeSequence, ref distanceMeters, ref lastNode);

                                }
                            }

                        }
                    }
                    routeMaster.ShapeID = shapeID; // Save for future reference
                    shapeID++;
                    shapeSequence = 1;
                }

                outfile.Close();
            }

        }


        private void SaveRoutes(string inFilename)
        {
            var outFilename = inFilename;

            if (!Directory.Exists(gtfsFolder)) Directory.CreateDirectory(gtfsFolder);

            SaveStops(gtfsFolder);
            SaveMasterRoutes(gtfsFolder);
            SaveShapes(gtfsFolder);


            // osmXMLDoc.Save(saveFilePath);

            LogMessage("Export complete", false);
        }


        private void ExportThread(object objOSMFilename)
        {
            try
            {
                string osmFilename = (string)objOSMFilename;
                LogMessage("OSM Export to GTFS started for " + osmFilename, false);
                DateTime startTime = DateTime.Now;
                stopPositions = new Dictionary<long, StopDistance>();
                LoadOSMFile(osmFilename);

                if (SortAllRouteWays())
                {
                    FindStopPositions();

                    // TODO - Move
                    SaveRoutes(osmFilename);



                    DateTime endTime = DateTime.Now;
                    var elapsed = endTime - startTime;
                    LogMessage("Export complete.  Elapsed time: " + elapsed.ToString(), false);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error during export operation: " + ex.Message + ex.StackTrace);
            }
            EndExportThread();

        }

        private void EndExportThread()
        {
            if (InvokeRequired)
            {
                Invoke(new MethodInvoker(EndExportThread));
                return;
            }
            btConvert.Enabled = true;
            btTimeSchedule.Enabled = true;
        }



        private void btConvert_Click(object sender, EventArgs e)
        {
            try
            {
                string osmFilename = tbFilename.Text;

                var dir = Path.GetDirectoryName(osmFilename);

                var baseFileName = Path.GetFileNameWithoutExtension(osmFilename);
                var ext = Path.GetExtension(osmFilename);
                var subfolder = "OSM_2_GTFS";
                gtfsFolder = Path.Combine(dir, subfolder);


                exportThread = new Thread(ExportThread);
                exportThread.Name = "Export Thread";
                exportThread.IsBackground = true;
                exportThread.Start(osmFilename);
                btConvert.Enabled = false;
                
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error starting Export operation: " + ex.Message + ex.StackTrace);
                btConvert.Enabled = true;
            }

        }

        private void btBrowseFolder_Click(object sender, EventArgs e)
        {
            openFileDialog1.FileName = tbFilename.Text;
            DialogResult result = openFileDialog1.ShowDialog();
            openFileDialog1.Filter = "OSM Map Vector Files(*.osm;*.xml)|*.osm;*.xml|All files (*.*)|*.*";
            openFileDialog1.FilterIndex = 2;

            if (result == DialogResult.OK)
            {
                tbFilename.Text = openFileDialog1.FileName;
                btConvert.Enabled = true;
            }

        }



        public void LogMessage(string msg, bool errorStatus)
        {

            const int MaxLogLines = 1000;

            if (terminating)
            {
                return;
            }
            if (InvokeRequired)
            {

                object[] arglist = new object[2];
                arglist[0] = msg;
                arglist[1] = errorStatus;
                Invoke(new LogMessageDelegate(LogMessage), arglist);
                return;
            }


            if (richTextLogBox.Lines.GetLength(0) > MaxLogLines)
            {
                // Delete the first line and reposition cursor to end
                richTextLogBox.Select(0, richTextLogBox.Lines[0].Length + 1);
                richTextLogBox.SelectedText = "";
                richTextLogBox.Select(richTextLogBox.TextLength, 0);
            }

            msg = DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss") + ": " + msg + "\n";


            if (errorStatus)
            {

                richTextLogBox.SelectionColor = Color.White;
                richTextLogBox.SelectionBackColor = Color.Red;
            }
            else
            {
                richTextLogBox.SelectionColor = Color.Black;
                richTextLogBox.SelectionBackColor = Color.White;
            }

            richTextLogBox.AppendText(msg);
            richTextLogBox.ScrollToCaret();

            Application.DoEvents();

        }


        private void btTimeSchedule_Click(object sender, EventArgs e)
        {
            using (TimeScheduleForm timeScheduleForm = new TimeScheduleForm(gtfsFolder, routeMasters, routes, ways, nodes, stopPositions))
            {
                timeScheduleForm.ShowDialog();
            }

        }


    }


    /// <summary>
    /// Characterize a distance from a stop position to the specified node segment of a way
    /// Used to determine stop position; but taking the shortest distance
    /// </summary>
    public class StopDistance
    {
        public Coord StopNode { get; set; }
        public Way Way { get; set; }
        public int WayPosition { get; set; } // New virtual node would be inserted after this position
        public Coord StopPosition { get; set; }
        public double Distance { get; set; }

        public StopDistance(Coord stopNode, Way way, int wayPosition, Coord stopPosition, double distance)
        {
            StopNode = stopNode;
            Way = way;
            WayPosition = wayPosition;
            StopPosition = stopPosition;
            Distance = distance;

        }

    }


}
