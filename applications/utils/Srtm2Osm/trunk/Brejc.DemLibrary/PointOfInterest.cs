using System;
using System.Collections.Generic;
using System.Text;

namespace Brejc.DemLibrary
{
    [Serializable]
    public class PointOfInterest : ICloneable
    {
        public GeoPosition Position
        {
            get { return position; }
            set { position = value; }
        }

        public string Text
        {
            get { return text; }
            set { text = value; }
        }

        public PointOfInterest () { }

        public PointOfInterest (GeoPosition position, string text)
        {
            this.position = position;
            this.text = text;
        }

        #region ICloneable Members

        public object Clone ()
        {
            PointOfInterest clone = new PointOfInterest ();

            clone.position = (GeoPosition)position.Clone ();
            clone.text = text;

            return clone;
        }

        #endregion
                
        private GeoPosition position;
        private string text;
    }
}
