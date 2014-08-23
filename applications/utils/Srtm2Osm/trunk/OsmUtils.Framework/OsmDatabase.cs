using System;
using System.Collections.Generic;
using System.Text;
using System.Globalization;
using System.Diagnostics.CodeAnalysis;
using OsmUtils.OsmSchema;
using Brejc.Geometry;

namespace OsmUtils.Framework
{
    public class OsmDatabase
    {
        public IDictionary<long, OsmNode> Nodes
        {
            get { return nodes; }
        }

        public IDictionary<long, OsmWay> Ways
        {
            get { return ways; }
        }

        /// <summary>
        /// Gets a dictionary of OSM relations in the OSM database.
        /// </summary>
        /// <value>A dictionary of OSM relations.</value>
        public IDictionary<long, OsmRelation> Relations
        {
            get { return relations; }
        }

        public void AddNode (OsmNode node)
        {
            if (this.nodes.ContainsKey(node.ObjectId))
                throw new OsmDuplicatedObjectIdException(node);

            nodes.Add (node.ObjectId, node);
        }

        public void AddWay (OsmWay way)
        {
            if (this.ways.ContainsKey(way.ObjectId))
                throw new OsmDuplicatedObjectIdException(way);

            ways.Add (way.ObjectId, way);
        }

        /// <summary>
        /// Adds a specified OSM relation to the OSM database.
        /// </summary>
        /// <param name="relation">The OSM relation to add.</param>
        public void AddRelation (OsmRelation relation)
        {
            if (this.relations.ContainsKey(relation.ObjectId))
                throw new OsmDuplicatedObjectIdException(relation);

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
                    node.Timestamp = DateTime.Parse (originalNode.Timestamp, CultureInfo.InvariantCulture);
                node.User = OsmUser.Fetch(originalNode.User, originalNode.Uid);
                node.ChangesetId = originalNode.Changeset;
                node.Visible = originalNode.Visible;
                node.Action = objectAction;

                foreach (tag tag in originalNode.Tag)
                    node.SetTag (tag.K, tag.V);

                AddNode(node);
            }

            foreach (osmWay originalWay in osmData.Way)
            {
                OsmObjectAction objectAction = ParseOsmObjectAction (originalWay.Action);
                if (objectAction == OsmObjectAction.Delete)
                    continue;

                OsmWay way = new OsmWay (originalWay.Id);
                if (originalWay.Timestamp != null)
                    way.Timestamp = DateTime.Parse (originalWay.Timestamp, CultureInfo.InvariantCulture);
                way.User = OsmUser.Fetch(originalWay.User, originalWay.Uid);
                way.ChangesetId = originalWay.Changeset;
                way.Visible = originalWay.Visible;
                way.Action = objectAction;

                foreach (osmWayND nd in originalWay.Nd)
                    way.AddNode (nd.Ref);

                foreach (tag tag in originalWay.Tag)
                    way.SetTag (tag.K, tag.V);

                AddWay(way);
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
                    relation.Timestamp = DateTime.Parse (originalRelation.Timestamp, CultureInfo.InvariantCulture);
                relation.User = OsmUser.Fetch(originalRelation.User, originalRelation.Uid);
                relation.ChangesetId = originalRelation.Changeset;
                relation.Visible = originalRelation.Visible;
                relation.Action = objectAction;

                foreach (osmRelationMember member in originalRelation.Member)
                {
                    OsmReferenceType referenceType = ParseOsmReferenceType (member.Type);
                    relation.AddMember (referenceType, member.Ref, member.Role);
                }

                foreach (tag tag in originalRelation.Tag)
                    relation.SetTag (tag.K, tag.V);

                AddRelation(relation);
            }
        }

        /// <summary>
        /// Exports the contents of this database to OSM XML.
        /// </summary>
        /// <param name="user">Username</param>
        /// <param name="uid">User ID</param>
        /// <param name="changeset">Changeset ID</param>
        /// <returns>Data for XmlSerializer</returns>
        [SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly")]
        public osm ExportData (string generator)
        {
            osm osmData = new osm ();
            osmData.Version = "0.6";
            osmData.Upload = false;
            if (String.IsNullOrEmpty(generator))
                osmData.Generator = "OsmUtils";
            else
                osmData.Generator = generator;

            osmData.Node = new List<osmNode> ();
            osmData.Way = new List<osmWay> ();
            osmData.Relation = new List<osmRelation> ();

            foreach (OsmNode node in nodes.Values)
            {
                osmNode exportedNode = new osmNode ();
                exportedNode.Id = node.ObjectId;
                exportedNode.Lat = node.Latitude;
                exportedNode.Lon = node.Longitude;
                exportedNode.Timestamp = node.Timestamp.ToString ("o", CultureInfo.InvariantCulture);
                exportedNode.User = node.User.Name;
                exportedNode.Uid = node.User.Id;
                exportedNode.Visible = node.Visible;
                exportedNode.Action = FormatOsmObjectAction (node.Action);
                exportedNode.Version = 1;
                exportedNode.Changeset = node.ChangesetId;

                // Do explicity set the Lat- / LonSpecified properties.
                // Otherwise the lat / lon XML attributes would not get written, if the node has
                // a latitude or longitude of exactly 0°.
                exportedNode.LatSpecified = true;
                exportedNode.LonSpecified = true;

                exportedNode.Tag = new List<tag> ();
                foreach (OsmTag tag in node.EnumerateTags ())
                    exportedNode.Tag.Add (new tag (tag.Key, tag.Value));

                osmData.Node.Add (exportedNode);
            }

            foreach (OsmWay way in ways.Values)
            {
                osmWay exportedWay = new osmWay ();
                exportedWay.Id = way.ObjectId;
                exportedWay.Timestamp = way.Timestamp.ToString ("o" , CultureInfo.InvariantCulture);
                exportedWay.User = way.User.Name;
                exportedWay.Uid = way.User.Id;
                exportedWay.Visible = way.Visible;
                exportedWay.Action = FormatOsmObjectAction (way.Action);
                exportedWay.Version = 1;
                exportedWay.Changeset = way.ChangesetId;

                exportedWay.Nd = new List<osmWayND> ();
                foreach (long nodeId in way.EnumerateNodesIds ())
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
                exportedRelation.Timestamp = relation.Timestamp.ToString ("o", CultureInfo.InvariantCulture);
                exportedRelation.User = relation.User.Name;
                exportedRelation.Uid = relation.User.Id;
                exportedRelation.Visible = relation.Visible;
                exportedRelation.Action = FormatOsmObjectAction (relation.Action);
                exportedRelation.Version = 1;
                exportedRelation.Changeset = relation.ChangesetId;

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

            return (OsmObjectAction) Enum.Parse (typeof (OsmObjectAction), action, true);
        }

        /// <summary>
        /// Formats a specified value of <see cref="OsmObjectAction"/> into a string.
        /// </summary>
        /// <param name="action">The OSM object action value.</param>
        /// <returns>The OSM object action value in a string form.</returns>
        static public string FormatOsmObjectAction (OsmObjectAction action)
        {
            return osmObjectActionStringConstants [(int)action];
        }

        private Dictionary<long, OsmNode> nodes = new Dictionary<long, OsmNode> ();
        private Dictionary<long, OsmWay> ways = new Dictionary<long, OsmWay> ();
        private Dictionary<long, OsmRelation> relations = new Dictionary<long, OsmRelation> ();

        static private string[] osmObjectActionStringConstants = new string[] { null, "delete", "modify" };
        static private string[] osmReferenceTypeStringConstants = new string[] { null, "node", "way", "relation" };
    }
}
