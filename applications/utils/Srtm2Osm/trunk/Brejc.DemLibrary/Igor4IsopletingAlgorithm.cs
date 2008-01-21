using System;
using System.Collections.Generic;
using System.Text;
using log4net;
//using Brejc.Visualization;
using System.Diagnostics.CodeAnalysis;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    [SuppressMessage ("Microsoft.Naming", "CA1714:FlagsEnumsShouldHavePluralNames")]
    [Flags]
    public enum IsohypseMovement
    {
        None = 0,
        North = 1,
        East = 2,
        South = 4,
        West = 8,
    }

    public class Igor4IsopletingAlgorithm : IIsopletingAlgorithm
    {
        public IActivityLogger ActivityLogger { get { return activityLogger; } set { activityLogger = value; } }
        //public IVisualizer Visualizer
        //{
        //    get { return visualizer; }
        //    set { visualizer = value; }
        //}

        [SuppressMessage ("Microsoft.Performance", "CA1814:PreferJaggedArraysOverMultidimensional", MessageId = "Body")]
        public void Isoplete (IRasterDigitalElevationModel dem, double elevationStep, NewIsohypseCallback callback)
        {
            DigitalElevationModelStatistics statistics = dem.CalculateStatistics ();

            double minElevation = Math.Floor (statistics.MinElevation / elevationStep) * elevationStep;
            double maxElevation = Math.Floor (statistics.MaxElevation / elevationStep) * elevationStep;

            //if (visualizer != null)
            //{
            //    GeoPosition geoPosMin = dem.GetGeoPosition (0, 0, false);
            //    GeoPosition geoPosMax = dem.GetGeoPosition (dem.LonLength, dem.LatLength, false);

            //    visualizer.Initialize (geoPosMin.Longitude, geoPosMin.Latitude,
            //        geoPosMax.Longitude, geoPosMax.Latitude);
            //}

            Array2<byte> characteristics = new Array2<byte> (dem.LonLength - 1, dem.LatLength - 1);
            Array2<byte> flags = new Array2<byte> (dem.LonLength - 1, dem.LatLength - 1);
            Array2<IsohypseMovement> movements = new Array2<IsohypseMovement> (dem.LonLength - 1, dem.LatLength - 1);

            int[,] adjacentCells = new int[,] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };

            // foreach elevation step
            for (double isoElev = minElevation; isoElev <= maxElevation; isoElev += elevationStep)
            {
                activityLogger.Log (ActivityLogLevel.Normal, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "Analyzing elevation {0}", isoElev));

                //if (visualizer != null)
                //{
                //    GeoPosition geoPosMin = dem.GetGeoPosition (0, 0, false);
                //    GeoPosition geoPosMax = dem.GetGeoPosition (dem.LongLength, dem.LatLength, false);

                //    visualizer.Initialize (geoPosMin.Longitude, geoPosMin.Latitude,
                //        geoPosMax.Longitude, geoPosMax.Latitude);

                //    for (int x = 0; x < dem.LongLength; x++)
                //    {
                //        for (int y = 0; y < dem.LatLength; y++)
                //        {
                //            int elevation = dem.GetElevation (x, y);
                //            GeoPosition geoPos = dem.GetGeoPosition (x, y);

                //            string color = elevation >= isoElev ? "orange" : "yellow";

                //            visualizer.DrawPoint (color, geoPos.Longitude, geoPos.Latitude);
                //            visualizer.DrawText ("pink", geoPos.Longitude, geoPos.Latitude, elevation.ToString ());
                //        }
                //    }
                //}

                Isohypse isohypse = new Isohypse (isoElev);

                characteristics.Initialize (0);
                flags.Initialize (0);
                movements.Initialize (0);

                for (int x = 0; x < dem.LonLength - 1; x++)
                {
                    for (int y = 0; y < dem.LatLength - 1; y++)
                    {
                        for (int i = 0; i < adjacentCells.GetLength (0); i++)
                        {
                            double adjacentCellElev = dem.GetElevationForDataPoint (x + adjacentCells[i, 0], y + adjacentCells[i, 1]);
                            if (adjacentCellElev >= isoElev)
                            {
                                characteristics.SetValue ((byte)(characteristics.GetValue (x, y) + 1), x, y);
                                flags.SetValue ((byte)(flags.GetValue (x, y) | (byte)(1 << i)), x, y);
                            }
                        }

                        Int16 cellCharacteristic = characteristics.GetValue (x, y);

                        // skip cells which are definitively not along the isohypse
                        if (cellCharacteristic == 0 || cellCharacteristic == 4)
                            continue;

                        if (cellCharacteristic == 2)
                        {
                            int maskedFlags = flags.GetValue (x, y) & 0x9;
                            if (maskedFlags == 0 || maskedFlags == 0x9)
                                movements.SetValue (IsohypseMovement.North | IsohypseMovement.South, x, y);
                            else
                            {
                                maskedFlags = flags.GetValue (x, y) & 0x3;
                                if (maskedFlags == 0 || maskedFlags == 0x3)
                                    movements.SetValue (IsohypseMovement.West | IsohypseMovement.East, x, y);

                                else
                                    movements.SetValue (
                                        IsohypseMovement.West | IsohypseMovement.East | IsohypseMovement.North | IsohypseMovement.South,
                                        x, y);
                            }
                        }
                        else
                        {
                            int maskedFlags = flags.GetValue (x, y) & 0x3;
                            if (maskedFlags != 0 && maskedFlags != 0x3)
                                movements.SetValue (movements.GetValue (x, y) | IsohypseMovement.North, x, y);

                            maskedFlags = flags.GetValue (x, y) & 0x6;
                            if (maskedFlags != 0 && maskedFlags != 0x6)
                                movements.SetValue (movements.GetValue (x, y) | IsohypseMovement.East, x, y);

                            maskedFlags = flags.GetValue (x, y) & 0xc;
                            if (maskedFlags != 0 && maskedFlags != 0xc)
                                movements.SetValue (movements.GetValue (x, y) | IsohypseMovement.South, x, y);

                            maskedFlags = flags.GetValue (x, y) & 0x9;
                            if (maskedFlags != 0 && maskedFlags != 0x9)
                                movements.SetValue (movements.GetValue (x, y) | IsohypseMovement.West, x, y);
                        }

                        //if (visualizer != null)
                        //{
                        //    if (cellCharacteristic > 0 && cellCharacteristic < 4)
                        //    {
                        //        GeoPosition geoPos = dem.GetGeoPosition (x + 0.5, y + 0.5);

                        //        if (movements.GetValue (x, y)
                        //            == (IsohypseMovement.West | IsohypseMovement.East | IsohypseMovement.North | IsohypseMovement.South))
                        //            visualizer.DrawText ("blue", geoPos.Longitude, geoPos.Latitude,
                        //                "X");

                        //        //visualizer.DrawText ("blue", geoPos.Longitude, geoPos.Latitude,
                        //        //    cellCharacteristic == 2 ? "K" : "T");

                        //        //List<Point2> points = new List<Point2> ();

                        //        //if ((movements[x, y] & IsohypseMovement.North) != 0)
                        //        //    points.Add (new Point2 (0.5, 0));
                        //        //if ((movements[x, y] & IsohypseMovement.East) != 0)
                        //        //    points.Add (new Point2 (1, 0.5));
                        //        //if ((movements[x, y] & IsohypseMovement.South) != 0)
                        //        //    points.Add (new Point2 (0.5, 1));
                        //        //if ((movements[x, y] & IsohypseMovement.West) != 0)
                        //        //    points.Add (new Point2 (0, 0.5));

                        //        //List<GeoPosition> geoPoints = new List<GeoPosition> ();

                        //        //foreach (Point2 point in points)
                        //        //    geoPoints.Add (dem.GetGeoPosition (x + point.X, y + point.Y));

                        //        //visualizer.DrawLine ("black", geoPoints[0].Longitude, geoPoints[0].Latitude,
                        //        //    geoPoints[1].Longitude, geoPoints[1].Latitude);

                        //        //if (geoPoints.Count > 2)
                        //        //    visualizer.DrawLine ("black", geoPoints[2].Longitude, geoPoints[2].Latitude,
                        //        //        geoPoints[3].Longitude, geoPoints[3].Latitude);
                        //    }
                        //}
                    }
                }

                for (int x = 0; x < dem.LonLength - 1; x++)
                {
                    for (int y = 0; y < dem.LatLength - 1; y++)
                    {
                        if (movements.GetValue (x, y) != IsohypseMovement.None)
                        {
                            IsohypseMovements isohypseMovements = ExtractIsohypseMovements (dem, isoElev, movements, flags, x, y);

                            Polyline isohypseSegment = ConstructIsohypseSegment (dem, isohypseMovements);

                            isohypseSegment.RemoveDuplicateVertices ();

                            isohypse.AddSegment (isohypseSegment);

                            activityLogger.Log (ActivityLogLevel.Verbose, String.Format (System.Globalization.CultureInfo.InvariantCulture,
                                "Found segment with {0} vertices", isohypseSegment.VerticesCount));
                        }
                    }
                }

                if (isohypse.Segments.Count > 0)
                    callback (isohypse);
   
                    //isoColl.AddIsohypse (isohypse);
            }
        }

        public IsohypseCollection Isoplete (IRasterDigitalElevationModel dem, double elevationStep)
        {
            IsohypseCollection isoColl = new IsohypseCollection ();

            Isoplete (dem, elevationStep, delegate (Isohypse isohypse)
            {
                isoColl.AddIsohypse (isohypse);
            });

            //if (visualizer != null)
            //{
            //    string[] segmentColors = new string[] { "red", "violet", "DarkRed" };

            //    int segmentCount = 0;
            //    foreach (Isohypse isohypse in isoColl.Isohypses.Values)
            //    {
            //        foreach (Polyline segment in isohypse.Segments)
            //        {
            //            segmentCount++;
            //            for (int i = 0; i < segment.Vertices.Count; i++)
            //            {
            //                // if the segment is not closed and we have reached the end, break out
            //                if (false == segment.IsClosed && i == segment.Vertices.Count - 1)
            //                    break;

            //                Point3<double> vertexA = segment.Vertices[i];
            //                Point3<double> vertexB = segment.Vertices[(i + 1) % segment.Vertices.Count];

            //                visualizer.DrawLine ("brown", vertexA.X, vertexA.Y,
            //                    vertexB.X, vertexB.Y);
            //                //visualizer.DrawLine (segmentColors[segmentCount % (segmentColors.Length)], vertexA.X, vertexA.Y,
            //                //    vertexB.X, vertexB.Y);
            //                //visualizer.DrawPoint ("black", vertexA.X, vertexA.Y);
            //            }
            //        }
            //    }
            //}

            return isoColl;
        }

        [SuppressMessage ("Microsoft.Performance", "CA1822:MarkMembersAsStatic")]
        private IsohypseMovements ExtractIsohypseMovements (
            IRasterDigitalElevationModel dem, 
            double isohypseElevation, 
            Array2<IsohypseMovement> movements,
            Array2<byte> flags,
            int startingX, 
            int startingY)
        {
            IsohypseMovements isohypseMovements = new IsohypseMovements (isohypseElevation);

            isohypseMovements.StartingX = startingX;
            isohypseMovements.StartingY = startingY;

            int x = isohypseMovements.StartingX;
            int y = isohypseMovements.StartingY;
            int lastX = 0, lastY = 0;
            IsohypseMovement lastMovement = IsohypseMovement.None;

            bool foundOneEnd = false;

            while (true)
            {
                // if we reached the end of the grid
                if (x < 0 || y < 0 || x >= dem.LonLength - 1 || y >= dem.LatLength - 1)
                {
                    // have we already found one end of the segment?
                    if (false == foundOneEnd)
                    {
                        // we haven't...
                        foundOneEnd = true;

                        // ... so reverse the segment and start moving from the other end
                        int oldStartingX = isohypseMovements.StartingX;
                        int oldStartingY = isohypseMovements.StartingY;

                        isohypseMovements.ReverseIsohypseMovements (x, y);

                        x = oldStartingX;
                        y = oldStartingY;
                    }
                    else
                        // we have, so we can exit
                        break;
                }

                if (x == isohypseMovements.StartingX 
                    && y == isohypseMovements.StartingY 
                    && isohypseMovements.Movements.Count > 0)
                {
                    // we found the starting point, this is a closed polygon
                    isohypseMovements.IsClosed = true;
                    break;
                }

                IsohypseMovement currentCellMovement = movements.GetValue (x, y);

                bool movementFound = false;

                for (int i = 0; i < 4; i++)
                {
                    IsohypseMovement movementConsidered = (IsohypseMovement)(1 << i);

                    if (0 != (currentCellMovement & movementConsidered))
                    {
                        int nextX = x, nextY = y;
                        switch (movementConsidered)
                        {
                            case IsohypseMovement.East:
                                nextX++;
                                break;
                            case IsohypseMovement.North:
                                nextY--;
                                break;
                            case IsohypseMovement.South:
                                nextY++;
                                break;
                            case IsohypseMovement.West:
                                nextX--;
                                break;
                            default:
                                throw new NotImplementedException ();
                        }

                        // find the opposite movement of the last movement
                        IsohypseMovement oppositeMovement = IsohypseMovements.GetOppositeMovement (lastMovement);

                        if (lastMovement != IsohypseMovement.None)
                        {
                            // check that we haven't moved back
                            if (nextX == lastX && nextY == lastY)
                                continue;

                            // special case when all movements are possible
                            if ((((int)currentCellMovement) & 0xf) == 0xf)
                            {
                                IsohypseMovement movementCombination = movementConsidered | oppositeMovement;

                                // check that we haven't moved in such a way as to make the polygon cross itself
                                if ((movementCombination & (IsohypseMovement.North | IsohypseMovement.South))
                                    == (IsohypseMovement.North | IsohypseMovement.South))
                                    continue;

                                // check that we haven't moved in such a way as to make the polygon cross itself
                                if ((movementCombination & (IsohypseMovement.West | IsohypseMovement.East))
                                    == (IsohypseMovement.West | IsohypseMovement.East))
                                    continue;

                                // check that the movement is the right one in regards to cells elevations
                                if (flags.GetValue (x, y) == 5)
                                {
                                    if (movementCombination == (IsohypseMovement.South | IsohypseMovement.East)
                                        || movementCombination == (IsohypseMovement.North | IsohypseMovement.West))
                                        continue;
                                }
                                else if (flags.GetValue (x, y) == 10)
                                {
                                    if (movementCombination == (IsohypseMovement.South | IsohypseMovement.West)
                                        || movementCombination == (IsohypseMovement.North | IsohypseMovement.East))
                                        continue;
                                }
                                else
                                    throw new NotImplementedException ();
                            }
                        }

                        isohypseMovements.Movements.Add (movementConsidered);

                        // remove the used movement from the array
                        movements.SetValue (movements.GetValue (x, y) & ~movementConsidered, x, y);

                        // remove this opposite movement from the array
                        movements.SetValue (movements.GetValue (x, y) & ~oppositeMovement, x, y);

                        lastX = x;
                        lastY = y;
                        lastMovement = movementConsidered;
                        x = nextX;
                        y = nextY;

                        movementFound = true;

                        break;
                    }
                }

                if (movementFound == false)
                    throw new NotImplementedException ();
            }

            return isohypseMovements;
        }

        [SuppressMessage ("Microsoft.Performance", "CA1822:MarkMembersAsStatic")]
        private Polyline ConstructIsohypseSegment (IRasterDigitalElevationModel dem, IsohypseMovements isohypseMovements)
        {
            Polyline polyline = new Polyline ();
            polyline.IsClosed = isohypseMovements.IsClosed;

            int x = isohypseMovements.StartingX;
            int y = isohypseMovements.StartingY;

            bool checkedOrientation = false;
            bool shouldBeReversed = false;

            foreach (IsohypseMovement movement in isohypseMovements.Movements)
            {
                int dx1 = 0, dx2 = 0, dy1 = 0, dy2 = 0;

                switch (movement)
                {
                    case IsohypseMovement.East:
                        x++;
                        dx1 = dx2 = x;
                        dy1 = y;
                        dy2 = y + 1;
                        break;
                    case IsohypseMovement.North:
                        dx1 = x;
                        dx2 = x + 1;
                        dy1 = dy2 = y;
                        y--;
                        break;
                    case IsohypseMovement.South:
                        y++;
                        dx1 = x + 1;
                        dx2 = x;
                        dy1 = dy2 = y;
                        break;
                    case IsohypseMovement.West:
                        dx1 = dx2 = x;
                        dy1 = y + 1;
                        dy2 = y;
                        x--;
                        break;
                }

                double elevation1 = dem.GetElevationForDataPoint (dx1, dy1);
                double elevation2 = dem.GetElevationForDataPoint (dx2, dy2);

                // check the orientation of the isohypse
                if (false == checkedOrientation)
                {
                    // the right-side elevation should be higher
                    if (elevation2 < elevation1)
                        shouldBeReversed = true;
                }

                double factor = (isohypseMovements.IsohypseElevation - elevation1) / (elevation2 - elevation1);
                double ix, iy;
                ix = (factor * (dx2 - dx1) + dx1);
                iy = (factor * (dy2 - dy1) + dy1);
                GeoPosition geoPos = dem.GetGeoPosition (ix, iy);
                Point3<double> isohypseVertex = new Point3<double> (geoPos.Longitude, geoPos.Latitude, isohypseMovements.IsohypseElevation);

                polyline.AddVertex (isohypseVertex);
            }

            // now reverse the polyline if needed
            if (shouldBeReversed)
                polyline.Reverse ();

            return polyline;
        }

        private class IsohypseMovements
        {
            public double IsohypseElevation
            {
                get { return isohypseElevation; }
            }

            public List<IsohypseMovement> Movements
            {
                get { return movements; }
            }

            public int StartingY
            {
                get { return startingY; }
                set { startingY = value; }
            }

            public int StartingX
            {
                get { return startingX; }
                set { startingX = value; }
            }

            public bool IsClosed
            {
                get { return isClosed; }
                set { isClosed = value; }
            }

            public IsohypseMovements (double isohypseElevation)
            {
                this.isohypseElevation = isohypseElevation;
            }

            static public IsohypseMovement GetOppositeMovement (IsohypseMovement movement)
            {
                switch (movement)
                {
                    case IsohypseMovement.East:
                        return IsohypseMovement.West;
                    case IsohypseMovement.North:
                        return IsohypseMovement.South;
                    case IsohypseMovement.South:
                        return IsohypseMovement.North;
                    case IsohypseMovement.West:
                        return IsohypseMovement.East;
                    default:
                        return IsohypseMovement.None;
                }
            }

            public void ReverseIsohypseMovements (int newStartingX, int newStartingY)
            {
                movements.Reverse ();

                for (int i = 0; i < movements.Count; i++)
                    movements[i] = GetOppositeMovement (movements[i]);

                startingX = newStartingX;
                startingY = newStartingY;
            }

            private double isohypseElevation;
            private List<IsohypseMovement> movements = new List<IsohypseMovement> ();
            private bool isClosed;
            private int startingX, startingY;
        }

        private IActivityLogger activityLogger = new ConsoleActivityLogger ();
        //private IVisualizer visualizer;

        [SuppressMessage ("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        static private readonly ILog log = LogManager.GetLogger (typeof (Igor4IsopletingAlgorithm));
    }
}
