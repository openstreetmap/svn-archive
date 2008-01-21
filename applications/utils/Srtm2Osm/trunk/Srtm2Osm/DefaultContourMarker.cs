using System;
using System.Collections.Generic;
using System.Text;
using OsmUtils.Framework;
using Brejc.DemLibrary;
using OsmUtils.OsmSchema;

namespace Srtm2Osm
{
    public class DefaultContourMarker : IContourMarker
    {
        public void Configure (double elevationUnits)
        {
            if (elevationUnits <= 0)
                throw new ArgumentOutOfRangeException ("elevationUnits", elevationUnits, "Elevation units must be a positive number");

            this.elevationUnits = elevationUnits;
        }

        public void MarkContour (OsmWay isohypseWay, Isohypse isohypse)
        {
            if (isohypseWay == null)
                throw new ArgumentNullException ("isohypseWay");

            if (isohypse == null)
                throw new ArgumentNullException ("isohypse");
            
            double elevation = Math.Round (isohypse.Elevation / elevationUnits);

            isohypseWay.SetTag ("ele", elevation.ToString (System.Globalization.CultureInfo.InvariantCulture));
            isohypseWay.SetTag ("contour", "elevation");
        }

        public void MarkContour (osmWay isohypseWay, Isohypse isohypse)
        {
            if (isohypseWay == null)
                throw new ArgumentNullException ("isohypseWay");

            if (isohypse == null)
                throw new ArgumentNullException ("isohypse");

            double elevation = Math.Round (isohypse.Elevation / elevationUnits);

            if (isohypseWay.Tag == null)
                isohypseWay.Tag = new List<tag> ();

            isohypseWay.Tag.Add (new tag ("ele", elevation.ToString (System.Globalization.CultureInfo.InvariantCulture)));
            isohypseWay.Tag.Add (new tag ("contour", "elevation"));
        }

        private double elevationUnits;
    }
}
