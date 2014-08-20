using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;
using Brejc.DemLibrary;
using Brejc.Geometry;
using OsmUtils.Framework;
using OsmUtils.OsmSchema;
using OsmUtils.OsmClient;

namespace Srtm2Osm
{
    /// <summary>
    /// This kind of output class stores every calculated isohypse in memory in an instance of OsmDatabase class.
    /// The content of this instance will then in the end serialized to the given output file.
    /// </summary>
    class DatabaseOutput : OutputBase
    {
        public DatabaseOutput (FileInfo file, OutputSettings settings)
            : base (file, settings)
        {
            this.osmDb = new OsmDatabase ();
            this.user = OsmUser.Fetch (settings.UserName, settings.UserId);
        }

        public override void Begin ()
        { }

        public override void ProcessIsohypse (Isohypse isohypse, NextIdCallback nodeCallback, NextIdCallback wayCallback)
        {
            foreach (Polyline polyline in isohypse.Segments)
            {
                OsmWay isohypseWay = new OsmWay (wayCallback ());
                isohypseWay.Visible = true;
                isohypseWay.Timestamp = DateTime.UtcNow;
                isohypseWay.User = this.user;
                isohypseWay.ChangesetId = settings.ChangesetId;

                settings.ContourMarker.MarkContour (isohypseWay, isohypse);
                osmDb.AddWay (isohypseWay);

                long firstWayNodeId = 0;

                for (int i = 0; i < polyline.VerticesCount; i++)
                {
                    Point3<double> point = polyline.Vertices[i];

                    OsmNode node = new OsmNode (nodeCallback (), point.Y + settings.LongitudeCorrection, point.X + settings.LatitudeCorrection);
                    node.Visible = true;
                    node.Timestamp = DateTime.UtcNow;
                    node.User = this.user;
                    node.ChangesetId = settings.ChangesetId;
                    osmDb.AddNode (node);

                    if (i == 0)
                        firstWayNodeId = node.ObjectId;

                    isohypseWay.AddNode (node.ObjectId);

                    // Split the polyline if the maximum node count per way is reached.
                    if (isohypseWay.NodesList.Count == settings.MaxWayNodes && polyline.VerticesCount > settings.MaxWayNodes)
                    {
                        // Don't create a new way if already at the end of the *unclosed* polyline
                        if (i == polyline.VerticesCount - 1 && !polyline.IsClosed)
                            continue;

                        isohypseWay = new OsmWay (wayCallback ());
                        isohypseWay.Visible = true;
                        isohypseWay.Timestamp = DateTime.UtcNow;
                        isohypseWay.User = this.user;
                        isohypseWay.ChangesetId = settings.ChangesetId;

                        settings.ContourMarker.MarkContour (isohypseWay, isohypse);
                        osmDb.AddWay (isohypseWay);
                        isohypseWay.AddNode (node.ObjectId);
                    }
                }

                // if the isohypse segment is closed, add the first node as the final node of the way
                if (polyline.IsClosed)
                    isohypseWay.AddNode (firstWayNodeId);
            }
        }

        public override void End ()
        {
            OsmUtils.OsmSchema.osm osmData = osmDb.ExportData ("Srtm2Osm");
            OsmUtils.OsmClient.OsmClient06.SaveFile (osmData, this.file.FullName);
        }

        public override void Merge (string mergeFile)
        {
            osm osmExistingFile = OsmClient06.LoadFile (mergeFile);
            osmDb.ImportData (osmExistingFile);
        }

        private readonly OsmDatabase osmDb;
        private readonly OsmUser user;
    }
}
