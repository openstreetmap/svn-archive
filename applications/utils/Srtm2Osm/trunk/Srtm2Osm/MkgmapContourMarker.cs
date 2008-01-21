using System;
using System.Collections.Generic;
using System.Text;
using OsmUtils.Framework;
using Brejc.DemLibrary;
using OsmUtils.OsmSchema;

namespace Srtm2Osm
{
    public class MkgmapContourMarker : IContourMarker
    {
        public double MediumFactor
        {
            get { return mediumFactor; }
        }

        public double MajorFactor
        {
            get { return majorFactor; }
        }

        public MkgmapContourMarker (double majorFactor, double mediumFactor)
        {
            if (majorFactor <= 0 || mediumFactor <= 0)
                throw new ArgumentException ("Major and medium factors should be positive values.");

            this.majorFactor = majorFactor;
            this.mediumFactor = mediumFactor;
        }

        public void Configure (double elevationUnits)
        {
            if (elevationUnits <= 0)
                throw new ArgumentOutOfRangeException ("elevationUnits", elevationUnits, "Elevation units must be a positive number");

            firstMarker.Configure (elevationUnits);
            this.elevationUnits = elevationUnits;
        }

        public void MarkContour (OsmWay isohypseWay, Isohypse isohypse)
        {
            if (isohypseWay == null)
                throw new ArgumentNullException ("isohypseWay");                

            if (isohypse == null)
                throw new ArgumentNullException ("isohypse");                
            
            firstMarker.MarkContour (isohypseWay, isohypse);

            double elevation = Math.Round (isohypse.Elevation / elevationUnits);

            string contourValue = null;
            if (elevation % majorFactor == 0)
                contourValue = "elevation_major";
            else if (elevation % mediumFactor == 0)
                contourValue = "elevation_medium";
            else
                contourValue = "elevation_minor";

            isohypseWay.SetTag ("contour_ext", contourValue);
        }

        public void MarkContour (OsmUtils.OsmSchema.osmWay isohypseWay, Isohypse isohypse)
        {
            if (isohypseWay == null)
                throw new ArgumentNullException ("isohypseWay");

            if (isohypse == null)
                throw new ArgumentNullException ("isohypse");

            firstMarker.MarkContour (isohypseWay, isohypse);

            double elevation = Math.Round (isohypse.Elevation / elevationUnits);

            string contourValue = null;
            if (elevation % majorFactor == 0)
                contourValue = "elevation_major";
            else if (elevation % mediumFactor == 0)
                contourValue = "elevation_medium";
            else
                contourValue = "elevation_minor";

            if (isohypseWay.Tag == null)
                isohypseWay.Tag = new List<tag> ();

            isohypseWay.Tag.Add (new tag ("contour_ext", contourValue));
        }

        private IContourMarker firstMarker = new DefaultContourMarker ();
        private double elevationUnits;
        private double majorFactor = 400, mediumFactor = 100;
    }
}
