using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using System.IO;
using System.Net;
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

        public static string SrtmHost
        {
            get { return host; }
            set { if (value.Length != 0) host = value; }
        }

        public static string SrtmDir
        {
            get { return basedir; }
            set { if (value.Length != 0) basedir = value; }
        }

        private static string host = "dds.cr.usgs.gov";
        private static string basedir = "srtm/version2_1/SRTM3";
        private static string pattern = "href=\"([A-Za-z0-9]*\\.hgt\\.zip)\"";

        /// <summary>
        /// Generates an index file by listing all available SRTM cells on the FTP site.
        /// </summary>
        public void Generate ()
        {
            for (SrtmContinentalRegion continentalRegion = (SrtmContinentalRegion)(SrtmContinentalRegion.None + 1); 
                 continentalRegion < SrtmContinentalRegion.End;
                 continentalRegion++)
            {
                string region = continentalRegion.ToString();
                string url = "http://" + host + "/" + basedir + "/" + region + "/";

                WebRequest request = HttpWebRequest.Create(new System.Uri(url));
                HttpWebResponse response = (HttpWebResponse) request.GetResponse();
                if (response.StatusCode == HttpStatusCode.OK)
                {
                    // Get the stream containing content returned by the server.
                    Stream dataStream = response.GetResponseStream();
                    // Open the stream using a StreamReader for easy access.
                    StreamReader reader = new StreamReader(dataStream);
                    // Read the content.
                    string responseFromServer = reader.ReadToEnd();
                    MatchCollection matches = Regex.Matches(responseFromServer, pattern, RegexOptions.IgnoreCase);
                    // Process each match.
                    foreach (Match match in matches)
                    {
                        GroupCollection groups = match.Groups;
                        string filename = groups[1].Value.Trim();
                        if (filename.Length == 0)
                            continue;

                        Srtm3Cell srtm3Cell = Srtm3Cell.CreateSrtm3Cell(filename, false);
                        SetValueForCell(srtm3Cell.CellLon, srtm3Cell.CellLat, (int)continentalRegion);
                    }
                    // Cleanup the streams and the response.
                    reader.Close();
                    dataStream.Close();
                }
                else
                {
                    Console.WriteLine("Request for {0} returned {1}.", url, response.StatusDescription);
                }
                response.Close();

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

        private int[] data = new int[360*180];
    }
}
