using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Represents a member of an OSM relation.
    /// </summary>
    public class OsmRelationMember
    {
        /// <summary>
        /// Gets the reference of this member.
        /// </summary>
        /// <value>The reference of this member.</value>
        public OsmRelationMemberReference MemberReference
        {
            get { return memberReference; }
        }

        /// <summary>
        /// Gets or sets the role of the relation's member. If no role exists, this
        /// property will have a <c>null</c> value.
        /// </summary>
        /// <value>The role of the relation's member.</value>
        public string Role
        {
            get { return role; }
            set { role = value; }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="OsmRelationMember"/> class
        /// with the specified member's reference.
        /// </summary>
        /// <param name="memberReference">The relation's member's reference.</param>
        public OsmRelationMember (OsmRelationMemberReference memberReference)
        {
            this.memberReference = memberReference;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="OsmRelationMember"/> class
        /// with the specified member's reference and role.
        /// </summary>
        /// <param name="memberReference">The relation's member's reference.</param>
        /// <param name="role">The role of the relation's member.</param>
        public OsmRelationMember (OsmRelationMemberReference memberReference,
            string role)
            : this (memberReference)
        {
            this.role = role;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="OsmRelationMember"/> class
        /// using a specified type of the OSM object to be referenced, its ID and its role.
        /// </summary>
        /// <param name="referenceType">Type of the referenced OSM object.</param>
        /// <param name="referenceId">The ID of the referenced OSM object.</param>
        /// <param name="role">The role of the relation's member.</param>
        public OsmRelationMember (OsmReferenceType referenceType,
            int referenceId,
            string role)
            : this (new OsmRelationMemberReference (referenceType, referenceId), role)
        {
        }

        /// <summary>
        /// Determines whether this relation's member fulfills a specified role.
        /// </summary>
        /// <param name="role">The role which to check.</param>
        /// <returns>
        /// 	<c>true</c> if this relation's member fulfills a specified role; otherwise, <c>false</c>.
        /// </returns>
        public bool HasRole (string role)
        {
            if (this.role == null)
                return false;

            return this.role == role;
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
                "{0}, role='{1}'", memberReference, role);
        }

        private OsmRelationMemberReference memberReference;
        private string role;
    }
}
