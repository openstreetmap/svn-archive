using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    public class OsmFeature
    {
        public IOsmElementSelector Selector
        {
            get { return selector; }
            set { selector = value; }
        }

        public OsmFeature (IOsmElementSelector selector)
        {
            this.selector = selector;
        }

        private IOsmElementSelector selector;
    }
}
