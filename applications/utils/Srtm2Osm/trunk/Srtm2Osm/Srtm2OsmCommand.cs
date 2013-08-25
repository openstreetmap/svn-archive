using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Common.Console;
using Brejc.DemLibrary;
using OsmUtils.Framework;
using OsmUtils.OsmSchema;
using System.IO;
using OsmUtils.OsmClient;
using System.Xml;
using System.Web;
using System.Collections.Specialized;
using System.Xml.Serialization;
using Brejc.Geometry;
using System.Text.RegularExpressions;

namespace Srtm2Osm
{
    public enum Srtm2OsmCommandOption
    {
        Bounds1,
        Bounds2,
        Bounds3,
        OutputFile,
        MergeFile,
        SrtmCachePath,
        RegenerateIndexFile,
        ElevationStep,
        Categories,
        Feet,
        LargeAreaMode,
        CorrectionXY,
        SrtmSource,
        MaxWayNodes
    }

    public class Srtm2OsmCommand : IConsoleApplicationCommand
    {
        #region IConsoleApplicationCommand Members

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes")]
        public void Execute ()
        {
            ConsoleActivityLogger activityLogger = new ConsoleActivityLogger();
            activityLogger.LogLevel = ActivityLogLevel.Verbose;

            // first make sure that the SRTM directory exists
            if (false == Directory.Exists (srtmDir))
                Directory.CreateDirectory (srtmDir);

            string srtmIndexFilename = Path.Combine (srtmDir, "SrtmIndex.dat");
            SrtmIndex srtmIndex = null;
            SrtmIndex.SrtmSource = srtmSource;

            try
            {
                srtmIndex = SrtmIndex.Load (srtmIndexFilename);
            }
            catch (Exception)
            {
                // in case of exception, regenerate the index
                generateIndex = true;
            }
            
            if (generateIndex)
            {
                srtmIndex = new SrtmIndex ();
                srtmIndex.ActivityLogger = activityLogger;
                srtmIndex.Generate ();
                srtmIndex.Save (srtmIndexFilename);

                srtmIndex = SrtmIndex.Load (srtmIndexFilename);
            }

            Srtm3Storage.SrtmSource = srtmSource;
            Srtm3Storage storage = new Srtm3Storage(Path.Combine(srtmDir, "SrtmCache"), srtmIndex);
            storage.ActivityLogger = activityLogger;

            Bounds2 corrBounds = new Bounds2(bounds.MinX - corrX, bounds.MinY - corrY, 
                bounds.MaxX - corrX, bounds.MaxY - corrY);
     
            IRasterDigitalElevationModel dem = (IRasterDigitalElevationModel) storage.LoadDemForArea (corrBounds);

            DigitalElevationModelStatistics statistics = dem.CalculateStatistics ();

            activityLogger.Log (ActivityLogLevel.Normal, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "DEM data points count: {0}", dem.DataPointsCount));
            activityLogger.Log (ActivityLogLevel.Normal, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "DEM minimum elevation: {0}", statistics.MinElevation));
            activityLogger.Log (ActivityLogLevel.Normal, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "DEM maximum elevation: {0}", statistics.MaxElevation));
            activityLogger.Log (ActivityLogLevel.Normal, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "DEM has missing points: {0}", statistics.HasMissingPoints));

            IIsopletingAlgorithm alg = new Igor4IsopletingAlgorithm ();
            alg.ActivityLogger = activityLogger;

            double elevationStepInUnits = elevationStep * elevationUnits;
            contourMarker.Configure (elevationUnits);

            // Start with highest possible ID and count down. That should give maximum space between
            // contour data and real OSM data.
            long wayId = long.MaxValue, nodeId = long.MaxValue;

            // The following IDs do exist in the OSM database
            string user = "Srtm2Osm";
            int uid = 941874;
            int changeset = 13341398;

            if (largeAreaMode)
            {
                XmlSerializer nodeSerializer = new XmlSerializer (typeof (osmNode), new XmlRootAttribute ("node"));
                XmlSerializer waySerializer = new XmlSerializer (typeof (osmWay), new XmlRootAttribute ("way"));

                XmlSerializerNamespaces ns = new XmlSerializerNamespaces ();
                ns.Add (String.Empty, String.Empty);

                using (FileStream stream = File.Open (outputOsmFile, FileMode.Create, FileAccess.Write))
                {
                    XmlWriterSettings settings = new XmlWriterSettings ();
                    settings.Indent = true;
                    settings.IndentChars = ("\t");
                    settings.Encoding = new UTF8Encoding (false);

                    using (XmlWriter writer = XmlWriter.Create (stream, settings))
                    {
                        writer.WriteStartElement ("osm");
                        writer.WriteAttributeString ("version", "0.6");
                        writer.WriteAttributeString ("generator", "Srtm2Osm");
                        writer.WriteAttributeString ("upload", "false");

                        alg.Isoplete (dem, elevationStepInUnits, delegate (Isohypse isohypse)
                        {
                            foreach (Polyline polyline in isohypse.Segments)
                            {
                                OsmUtils.OsmSchema.osmWay way = new osmWay ();

                                contourMarker.MarkContour (way, isohypse);

                                way.Id = wayId--;
                                way.Nd = new List<osmWayND> ();
                                way.Timestamp = DateTime.UtcNow.ToString("o", System.Globalization.CultureInfo.InvariantCulture);
                                way.Version = 1;
                                way.User = user;
                                way.Uid = uid;
                                way.Changeset = changeset;

                                long firstNodeId = 0;

                                for (int i = 0; i < polyline.Vertices.Count; i++)
                                {
                                    Point3<double> point = polyline.Vertices[i];

                                    OsmUtils.OsmSchema.osmNode node = new osmNode ();
                                    node.Id = nodeId--;
                                    node.Lat = point.Y + corrY;
                                    node.Lon = point.X + corrX;
                                    node.Timestamp = DateTime.UtcNow.ToString("o", System.Globalization.CultureInfo.InvariantCulture);
                                    node.Version = 1;
                                    node.User = user;
                                    node.Uid = uid;
                                    node.Changeset = changeset;

                                    // Do explicity set the Lat- / LonSpecified properties.
                                    // Otherwise the lat / lon XML attributes would not get written, if the node has
                                    // a latitude or longitude of exactly 0°.
                                    node.LatSpecified = true;
                                    node.LonSpecified = true;

                                    if (i == 0)
                                        firstNodeId = node.Id;
                                    
                                    nodeSerializer.Serialize (writer, node, ns);

                                    way.Nd.Add (new osmWayND (node.Id, true));

                                    // Split the way if the maximum node count per way is reached.
                                    if (way.Nd.Count == maxWayNodes && polyline.VerticesCount > maxWayNodes)
                                    {
                                        // Don't create a new way if already at the end of the *unclosed* polyline
                                        if (i == polyline.VerticesCount - 1 && !polyline.IsClosed)
                                            continue;

                                        // first, serialize old way
                                        waySerializer.Serialize(writer, way, ns);

                                        way = new osmWay();
                                        way.Id = wayId--;
                                        way.Nd = new List<osmWayND>();
                                        way.Timestamp = DateTime.UtcNow.ToString("o", System.Globalization.CultureInfo.InvariantCulture);
                                        way.Version = 1;
                                        way.User = user;
                                        way.Uid = uid;
                                        way.Changeset = changeset;

                                        contourMarker.MarkContour(way, isohypse);
                                        way.Nd.Add(new osmWayND(node.Id, true));
                                    }
                                }

                                // if the isohypse segment is closed, add the first node as the final node of the way
                                if (polyline.IsClosed)
                                    way.Nd.Add (new osmWayND (firstNodeId, true));
                                
                                waySerializer.Serialize (writer, way, ns);
                            }
                        });

                        writer.WriteEndElement ();
                    }
                }
            }
            else
            {
                IsohypseCollection isoCollection = alg.Isoplete (dem, elevationStepInUnits);

                OsmDatabase osmDb = new OsmDatabase ();
                if (osmMergeFile != null)
                {
                    osm osmExistingFile = OsmClient06.LoadFile (osmMergeFile);
                    osmDb.ImportData (osmExistingFile);
                }

                foreach (Isohypse isohypse in isoCollection.Isohypses.Values)
                {
                    foreach (Polyline polyline in isohypse.Segments)
                    {
                        OsmWay isohypseWay = new OsmWay (wayId--);
                        isohypseWay.Visible = true;
                        isohypseWay.Timestamp = DateTime.UtcNow;
                        contourMarker.MarkContour (isohypseWay, isohypse);
                        osmDb.AddWay (isohypseWay);

                        long firstNodeId = nodeId;

                        for (int i = 0; i < polyline.VerticesCount; i++)
                        {
                            Point3<double> point = polyline.Vertices[i];

                            OsmNode node = new OsmNode(nodeId--, point.Y + corrY, point.X + corrX);
                            node.Visible = true;
                            node.Timestamp = DateTime.UtcNow;
                            osmDb.AddNode(node);

                            isohypseWay.AddNode (node.ObjectId);

                            // Split the polyline if the maximum node count per way is reached.
                            if (isohypseWay.NodesList.Count == maxWayNodes && polyline.VerticesCount > maxWayNodes)
                            {
                                // Don't create a new way if already at the end of the *unclosed* polyline
                                if (i == polyline.VerticesCount - 1 && !polyline.IsClosed)
                                    continue;
                                    
                                isohypseWay = new OsmWay (wayId--);
                                isohypseWay.Visible = true;
                                isohypseWay.Timestamp = DateTime.UtcNow;

                                contourMarker.MarkContour (isohypseWay, isohypse);
                                osmDb.AddWay (isohypseWay);
                                isohypseWay.AddNode (node.ObjectId);
                            }
                        }

                        // if the isohypse segment is closed, add the first node as the final node of the way
                        if (polyline.IsClosed)
                            isohypseWay.AddNode (firstNodeId);
                    }
                }

                activityLogger.Log (ActivityLogLevel.Normal, "Saving the contour data to the file...");
                OsmUtils.OsmSchema.osm osmData = osmDb.ExportData (user, uid, changeset);
                OsmUtils.OsmClient.OsmClient06.SaveFile (osmData, outputOsmFile);
            }

            // TODO: SVG file generator code

