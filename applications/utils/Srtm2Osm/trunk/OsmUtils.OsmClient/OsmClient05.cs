using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using OsmUtils.OsmSchema;
using System.IO;
using System.Xml.Serialization;

namespace OsmUtils.OsmClient
{
    public class OsmClient05
    {
        public osm GetMap (double bottomLeftLng, double bottomLeftLat, double topRightLng, double topRightLat)
        {
            WebClient webClient = new WebClient ();

            UriBuilder requestUrl = new UriBuilder(GetCommandUrl ("map"));
            requestUrl.Query = String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "bbox={0},{1},{2},{3}", bottomLeftLng, bottomLeftLat, topRightLng, topRightLat);

            byte[] data = webClient.DownloadData (requestUrl.Uri);

            osm map = null;

            using (MemoryStream stream = new MemoryStream (data))
            {
                XmlSerializer serializer = new XmlSerializer (typeof (osm));
                map = (osm)serializer.Deserialize (stream);
            }

            return map;
        }

        static public osm LoadFile (string osmFileName)
        {
            using (FileStream stream = File.Open (osmFileName, FileMode.Open))
            {
                XmlSerializer serializer = new XmlSerializer (typeof (osm));
                return (osm) serializer.Deserialize (stream);
            }
        }

        static public void SaveFile (osm osmData, string osmFileName)
        {
            using (FileStream stream = File.Open (osmFileName, FileMode.Create))
            {
                XmlSerializer serializer = new XmlSerializer (typeof (osm));
                serializer.Serialize (stream, osmData);
            }
        }

        [System.Diagnostics.CodeAnalysis.SuppressMessage ("Microsoft.Usage", "CA2234:PassSystemUriObjectsInsteadOfStrings")]
        protected Uri GetCommandUrl (string commandName)
        {
            return new Uri (osmApiServerUrl, commandName);
        }

        private Uri osmApiServerUrl = new Uri (@"http://api.openstreetmap.org/api/0.5/");
    }
}
