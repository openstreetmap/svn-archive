using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Represents a relation in the OSM database.
    /// </summary>
    public class OsmRelation : OsmObjectBase
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="OsmRelation"/> class with
        /// a specified ID.
        /// </summary>
        /// <param name="relationId">The relation ID.</param>
        public OsmRelation (int relationId)
            : base (relationId)
        {
        }

        /// <summary>
        /// Adds a new member to the relation.
        /// </summary>
        /// <param name="referenceType">Type of the referenced OSM object.</param>
        /// <param name="referenceId">The ID of the referenced OSM object.</param>
        /// <param name="role">The role of the relation's member.</param>
        /// <exception cref="ArgumentException">A member with the same key already exists in the relation.</exception>
        public void AddMember (OsmReferenceType referenceType, int referenceId, string role)
        {
            OsmRelationMember member = new OsmRelationMember (referenceType, referenceId, role);
            members.Add (member.MemberReference, member);
        }

        /// <summary>
        /// Enumerates all of the relation's members.
        /// </summary>
        /// <returns>Enumeration interface.</returns>
        public IEnumerable<OsmRelationMember> EnumerateMembers ()
        {
            return members.Values;
        }

        /// <summary>
        /// Returns a list of all relation's members which fulfill a specified role.
        /// </summary>
        /// <param name="p">The role which to look for.</param>
        /// <returns>A list of all relation's members references which fulfill a specified role.</returns>
        public IList<OsmRelationMemberReference> FindMembersWithRole (string role)
        {
            List<OsmRelationMemberReference> membersForRole = new List<OsmRelationMemberReference> ();

            foreach (OsmRelationMember member in members.Values)
            {
                if (member.HasRole (role))
                    membersForRole.Add (member.MemberReference);
            }

            return membersForRole;
        }

        /// <summary>
        /// Finds the first relation's members which fulfills a specified role.
        /// </summary>
        /// <param name="role">The role which to look for.</param>
        /// <returns><see cref="OsmRelationMemberReference"/> object pointing to the member, if found; otherwise <c>null</c>.</returns>
        public OsmRelationMemberReference FindFirstMemberWithRole (string role)
        {
            foreach (OsmRelationMember member in members.Values)
            {
                if (member.HasRole (role))
                    return member.MemberReference;
            }

            return null;
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
                "OsmRelation {0}", base.ToString());
        }

        private Dictionary<OsmRelationMemberReference, OsmRelationMember> members 
            = new Dictionary<OsmRelationMemberReference, OsmRelationMember> ();
    }
}
