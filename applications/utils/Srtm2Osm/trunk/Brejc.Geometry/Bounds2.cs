using System;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics;

namespace Brejc.Geometry
{
    public class Bounds2
    {
        public double MaxY
        {
            [DebuggerStepThrough]
            get { return maxY; }
            [DebuggerStepThrough]
            set { maxY = value; }
        }

        public double MinY
        {
            [DebuggerStepThrough]
            get { return minY; }
            [DebuggerStepThrough]
            set { minY = value; }
        }

        public double MaxX
        {
            [DebuggerStepThrough]
            get { return maxX; }
            [DebuggerStepThrough]
            set { maxX = value; }
        }

        public double MinX
        {
            [DebuggerStepThrough]
            get { return minX; }
            [DebuggerStepThrough]
            set { minX = value; }
        }

        public Point2<double> Center
        {
            [DebuggerStepThrough]
            get
            {
                return new Point2<double>((minX + maxX) / 2, (minY + maxY) / 2);
            }
        }

        public double DeltaX
        {
            [DebuggerStepThrough]
            get { return maxX - minX; }
        }

        public double DeltaY
        {
            [DebuggerStepThrough]
            get { return maxY - minY; }
        }

        public Point2<double> MinPoint
        {
            [DebuggerStepThrough]
            get { return new Point2<double> (minX, minY); }
        }

        public Point2<double> MaxPoint
        {
            [DebuggerStepThrough]
            get { return new Point2<double> (maxX, maxY); }
        }

        [DebuggerStepThrough]
        public Bounds2 () { }

