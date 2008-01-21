using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    public interface IDigitalElevationModel : ICloneable
    {
        DigitalElevationModelStatistics CalculateStatistics ();
    }
}
