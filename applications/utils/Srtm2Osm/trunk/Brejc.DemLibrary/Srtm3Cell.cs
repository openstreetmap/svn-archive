using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Text.RegularExpressions;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.DemLibrary
{
    [Serializable]
    public class Srtm3Cell : RasterDigitalElevationModelBase
    {
        public Int16 CellLat
        {
            get { return (Int16) LatOffset; }
        }

        public Int16 CellLon
        {
            get { return (Int16) LonOffset; }
        }

        public bool IsLoaded { get { return data != null; } }

        public string CellFileName
        {
            get
            {
                return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "{0}{1:00}{2}{3:000}.hgt",
                    CellLat >= 0 ? 'N' : 'S',
                    Math.Abs (CellLat),
                    CellLon >= 0 ? 'E' : 'W',
                    Math.Abs (CellLon));
            }
        }

        public Srtm3Cell (Int16 longitude, Int16 latitude)
            : base (1200, 1200, longitude, latitude, 1201, 1201)
        {
        }

        static public Srtm3Cell CreateSrtm3Cell (string fileName, bool load)
        {
            Regex regex = new Regex ("^(?'latSign'[N|S])(?'latitude'[0-9]{2})(?'longSign'[W|E])(?'longitude'[0-9]{3})",
                RegexOptions.ExplicitCapture);

            Match match = regex.Match (fileName);
            if (false == match.Success)
                throw new ArgumentException ("Invalid filename", "fileName");

            char latSign = match.Groups["latSign"].Value[0];
            Int16 cellLat = Int16.Parse (match.Groups["latitude"].Value, System.Globalization.CultureInfo.InvariantCulture);
            char lonSign = match.Groups["longSign"].Value[0];
            Int16 cellLon = Int16.Parse (match.Groups["longitude"].Value, System.Globalization.CultureInfo.InvariantCulture);
            if (latSign == 'S')
                cellLat = (Int16)(-cellLat);
            if (lonSign == 'W')
                cellLon = (Int16)(-cellLon);

            Srtm3Cell cell = new Srtm3Cell (cellLon, cellLat);

            if (load)
                cell.LoadFromFile (fileName);

            return cell;
        }

        [SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "lat+90")]
        [SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "lng+180")]
        static public int CalculateCellKey (int lng, int lat)
        {
            int key = ((lng + 180) << 16) | (lat + 90);
            return key;
        }

        [SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "lng+180")]
        static public int CalculateCellKey (Srtm3Cell cell)
        {
            if (cell == null)
                throw new ArgumentNullException ("cell");                
            
            int key = (((int)cell.CellLon + 180) << 16) | ((int)cell.CellLat + 90);
            return key;
        }

        public void LoadFromFile (string filePath)
        {
            using (Stream stream = File.Open (filePath, FileMode.Open, FileAccess.Read))
            {
                BinaryReader reader = new BinaryReader (stream);

                data = reader.ReadBytes ((int)stream.Length);
            }
        }

        public void LoadFromCache (string cacheDir)
        {
            string filePath = Path.Combine (cacheDir, CellFileName);
            LoadFromFile (filePath);
        }

        [SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "localLon*2")]
        [SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "1201-localLat")]
        public override double GetElevationForDataPoint (int localLon, int localLat)
        {
            // if the cell is empty, return "missing value"
            if (data == null)
                return Int16.MinValue;

            int bytesPos = ((1201 - localLat - 1) * 1201 * 2) + localLon * 2;

            return (Int16)((data[bytesPos]) << 8 | data[bytesPos + 1]);
        }

        public override void SetElevationForDataPoint (int localLon, int localLat, double elevation)
        {
            throw new NotImplementedException ("The method or operation is not implemented.");
        }

        public override object Clone ()
        {
            throw new NotImplementedException ("The method or operation is not implemented.");
        }

        private byte[] data;
    }
}
