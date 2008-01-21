using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Represents a reference to an OSM object. <see cref="OsmRelationMemberReference"/> is used
    /// in <see cref="OsmRelationMember"/> class to identify members of the relation.
    /// </summary>
    public class OsmRelationMemberReference
    {
        /// <summary>
        /// Gets the type of the referenced OSM object.
        /// </summary>
        /// <value>The type of the referenced OSM object.</value>
        public OsmReferenceType ReferenceType
        {
            get { return referenceType; }
        }

        /// <summary>
        /// Gets the ID of the referenced OSM object.
        /// </summary>
        /// <value>The ID of the referenced OSM object.</value>
        public int ReferenceId
        {
            get { return referenceId; }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="OsmRelationMemberReference"/> class
        /// using a specified type of the OSM object to be referenced and its ID.
        /// </summary>
        /// <param name="referenceType">Type of the referenced OSM object.</param>
        /// <param name="referenceId">The ID of the referenced OSM object.</param>
        public OsmRelationMemberReference (OsmReferenceType referenceType, int referenceId)
        {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
        }

        /// <summary>
        /// Compares the current <see cref="OsmRelationMemberReference"/> object to the specified object for equivalence.
        /// </summary>
        /// <param name="obj">The <see cref="OsmRelationMemberReference"/> object to test for equivalence with the current object.</param>
        /// <returns>
        /// <c>true</c> if the two <see cref="OsmRelationMemberReference"/> objects are equal; otherwise, <c>false</c>.
        /// </returns>
        public override bool Equals (object obj)
        {
            if (obj == null)
                return false;

            OsmRelationMemberReference that = obj as OsmRelationMemberReference;

            if (that == null)
                return false;

            return referenceType.Equals (that.referenceType) && referenceId.Equals (that.referenceId);
        }

        /// <summary>
        /// Returns the hash code for this <see cref="OsmRelationMemberReference"/> object.
        /// </summary>
        /// <returns>
        /// A 32-bit signed integer hash code.
        /// </returns>
        public override int GetHashCode ()
        {
            return referenceType.GetHashCode () ^ referenceId.GetHashCode ();
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
                "{0} (id={1})", referenceType, referenceId);
        }

        private OsmReferenceType referenceType;
        private int referenceId;
    }
}
