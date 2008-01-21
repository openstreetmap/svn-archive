using System;
using System.Collections.Generic;
using System.Text;
using OsmUtils.OsmSchema;
using Brejc.Geometry;

namespace OsmUtils.Framework
{
    public class OsmDatabase
    {
        public IDictionary<int, OsmNode> Nodes
        {
            get { return nodes; }
        }

        public IDictionary<int, OsmWay> Ways
        {
            get { return ways; }
        }

        /// <summary>
        /// Gets a dictionary of OSM relations in the OSM database.
        /// </summary>
        /// <value>A dictionary of OSM relations.</value>
        public IDictionary<int, OsmRelation> Relations
        {
            get { return relations; }
        }

        public void AddNode (OsmNode node)
        {
            nodes.Add (node.ObjectId, node);
        }

        public void AddWay (OsmWay way)
        {
            ways.Add (way.ObjectId, way);
        }

        /// <summary>
        /// Adds a specified OSM relation to the OSM database.
        /// </summary>
        /// <param name="relation">The OSM relation to add.</param>
        public void AddRelation (OsmRelation relation)
        {
            relations.Add (relation.ObjectId, relation);
        }

        public void ImportData (osm osmData)
        {
            foreach (osmNode originalNode in osmData.Node)
            {
                OsmObjectAction objectAction = ParseOsmObjectAction (originalNode.Action);
                if (objectAction == OsmObjectAction.Delete)
                    continue;

                OsmNode node = new OsmNode (originalNode.Id, originalNode.Lat, originalNode.Lon);
                if (originalNode.Timestamp != null)
                    node.Timestamp = DateTime.Parse (originalNode.Timestamp, System.Globalization.CultureInfo.InvariantCulture);
                node.User = originalNode.User;
                node.Visible = originalNode.Visible;
                node.Action = objectAction;

                foreach (tag tag in originalNode.Tag)
                    node.SetTag (tag.K, tag.V);

                //if (false == nodes.ContainsKey (node.ObjectId))
                nodes.Add (node.ObjectId, node);
            }

            foreach (osmWay originalWay in osmData.Way)
            {
                OsmObjectAction objectAction = ParseOsmObjectAction (originalWay.Action);
                if (objectAction == OsmObjectAction.Delete)
                    continue;

                OsmWay way = new OsmWay (originalWay.Id);
                if (originalWay.Timestamp != null)
                    way.Timestamp = DateTime.Parse (originalWay.Timestamp, System.Globalization.CultureInfo.InvariantCulture); ;
                way.User = originalWay.User;
                way.Visible = originalWay.Visible;
                way.Action = objectAction;

                foreach (osmWayND nd in originalWay.Nd)
                    way.AddNode (nd.Ref);

                foreach (tag tag in originalWay.Tag)
                    way.SetTag (tag.K, tag.V);

                //if (false == ways.ContainsKey (way.ObjectId))
                ways.Add (way.ObjectId, way);
            }

            foreach (osmRelation originalRelation in osmData.Relation)
            {
                OsmObjectAction objectAction = ParseOsmObjectAction (originalRelation.Action);
                if (objectAction == OsmObjectAction.Delete)
                    continue;

                if (originalRelation.Action == "delete")
                    continue;

                OsmRelation relation = new OsmRelation (originalRelation.Id);
                if (originalRelation.Timestamp != null)
                    relation.Timestamp = DateTime.Parse (originalRelation.Timestamp, System.Globalization.CultureInfo.InvariantCulture); ;
                relation.User = originalRelation.User;
                relation.Visible = originalRelation.Visible;
                relation.Action = objectAction;

                foreach (osmRelationMember member in originalRelation.Member)
                {
                    OsmReferenceType referenceType = ParseOsmReferenceType (member.Type);
                    relation.AddMember (referenceType, member.Ref, member.Role);
                }

                foreach (tag tag in originalRelation.Tag)
                    relation.SetTag (tag.K, tag.V);

                //if (false == relations.ContainsKey (relation.ObjectId))
                relations.Add (relation.ObjectId, relation);
            }
        }

