using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;
using Brejc.DemLibrary;
using Brejc.Geometry;
using OsmUtils.OsmSchema;

namespace Srtm2Osm
{
    /// <summary>
    /// This kind of output serializes every calculated isohypse directly to the given file.
    /// </summary>
    class DirectOutput : OutputBase
    {
        public DirectOutput (FileInfo file, OutputSettings settings)
            : base (file, settings)
        {
            this.stream = file.Create ();

            nodeSerializer = new XmlSerializer (typeof (osmNode), new XmlRootAttribute ("node"));
            waySerializer = new XmlSerializer (typeof (osmWay), new XmlRootAttribute ("way"));

            ns = new XmlSerializerNamespaces ();
            ns.Add (String.Empty, String.Empty);
        }

        public override void Begin ()
        {
            XmlWriterSettings settings = new XmlWriterSettings ();
            settings.Indent = true;
            settings.IndentChars = ("\t");
            settings.Encoding = new UTF8Encoding (false);

            writer = XmlWriter.Create (stream, settings);
            writer.WriteStartElement ("osm");
            writer.WriteAttributeString ("version", "0.6");
            writer.WriteAttributeString ("generator", "Srtm2Osm");
            writer.WriteAttributeString ("upload", "false");
        }

        public override void ProcessIsohypse (Isohypse isohypse, NextIdCallback nodeCallback, NextIdCallback wayCallback)
        {
            if (writer == null)
                throw new InvalidOperationException ("The Begin method must be called before calling ProcessIsohypse.");
            if (isohypse == null)
                throw new ArgumentNullException("isohypse");

            foreach (Polyline polyline in isohypse.Segments)
            {
                OsmUtils.OsmSchema.osmWay way = new osmWay ();

                settings.ContourMarker.MarkContour (way, isohypse);

                way.Id = wayCallback ();
                way.Nd = new List<osmWayND> ();
                way.Timestamp = DateTime.UtcNow.ToString ("o", System.Globalization.CultureInfo.InvariantCulture);
                way.Version = 1;
                way.User = settings.UserName;
                way.Uid = settings.UserId;
                way.Changeset = settings.ChangesetId;

                long firstWayNodeId = 0;

                for (int i = 0; i < polyline.Vertices.Count; i++)
                {
                    Point3<double> point = polyline.Vertices[i];

                    OsmUtils.OsmSchema.osmNode node = new osmNode ();
                    node.Id = nodeCallback ();
                    node.Lat = point.Y + settings.LatitudeCorrection;
                    node.Lon = point.X + settings.LongitudeCorrection;
                    node.Timestamp = DateTime.UtcNow.ToString ("o", System.Globalization.CultureInfo.InvariantCulture);
                    node.Version = 1;
                    node.User = settings.UserName;
                    node.Uid = settings.UserId;
                    node.Changeset = settings.ChangesetId;

                    // Do explicity set the Lat- / LonSpecified properties.
                    // Otherwise the lat / lon XML attributes would not get written, if the node has
                    // a latitude or longitude of exactly 0°.
                    node.LatSpecified = true;
                    node.LonSpecified = true;

                    if (i == 0)
                        firstWayNodeId = node.Id;

                    nodeSerializer.Serialize (writer, node, ns);

                    way.Nd.Add (new osmWayND (node.Id, true));

                    // Split the way if the maximum node count per way is reached.
                    if (way.Nd.Count == settings.MaxWayNodes && polyline.VerticesCount > settings.MaxWayNodes)
                    {
                        // Don't create a new way if already at the end of the *unclosed* polyline
                        if (i == polyline.VerticesCount - 1 && !polyline.IsClosed)
                            continue;

                        // first, serialize old way
                        waySerializer.Serialize (writer, way, ns);

                        way = new osmWay ();
                        way.Id = wayCallback ();
                        way.Nd = new List<osmWayND> ();
                        way.Timestamp = DateTime.UtcNow.ToString ("o", System.Globalization.CultureInfo.InvariantCulture);
                        way.Version = 1;
                        way.User = settings.UserName;
                        way.Uid = settings.UserId;
                        way.Changeset = settings.ChangesetId;

                        settings.ContourMarker.MarkContour (way, isohypse);
                        way.Nd.Add (new osmWayND (node.Id, true));
                    }
                }

                // if the isohypse segment is closed, add the first node as the final node of the way
                if (polyline.IsClosed)
                    way.Nd.Add (new osmWayND (firstWayNodeId, true));

                waySerializer.Serialize (writer, way, ns);
            }
        }

        public override void End ()
        {
            if (writer == null)
                throw new InvalidOperationException ("The Begin method must be called before calling End.");

            writer.WriteEndElement ();
            writer.Close ();
        }

        public override void Merge (string mergeFile)
        {
            if (writer == null)
                throw new InvalidOperationException ("The Begin method must be called before calling Merge.");
            if (!File.Exists(mergeFile))
                throw new FileNotFoundException("File not found.", mergeFile);

            XmlReader reader = XmlReader.Create(mergeFile);

            // Iterate through all elements...
            while (reader.Read())
            {
                // ... but ignore the root element "osm".
                if (reader.NodeType == XmlNodeType.Element && reader.Name == "osm")
                    continue;
                else if (reader.NodeType == XmlNodeType.EndElement && reader.Name == "osm")
                    continue;

                // Only the elements are merged with the new file.
                // Text, CDATA, whitespace, comments, etc. are ignored.
                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:
                        writer.WriteStartElement(reader.Name);
                        writer.WriteAttributes(reader, true);
                        if (reader.IsEmptyElement)
                            writer.WriteEndElement();
                        break;
                    case XmlNodeType.EndElement:
                        writer.WriteFullEndElement();
                        break;
                }
            }

            reader.Close();
        }

        private readonly FileStream stream;
        private readonly XmlSerializer nodeSerializer;
        private readonly XmlSerializer waySerializer;
        private readonly XmlSerializerNamespaces ns;
        private XmlWriter writer;
    }
}
