using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    [Serializable]
    public class OsmElementSelectorException : System.Exception
    {
        public OsmElementSelectorException () { }
        public OsmElementSelectorException (string message) : base (message) { }
        public OsmElementSelectorException (string message, Exception innerException) : base (message, innerException) { }

        protected OsmElementSelectorException (System.Runtime.Serialization.SerializationInfo serializationInfo,
            System.Runtime.Serialization.StreamingContext context)
            : base (serializationInfo, context) { }
    }
}
