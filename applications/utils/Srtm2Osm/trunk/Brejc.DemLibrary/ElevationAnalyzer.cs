using System;
using System.Collections.Generic;
using System.Text;
using Brejc.Geometry;

namespace Brejc.DemLibrary
{
    public sealed class ElevationAnalyzer
    {
        static public IList<PointOfInterest> FindPeaks (IRasterDigitalElevationModel dem, int howMany)
        {
            IList<PointOfInterest> peaks = new List<PointOfInterest> ();

            Point2<int>[] neighbourPoints = { new Point2<int> (-1, 0), new Point2<int> (1, 0), new Point2<int> (0,-1),
                new Point2<int> (0,1)};

            for (int x = 1; x < dem.LonLength - 1; x++)
            {
                for (int y = 1; y < dem.LatLength - 1; y++)
                {
                    double elevation = dem.GetElevationForDataPoint (x, y);

                    // first check if it is a peak
                    bool isPeak = true;
                    for (int i = 0; i < neighbourPoints.Length; i++)
                    {
                        if (elevation <= dem.GetElevationForDataPoint ((int)(x + neighbourPoints[i].X), (int)(y + neighbourPoints[i].Y)))
                        {
                            isPeak = false;
                            break;
                        }
                    }

                    if (isPeak)
                    {
                        for (int i = 0; i < peaks.Count + 1; i++)
                        {
                            bool addPeak = false;

                            if (i == peaks.Count)
                                addPeak = true;
                            else
                            {
                                PointOfInterest peak = peaks[i] as PointOfInterest;

                                if (elevation > peak.Position.Elevation)
                                    addPeak = true;
                            }

                            if (addPeak)
                            {
                                PointOfInterest newPeak = new PointOfInterest (dem.GetGeoPosition (x, y),
                                    String.Format (System.Globalization.CultureInfo.InvariantCulture, "Peak {0}", elevation));
                                peaks.Insert (i, newPeak);
                                if (peaks.Count > howMany)
                                    peaks.RemoveAt (peaks.Count - 1);
                                break;
                            }
                        }
                    }
                }
            }

            return peaks;
        }

        private ElevationAnalyzer () { }

    }
}
