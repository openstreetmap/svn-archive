using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    public interface IDemProcessor
    {
        IRasterDigitalElevationModel Process (IRasterDigitalElevationModel input);
    }

    public class MissingElevationDataInterpolator : IDemProcessor
    {
        public IRasterDigitalElevationModel Process (IRasterDigitalElevationModel input)
        {
            // clone data
            IRasterDigitalElevationModel output = input.Clone () as IRasterDigitalElevationModel;

            SortedList<MissingElevation, Point2<double>> missingElevations = new SortedList<MissingElevation, Point2<double>> ();

            // find all missing elevations
            for (int x = 0; x < output.LonLength; x++)
            {
                for (int y = 0; y < output.LatLength; y++)
                {
                    double elevation = output.GetElevationForDataPoint (x, y);
                    if (elevation == double.MinValue)
                    {
                        MissingElevation me = new MissingElevation ();
                        me.Lng = x;
                        me.Lat = y;

                        // now calculate missing neighbours count
                        CalculateNeighbours (output, me);

                        // now add it to the list
                        missingElevations.Add (me, new Point2<double> (me.Lng, me.Lat));
                    }
                }
            }

            int startingMissingElevations = missingElevations.Count;

            // while there are missing elevations
            while (missingElevations.Count > 0)
            {
                // use first missing elevation
                MissingElevation me = missingElevations.Keys[0];

                // based on the neighbouring non-missing data, calculate interpolated elevation
                double interpolatedElevation = CalculateNeighbours (output, me);

                // enter this elevation into the output ElevationData
                output.SetElevationForDataPoint (me.Lng, me.Lat, interpolatedElevation);

                // remove this elevation from missing elevation list
                missingElevations.Remove (me);

                // for each neighbour which is also missing elevation:
                // decrease the missingNeighbours counter
                // and reposition the neighbour in the missing elevations list

                Point2<double>[] neighbourPoints = { new Point2<double> (me.Lng - 1, me.Lat), 
                    new Point2<double> (me.Lng + 1, me.Lat), new Point2<double> (me.Lng, me.Lat-1),
                    new Point2<double> (me.Lng, me.Lat + 1)};
                for (int i = 0; i < 4; i++)
                {
                    Point2<double> p = neighbourPoints[i];
                    if (missingElevations.ContainsValue (p))
                    {
                        MissingElevation me2 = missingElevations.Keys[missingElevations.IndexOfValue (p)];
                        missingElevations.Remove (me2);
                        me2.MissingNeighbours--;
                        missingElevations.Add (me2, p);
                    }
                }
            }

            return output;
        }

        static private double CalculateNeighbours (IRasterDigitalElevationModel dem, MissingElevation me)
        {
            double interpolatedElevation = 0;
            me.MissingNeighbours = 0;

            if (me.Lng > 0)
            {
                double elevation = dem.GetElevationForDataPoint (me.Lng - 1, me.Lat);
                if (elevation == Int16.MinValue)
                    me.MissingNeighbours++;
                else
                    interpolatedElevation += elevation;
            }
            else
                me.MissingNeighbours++;

            if (me.Lng < dem.LonLength - 1)
            {
                double elevation = dem.GetElevationForDataPoint (me.Lng + 1, me.Lat);
                if (elevation == Int16.MinValue)
                    me.MissingNeighbours++;
                else
                    interpolatedElevation += elevation;
            }
            else
                me.MissingNeighbours++;

            if (me.Lat > 0)
            {
                double elevation = dem.GetElevationForDataPoint (me.Lng, me.Lat - 1);
                if (elevation == Int16.MinValue)
                    me.MissingNeighbours++;
                else
                    interpolatedElevation += elevation;
            }
            else
                me.MissingNeighbours++;

            if (me.Lat < dem.LatLength - 1)
            {
                double elevation = dem.GetElevationForDataPoint (me.Lng, me.Lat + 1);
                if (elevation == Int16.MinValue)
                    me.MissingNeighbours++;
                else
                    interpolatedElevation += elevation;
            }
            else
                me.MissingNeighbours++;

            if (me.MissingNeighbours < 4)
                interpolatedElevation /= (4 - me.MissingNeighbours);
            else
                interpolatedElevation = 0;

            return interpolatedElevation;
        }

        internal class MissingElevation : IComparable
        {
            public int Lat
            {
                get { return lat; }
                set { lat = value; }
            }

            public int Lng
            {
                get { return lng; }
                set { lng = value; }
            }

            public int MissingNeighbours
            {
                get { return missingNeighbours; }
                set { missingNeighbours = value; }
            }

            #region IComparable Members

            public int CompareTo (object obj)
            {
                MissingElevation me2 = (MissingElevation)obj;

                int c = missingNeighbours.CompareTo (me2.missingNeighbours);
                if (c != 0)
                    return c;
                c = lng.CompareTo (me2.lng);
                if (c != 0)
                    return c;
                return lat.CompareTo (me2.lat);
            }

            #endregion

            private int lng, lat;
            private int missingNeighbours;
        }
    }
}
