using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Represents an user entity in the OSM database. All created objects are stored in an internal cache.
    /// </summary>
    public class OsmUser : ICloneable
    {
        /// <summary>
        /// Name of the user.
        /// </summary>
        public string Name
        {
            get { return name; }
        }

        /// <summary>
        /// ID of the user.
        /// </summary>
        public int Id
        {
            get { return id; }
        }

        private OsmUser (string name, int id)
        {
            if (String.IsNullOrEmpty (name))
                throw new ArgumentException("String is null or empty.", "name");

            if (id < 1)
                throw new ArgumentOutOfRangeException("id", "Value is smaller than one.");

            this.name = name;
            this.id = id;
        }

        /// <summary>
        /// Fetch a OsmUser object from the internal cache. If an object with the given data
        /// is not found in the cache, it will be created, saved in the cache and returned.
        /// </summary>
        /// <param name="name">Name of the user.</param>
        /// <param name="id">ID of the user.</param>
        /// <returns>OsmUser object containing the given user data.</returns>
        public static OsmUser Fetch (string name, int id)
        {
            if (userPool.ContainsKey (id))
                return userPool[id];
            else
            {
                userPool.Add (id, new OsmUser (name, id));
                return Fetch (name, id);
            }
        }

        private readonly string name;
        private readonly int id;

        private static Dictionary<int, OsmUser> userPool = new Dictionary<int, OsmUser>();

        public object Clone ()
        {
            return new OsmUser (this.name, this.id);
        }
    }
}
