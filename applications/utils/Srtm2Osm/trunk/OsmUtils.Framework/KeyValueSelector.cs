using System;
using System.Collections.Generic;
using System.Text;
using Ciloci.Flee;

namespace OsmUtils.Framework
{
    public class KeyValueSelector : IOsmElementSelector
    {
        public KeyValueSelector (OsmTag tag)
        {
            this.tag = tag;
        }

        #region IOsmElementSelector Members

        public bool IsMatch (OsmObjectBase element)
        {
            if (element.HasTag (tag.Key))
                return element.GetTagValue (tag.Key) == tag.Value;

            return false;
        }

        #endregion

        private OsmTag tag;
    }
}
