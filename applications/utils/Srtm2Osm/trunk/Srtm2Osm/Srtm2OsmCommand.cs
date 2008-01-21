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
    }

    public class Srtm2OsmCommand : IConsoleApplicationCommand
    {
        #region IConsoleApplicationCommand Members

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes")]
        public void Execute ()
        {
            // first make sure that the SRTM directory exists
            if (false == Directory.Exists (srtmDir))
                Directory.CreateDirectory (srtmDir);

            string srtmIndexFilename = Path.Combine (srtmDir, "SrtmIndex.dat");
            SrtmIndex srtmIndex = null;

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
                srtmIndex.Generate ();
                srtmIndex.Save (srtmIndexFilename);

                srtmIndex = SrtmIndex.Load (srtmIndexFilename);
            }

            Srtm3Storage storage = new Srtm3Storage (Path.Combine (srtmDir, "SrtmCache"), srtmIndex);
            IRasterDigitalElevationModel dem = (IRasterDigitalElevationModel) storage.LoadDemForArea (bounds);

            ConsoleActivityLogger activityLogger = new ConsoleActivityLogger ();
            activityLogger.LogLevel = ActivityLogLevel.Verbose;

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

            int wayId = 1000000000, nodeId = 1000000000;

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
                        writer.WriteAttributeString ("version", "0.5");
                        writer.WriteAttributeString ("generator", "Srtm2Osm");

                        alg.Isoplete (dem, elevationStepInUnits, delegate (Isohypse isohypse)
                        {
                            foreach (Polyline polyline in isohypse.Segments)
                            {
                                OsmUtils.OsmSchema.osmWay way = new osmWay ();

                                contourMarker.MarkContour (way, isohypse);

                                way.Id = wayId++;
                                way.Nd = new List<osmWayND> ();

                                int firstNodeId = 0;

                                for (int i = 0; i < polyline.Vertices.Count; i++)
                                {
                                    Point3<double> point = polyline.Vertices[i];

                                    OsmUtils.OsmSchema.osmNode node = new osmNode ();
                                    node.Id = nodeId++;
                                    node.Lat = point.Y;
                                    node.Lon = point.X;

                                    if (i == 0)
                                        firstNodeId = node.Id;
                                    
                                    nodeSerializer.Serialize (writer, node, ns);

                                    way.Nd.Add (new osmWayND (node.Id, true));
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
                    osm osmExistingFile = OsmClient05.LoadFile (osmMergeFile);
                    osmDb.ImportData (osmExistingFile);
                }

                foreach (Isohypse isohypse in isoCollection.Isohypses.Values)
                {
                    foreach (Polyline polyline in isohypse.Segments)
                    {
                        OsmWay isohypseWay = new OsmWay (wayId++);
                        contourMarker.MarkContour (isohypseWay, isohypse);
                        osmDb.AddWay (isohypseWay);

                        int firstNodeId = nodeId;

                        for (int i = 0; i < polyline.VerticesCount; i++)
                        {
                            Point3<double> point = polyline.Vertices[i];

                            OsmNode node = new OsmNode (nodeId++, point.Y, point.X);
                            osmDb.AddNode (node);

                            isohypseWay.AddNode (node.ObjectId);
                        }

                        // if the isohypse segment is closed, add the first node as the final node of the way
                        if (polyline.IsClosed)
                            isohypseWay.AddNode (firstNodeId);
                    }
                }

                activityLogger.Log (ActivityLogLevel.Normal, "Saving the contour data to the file...");
                OsmUtils.OsmSchema.osm osmData = osmDb.ExportData ();
                OsmUtils.OsmClient.OsmClient05.SaveFile (osmData, outputOsmFile);
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

            startFrom = options.ParseArgs (args, startFrom);

            foreach (ConsoleApplicationOption option in options.UsedOptions)
            {
                switch ((Srtm2OsmCommandOption)option.OptionId)
                {
                    case Srtm2OsmCommandOption.Bounds1:
                        {
                            double minLat = Double.Parse (option.Parameters[0],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double minLng = Double.Parse (option.Parameters[1],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double maxLat = Double.Parse (option.Parameters[2],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double maxLng = Double.Parse (option.Parameters[3],
                                System.Globalization.CultureInfo.InvariantCulture);

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
                            double lat = Double.Parse (option.Parameters[0],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double lng = Double.Parse (option.Parameters[1],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double boxSizeInKilometers = Double.Parse (option.Parameters[2],
                                System.Globalization.CultureInfo.InvariantCulture);

                            bounds = CalculateBounds (lat, lng, boxSizeInKilometers);
                            continue;
                        }

                    case Srtm2OsmCommandOption.Bounds3:
                        {
                            Uri slippyMapUrl = new Uri (option.Parameters[0]);

                            string queryPart = slippyMapUrl.Query;
                            NameValueCollection queryParameters = HttpUtility.ParseQueryString (queryPart);

                            if (queryParameters["lat"] == null
                                || queryParameters["lon"] == null
                                || queryParameters["zoom"] == null)
                                throw new ArgumentException ("Invalid slippymap URL.");

                            double lat = Double.Parse (queryParameters ["lat"],
                                System.Globalization.CultureInfo.InvariantCulture);
                            double lng = Double.Parse (queryParameters ["lon"],
                                System.Globalization.CultureInfo.InvariantCulture);
                            int zoomLevel = Int32.Parse (queryParameters["zoom"],
                                System.Globalization.CultureInfo.InvariantCulture);

                            if (zoomLevel < 2 || zoomLevel >= zoomLevels.Length)
                                throw new ArgumentException ("Zoom level is out of range.");

                            // 30 is the width of the screen in centimeters
                            double boxSizeInKilometers = zoomLevels[zoomLevel] * 30.0 / 100 / 1000;

                            bounds = CalculateBounds (lat, lng, boxSizeInKilometers);
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
                        elevationStep = int.Parse (option.Parameters [0],
                            System.Globalization.CultureInfo.InvariantCulture);

                        if (elevationStep <= 0)
                            throw new ArgumentException ("Elevation step should be a positive integer value.");

                        continue;

                    case Srtm2OsmCommandOption.Categories:
                        majorFactor = double.Parse (option.Parameters[0],
                            System.Globalization.CultureInfo.InvariantCulture);
                        mediumFactor = double.Parse (option.Parameters[1],
                            System.Globalization.CultureInfo.InvariantCulture);

                        contourMarker = new MkgmapContourMarker (majorFactor, mediumFactor);

                        continue;

                    case Srtm2OsmCommandOption.Feet:
                        elevationUnits = 0.30480061;
                        continue;

                    case Srtm2OsmCommandOption.LargeAreaMode:
                        largeAreaMode = true;
                        continue;
                }
            }

            return startFrom;
        }

        #endregion

        static private Bounds2 CalculateBounds (double lat, double lng, double boxSizeInKilometers)
        {
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

            if (boxSizeInKilometers <= 0)
                throw new ArgumentException ("Box size must be a positive number.");

            return new Bounds2 (minLng, minLat, maxLng, maxLat);
        }

        private Bounds2 bounds;
        private bool generateIndex;
        private string srtmDir = "srtm";
        private int elevationStep = 20;
        private string outputOsmFile = "srtm.osm";
        private string osmMergeFile;
        private double elevationUnits = 1;
        private double majorFactor, mediumFactor;
        private IContourMarker contourMarker = new DefaultContourMarker ();
        private bool largeAreaMode;

        private int[] zoomLevels = {0, 0, 111000000, 55000000, 28000000, 14000000, 7000000, 3000000, 2000000, 867000,
            433000, 217000, 108000, 54000, 27000, 14000, 6771, 3385, 1693};
    }
}

// http://www.openstreetmap.org/?lat=46.79387319944362&lon=13.599213077626766&zoom=11&layers=0BF