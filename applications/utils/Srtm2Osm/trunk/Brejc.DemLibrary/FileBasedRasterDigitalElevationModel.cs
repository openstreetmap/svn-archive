using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace Brejc.DemLibrary
{
    [Serializable]
    public class FileBasedRasterDigitalElevationModel : RasterDigitalElevationModelBase
    {
        public FileBasedRasterDigitalElevationModel (int lonResolution,
            int latResolution,
            int lonOffset,
            int latOffset,
            int lonLength,
            int latLength)
            : base (lonResolution, latResolution, lonOffset, latOffset, lonLength, latLength)
        {
            data = new FileStream (Path.GetTempFileName(), FileMode.Create);
            data.SetLength ((long) this.LonLength * this.LatLength * 2);
        }

        ~FileBasedRasterDigitalElevationModel()
        {
            try
            { File.Delete(data.Name); }
            catch
            { }
        }

        public override double GetElevationForDataPoint (int localLon, int localLat)
        {
            SeekToPosition (localLon, localLat);
            byte lowByte = (byte) data.ReadByte();
            byte highByte = (byte) data.ReadByte();

            Int16 elevation16 = (short)((highByte << 8) | lowByte);

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

            byte lowByte = (byte) (elevation16 & 0xff);
            byte highByte = (byte) ((elevation16 >> 8) & 0xff);

            SeekToPosition (localLon, localLat);
            data.WriteByte (lowByte);
            data.WriteByte (highByte);
        }

        private void SeekToPosition (int localLon, int localLat)
        {
            long pos = (localLon + localLat * LonLength) * 2L;
            data.Seek (pos, SeekOrigin.Begin);
        }

        #region ICloneable Members

        public override object Clone ()
        {
            throw new NotSupportedException();
        }

        #endregion

        [NonSerialized()]
        private FileStream data;
    }
}