//            using (FileStream stream = File.Open ("test.svg", FileMode.Create, FileAccess.Write))
//            {
//                using (StreamWriter writer = new StreamWriter (stream))
//                {
//                    int width = 1000;
//                    int height = 800;
//                    double aspectRatio = (maxLat - minLat) / height;
//                    aspectRatio = Math.Max (aspectRatio, (maxLng - minLng) / width);

//                    writer.WriteLine (String.Format (System.Globalization.CultureInfo.InvariantCulture,
//                        @"<?xml version='1.0' encoding='utf-8' standalone='yes'?>
//<!DOCTYPE svg[]>
//<svg viewBox='{0} {1} {2} {3}' width='{2}' height='{3}' id='0'>", 0, 0, width, height));
                   
//                    foreach (Isohypse isohypse in isoCollection.Isohypses.Values)
//                    {
//                        foreach (Polyline polyline in isohypse.Segments)
//                        {
//                            StringBuilder pathString = new StringBuilder ();
//                            for (int i = 0; i < polyline.VerticesCount; i++)
//                            {
//                                Point3 point = polyline.Vertices[i];

//                                if (i == 0)
//                                {
//                                    pathString.AppendFormat ("M ");
//                                }
//                                else if (i == 1)
//                                {
//                                    pathString.AppendFormat ("C ");
//                                }

