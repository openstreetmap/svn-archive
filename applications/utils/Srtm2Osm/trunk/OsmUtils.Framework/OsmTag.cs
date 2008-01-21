using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    public class OsmTag : ICloneable
    {
        public string Key
        {
            get { return key; }
        }

        public string Value
        {
            get { return this.value; }
        }

        public OsmTag (string key, string value)
        {
            if (key == null)
                throw new ArgumentNullException ("key");                

            if (value == null)
                throw new ArgumentNullException ("value");                

            this.key = key;
            this.value = value;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </returns>
        public override string ToString ()
        {
            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "{0} = {1}", key, value);
        }

        /// <summary>
        /// Compares the current <see cref="OsmTag"/> object to the specified object for equivalence.
        /// </summary>
        /// <param name="obj">The <see cref="OsmTag"/> object to test for equivalence with the current object.</param>
        /// <returns>
        /// <c>true</c> if the two <see cref="OsmTag"/> objects are equal; otherwise, <c>false</c>.
        /// </returns>
        public override bool Equals (object obj)
        {
            if (obj == null)
                return false;

            OsmTag that = obj as OsmTag;

            if (that == null)
                return false;

            return key.Equals (that.key) && value.Equals (that.value);
        }

        /// <summary>
        /// Returns the hash code for this <see cref="OsmTag"/> object.
        /// </summary>
        /// <returns>
        /// A 32-bit signed integer hash code.
        /// </returns>
        public override int GetHashCode ()
        {
            return key.GetHashCode () ^ value.GetHashCode ();
        }

        #region ICloneable Members

        public object Clone ()
        {
            string cloneKey = key;
            if (cloneKey != null)
                cloneKey = (string)cloneKey.Clone ();

            string cloneValue = value;
            if (cloneValue != null)
                cloneValue = (string)cloneValue.Clone ();

            return new OsmTag (cloneKey, cloneValue);
        }

        #endregion

        private string key;
        private string value;
    }
}
