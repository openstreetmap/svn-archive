using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Srtm2Osm
{
    /// <summary>
    /// Contains methods to split a given boundary into smaller ones.
    /// </summary>
    static class BoundsSplitter
    {
        /// <summary>
        /// Returns a list of <see cref="Bounds2"/> objects which have the maximum extend given in
        /// <paramref name="tile"/>. The <paramref name="original"/> object will enclose the result.
        /// </summary>
        /// <param name="original">Original <see cref="Bounds2"/> object which should be splitted.</param>
        /// <param name="tile">Maximum extend of a single resulting <see cref="Bounds2"/> object.</param>
        /// <returns>List of <see cref="Bounds2"/> objects which have the maximum size defined in
        /// <paramref name="tile"/>. All objects fit inside the <paramref name="original"/> object.</returns>
        /// <remarks>The bound inside the <paramref name="tile"/> object must start at zero.</remarks>
        public static List<Bounds2> Split (Bounds2 original, Bounds2 tile)
        {
            if (original == null)
                throw new ArgumentNullException ("original");
            if (tile == null)
                throw new ArgumentNullException ("tile");
            if (tile.MinX != 0 || tile.MinY != 0)
                throw new ArgumentException ("The MinX and MinY property must be zero.", "tile");
            if (tile.DeltaX == 0 || tile.DeltaY == 0)
                throw new ArgumentException ("The given tile bound has no extent.", "tile");

            List<Bounds2> result = new List<Bounds2> ();

            double currentX = original.MinX;

            while (currentX < original.MaxX)
            {
                double currentY = original.MinY;

                while (currentY < original.MaxY)
                {
                    tile.MoveTo (currentX, currentY);

                    Bounds2 newTile = original.Intersect (tile);
                    result.Add (newTile);

                    currentY += tile.DeltaY;
                }

                currentX += tile.DeltaX;
            }

            return result;
        }

        /// <summary>
        /// Returns a list of <see cref="Bounds2"/> objects which have the maximum size given in
        /// <paramref name="width"/> and <paramref name="height"/>. The <paramref name="original"/>
        /// object will enclose the result.
        /// </summary>
        /// <param name="original">Original <see cref="Bounds2"/> object which should be splitted.</param>
        /// <param name="width">Width of the boundary.</param>
        /// <param name="height">Height of the boundary.</param>
        /// <returns>List of <see cref="Bounds2"/> objects which have the maximum size defined in
        /// <paramref name="width"/> and <paramref name="height"/>. All objects fit inside the
        /// <paramref name="original"/> object.</returns>
        public static List<Bounds2> Split (Bounds2 original, double width, double height)
        {
            Bounds2 tile = new Bounds2 (0, 0, width, height);
            return Split (original, tile);
        }
    }
}
