using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    [Serializable]
    public struct GeoPosition : ICloneable
    {
        public double Longitude
        {
            get { return longitude; }
            set { longitude = value; }
        }

        public double Latitude
        {
            get { return latitude; }
            set { latitude = value; }
        }

        public double Elevation
        {
            get { return elevation; }
            set { elevation = value; }
        }

        public bool IsElevationSet { get { return elevation != double.MinValue; } }

        public GeoPosition (double longitude, double latitude, double elevation)
        {
            this.longitude = longitude;
            this.latitude = latitude;
            this.elevation = elevation;
        }

        public GeoPosition (double longitude, double latitude) : this (longitude, latitude, double.MinValue) { }

        public GeoPosition (Point3<double> point) : this (point.X, point.Y, point.Z) { }

        #region ICloneable Members

        public object Clone ()
        {
            GeoPosition clone = new GeoPosition ();

            clone.longitude = longitude;
            clone.latitude = latitude;
            clone.elevation = elevation;

            return clone;
        }

        #endregion

        public override string ToString ()
        {
            if (IsElevationSet)
                return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "longitude={0}, latitude={1}, elevation={2}", longitude, latitude, elevation);

            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "longitude={0}, latitude={1}", longitude, latitude);
        }

        /// <summary>
        /// Compares the current <see cref="GeoPosition"/> object to the specified object for equivalence.
        /// </summary>
        /// <param name="obj">The <see cref="GeoPosition"/> object to test for equivalence with the current object.</param>
        /// <returns>
        /// <c>true</c> if the two <see cref="GeoPosition"/> objects are equal; otherwise, <c>false</c>.
        /// </returns>
        public override bool Equals (object obj)
        {
            GeoPosition that = (GeoPosition)obj;

            return this.Elevation.Equals (that.Elevation) && this.Latitude.Equals (that.Latitude)
                && this.Longitude.Equals (that.Longitude);
        }

        /// <summary>
        /// Returns the hash code for this <see cref="GeoPosition"/> object.
        /// </summary>
        /// <returns>
        /// A 32-bit signed integer hash code.
        /// </returns>
        public override int GetHashCode ()
        {
            return longitude.GetHashCode () ^ latitude.GetHashCode () ^ elevation.GetHashCode ();
        }

        /// <summary>
        /// Implements the operator ==.
        /// </summary>
        /// <param name="a">The first object.</param>
        /// <param name="b">The second object.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator == (GeoPosition a, GeoPosition b)
        {
            return a.Equals (b);
        }

        /// <summary>
        /// Implements the operator !=.
        /// </summary>
        /// <param name="a">The first object.</param>
        /// <param name="b">The second object.</param>
        /// <returns>The result of the operator.</returns>
        public static bool operator != (GeoPosition a, GeoPosition b)
        {
            return !a.Equals (b);
        }
                
        private double longitude;
        private double latitude;
        private double elevation;
    }
}
