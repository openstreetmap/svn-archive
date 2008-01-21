using System;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics.CodeAnalysis;

namespace OsmUtils.Framework
{
    /// <summary>
    /// Defines the logical operation used in <see cref="MultipleKeyValueSelector.IsMatch"/> to determine
    /// if the given OSM element is a match.
    /// </summary>
    public enum MultipleKeyValueSelectorOperation
    {
        /// <summary>
        /// None.
        /// </summary>
        None,
        /// <summary>
        /// Logical AND will be used. 
        /// The OSM element must match all of the tags defined in the <see cref="MultipleKeyValueSelector"/>.
        /// </summary>
        And,
        /// <summary>
        /// Logical OR will be used. 
        /// The OSM element must match one or more of the tags defined in the <see cref="MultipleKeyValueSelector"/>.
        /// </summary>
        Or,
    }

    /// <summary>
    /// OSM element selector which can have one or more tags. 
    /// Its behavior depends on the <see cref="MultipleKeyValueSelector.Operation"/> value.
    /// </summary>
    public class MultipleKeyValueSelector : IOsmElementSelector
    {
        /// <summary>
        /// Gets or sets the operation used in <see cref="MultipleKeyValueSelector.IsMatch"/> to determine
        /// if the given OSM element is a match.
        /// </summary>
        /// <value>The operation.</value>
        public MultipleKeyValueSelectorOperation Operation
        {
            get { return operation; }
            set { operation = value; }
        }

        /// <summary>
        /// Gets the array of OSM tags used by this selector in the matching operation.
        /// </summary>
        /// <value>The tags.</value>
        [SuppressMessage ("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays")]
        public OsmTag[] Tags
        {
            get { return tags; }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MultipleKeyValueSelector"/> class
        /// using a specified logical operation and an array of tags.
        /// </summary>
        /// <param name="operation">The logical operation to be used by this instance.</param>
        /// <param name="tags">The tags to used when matching.</param>
        public MultipleKeyValueSelector (MultipleKeyValueSelectorOperation operation,
            OsmTag[] tags)
        {
            this.operation = operation;
            this.tags = tags;
        }

        #region IOsmElementSelector Members

        /// <summary>
        /// Determines whether the specified OSM element is a match for this selector.
        /// </summary>
        /// <param name="element">The OSM element to check.</param>
        /// <returns>
        /// 	<c>true</c> if the specified element is a match; otherwise, <c>false</c>.
        /// </returns>
        public bool IsMatch (OsmObjectBase element)
        {
            foreach (OsmTag tag in tags)
            {
                bool isTagMatch = element.HasTag (tag.Key) && element.GetTagValue (tag.Key) == tag.Value;

                if (false == isTagMatch && operation == MultipleKeyValueSelectorOperation.And)
                    return false;

                if (true == isTagMatch && operation == MultipleKeyValueSelectorOperation.Or)
                    return true;
            }

            if (operation == MultipleKeyValueSelectorOperation.And)
                return true;

            return false;
        }

        #endregion

        private MultipleKeyValueSelectorOperation operation = MultipleKeyValueSelectorOperation.And;
        private OsmTag[] tags;
    }
}
