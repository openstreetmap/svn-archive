using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Common.Console;

namespace Srtm2Osm
{
    public class ConsoleApp : Brejc.Common.Console.ConsoleApplicationBase
    {
        public ConsoleApp (string[] args) : base (args) {}

        public override IList<IConsoleApplicationCommand> ParseArguments ()
        {
            if (Args.Length == 0)
                return null;

            List<IConsoleApplicationCommand> cmdList = new List<IConsoleApplicationCommand> ();
            IConsoleApplicationCommand cmd = null;

            cmd = new Srtm2OsmCommand ();

            cmd.ParseArgs (Args, 0);
            cmdList.Add (cmd);

            return cmdList;
        }

        public override void ShowBanner ()
        {
            System.Diagnostics.FileVersionInfo version = System.Diagnostics.FileVersionInfo.GetVersionInfo
                (System.Reflection.Assembly.GetExecutingAssembly ().Location);
            System.Console.Out.WriteLine ("Srtm2Osm v{0} by Igor Brejc and others", version.FileVersion);
            System.Console.Out.WriteLine ();
            System.Console.Out.WriteLine ("Uses SRTM data to generate elevation contour lines for use in OpenStreetMap");
            System.Console.Out.WriteLine ();
        }

        public override void ShowHelp ()
        {
            System.Console.Out.WriteLine ();
            System.Console.Out.WriteLine ("USAGE:");
            System.Console.Out.WriteLine ("Srtm2Osm <bounds> <options>");
            System.Console.Out.WriteLine ();
            System.Console.Out.WriteLine ("BOUNDS (choose one):");
            System.Console.Out.WriteLine ("-bounds1 <minLat> <minLng> <maxLat> <maxLng>: specifies the area to cover");
            System.Console.Out.WriteLine ("-bounds2 <lat> <lng> <boxsize (km)>: specifies the area to cover");
            System.Console.Out.WriteLine ("-bounds3 <slippymap link>: specifies the area to cover using the URL link from slippymap");
            System.Console.Out.WriteLine ("OPTIONS:");
            System.Console.Out.WriteLine ("-o <path>: specifies an output OSM file (default: 'srtm.osm')");
            System.Console.Out.WriteLine ("-merge <path>: specifies an OSM file to merge with the output");
            System.Console.Out.WriteLine ("-d <path>: specifies a SRTM cache directory (default: 'Srtm')");
            System.Console.Out.WriteLine ("-i: forces the regeneration of SRTM index file (default: no)");
            System.Console.Out.WriteLine ("-feet: uses feet units for elevation instead of meters");
            System.Console.Out.WriteLine ("-step <elevation>: elevation step between contours (default: 20 units)");
            System.Console.Out.WriteLine ("-cat <major> <medium>: adds contour category tag to OSM ways");
            System.Console.Out.WriteLine ("       example: -cat 400 100 will mark:");
            System.Console.Out.WriteLine ("           contours 400, 800, 1200 etc as contour_ext=elevation_major");
            System.Console.Out.WriteLine ("           contours 100, 200, 300, 500 etc as contour_ext=elevation_medium");
            System.Console.Out.WriteLine ("           and all others as contour_ext=elevation_minor");
            System.Console.Out.WriteLine ("-large: runs in 'large area mode' in which each contour is written to OSM file ");
            System.Console.Out.WriteLine ("       immediately upon discovery. This prevents 'out-of-memory' errors.");
            System.Console.Out.WriteLine ("-corrxy <corrLng> <corrLat>: correction values to shift contours");
            System.Console.Out.WriteLine ("-source <url>: base URL used for download");
            System.Console.Out.WriteLine ("       (default 'http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/')");
            System.Console.Out.WriteLine ("-maxwaynodes <count>: specifies the maximum number of nodes in a single way");
            System.Console.Out.WriteLine ("-firstnodeid <id>: specifies the first ID of a node (default: 2^63-1)");
            System.Console.Out.WriteLine ("-firstwayid <id>: specifies the first ID of a way (default: 2^63-1)");
            System.Console.Out.WriteLine ("-incrementid: runs in 'ID incrementation mode' in which the OSM element ID is");
            System.Console.Out.WriteLine ("       incremented instead of decremented. Needs the firstnodeid and firstwayid");
            System.Console.Out.WriteLine ("       parameters set.");
            System.Console.Out.WriteLine ();
        }
    }
}
