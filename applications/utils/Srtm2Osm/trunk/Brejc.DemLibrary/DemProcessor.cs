using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using System.Diagnostics.CodeAnalysis;
using log4net;
using Brejc.DemLibrary.Shading;

namespace Brejc.DemLibrary
{
    public sealed class DemProcessor
    {
        static public Image GenerateShadedReliefImage (
            IRasterDigitalElevationModel dem,
            IShadingMethod shadingMethod,
            ShadingParameters shadingParameters)
        {
            Bitmap bitmap = new Bitmap (dem.LonLength, dem.LatLength);

            double[][] window = new double[3][] { new double[3], new double[3], new double[3] };

            double earthRadius = 6360000;
            double earthCircumference = earthRadius * 2 * Math.PI;
            double latSpacing = earthCircumference / (360 * dem.LatResolution);

            shadingMethod.Initialize (shadingParameters);

            for (int y = 1; y < dem.LatLength - 1; y++)
            {
                GeoPosition geoPos = dem.GetGeoPosition (0, y);
                double lonSpacing = earthCircumference / (360 * dem.LonResolution) * Math.Cos (geoPos.Latitude * Math.PI / 180.0);

                for (int x = 1; x < dem.LonLength - 1; x++)
                {
                    GetMovingWindow (dem, window, x, y);

                    double dzdx = ((window[0][0] + 2 * window[0][1] + window[0][2])
                        - (window[2][0] + 2 * window[2][1] + window[2][2]))
                        / (8 * lonSpacing);

                    if (double.IsNaN (dzdx))
                        continue;

                    double dzdy = ((window[0][0] + 2 * window[1][0] + window[2][0])
                        - (window[0][2] + 2 * window[1][2] + window[2][2]))
                        / (8 * latSpacing);

                    if (double.IsNaN (dzdy))
                        continue;

                    double riseRun = Math.Sqrt (dzdx * dzdx + dzdy * dzdy);
                    double slope = Math.PI / 2 - Math.Atan (riseRun);

                    double aspect = Math.Atan2 (dzdy, dzdx);

                    //double aspect = Math.Atan2 (window[1][0] - window[1][2],
                    //    window[0][1] - window[2][1]);

                    if (dzdx != 0 || dzdy != 0)
                    {
                        if (aspect == 0)
                            aspect = Math.PI * 2;
                    }
                    else
                    {
                        aspect = 0;
                    }

                    Color color = shadingMethod.CalculateColor (aspect, slope);

                    bitmap.SetPixel (x, bitmap.Height - y, color);
                }
            }

            return bitmap;
        }

        static private void GetMovingWindow (IRasterDigitalElevationModel dem, double[][] window, int x, int y)
        {
            for (int xi = -1; xi <= 1; xi++)
                for (int yi = -1; yi <= 1; yi++)
                    window[xi + 1][yi + 1] = dem.GetElevationForDataPoint (x + xi, y + yi);
        }

        private DemProcessor () { }

        [SuppressMessage ("Microsoft.Performance", "CA1823:AvoidUnusedPrivateFields")]
        static readonly private ILog log = LogManager.GetLogger (typeof (DemProcessor));
    }
}
