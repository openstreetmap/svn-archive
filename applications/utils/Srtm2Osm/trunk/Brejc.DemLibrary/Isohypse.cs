using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    [Serializable]
    public class Isohypse
    {
        public IList<Polyline> Segments
        {
            get { return segments; }
        }

        public double Elevation
        {
            get { return elevation; }
            set { elevation = value; }
        }

        public Isohypse (double elevation)
        {
            this.elevation = elevation;
        }

        public void AddSegment (Polyline segment)
        {
            segments.Add (segment);
        }

        private double elevation;
        private List<Polyline> segments = new List<Polyline> ();
    }
}
