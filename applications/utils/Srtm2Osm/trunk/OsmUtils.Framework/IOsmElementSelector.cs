using System;
using System.Collections.Generic;
using System.Text;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Represents a selector for OSM elements.
    /// </summary>
    public interface IOsmElementSelector
    {
        /// <summary>
        /// Determines whether the specified OSM element is a match for this selector.
        /// </summary>
        /// <param name="element">The OSM element to check.</param>
        /// <returns>
        /// 	<c>true</c> if the specified element is a match; otherwise, <c>false</c>.
        /// </returns>
        bool IsMatch (OsmObjectBase element);
    }
}
