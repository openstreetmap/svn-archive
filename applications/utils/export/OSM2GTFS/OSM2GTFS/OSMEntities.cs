using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace OSM2GTFS
{

    public class Coord
    {
        public double Lat { get; set; }
        public double Lon { get; set; }
        public Int64 NodeID { get; set; }
        public Dictionary<string, string> TagList { get; set; }

        public int StopID { get; set; }

        public Coord(Int64 nodeID, double lat, double lon, Dictionary<string, string> tagList)
        {
            StopID = -1; // Unassigned
            NodeID = nodeID;
            Lat = lat;
            Lon = lon;
            TagList = tagList;
        }
        public Coord(Int64 nodeID, string lat, string lon, Dictionary<string, string> tagList)
        {
            StopID = -1; // Unassigned
            NodeID = nodeID;
            Lat = Convert.ToDouble(lat);
            Lon = Convert.ToDouble(lon);
            TagList = tagList;
        }

        public double DegreeToRadians(double degrees)
        {
            return degrees * Math.PI / 180.0;
        }


        public double RadianToDegree(double angle)
        {
            return angle * (180.0 / Math.PI);
        }


        /// <summary>
        /// Computes the distance between this coordinate and another point on the earth.
        /// Uses spherical law of cosines formula, not Haversine.
        /// </summary>
        /// <param name="other">The other point</param>
        /// <returns>Distance in meters</returns>
        public double GreatCircleDistance(Coord other)
        {
            var epsilon = Math.Abs(other.Lon - Lon) + Math.Abs(other.Lat - Lat);
            if (epsilon < 1.0e-6) return 0.0;

            double meters = (Math.Acos(
                    Math.Sin(DegreeToRadians(Lat)) * Math.Sin(DegreeToRadians(other.Lat)) +
                    Math.Cos(DegreeToRadians(Lat)) * Math.Cos(DegreeToRadians(other.Lat)) *
                    Math.Cos(DegreeToRadians(other.Lon - Lon))) * 6378135);

            return (meters);
        }

    }

    public class Way
    {
        public List<Int64> NodeList { get; set; }

        public Dictionary<string, string> TagList { get; set; }

        public Int64 FirstNode
        {
            get { return NodeList[0]; }
        }
        public Int64 LastNode
        {
            get { return NodeList.Last(); }
        }

        public Int64 FirstRoleNode(string role)
        {
            if (role == "forward")
            {
                return NodeList[0];
            }
            else
            {
                return NodeList.Last();
            }

        }

        public Int64 LastRoleNode(string role)
        {
            if (role == "backward")
            {
                return NodeList[0];
            }
            else
            {
                return NodeList.Last();
            }

        }


        public Int64 WayID { get; set; }


        public Way(Int64 wayID, List<Int64> nodeListToUse, Dictionary<string, string> tagList)
        {
            NodeList = nodeListToUse;
            WayID = wayID;
            TagList = tagList;
        }

    }


    public class RelationMember
    {
        public string RelationType { get; set; }
        public Int64 RelationRef { get; set; }
        public string Role { get; set; }

        public RelationMember(string relationType, Int64 relationRef, string role)
        {
            RelationType = relationType;
            RelationRef = relationRef;
            Role = role;
        }
    }


    /// <summary>
    /// Not an OSM Entity, but associated with a route relation
    /// </summary>
    public class Trip
    {
        public List<DateTime> StopTimeList { get; set; }
        public List<int> StopIDList { get; set; }
        public string ServiceId { get; set; }
        public int TripID { get; set; }


        public Trip(string serviceID)
        {
            ServiceId = serviceID;
            StopTimeList = null;
            StopIDList = null;
            TripID = -1;
        }

    }

    public class Relation
    {
        public List<RelationMember> Members { get; set; }
        public Dictionary<string, string> TagList { get; set; }

        public List<RelationMember> SortedWays { get; set; }
        public List<RelationMember> SortedStops { get; set; }

        public Int64 RelationID { get; set; }

        public string RouteID { get; set; }

        public int ShapeID { get; set; }

        public List<Trip> TripList { get; set; }

        public List<double> StopDistances { get; set; }


        public Relation(Int64 relationID, List<RelationMember> membersToUse, Dictionary<string, string> tagsToUse)
        {
            RelationID = relationID;
            Members = membersToUse;
            TagList = tagsToUse;


            SortedWays = null;
            SortedStops = null;
            TripList = null;
            StopDistances = null;

        }
    }

}
