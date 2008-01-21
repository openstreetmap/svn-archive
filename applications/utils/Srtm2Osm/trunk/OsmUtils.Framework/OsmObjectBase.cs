using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    public enum OsmObjectAction
    {
        None,
        Delete,
        Modify,
    }

    public abstract class OsmObjectBase
    {
        public int ObjectId
        {
            get { return objectId; }
        }

        public DateTime Timestamp
        {
            get { return timestamp; }
            set { timestamp = value; }
        }

        public string User
        {
            get { return user; }
            set { user = value; }
        }

        public bool Visible
        {
            get { return visible; }
            set { visible = value; }
        }

        public OsmObjectAction Action
        {
            get { return action; }
            set { action = value; }
        }

        public bool HasTags
        {
            get { return tags.Count > 0; }
        }

        public IDictionary<string, OsmTag> Tags { get { return tags; } }

        protected OsmObjectBase (int objectId)
        {
            this.objectId = objectId;
        }

        public string GetTagValue (string tagKey)
        {
            return tags[tagKey].Value;
        }

        public void SetTag (string tagKey, string tagValue)
        {
            tags[tagKey] = new OsmTag (tagKey, tagValue);
        }

        public bool HasTag (string tagKey)
        {
            return tags.ContainsKey (tagKey);
        }

        /// <summary>
        /// Determines whether the OSM object has the specified tag with the specified value.
        /// </summary>
        /// <param name="tagKey">The tag key.</param>
        /// <param name="tagValue">The tag value.</param>
        /// <returns>
        /// 	<c>true</c> if OSM object has the specified tag with the specified value; otherwise, <c>false</c>.
        /// </returns>
        public bool HasTag (string tagKey, string tagValue)
        {
            return tags.ContainsKey (tagKey) && tags [tagKey].Value == tagValue;
        }

        public IEnumerable<OsmTag> EnumerateTags ()
        {
            return tags.Values;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </returns>
        public override string ToString ()
        {
            StringBuilder formattedTags = new StringBuilder ();

            string comma = String.Empty;
            foreach (OsmTag tag in EnumerateTags ())
            {
                formattedTags.AppendFormat (System.Globalization.CultureInfo.InvariantCulture,
                    "{2}'{0}'='{1}'", tag.Key, tag.Value, comma);
                comma = ", ";
            }

            return String.Format (System.Globalization.CultureInfo.InvariantCulture,
                "id={0}, tags=({1})", objectId, formattedTags);
        }

        public virtual void CopyToClone (OsmObjectBase clone)
        {
            clone.objectId = objectId;
            clone.timestamp = timestamp;
            if (user != null)
                clone.user = (string)user.Clone ();
            else
                clone.user = null;
            clone.visible = visible;
            clone.action = action;

            foreach (string key in tags.Keys)
            {
                string cloneKey = (string)key.Clone ();
                OsmTag cloneValue = (OsmTag) tags[key].Clone ();
                clone.tags[cloneKey] = cloneValue;
            }
        }

        private int objectId;
        private DateTime timestamp;
        private string user;
        private bool visible;
        private OsmObjectAction action;
        private Dictionary<string, OsmTag> tags = new Dictionary<string, OsmTag> ();
    }
}
