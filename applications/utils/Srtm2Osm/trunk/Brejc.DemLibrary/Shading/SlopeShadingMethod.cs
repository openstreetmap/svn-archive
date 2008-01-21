using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.DemLibrary.Shading
{
    public class SlopeShadingMethod : IShadingMethod
    {
        public void Initialize (ShadingParameters parameters)
        {
            this.shadingParameters = parameters;
        }

        public Color CalculateColor (double aspect, double slope)
        {
            if (slope < 0)
                slope = 0;

            int alpha = 255 - (int)(255 * slope / (Math.PI / 2));

            return Color.FromArgb (alpha, 0, 0, 0);
        }

        [SuppressMessage ("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        private ShadingParameters shadingParameters;
    }
}
