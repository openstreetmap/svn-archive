using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace OsmUtils.Framework
{
    public class OsmWay : OsmObjectBase, ICloneable
    {
        public IList<int> NodesList
        {
            get { return nodesList; }
        }

        public bool IsClosed
        {
            get
            {
                if (nodesList.Count == 0)
                    return false;

                return nodesList[0] == nodesList[nodesList.Count - 1];
            }
        }

        public OsmWay (int wayId)
            : base (wayId)
        {
        }

        public void AddNode (int nodeId)
        {
            nodesList.Add (nodeId);
        }

        public IEnumerable<int> EnumerateNodesIds ()
        {
            return nodesList;
        }

        /// <summary>
        /// Calculates the orientation of the way.
        /// </summary>
        /// <param name="osmDB">The OSM database.</param>
        /// <returns>A value indicating the orientation:
        /// <list>
        /// <item>&lt;0</item><description>Way is clockwise.</description>
        /// <item>&gt;=0</item><description>Way is counterclockwise.</description>
        /// </list>
        /// </returns>
        public double CalculateOrientation(OsmDatabase osmDB)
        {
            if (nodesList.Count < 3)
                return 0;

            // first find rightmost lowest vertex of the polygon
            int rmin = 0;
            double xmin = osmDB.Nodes [nodesList[0]].Longitude;
            double ymin = osmDB.Nodes [nodesList[0]].Latitude;

            for (int i = 1; i < nodesList.Count; i++)
            {
                OsmNode node = osmDB.Nodes[nodesList[i]];

                if (node.Latitude > ymin)
                    continue;
                if (node.Latitude == ymin)
                {    // just as low
                    if (node.Longitude < xmin)   // and to left
                        continue;
                }
                rmin = i;          // a new rightmost lowest vertex
                xmin = node.Longitude;
                ymin = node.Latitude;
            }

            OsmNode point0 = osmDB.Nodes[nodesList[(rmin - 1 + nodesList.Count) % nodesList.Count]];
            OsmNode point1 = osmDB.Nodes[nodesList[rmin]];
            OsmNode point2 = osmDB.Nodes[nodesList[(rmin + 1) % nodesList.Count]];

            // test orientation at this rmin vertex
            // ccw <=> the edge leaving is left of the entering edge
            return ((point1.Longitude - point0.Longitude) * (point2.Latitude - point0.Latitude)
                    - (point2.Longitude - point0.Longitude) * (point1.Latitude - point0.Latitude));
        }

        public override string ToString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "Way (id={0}, nodes={1})", ObjectId, nodesList.Count);
        }

        public override void CopyToClone (OsmObjectBase clone)
        {
            base.CopyToClone (clone);

            ((OsmWay)clone).nodesList = new List<int> (nodesList);
        }

        #region ICloneable Members

        /// <summary>
        /// Creates a new object that is a copy of the current instance.
        /// </summary>
        /// <returns>
        /// A new object that is a copy of this instance.
        /// </returns>
        public object Clone ()
        {
            OsmWay clone = new OsmWay (ObjectId);
            CopyToClone (clone);
            clone.nodesList = new List<int> (nodesList);
            return clone;
        }

        #endregion

        private List<int> nodesList = new List<int> ();
    }
}
