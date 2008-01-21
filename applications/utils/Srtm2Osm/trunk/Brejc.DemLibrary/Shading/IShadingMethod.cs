using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;

namespace Brejc.DemLibrary.Shading
{
    public interface IShadingMethod
    {
        void Initialize (ShadingParameters parameters);

        Color CalculateColor (double aspect, double slope);
    }
}
