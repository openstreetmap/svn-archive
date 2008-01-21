using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.Geometry
{
    public sealed class GeometryUtils
    {
        public static double Deg2Rad (double degrees)
        {
            return degrees * Math.PI / 180;
        }

        public static double Rad2Deg (double radians)
        {
            return radians * 180 / Math.PI;
        }

        // IsLeft(): tests if a point is Left|On|Right of an infinite line.
        //    Input:  three points P0, P1, and P2
        //    Return: >0 for P2 left of the line through P0 and P1
        //            =0 for P2 on the line
        //            <0 for P2 right of the line
        static public double IsLeft (Point3<double> point0, Point3<double> point1, Point3<double> point2)
        {
            return ((point1.X - point0.X) * (point2.Y - point0.Y)
                    - (point2.X - point0.X) * (point1.Y - point0.Y));
        }

        static public double DifferenceBetweenAngles (double angle1, double angle2, double normalizer)
        {
            double diff = NormalizeAngle (angle1, normalizer) - NormalizeAngle (angle2, normalizer);
            diff = Math.Abs (diff);
            if (diff > normalizer / 2)
                diff = normalizer - diff;
            return diff;
        }

        static public double NormalizeAngle (double angle, double normalizer)
        {
            angle = angle % normalizer;
            if (angle < 0)
                angle = normalizer + angle;

            return angle;
        }

        /// <summary>
        /// Returns a string representation of a specified angle value.
        /// </summary>
        /// <param name="angle">The angle value.</param>
        /// <param name="shortenIfPossible">if set to <c>true</c>, the method will not display mintes and seconds
        /// if they are equal to zero.</param>
        /// <returns>A string representation of a specified angle value.</returns>
        static public string FormatAngle (double angle, bool shortenIfPossible)
        {
            StringBuilder builder = new StringBuilder ();
            builder.AppendFormat ("{0}\u00b0", (int)angle);

            angle = Math.Abs (angle);

            int minutes = (int)((angle * 60) % 60);
            int seconds = (int)Math.Round ((angle * 3600) % 60);

            if (minutes != 0 || seconds != 0 || false == shortenIfPossible)
                builder.AppendFormat ("{0:00}'", minutes);

            if (seconds != 0 || false == shortenIfPossible)
                builder.AppendFormat ("{0:00}\"", seconds);

            return builder.ToString ();
        }

        private GeometryUtils () { }
    }
}
