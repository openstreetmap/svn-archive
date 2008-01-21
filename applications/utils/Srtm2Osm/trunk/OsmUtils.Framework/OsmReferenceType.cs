using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Specifies the type of the OSM object that is being referenced.
    /// </summary>
    public enum OsmReferenceType
    {
        /// <summary>
        /// None
        /// </summary>
        None,
        /// <summary>
        /// Referenced type is an OSM node.
        /// </summary>
        Node,
        /// <summary>
        /// Referenced type is an OSM way.
        /// </summary>
        Way,
        /// <summary>
        /// Referenced type is an OSM relation.
        /// </summary>
        Relation,
    }
}
