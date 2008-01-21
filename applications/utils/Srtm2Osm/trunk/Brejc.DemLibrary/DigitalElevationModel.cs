using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    [Serializable]
    public abstract class RasterDigitalElevationModelBase : IRasterDigitalElevationModel
    {
        public int LatResolution
        {
            get { return latResolution; }
        }

        public int LonResolution
        {
            get { return lonResolution; }
        }

        public int LatOffset
        {
            get { return latOffset; }
            set { latOffset = value; }
        }

        public int LonOffset
        {
            get { return lonOffset; }
            set { lonOffset = value; }
        }

        public int LatLength
        {
            get { return latLength; }
        }

        public int LonLength
        {
            get { return lonLength; }
        }

        public int DataPointsCount
        {
            get
            {
                return LonLength * LatLength;
            }
        }

        public Bounds2 Bounds 
        {
            get
            {
                Bounds2 bounds = new Bounds2 ();
                bounds.MinX = (lonOffset - 0.5) / lonResolution;
                bounds.MinY = (latOffset - 0.5) / latResolution;
                bounds.MaxX = (lonOffset + (lonLength - 1) + 0.5) / lonResolution;
                bounds.MaxY = (latOffset + (latLength - 1) + 0.5) / lonResolution;
                bounds.Normalize ();

                return bounds;
            }
        }

        protected RasterDigitalElevationModelBase (int lonResolution, 
            int latResolution,
            int lonOffset, 
            int latOffset, 
            int lonLength, 
            int latLength)
        {
            this.lonResolution = lonResolution;
            this.latResolution = latResolution;
            this.lonOffset = lonOffset;
            this.latOffset = latOffset;
            this.lonLength = lonLength;
            this.latLength = latLength;
        }

        public virtual GeoPosition GetGeoPosition (double lonPos, double latPos)
        {
            return GetGeoPosition (lonPos, latPos, true);
        }

        public virtual GeoPosition GetGeoPosition (double lonPos, double latPos, bool calculateElevation)
        {
            GeoPosition pos = new GeoPosition (
                (lonOffset + lonPos - 0.5) / lonResolution,
                (latOffset + latPos - 0.5) / latResolution,
                (calculateElevation ? CalculateElevationForDataPoint (lonPos, latPos) : 0));

            return pos;
        }

        public virtual double CalculateElevationForDataPoint (double localLon, double localLat)
        {
            int lonPos0 = (int)Math.Floor (localLon);
            int lonPos1 = (int)Math.Ceiling (localLon);
            int latPos0 = (int)Math.Floor (localLat);
            int latPos1 = (int)Math.Ceiling (localLat);

            if (lonPos0 < 0
                || lonPos1 >= this.LonLength
                || latPos0 < 0
                || latPos1 >= this.LatLength)
                return double.MinValue;

            // get elevations of adjacent known elevation points
            double[] elevations = new double[] { 
                GetElevationForDataPoint (lonPos0, latPos0),
                GetElevationForDataPoint (lonPos1, latPos0),
                GetElevationForDataPoint (lonPos0, latPos1),
                GetElevationForDataPoint (lonPos1, latPos1)};

            double elev1 = (elevations[0] - elevations[1]) * (localLon - lonPos0) + elevations[1];
            double elev2 = (elevations[2] - elevations[3]) * (localLon - lonPos0) + elevations[3];
            return (elev1 - elev2) * (localLat - latPos0) + elev2;
        }

        /// <summary>
        /// Returns elevation for a specified geographic position.
        /// </summary>
        /// <param name="geoPosition">The geo position.</param>
        /// <returns>
        /// Elevation of specified geographic position.
        /// </returns>
        /// <remarks>The method calculates the elevation through interpolation of known adjacent elevation points.</remarks>
        public virtual double CalculateElevationForGeoPosition (GeoPosition geoPosition)
        {
            double lonPos = geoPosition.Longitude * lonResolution + 0.5 - lonOffset;
            double latPos = geoPosition.Latitude * latResolution + 0.5 - latOffset;

            return CalculateElevationForDataPoint (lonPos, latPos);
        }

        public virtual void CopyElevationPointsFrom (IRasterDigitalElevationModel dem)
        {
            if (dem == null)
                throw new ArgumentNullException ("dem");

            if (LatResolution != dem.LatResolution || LonResolution != dem.LonResolution)
                throw new ArgumentException ("The two DEM's have incompatibile resolutions.");

            // cell absolute position
            int cellLonAbs = dem.LonOffset * dem.LonResolution;
            int cellLatAbs = dem.LatOffset * dem.LatResolution;

            // first find the place to copy to and extent

            // initialize the copying rectangle to the cell's extent
            int west, south, east, north;
            west = cellLonAbs;
            south = cellLatAbs;
            east = west + dem.LonResolution - 1;
            north = south + dem.LatResolution - 1;

            // now intersect it with the destination extent
            if (LonOffset > west)
                west = LonOffset;
            if (LatOffset > south)
                south = LatOffset;
            if (LonOffset + LonLength - 1 < east)
                east = LonOffset + LonLength - 1;
            if (LatOffset + LatLength - 1 < north)
                north = LatOffset + LatLength - 1;

            for (int xx = west; xx <= east; xx++)
            {
                for (int yy = south; yy <= north; yy++)
                {
                    double elevation = dem.GetElevationForDataPoint (xx - cellLonAbs, yy - cellLatAbs);
                    SetElevationForDataPoint (xx - LonOffset, yy - LatOffset, elevation);
                }
            }
        }

        public virtual DigitalElevationModelStatistics CalculateStatistics ()
        {
            DigitalElevationModelStatistics statistics = new DigitalElevationModelStatistics ();

            for (int lng = 0; lng < lonLength; lng++)
            {
                for (int lat = 0; lat < latLength; lat++)
                {
                    double elev = GetElevationForDataPoint (lng, lat);
                    if (elev == double.MinValue)
                        statistics.HasMissingPoints = true;
                    else
                    {
                        if (elev < statistics.MinElevation)
                            statistics.MinElevation = elev;
                        if (elev > statistics.MaxElevation)
                            statistics.MaxElevation = elev;
                    }
                }
            }

            return statistics;
        }

        public abstract double GetElevationForDataPoint (int localLon, int localLat);
        public abstract void SetElevationForDataPoint (int localLon, int localLat, double elevation);

        public abstract object Clone ();

        private int lonResolution, latResolution;

        private int lonOffset, latOffset;
        private int lonLength, latLength;
    }
}