//                                pathString.AppendFormat (System.Globalization.CultureInfo.InvariantCulture, "{0},{1} ",
//                                    (point.X - minLng) / aspectRatio, (maxLat - point.Y) / aspectRatio);

//                                if (i > 0)
//                                    pathString.AppendFormat (System.Globalization.CultureInfo.InvariantCulture, "{0},{1} ",
//                                        (point.X - minLng) / aspectRatio, (maxLat - point.Y) / aspectRatio);
//                            }

//                            writer.WriteLine (@"<path d='{0}' fill='none' stroke='black' stroke-width='0.25px'/>", pathString.ToString ());
//                        }
//                    }

//                    writer.WriteLine (@"</svg>");
//                }
//            }
        }

        public int ParseArgs (string[] args, int startFrom)
        {
            if (args == null)
                throw new ArgumentNullException ("args");

            SupportedOptions options = new SupportedOptions ();
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.Bounds1, "bounds1", 4));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.Bounds2, "bounds2", 3));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.Bounds3, "bounds3", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.OutputFile, "o", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.MergeFile, "merge", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.SrtmCachePath, "d", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.RegenerateIndexFile, "i"));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.ElevationStep, "step", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.Categories, "cat", 2));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.Feet, "feet", 0));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.LargeAreaMode, "large", 0));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.CorrectionXY, "corrxy", 2));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.SrtmSource, "source", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.MaxWayNodes, "maxwaynodes", 1));

            startFrom = options.ParseArgs (args, startFrom);
            System.Globalization.CultureInfo invariantCulture = System.Globalization.CultureInfo.InvariantCulture;

            foreach (ConsoleApplicationOption option in options.UsedOptions)
            {
                switch ((Srtm2OsmCommandOption)option.OptionId)
                {
                    case Srtm2OsmCommandOption.CorrectionXY:
                        {
                            corrX = Double.Parse (option.Parameters[0], invariantCulture);
                            corrY = Double.Parse (option.Parameters[1], invariantCulture);
                            continue;
                        }

                    case Srtm2OsmCommandOption.SrtmSource:
                        {
                            Uri uri;

                            try
                            {
                                uri = new Uri (option.Parameters[0]);
                            }
                            catch (UriFormatException)
                            {
                                throw new ArgumentException ("The source URL is not valid.");
                            }

                            // Check if the prefix is supported. Unfortunately I couldn't find a method to check which
                            // prefixes are registered without calling WebRequest.Create(), which I didn't want here.
                            if (uri.Scheme != "http" && uri.Scheme != "https" && uri.Scheme != "ftp")
                            {
                                string error = String.Format(invariantCulture, "The source's scheme ('{0}') is not supported.", uri.Scheme);
                                throw new ArgumentException (error);
                            }

                            srtmSource = uri.AbsoluteUri;

                            continue;
                        }

                    case Srtm2OsmCommandOption.Bounds1:
                        {
                            double minLat = Double.Parse (option.Parameters[0], invariantCulture);
                            double minLng = Double.Parse (option.Parameters[1], invariantCulture);
                            double maxLat = Double.Parse (option.Parameters[2], invariantCulture);
                            double maxLng = Double.Parse (option.Parameters[3], invariantCulture);

                            if (minLat == maxLat)
                                throw new ArgumentException ("Minimum and maximum latitude should not have the same value.");

                            if (minLng == maxLng)
                                throw new ArgumentException ("Minimum and maximum longitude should not have the same value.");

                            if (minLat > maxLat)
                            {
                                double sw = minLat;
                                minLat = maxLat;
                                maxLat = sw;
                            }

                            if (minLng > maxLng)
                            {
                                double sw = minLng;
                                minLng = maxLng;
                                maxLng = sw;
                            }

                            if (minLat <= -90 || maxLat > 90)
                                throw new ArgumentException ("Latitude is out of range.");

                            if (minLng <= -180 || maxLng > 180)
                                throw new ArgumentException ("Longitude is out of range.");

                            bounds = new Bounds2 (minLng, minLat, maxLng, maxLat);
                            continue;
                        }

                    case Srtm2OsmCommandOption.Bounds2:
                        {
                            double lat = Double.Parse (option.Parameters[0], invariantCulture);
                            double lng = Double.Parse (option.Parameters[1], invariantCulture);
                            double boxSizeInKilometers = Double.Parse (option.Parameters[2], invariantCulture);

                            bounds = CalculateBounds (lat, lng, boxSizeInKilometers);
                            continue;
                        }

                    case Srtm2OsmCommandOption.Bounds3:
                        {
                            Uri slippyMapUrl = new Uri (option.Parameters[0]);
                            double lat = 0;
                            double lng = 0;
                            int zoomLevel = 0;

                            if (slippyMapUrl.Fragment != String.Empty)
                            {
                                // map=18/50.07499/10.21574
                                string pattern = @"map=(\d+)/([-\.\d]+)/([-\.\d]+)";
                                Match match = Regex.Match(slippyMapUrl.Fragment, pattern);

                                if (match.Success)
                                {
                                    try
                                    {
                                        zoomLevel = Int32.Parse(match.Groups[1].Value, invariantCulture); 
                                        lat = Double.Parse(match.Groups[2].Value, invariantCulture);
                                        lng = Double.Parse(match.Groups[3].Value, invariantCulture);
                                    }
                                    catch (FormatException fex)
                                    {
                                        throw new ArgumentException("Invalid slippymap URL.", fex);
                                    }

                                    bounds = CalculateBounds(lat, lng, zoomLevel);
                                }
                                else
                                    throw new ArgumentException("Invalid slippymap URL.");
                            }
                            else if (slippyMapUrl.Query != String.Empty)
                            {
                                string queryPart = slippyMapUrl.Query;
                                NameValueCollection queryParameters = HttpUtility.ParseQueryString(queryPart);

                                if (queryParameters["lat"] != null
                                    && queryParameters["lon"] != null
                                    && queryParameters["zoom"] != null)
                                {
                                    try
                                    {
                                        lat = Double.Parse(queryParameters["lat"], invariantCulture);
                                        lng = Double.Parse(queryParameters["lon"], invariantCulture);
                                        zoomLevel = Int32.Parse(queryParameters["zoom"], invariantCulture);
                                    }
                                    catch (FormatException fex)
                                    {
                                        throw new ArgumentException("Invalid slippymap URL.", fex);
                                    }

                                    bounds = CalculateBounds(lat, lng, zoomLevel);
                                }
                                else if (queryParameters["bbox"] != null)
                                    bounds = CalculateBounds(queryParameters["bbox"]);
                                else
                                    throw new ArgumentException("Invalid slippymap URL.");
                            }
                            else
                                throw new ArgumentException("Invalid slippymap URL.");

                            continue;
                        }

                    case Srtm2OsmCommandOption.OutputFile:
                        outputOsmFile = option.Parameters [0];
                        continue;

                    case Srtm2OsmCommandOption.MergeFile:
                        osmMergeFile = option.Parameters [0];
                        continue;

                    case Srtm2OsmCommandOption.SrtmCachePath:
                        srtmDir = option.Parameters[0];
                        continue;

                    case Srtm2OsmCommandOption.RegenerateIndexFile:
                        generateIndex = true;
                        continue;

                    case Srtm2OsmCommandOption.ElevationStep:
                        elevationStep = int.Parse (option.Parameters [0], invariantCulture);

                        if (elevationStep <= 0)
                            throw new ArgumentException ("Elevation step should be a positive integer value.");

                        continue;

                    case Srtm2OsmCommandOption.Categories:
                        majorFactor = double.Parse (option.Parameters[0], invariantCulture);
                        mediumFactor = double.Parse (option.Parameters[1], invariantCulture);

                        contourMarker = new MkgmapContourMarker (majorFactor, mediumFactor);

                        continue;

                    case Srtm2OsmCommandOption.Feet:
                        elevationUnits = 0.30480061;
                        continue;

                    case Srtm2OsmCommandOption.LargeAreaMode:
                        largeAreaMode = true;
                        continue;

                    case Srtm2OsmCommandOption.MaxWayNodes:
                        maxWayNodes = Int32.Parse (option.Parameters[0], invariantCulture);

                        if (maxWayNodes < 2)
                            throw new ArgumentException ("The minimum number of nodes in a single way is 2.");

                        continue;
                }
            }

            // Check if bounds were specified
            if (bounds == null)
                throw new ArgumentException ("No bounds specified.");

            return startFrom;
        }

        #endregion

        static private Bounds2 CalculateBounds (double lat, double lng, int zoomLevel)
        {
            if (zoomLevel < 2 || zoomLevel >= zoomLevels.Length)
                throw new ArgumentException("Zoom level is out of range.");

            // 30 is the width of the screen in centimeters
            double boxSizeInKilometers = zoomLevels[zoomLevel] * 30.0 / 100 / 1000;

            return CalculateBounds(lat, lng, boxSizeInKilometers);
        }

        static private Bounds2 CalculateBounds (double lat, double lng, double boxSizeInKilometers)
        {
            if (boxSizeInKilometers <= 0)
                throw new ArgumentException("Box size must be a positive number.");

            double minLat, maxLat, minLng, maxLng;

            // calculate deltas for the given kilometers
            double earthRadius = 6360000;
            double earthCircumference = earthRadius * 2 * Math.PI;
            double latDelta = boxSizeInKilometers / 2 * 1000 / earthCircumference * 360;
            double lngDelta = latDelta / Math.Cos (lat * Math.PI / 180.0);

            minLng = lng - lngDelta / 2;
            minLat = lat - latDelta / 2;
            maxLng = lng + lngDelta / 2;
            maxLat = lat + latDelta / 2;

            if (minLat <= -90 || maxLat > 90)
                throw new ArgumentException ("Latitude is out of range.");

            if (minLng <= -180 || maxLng > 180)
                throw new ArgumentException ("Longitude is out of range.");

            return new Bounds2 (minLng, minLat, maxLng, maxLat);
        }

        static private Bounds2 CalculateBounds (string bbox)
        {
            if (String.IsNullOrEmpty(bbox))
                throw new ArgumentException ("String is NULL or empty.", "bbox");

            string[] parts = bbox.Split (new char[] { ',' });

            if (parts.Length != 4)
                throw new ArgumentException ("Bounding box has not exactly four parts.", "bbox");

            double minLat, maxLat, minLng, maxLng;

            try
            {
                minLng = Double.Parse (parts[0], System.Globalization.CultureInfo.InvariantCulture);
                minLat = Double.Parse (parts[1], System.Globalization.CultureInfo.InvariantCulture);
                maxLng = Double.Parse (parts[2], System.Globalization.CultureInfo.InvariantCulture);
                maxLat = Double.Parse (parts[3], System.Globalization.CultureInfo.InvariantCulture);
            }
            catch (FormatException fex)
            {
                throw new ArgumentException ("Bounding box was not parseable.", fex);
            }

            if (minLat <= -90 || maxLat > 90)
                throw new ArgumentException ("Latitude is out of range.");

            if (minLng <= -180 || maxLng > 180)
                throw new ArgumentException ("Longitude is out of range.");

            return new Bounds2 (minLng, minLat, maxLng, maxLat);
        }

        private Bounds2 bounds;
        private double corrX, corrY;
        private bool generateIndex;
        private string srtmDir = "srtm";
        private int elevationStep = 20;
        private string outputOsmFile = "srtm.osm";
        private string osmMergeFile;
        private double elevationUnits = 1;
        private double majorFactor, mediumFactor;
        private IContourMarker contourMarker = new DefaultContourMarker ();
        private bool largeAreaMode;
        private string srtmSource = "";
        private int maxWayNodes = Int32.MaxValue;

        private static int[] zoomLevels = {0, 0, 111000000, 55000000, 28000000, 14000000, 7000000, 3000000, 2000000, 867000,
            433000, 217000, 108000, 54000, 27000, 14000, 6771, 3385, 1693};
    }
}
