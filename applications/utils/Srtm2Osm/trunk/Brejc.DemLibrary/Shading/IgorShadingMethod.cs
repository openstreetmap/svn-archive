using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Brejc.DemLibrary.Shading
{
    public class IgorShadingMethod : IShadingMethod
    {
        public int MinAlpha
        {
            get { return minAlpha; }
        }

        public void Initialize (ShadingParameters parameters)
        {
            this.shadingParameters = parameters;
        }

        public Color CalculateColor (double aspect, double slope)
        {
            double aspectDiff = Geometry.GeometryUtils.DifferenceBetweenAngles (aspect,
                Math.PI * 3/2 - shadingParameters.SunAzimuth, Math.PI * 2);

            if (slope < 0)
                slope = 0;

            double slopeDegrees = slope / Math.PI * 180;
            if (slopeDegrees > 90)
                slopeDegrees = 90;

            double slopeStrength = slopeDegrees / 90;
            double aspectStrength = aspectDiff / Math.PI;
            double whiteness = (1 - slopeStrength) * (1 - aspectStrength);

            int alpha = (int)(255 * whiteness);

            if (alpha < minAlpha)
                minAlpha = alpha;

            return Color.FromArgb (alpha, 0, 0, 0);
        }

        private ShadingParameters shadingParameters;
        private int minAlpha = int.MaxValue;
    }
}
