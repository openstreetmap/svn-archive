using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary.Shading
{
    public class ShadingParameters
    {
        /// <summary>
        /// Gets or sets the altitude of the sun in degrees above the horizon (a value between 0 and 90 degrees).
        /// </summary>
        /// <value>The sun altitude.</value>
        public double SunAltitude
        {
            get { return sunAltitude; }
            set { sunAltitude = value; }
        }

        /// <summary>
        /// Gets or sets the azimuth of the sun in degrees to the east of north (a value between -1 and 360 degrees).
        /// </summary>
        /// <value>The sun azimuth.</value>
        public double SunAzimuth
        {
            get { return sunAzimuth; }
            set { sunAzimuth = value; }
        }

        public ShadingParameters ()
        {
            sunAltitude = Geometry.GeometryUtils.Deg2Rad (25);
            sunAzimuth = Geometry.GeometryUtils.Deg2Rad (-45);
        }

        private double sunAltitude;
        private double sunAzimuth;
    }
}