        [DebuggerStepThrough]
        public Bounds2 (double minX, double minY, double maxX, double maxY)
        {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Bounds2"/> class using
        /// an array of points which all have to be included in the boundaries.
        /// </summary>
        /// <param name="pointsToCover">Points which this instance should cover.</param>
        public Bounds2 (Point2<double>[] pointsToCover)
        {
            foreach (Point2<double> point in pointsToCover)
                ExtendToCover (point);
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Bounds2"/> class so that it
        /// has a specified point in its center and has width and height equal to the speficied <c>size</c> parameter.
        /// </summary>
        /// <param name="centerPoint">The center point of the new boundary.</param>
        /// <param name="size">The width and the height of the boundary.</param>
        public Bounds2 (Point2<double> centerPoint, double size)
        {
            double halfSize = size / 2;
            minX = centerPoint.X - halfSize;
            minY = centerPoint.Y - halfSize;
            maxX = centerPoint.X + halfSize;
            maxY = centerPoint.Y + halfSize;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Bounds2"/> class with the two
        /// points specifying its extent.
        /// </summary>
        /// <param name="point1">The first point.</param>
        /// <param name="point2">The second point.</param>
        public Bounds2 (Point2<double> point1, Point2<double> point2)
        {
            ExtendToCover (point1);
            ExtendToCover (point2);
        }

        public void Normalize ()
        {
            double swap;

            if (minX > maxX)
            {
                swap = minX;
                minX = maxX;
                maxX = swap;
            }

            if (minY > maxY)
            {
                swap = minY;
                minY = maxY;
                maxY = swap;
            }
        }

        public void ExtendToCover (Point2<double> point)
        {
            ExtendToCover (point.X, point.Y);
        }

        public void ExtendToCover (double x, double y)
        {
            if (x.CompareTo (minX) < 0)
                minX = x;
            if (x.CompareTo (maxX) > 0)
                maxX = x;
            if (y.CompareTo (minY) < 0)
                minY = y;
            if (y.CompareTo (maxY) > 0)
                maxY = y;
        }

        /// <summary>
        /// Returns the intersection of two <see cref="GeoBounds"/> objects.
        /// </summary>
        /// <param name="with"><see cref="GeoBounds"/> object to intersect with.</param>
        /// <returns>Intersection of two <see cref="GeoBounds"/> objects.</returns>
        public Bounds2 Intersect (Bounds2 with)
        {
            if (with == null)
                throw new ArgumentNullException ("with");

            return new Bounds2 (Math.Max (MinX, with.MinX), Math.Max (MinY, with.MinY),
                Math.Min (MaxX, with.MaxX), Math.Min (MaxY, with.MaxY));
        }

        /// <summary>
        /// Checks if the object intersects with specified boundaries.
        /// </summary>
        /// <returns><c>True</c> if it intersects, <c>false</c> otherwise.</returns>
        public bool IntersectsWith (double minX, double minY, double maxX, double maxY)
        {
            return !(MinY > maxY || MaxY < minY || MaxX < minX || MinX > maxX);
        }

        /// <summary>
        /// Checks if the object intersects with specified boundaries.
        /// </summary>
        /// <param name="other">The other boundaries.</param>
        /// <returns>
        /// 	<c>True</c> if it intersects, <c>false</c> otherwise.
        /// </returns>
        public bool IntersectsWith (Bounds2 other)
        {
            return IntersectsWith (other.minX, other.minY, other.maxX, other.maxY);
        }

        /// <summary>
        /// Checks if the specified geographic point is inside bounds.
        /// </summary>
        /// <param name="lon">Longitude of the point.</param>
        /// <param name="lat">Latitude of the point.</param>
        /// <returns><c>True</c> if the point is inside, <c>false</c> otherwise.</returns>
        public bool IsInside (double x, double y)
        {
            return MinX <= x && x <= MaxX && MinY <= y && y <= MaxY;
        }

        /// <summary>
        /// Checks if the specified geographic point is inside bounds.
        /// </summary>
        /// <param name="geoPosition">Geographic position of the point.</param>
        /// <returns><c>True</c> if the point is inside, <c>false</c> otherwise.</returns>
        public bool IsInside (Point2<double> point)
        {
            return MinX <= point.X && point.X <= MaxX
                && MinY <= point.Y && point.Y <= MaxY;
        }

        /// <summary>
        /// Returns the union of two <see cref="GeoBounds"/> objects.
        /// </summary>
        /// <param name="with"><see cref="GeoBounds"/> object to unite with.</param>
        /// <returns>Union of two <see cref="GeoBounds"/> objects.</returns>
        public Bounds2 Union (Bounds2 with)
        {
            if (with == null)
                throw new ArgumentNullException ("with");

            return new Bounds2 (Math.Min (MinX, with.MinX), Math.Min (MinY, with.MinY),
                Math.Max (MaxX, with.MaxX), Math.Max (MaxY, with.MaxY));
        }

        /// <summary>
        /// Inflates the bounds by a factor.
        /// </summary>
        /// <param name="factor"><see cref="Double"/> value of inflation.
        /// <list type="table">
        ///     <item>
        ///         <term>factor > 1</term>
        ///         <description>bounds are inflated</description>
        ///     </item>
        ///     <item>
        ///         <term>0 <= factor < 1</term>
        ///         <description>bounds are deflated</description>
        ///     </item>
        ///     <item>
        ///         <term>factor == 1</term>
        ///         <description>bounds remain the same size</description>
        ///     </item>
        ///     <item>
        ///         <term>factor < 0</term>
        ///         <description><see cref="ArgumentOutOfRangeException"/> is thrown.</description>
        ///     </item>
        /// </list>
        /// </param>
        /// <returns>Inflated <see cref="GeoBounds"/> object.</returns>
        public Bounds2 InflateBy (double factor)
        {
            if (factor < 0)
                throw new ArgumentOutOfRangeException ("factor", factor, "Factor cannot be a negative value.");

            double yFactor = DeltaY * (factor - 1) / 2;
            double xFactor = DeltaX * (factor - 1) / 2;

            return new Bounds2 (MinX - xFactor, MinY - yFactor,
                MaxX + xFactor, MaxY + yFactor);
        }

        public Bounds2 Inflate (double dx, double dy)
        {
            return new Bounds2 (MinX - dx, MinY - dy,
                MaxX + dx, MaxY + dy);
        }

        /// <summary>
        /// Compares the current <see cref="Bounds2"/> object to the specified object for equivalence.
        /// </summary>
        /// <param name="obj">The <see cref="Bounds2"/> object to test for equivalence with the current object.</param>
        /// <returns>
        /// <c>true</c> if the two <see cref="Bounds2"/> objects are equal; otherwise, <c>false</c>.
        /// </returns>
        public override bool Equals (object obj)
        {
            if (obj == null)
                return false;

            Bounds2 that = obj as Bounds2;

            if (that == null)
                return false;

            return minX.Equals (that.minX) && minY.Equals (that.minY) && maxX.Equals (that.maxX) && maxY.Equals (that.MaxY);
        }

        /// <summary>
        /// Returns the hash code for this <see cref="Bounds2"/> object.
        /// </summary>
        /// <returns>
        /// A 32-bit signed integer hash code.
        /// </returns>
        public override int GetHashCode ()
        {
            return minX.GetHashCode () ^ minY.GetHashCode () ^ maxX.GetHashCode () ^ maxY.GetHashCode ();
        }
                
        public override string ToString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "{0},{1},{2},{3}", minX, minY, maxX, maxY);
        }

        private double minX = double.MaxValue, maxX = double.MinValue, minY = double.MaxValue, maxY = double.MinValue;
    }
}
