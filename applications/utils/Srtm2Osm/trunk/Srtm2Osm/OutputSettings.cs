using System;
using System.Collections.Generic;
using System.Text;
using Brejc.DemLibrary;

namespace Srtm2Osm
{
    /// <summary>
    /// Storage for specific settings used for the output.
    /// </summary>
    public class OutputSettings
    {
        /// <summary>
        /// Additional longitude correction value.
        /// </summary>
        public double LongitudeCorrection
        { get { return this.longitudeCorrection; } set { this.longitudeCorrection = value; } }

        /// <summary>
        /// Additional latitude correction value.
        /// </summary>
        public double LatitudeCorrection
        { get { return this.latitudeCorrection; } set { this.latitudeCorrection = value; } }

        /// <summary>
        /// Maximum number of nodes per way.
        /// </summary>
        public int MaxWayNodes
        { get { return this.maxWayNodes; } set { this.maxWayNodes = value; } }

        /// <summary>
        /// Changeset ID which should be used in the output file.
        /// </summary>
        public int ChangesetId
        { get { return this.changesetId; } set { this.changesetId = value; } }

        /// <summary>
        /// User ID which should be used in the output file.
        /// </summary>
        public int UserId
        { get { return this.userId; } set { this.userId = value; } }

        /// <summary>
        /// User name which should be used in the output file.
        /// </summary>
        public string UserName
        {
            get { return this.user; }
            set
            {
                if (String.IsNullOrEmpty (value))
                    throw new ArgumentException ("Value null or empty.", "value");

                this.user = value;
            }
        }

        /// <summary>
        /// Instance of the desired contour marker algorithm.
        /// </summary>
        public IContourMarker ContourMarker
        {
            get { return this.contourMarker; }
            set
            {
                if (value == null)
                    throw new ArgumentNullException ("value");

                this.contourMarker = value;
            }
        }

        double longitudeCorrection, latitudeCorrection;
        int maxWayNodes;
        IContourMarker contourMarker = new DefaultContourMarker();
        int changesetId;
        string user;
        int userId;
    }
}
