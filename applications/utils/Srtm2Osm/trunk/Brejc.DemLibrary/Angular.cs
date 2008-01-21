using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    [Serializable]
    public struct Angular : IComparable
    {
        public double Angle
        {
            get { return angle; }
            set { angle = value; }
        }

        public double AngleInRadians
        {
            get { return angle * Math.PI / 180d; }
            set { angle = value * 180d / Math.PI; }
        }

        public int Degrees
        {
            get
            {
                return (int)angle;
            }
            set
            {
                SetDms (value, Minutes, Seconds);
            }
        }

        public int Minutes
        {
            get
            {
                return (int)Math.Abs((angle - Degrees) * 60);
            }
            set
            {
                if (value < 0 || value >= 60)
                    throw new ArgumentOutOfRangeException ("value", value, "Minutes can be an integer value from 0 to 59.");

                SetDms (Degrees, value, Seconds);
            }
        }

        public double Seconds
        {
            get
            {
                return Math.Abs (Math.Round ((angle * 3600) % 60, 4));
            }
            set
            {
                if (value < 0 || value >= 60)
                    throw new ArgumentOutOfRangeException ("value", value, "Seconds can be a double value from 0 to 60.");

                SetDms (Degrees, Minutes, value);
            }
        }

        public int DegreesCeiling
        {
            get
            {
                return (int)Math.Ceiling (Angle);
            }
        }

        public double Cos
        {
            get
            {
                return Math.Cos (AngleInRadians);
            }
        }

        public double Sin
        {
            get
            {
                return Math.Sin (AngleInRadians);
            }
        }

        public int Sign
        {
            get
            {
                return Math.Sign (Degrees);
            }
        }

        public Angular (int degrees, int minutes, double seconds)
        {
            angle = 0;
            SetDms (degrees, minutes, seconds);
        }

        public Angular (Angular a)
        {
            angle = a.angle;
        }

        public Angular (double angle)
        {
            this.angle = angle;
        }

        public void SetDms (int degrees, int minutes, double seconds)
        {
            angle = Angular.FromDms (degrees, minutes, seconds);
        }

        public Angular AddDegrees (int add)
        {
            Angular a = new Angular (this);
            a.angle += add;
            return a;
        }

        public Angular AddMinutes (int minutes)
        {
            Angular a = new Angular (this);
            a.angle += minutes / 60d;
            return a;
        }

        public Angular AddSeconds (double seconds)
        {
            Angular a = new Angular (this);
            a.angle += seconds / 3600d;
            return a;
        }

        static public double FromDms (int degrees, int minutes, double seconds)
        {
            double absValue = (Math.Abs (degrees) + minutes / 60d + seconds / 3600d);
            if (degrees < 0)
                return -absValue;
            return absValue;
        }

        static public int GetDegrees (double angle)
        {
            return (int)angle;
        }

        static public int GetDegreesCeiling (double angle)
        {
            return (int)Math.Ceiling (angle);
        }

        static public int GetDegreesFloor (double angle)
        {
            return (int)Math.Floor (angle);
        }

        static public int GetMinutes (double angle)
        {
            return (int)((angle - GetDegrees (angle)) * 60);
        }

        static public double GetSeconds (double angle)
        {
            return Math.Round ((angle * 3600) % 60, 4);
        }

        static public Angular operator - (Angular a)
        {
            return new Angular (-a.Angle);
        }

        static public Angular operator + (Angular a, Angular b)
        {
            return new Angular (a.Angle + b.Angle);
        }

        static public Angular operator - (Angular a, Angular b)
        {
            return new Angular (a.Angle - b.Angle);
        }

        static public Angular operator / (Angular a, int div)
        {
            return new Angular (0, 0, (a.Degrees * 3600.0 + a.Minutes * 60 + a.Seconds) / div);
        }

        static public Angular Negate (Angular a)
        {
            return new Angular (-a.Angle);
        }

        static public Angular Add (Angular a, Angular b)
        {
            return new Angular (a.Angle + b.Angle);
        }

        static public Angular Subtract (Angular a, Angular b)
        {
            return new Angular (a.Angle - b.Angle);
        }

        static public Angular Divide (Angular a, int div)
        {
            return new Angular (0, 0, (a.Degrees * 3600.0 + a.Minutes * 60 + a.Seconds) / div);
        }

        static public bool operator == (Angular a, Angular b) { return a.CompareTo (b) == 0; }
        static public bool operator != (Angular a, Angular b) { return a.CompareTo (b) != 0; }
        static public bool operator > (Angular a, Angular b) { return a.CompareTo (b) > 0; }
        static public bool operator >= (Angular a, Angular b) { return a.CompareTo (b) >= 0; }
        static public bool operator < (Angular a, Angular b) { return a.CompareTo (b) < 0; }
        static public bool operator <= (Angular a, Angular b) { return a.CompareTo (b) <= 0; }

        static public Angular Max (Angular a, Angular b)
        {
            if (a >= b)
                return a;
            return b;
        }

        static public Angular Min (Angular a, Angular b)
        {
            if (a <= b)
                return a;
            return b;
        }

        #region IComparable Members

        public int CompareTo (object obj)
        {
            if (obj == null)
                return 1;

            if (false == obj is Angular)
                throw new ArgumentException ("Not of Angular type.");

            Angular b = (Angular)obj;

            return Math.Round (angle, 10).CompareTo (Math.Round (b.angle, 10));
        }

        #endregion

        public override bool Equals (object obj)
        {
            return CompareTo (obj) == 0;
        }

        public override int GetHashCode ()
        {
            return angle.GetHashCode ();
        }

        public override string ToString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "{0}\u00b0{1}'{2}\"", Degrees, Math.Abs (Minutes), Math.Abs (Seconds));
        }

        public string ToLatitudeString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "{0}{1}", angle >= 0 ? 'N' : 'S', new Angular (Math.Abs (angle)).ToString ());
        }

        public string ToLongitudeString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "{0}{1}", angle >= 0 ? 'E' : 'W', new Angular (Math.Abs (angle)).ToString ());
        }

        private double angle;
    }
}
