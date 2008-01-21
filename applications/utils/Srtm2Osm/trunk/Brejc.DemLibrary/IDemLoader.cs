using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    public interface IDemLoader
    {
        IDigitalElevationModel LoadDemForArea (Bounds2 bounds);
    }
}
