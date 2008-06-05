using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.Common.Xml
{
    /// <summary>
    /// A fluent interface wrapper for the <see cref="XmlNodeList"/> class.
    /// </summary>
    [SuppressMessage ("Microsoft.Naming", "CA1710:IdentifiersShouldHaveCorrectSuffix")]
    public class XmlFluentNodeList : IEnumerable<XmlFluentNode>
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentNodeList"/> class
        /// using a specified XML document and an <see cref="XmlNodeList"/> class to be wrapped.
        /// </summary>
        /// <param name="xmlDocument">The XML document.</param>
        /// <param name="xmlNodeList">The XML node list to be wrapped.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentNodeList (XmlDocument xmlDocument, XmlNodeList xmlNodeList)
        {
            this.xmlDocument = xmlDocument;
            this.xmlNodeList = xmlNodeList;
        }

        /// <summary>
        /// Returns an enumerator that iterates through the collection.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.Collections.Generic.IEnumerator`1"></see> that can be used to iterate through the collection.
        /// </returns>
        public IEnumerator<XmlFluentNode> GetEnumerator ()
        {
            return new XmlFluentNodeEnumerator (xmlDocument, xmlNodeList);
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator ()
        {
            return new XmlFluentNodeEnumerator (xmlDocument, xmlNodeList);
        }

        private XmlDocument xmlDocument;
        private XmlNodeList xmlNodeList;
    }
}
