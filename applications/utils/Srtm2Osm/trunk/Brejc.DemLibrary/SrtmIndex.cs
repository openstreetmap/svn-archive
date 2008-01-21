using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.DemLibrary
{
    // NOTE: names correspond to actual directory names on the srtm ftp server!
    public enum SrtmContinentalRegion
    {
        None,
        Australia,
        Eurasia,
        Africa,
        Islands,
        [SuppressMessage ("Microsoft.Naming", "CA1707:IdentifiersShouldNotContainUnderscores")]
        North_America,
        [SuppressMessage ("Microsoft.Naming", "CA1707:IdentifiersShouldNotContainUnderscores")]
        South_America,
        End,
    }

    [Serializable]
    public class SrtmIndex
    {
        public FtpClient FtpClient
        {
            get { return ftpClient; }
            set { ftpClient = value; }
        }

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "latitude+90")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "longitude+180")]
        public int GetValueForCell (int longitude, int latitude)
        {
            return data[longitude + 180 + 360 * (latitude + 90)];
        }

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "latitude+90")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Usage", "CA2233:OperationsShouldNotOverflow", MessageId = "longitude+180")]
        public void SetValueForCell (int longitude, int latitude, int value)
        {
            data[longitude + 180 + 360 * (latitude + 90)] = value;
        }

        /// <summary>
        /// Generates an index file by listing all available SRTM cells on the FTP site.
        /// </summary>
        public void Generate ()
        {
            ftpClient.setRemoteHost ("e0srp01u.ecs.nasa.gov");
            ftpClient.setRemotePath (@"srtm/version2/SRTM3");

            ftpClient.login ();

            try
            {
                for (SrtmContinentalRegion continentalRegion = (SrtmContinentalRegion)(SrtmContinentalRegion.None + 1); 
                    continentalRegion < SrtmContinentalRegion.End;
                    continentalRegion++)
                {
                    ftpClient.chdir (continentalRegion.ToString());
                    string[] fileList = ftpClient.getFileList ("*.hgt.zip");

                    foreach (string filename in fileList)
                    {
                        string trimmedFilename = filename.Trim ();
                        if (trimmedFilename.Length == 0)
                            continue;

                        Srtm3Cell srtm3Cell = Srtm3Cell.CreateSrtm3Cell (trimmedFilename, false);
                        SetValueForCell (srtm3Cell.CellLon, srtm3Cell.CellLat, (int)continentalRegion);
                    }

                    ftpClient.chdir ("..");
                }
            }
            finally
            {
                ftpClient.close ();
            }
        }

        /// <summary>
        /// Saves the SRTM index file to a specified location.
        /// </summary>
        /// <param name="filePath">The file path.</param>
        public void Save (string filePath)
        {
            using (FileStream file = File.Open (filePath, FileMode.Create, FileAccess.Write))
            {
                BinaryFormatter formatter = new BinaryFormatter ();
                formatter.Serialize (file, this);
            }
        }

        /// <summary>
        /// Loads a SRTM index from a specified file.
        /// </summary>
        /// <param name="filePath">The file path.</param>
        /// <returns></returns>
        static public SrtmIndex Load (string filePath)
        {
            SrtmIndex index;

            using (FileStream file = File.Open (filePath, FileMode.Open, FileAccess.Read))
            {
                BinaryFormatter formatter = new BinaryFormatter ();
                index = formatter.Deserialize (file) as SrtmIndex;
            }

            return index;
        }

        [NonSerialized]
        private FtpClient ftpClient = new FtpClient ();

        private int[] data = new int[360*180];
    }
}
