using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    [Serializable]
    public class RasterDigitalElevationModel : RasterDigitalElevationModelBase
    {
        public RasterDigitalElevationModel (int lonResolution,
            int latResolution,
            int lonOffset,
            int latOffset,
            int lonLength,
            int latLength)
            : base (lonResolution, latResolution, lonOffset, latOffset, lonLength, latLength)
        {
            data = new Int16[this.LonLength * this.LatLength];
        }

        public override double GetElevationForDataPoint (int localLon, int localLat)
        {
            Int16 elevation16 = data[localLon + localLat * LonLength];

            if (elevation16 != Int16.MinValue)
                return elevation16;

            return double.MinValue;
        }

        public override void SetElevationForDataPoint (int localLon, int localLat, double elevation)
        {
            Int16 elevation16;

            if (elevation == double.MinValue)
                elevation16 = Int16.MinValue;
            else
                elevation16 = (Int16)elevation;

            data[localLon + localLat * LonLength] = elevation16;
        }

        #region ICloneable Members

        public override object Clone ()
        {
            RasterDigitalElevationModel clone = new RasterDigitalElevationModel (
                LonResolution, LatResolution, LonOffset, LatOffset, LonLength, LatLength);
            clone.data = (Int16[])this.data.Clone ();
            return clone;
        }

        #endregion

        private Int16[] data;
    }
}