        public osm ExportData ()
        {
            osm osmData = new osm ();
            osmData.Version = "0.5";
            osmData.Generator = "OsmUtils";
            osmData.Node = new List<osmNode> ();
            osmData.Way = new List<osmWay> ();
            osmData.Relation = new List<osmRelation> ();

            foreach (OsmNode node in nodes.Values)
            {
                osmNode exportedNode = new osmNode ();
                exportedNode.Id = node.ObjectId;
                exportedNode.Lat = node.Latitude;
                exportedNode.Lon = node.Longitude;
                exportedNode.Timestamp = node.Timestamp.ToString ("s", System.Globalization.CultureInfo.InvariantCulture);
                exportedNode.User = node.User;
                exportedNode.Visible = node.Visible;
                exportedNode.Action = FormatOsmObjectAction (node.Action);

                exportedNode.Tag = new List<tag> ();
                foreach (OsmTag tag in node.EnumerateTags ())
                    exportedNode.Tag.Add (new tag (tag.Key, tag.Value));

                osmData.Node.Add (exportedNode);
            }

            foreach (OsmWay way in ways.Values)
            {
                osmWay exportedWay = new osmWay ();
                exportedWay.Id = way.ObjectId;
                exportedWay.Timestamp = way.Timestamp.ToString ("s" , System.Globalization.CultureInfo.InvariantCulture);
                exportedWay.User = way.User;
                exportedWay.Visible = way.Visible;
                exportedWay.Action = FormatOsmObjectAction (way.Action);

                exportedWay.Nd = new List<osmWayND> ();
                foreach (int nodeId in way.EnumerateNodesIds ())
                {
                    osmWayND wayNode = new osmWayND ();
                    wayNode.Ref = nodeId;
                    exportedWay.Nd.Add (wayNode);
                }

                exportedWay.Tag = new List<tag> ();
                foreach (OsmTag tag in way.EnumerateTags ())
                    exportedWay.Tag.Add (new tag (tag.Key, tag.Value));

                osmData.Way.Add (exportedWay);
            }

            foreach (OsmRelation relation in relations.Values)
            {
                osmRelation exportedRelation = new osmRelation ();
                exportedRelation.Id = relation.ObjectId;
                exportedRelation.Timestamp = relation.Timestamp.ToString ("s", System.Globalization.CultureInfo.InvariantCulture);
                exportedRelation.User = relation.User;
                exportedRelation.Visible = relation.Visible;
                exportedRelation.Action = FormatOsmObjectAction (relation.Action);

                exportedRelation.Member = new List<osmRelationMember> ();
                foreach (OsmRelationMember member in relation.EnumerateMembers ())
                {
                    osmRelationMember exportedMember = new osmRelationMember ();
                    exportedMember.Type = FormatOsmReferenceType (member.MemberReference.ReferenceType);
                    exportedMember.Ref = member.MemberReference.ReferenceId;
                    exportedMember.Role = member.Role;
                    exportedRelation.Member.Add (exportedMember);
                }

                exportedRelation.Tag = new List<tag> ();
                foreach (OsmTag tag in relation.EnumerateTags ())
                    exportedRelation.Tag.Add (new tag (tag.Key, tag.Value));

                osmData.Relation.Add (exportedRelation);
            }

            return osmData;
        }

        public Bounds2 CalculateBounds ()
        {
            Bounds2 bounds = new Bounds2 ();

            foreach (OsmNode node in nodes.Values)
                bounds.ExtendToCover (node.Longitude, node.Latitude);

            return bounds;
        }

        /// <summary>
        /// Parses the OSM reference type from a string and returns <see cref="OsmReferenceType"/> enumeration value.
        /// </summary>
        /// <param name="referenceType">OSM reference type in a string form. The method is case insensitive.</param>
        /// <returns><see cref="OsmReferenceType"/> enumeration value.</returns>
        /// <exception cref="ArgumentException">The value could not be parsed.</exception>
        static public OsmReferenceType ParseOsmReferenceType (string referenceType)
        {
            return (OsmReferenceType) Enum.Parse (typeof (OsmReferenceType), referenceType, true);
        }

        /// <summary>
        /// Formats a specified value of <see cref="OsmReferenceType"/> into a string.
        /// </summary>
        /// <param name="action">The OSM reference type value.</param>
        /// <returns>The OSM reference type value in a string form.</returns>
        static public string FormatOsmReferenceType (OsmReferenceType osmReferenceType)
        {
            return osmReferenceTypeStringConstants [(int)osmReferenceType];
        }

        /// <summary>
        /// Parses the OSM object action from a string and returns <see cref="OsmReferenceType"/> enumeration value.
        /// </summary>
        /// <param name="referenceType">OSM object action in a string form. The method is case insensitive.</param>
        /// <returns><see cref="OsmObjectAction"/> enumeration value.</returns>
        /// <exception cref="ArgumentException">The value could not be parsed.</exception>
        static public OsmObjectAction ParseOsmObjectAction (string action)
        {
            if (action == null)
                return OsmObjectAction.None;

            return (OsmObjectAction)Enum.Parse (typeof (OsmObjectAction), action, true);
        }

        /// <summary>
        /// Formats a specified value of <see cref="OsmObjectAction"/> into a string.
        /// </summary>
        /// <param name="action">The OSM object action value.</param>
        /// <returns>The OSM object action value in a string form.</returns>
        static public string FormatOsmObjectAction (OsmObjectAction action)
        {
            return osmObjectActionStringConstants[(int)action];
        }

        private Dictionary<int, OsmNode> nodes = new Dictionary<int, OsmNode> ();
        private Dictionary<int, OsmWay> ways = new Dictionary<int, OsmWay> ();
        private Dictionary<int, OsmRelation> relations = new Dictionary<int, OsmRelation> ();

        static private string[] osmObjectActionStringConstants = new string[] { null, "delete", "modify" };
        static private string[] osmReferenceTypeStringConstants = new string[] { null, "node", "way", "relation" };
    }
}
