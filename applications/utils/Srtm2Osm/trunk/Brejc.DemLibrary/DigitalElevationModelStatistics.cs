using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    public class DigitalElevationModelStatistics
    {
        public double MinElevation
        {
            get { return minElevation; }
            set { minElevation = value; }
        }

        public double MaxElevation
        {
            get { return maxElevation; }
            set { maxElevation = value; }
        }

        public bool HasMissingPoints
        {
            get { return hasMissingPoints; }
            set { hasMissingPoints = value; }
        }

        private double minElevation = double.MaxValue;
        private double maxElevation = double.MinValue;
        private bool hasMissingPoints;
    }
}
