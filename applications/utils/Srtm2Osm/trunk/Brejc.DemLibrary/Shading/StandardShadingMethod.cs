using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Brejc.DemLibrary.Shading
{
    public class StandardShadingMethod : IShadingMethod
    {
        #region IShadingMethod Members

        public void Initialize (ShadingParameters parameters)
        {
            this.shadingParameters = parameters;
            sinSunAltitude = Math.Sin (shadingParameters.SunAltitude);
            cosSunAltitude = Math.Cos (shadingParameters.SunAltitude);
        }

        public Color CalculateColor (double aspect, double slope)
        {
            double cang = sinSunAltitude * Math.Sin (slope)
                + cosSunAltitude
                * Math.Cos (slope)
                * Math.Cos (shadingParameters.SunAzimuth - aspect);

            if (cang < 0)
                cang = 0;

            cang = cang / Math.PI * 180;

            int alpha = (int)(255 * cang / 65);

            return Color.FromArgb (alpha, 0, 0, 0);
        }

        #endregion

        private ShadingParameters shadingParameters;
        private double sinSunAltitude;
        private double cosSunAltitude;
    }
}
