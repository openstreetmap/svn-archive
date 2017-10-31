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
using System.Globalization;
using System.Net;

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
        MaxWayNodes,
        FirstNodeId,
        FirstWayId,
        IncrementId,
        SplitBounds
    }

    public class Srtm2OsmCommand : IConsoleApplicationCommand
    {
        #region IConsoleApplicationCommand Members

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes")]
        public void Execute ()
        {
            ConsoleActivityLogger activityLogger = new ConsoleActivityLogger();
            activityLogger.LogLevel = ActivityLogLevel.Verbose;

            // Use all available encryption protocols supported in the .NET Framework 4.0.
            // TLS versions > 1.0 are supported and available via the extensions.
            // see https://blogs.perficient.com/microsoft/2016/04/tsl-1-2-and-net-support/
            // This is a global setting for all HTTP requests.
            ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls | SecurityProtocolTypeExtensions.Tls11 | SecurityProtocolTypeExtensions.Tls12 | SecurityProtocolType.Ssl3;

            // first make sure that the SRTM directory exists
            if (!Directory.Exists (srtmDir))
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

            IIsopletingAlgorithm alg = new Igor4IsopletingAlgorithm ();
            alg.ActivityLogger = activityLogger;

            double elevationStepInUnits = elevationStep * elevationUnits;
            contourMarker.Configure (elevationUnits);

            // Default: Start with highest possible ID and count down. That should give maximum space
            // between contour data and real OSM data.
            IdCounter nodeCounter = new IdCounter (incrementId, firstNodeId);
            IdCounter wayCounter = new IdCounter (incrementId, firstWayId);

            OutputSettings settings = new OutputSettings ();
            settings.ContourMarker = contourMarker;
            settings.LongitudeCorrection = corrX;
            settings.LatitudeCorrection = corrY;
            settings.MaxWayNodes = maxWayNodes;

            // The following IDs and name do exist in the OSM database.
            settings.UserName = "Srtm2Osm";
            settings.UserId = 941874;
            settings.ChangesetId = 13341398;

            OutputBase output = null;

            if (largeAreaMode)
                output = new DirectOutput (new FileInfo (outputOsmFile), settings);
            else
                output = new DatabaseOutput (new FileInfo (outputOsmFile), settings);

            output.Begin ();

            if (osmMergeFile != null)
            {
                activityLogger.LogFormat (ActivityLogLevel.Normal, "Importing dataset from {0}", osmMergeFile);
                output.Merge (osmMergeFile);
            }

            if (this.splitWidth != 0 && this.splitHeight != 0)
            {
                List<Bounds2> newBounds = new List<Bounds2> ();

                foreach (Bounds2 bound in this.bounds)
                    newBounds.AddRange (BoundsSplitter.Split (bound, this.splitWidth, this.splitHeight));

                this.bounds = newBounds;

                activityLogger.LogFormat (ActivityLogLevel.Normal, "Will process {0} seperate bounds.", bounds.Count);
            }

            foreach (Bounds2 bound in this.bounds)
            {
                Bounds2 corrBounds = new Bounds2 (bound.MinX - corrX, bound.MinY - corrY,
                    bound.MaxX - corrX, bound.MaxY - corrY);

                activityLogger.LogFormat (ActivityLogLevel.Normal, "Calculating contour data for bound {0}...", corrBounds);

                IRasterDigitalElevationModel dem = (IRasterDigitalElevationModel) storage.LoadDemForArea (corrBounds);

                // clear up some memory used in storage object
                if (this.bounds.Count == 1)
                {
                    storage = null;
                    GC.Collect ();
                }

                DigitalElevationModelStatistics statistics = dem.CalculateStatistics ();

                activityLogger.Log (ActivityLogLevel.Normal, String.Format (CultureInfo.InvariantCulture,
                    "DEM data points count: {0}", dem.DataPointsCount));
                activityLogger.Log (ActivityLogLevel.Normal, String.Format (CultureInfo.InvariantCulture,
                    "DEM minimum elevation: {0}", statistics.MinElevation));
                activityLogger.Log (ActivityLogLevel.Normal, String.Format (CultureInfo.InvariantCulture,
                    "DEM maximum elevation: {0}", statistics.MaxElevation));
                activityLogger.Log (ActivityLogLevel.Normal, String.Format (CultureInfo.InvariantCulture,
                    "DEM has missing points: {0}", statistics.HasMissingPoints));

                try
                {
                    alg.Isoplete (dem, elevationStepInUnits, delegate(Isohypse isohypse)
                    {
                        output.ProcessIsohypse (isohypse, delegate() { return GetNextId (nodeCounter, true); },
                            delegate() { return GetNextId (wayCounter, false); });
                    });
                }
                catch (OutOfMemoryException)
                {
                    string msg = "Not enough memory. ";
                    if (this.splitWidth == 0 && this.splitHeight == 0)
                        msg += "Try to decrease the bounding box or use the splitbounds parameter.";
                    else
                        msg += "Try to decrease the splitbounds value.";

                    activityLogger.Log (ActivityLogLevel.Error, msg);
                    break;
                }
            }

            if (!largeAreaMode)
                activityLogger.Log (ActivityLogLevel.Normal, "Saving contour data to file...");

            output.End ();

            activityLogger.Log (ActivityLogLevel.Normal, "Done.");

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

        public int ParseArgs(string[] args, int startFrom)
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
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.FirstNodeId, "firstnodeid", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.FirstWayId, "firstwayid", 1));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.IncrementId, "incrementid", 0));
            options.AddOption (new ConsoleApplicationOption ((int)Srtm2OsmCommandOption.SplitBounds, "splitbounds", 2));

            startFrom = options.ParseArgs (args, startFrom);
            CultureInfo invariantCulture = CultureInfo.InvariantCulture;

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

                            this.bounds.Add (new Bounds2 (minLng, minLat, maxLng, maxLat));
                            continue;
                        }

                    case Srtm2OsmCommandOption.Bounds2:
                        {
                            double lat = Double.Parse (option.Parameters[0], invariantCulture);
                            double lng = Double.Parse (option.Parameters[1], invariantCulture);
                            double boxSizeInKilometers = Double.Parse (option.Parameters[2], invariantCulture);

                            this.bounds.Add (CalculateBounds (lat, lng, boxSizeInKilometers));
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

                                    this.bounds.Add (CalculateBounds (lat, lng, zoomLevel));
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

                                    this.bounds.Add (CalculateBounds (lat, lng, zoomLevel));
                                }
                                else if (queryParameters["bbox"] != null)
                                    this.bounds.Add (CalculateBounds (queryParameters["bbox"]));
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

                    case Srtm2OsmCommandOption.FirstNodeId:
                        firstNodeId = long.Parse(option.Parameters[0], invariantCulture);

                        if (firstNodeId <= 0)
                            throw new ArgumentException ("A negative or zero node ID is not supported.");

                        continue;

                    case Srtm2OsmCommandOption.FirstWayId:
                        firstWayId = long.Parse(option.Parameters[0], invariantCulture);

                        if (firstWayId <= 0)
                            throw new ArgumentException ("A negative or zero way ID is not supported.");

                        continue;

                    case Srtm2OsmCommandOption.IncrementId:
                        incrementId = true;
                        continue;

                    case Srtm2OsmCommandOption.SplitBounds:
                        splitHeight = Double.Parse (option.Parameters[0], invariantCulture);
                        splitWidth = Double.Parse (option.Parameters[1], invariantCulture);

                        if (splitWidth <= 0 || splitHeight <= 0)
                            throw new ArgumentException ("The split width or height may not be smaller than zero.");

                        continue;
                }
            }

            // Check if bounds were specified
            if (bounds.Count == 0 && osmMergeFile != null)
                this.bounds = RetrieveBoundsFromFile (osmMergeFile);

            if (bounds.Count == 0)
                throw new ArgumentException ("No bounds specified.");

            // Check if both first*id's are set when the user wants to increment the IDs
            if (incrementId && (firstNodeId == long.MaxValue || firstWayId == long.MaxValue))
                throw new ArgumentException ("firstnodeid and firstwayid must be set when ID incrementation mode is active.");

            return startFrom;
        }

        #endregion

        static private List<Bounds2> RetrieveBoundsFromFile(string file)
        {
            if (!File.Exists (file))
                throw new FileNotFoundException ("File not found.", file);

            List<Bounds2> result = new List<Bounds2> ();

            XmlReader reader = XmlReader.Create (file);
            while(reader.ReadToFollowing ("bounds"))
            {
                string minlat = reader.GetAttribute ("minlat");
                string minlon = reader.GetAttribute ("minlon");
                string maxlat = reader.GetAttribute ("maxlat");
                string maxlon = reader.GetAttribute ("maxlon");

                Bounds2 bound = new Bounds2 ();

                try
                {
                    bound.MinX = double.Parse (minlon, CultureInfo.InvariantCulture);
                    bound.MinY = double.Parse (minlat, CultureInfo.InvariantCulture);
                    bound.MaxX = double.Parse (maxlon, CultureInfo.InvariantCulture);
                    bound.MaxY = double.Parse (maxlat, CultureInfo.InvariantCulture);
                }
                catch (FormatException fex)
                {
                    throw new ArgumentException ("Bounding box XML element was not parseable.", fex);
                }

                result.Add (bound);
            }

            reader.Close ();

            return result;
        }

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
                minLng = Double.Parse (parts[0], CultureInfo.InvariantCulture);
                minLat = Double.Parse (parts[1], CultureInfo.InvariantCulture);
                maxLng = Double.Parse (parts[2], CultureInfo.InvariantCulture);
                maxLat = Double.Parse (parts[3], CultureInfo.InvariantCulture);
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

        private long GetNextId (IdCounter counter, bool isNodeCounter)
        {
            bool valid = false;
            long result = counter.GetNextId (out valid);

            if (!valid)
            {
                string msg = String.Format (CultureInfo.InvariantCulture,
                    "Ran out of available ID numbers. {0}crement first{1}id parameter.",
                    incrementId ? "De" : "In", isNodeCounter ? "node" : "way");
                throw new ArgumentException (msg);
            }

            return result;
        }

        private List<Bounds2> bounds = new List<Bounds2>();
        private double corrX, corrY;
        private bool generateIndex;
        private string srtmDir = "srtm";
        private int elevationStep = 20;
        private string outputOsmFile = "srtm.osm";
        private string osmMergeFile;
        private double elevationUnits = 1;
        private double majorFactor, mediumFactor;
        private IContourMarker contourMarker = new DefaultContourMarker();
        private bool largeAreaMode;
        private string srtmSource = "";
        private int maxWayNodes = Int32.MaxValue;
        private long firstNodeId = long.MaxValue;
        private long firstWayId = long.MaxValue;
        private bool incrementId = false;
        private double splitWidth, splitHeight;

        private static int[] zoomLevels = {0, 0, 111000000, 55000000, 28000000, 14000000, 7000000, 3000000, 2000000, 867000,
            433000, 217000, 108000, 54000, 27000, 14000, 6771, 3385, 1693};
    }
}
