using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    public class OsmNode : OsmObjectBase
    {
        public double Latitude
        {
            get { return latitude; }
        }

        public double Longitude
        {
            get { return longitude; }
        }

        public OsmNode (int nodeId, double latitude, double longitude)
            : base (nodeId)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        private double latitude;
        private double longitude;
    }
}
