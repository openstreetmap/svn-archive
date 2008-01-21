using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    public interface IRasterDigitalElevationModel : IDigitalElevationModel
    {
        /// <summary>
        /// Gets the latitudinal resolution of the digital elevation model. 
        /// The latitudinal resolution is defined as the number of data points needed to cover one degree of latitude.
        /// </summary>
        /// <value>The latitudinal resolution.</value>
        int LatResolution { get; }

        /// <summary>
        /// Gets the longitudinal resolution of the digital elevation model. 
        /// The longitudinal resolution is defined as the number of data points needed to cover one degree of longitude.
        /// </summary>
        /// <value>The longitudinal resolution.</value>
        int LonResolution { get; }

        int LatOffset { get; }
        int LonOffset { get;}
        int LatLength { get; }
        int LonLength { get;}

        int DataPointsCount { get;}

        Bounds2 Bounds { get;}

        GeoPosition GetGeoPosition (double lonPos, double latPos);
        GeoPosition GetGeoPosition (double lonPos, double latPos, bool calculateElevation);

        double GetElevationForDataPoint (int localLon, int localLat);
        double CalculateElevationForDataPoint (double localLon, double localLat);
        double CalculateElevationForGeoPosition (GeoPosition geoPosition);

        void SetElevationForDataPoint (int localLon, int localLat, double elevation);

        void CopyElevationPointsFrom (IRasterDigitalElevationModel dem);
    }
}
