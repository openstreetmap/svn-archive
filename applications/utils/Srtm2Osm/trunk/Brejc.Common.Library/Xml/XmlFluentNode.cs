using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.Common.Xml
{
    /// <summary>
    /// Represents an XML node with the fluent interface.
    /// </summary>
    public class XmlFluentNode
    {
        /// <summary>
        /// Gets the XML document to which this instance.
        /// </summary>
        /// <value>The XML document.</value>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlDocument Document { get { return xmlDocument; } }

        /// <summary>
        /// Gets the inner text of this XML node instance.
        /// </summary>
        /// <value>The inner text.</value>
        public string InnerText { get { return xmlNode.InnerText; } }

        /// <summary>
        /// Gets the inner XML of this XML node instance.
        /// </summary>
        /// <value>The inner XML.</value>
        public string InnerXml { get { return xmlNode.InnerXml; } }

        public string Name { get { return xmlNode.Name; } }

        public XmlFluentNode Up
        {
            get { return new XmlFluentNode (this.Document, this.xmlNode.ParentNode); }
        }

        public XmlNode XmlNode { get { return xmlNode; } }

        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentNode"/> class
        /// using a specified <see cref="XmlDocument"/>.
        /// </summary>
        /// <param name="xmlDoc">The XML document.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentNode (XmlDocument xmlDoc)
        {
            xmlDocument = xmlDoc;
            xmlNode = xmlDoc;
        }

        public XmlFluentNode (XmlNode xmlNode)
        {
            this.xmlNode = xmlNode;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentNode"/> class
        /// using a specified <see cref="XmlNode"/> as the original node and a
        /// specified <see cref="XmlNode"/> as the node which will be represented by this
        /// instance.
        /// </summary>
        /// <param name="originalNode">The original node.</param>
        /// <param name="xmlNode">The XML node which will be represented by this instance.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentNode (XmlFluentNode originalNode, XmlNode xmlNode)
        {
            this.xmlDocument = originalNode.Document;
            this.xmlNode = xmlNode;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentNode"/> class using
        /// a specified <see cref="XmlDocument"/> object and a
        /// specified <see cref="XmlNode"/> as the node which will be represented by this
        /// instance.
        /// </summary>
        /// <param name="xmlDocument">The XML document.</param>
        /// <param name="xmlNode">The XML node which will be represented by this instance.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentNode (XmlDocument xmlDocument, XmlNode xmlNode)
        {
            this.xmlDocument = xmlDocument;
            this.xmlNode = xmlNode;
        }

        /// <summary>
        /// Creates a new XML document and returns a <see cref="XmlFluentNode"/> object
        /// which points to it.
        /// </summary>
        /// <param name="documentElementName">The name of the document element.</param>
        /// <returns>A <see cref="XmlFluentNode"/> object which points to the new XML document.</returns>
        static public XmlFluentNode CreateDocument (string documentElementName)
        {
            XmlDocument xmlDoc = new XmlDocument ();
            XmlElement xmlElement = xmlDoc.CreateElement (documentElementName);
            xmlDoc.AppendChild (xmlElement);
            return new XmlFluentNode (xmlDoc, xmlElement);
        }

        /// <summary>
        /// Determines whether there exists an XML node specified by the XPath expression.
        /// </summary>
        /// <param name="xpath">The XPath expression.</param>
        /// <returns><c>true</c> if the XML node exists; <c>false</c> otherwise.</returns>
        public bool Exists (string xpath)
        {
            return xmlNode.SelectSingleNode (xpath) != null;
        }

        /// <summary>
        /// Selects a single node specified by the XPath expression and returns a
        /// <see cref="XmlFluentNode"/> object which represents it.
        /// </summary>
        /// <param name="xpath">The XPath expression.</param>
        /// <returns>A <see cref="XmlFluentNode"/> object.</returns>
        /// <exception cref="ArgumentException">Node specified by the XPath expression does not exist.</exception>
        public XmlFluentNode Node (string xpath)
        {
            if (false == Exists (xpath))
                throw new ArgumentException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "Node '{0}' is missing", xpath));

            return new XmlFluentNode (this, xmlNode.SelectSingleNode (xpath));
        }

        /// <summary>
        /// Selects a list of nodes matching the specified XPath expression and returns a
        /// <see cref="XmlFluentNodeList"/> object which contains those nodes.
        /// </summary>
        /// <param name="xpath">The XPath expression.</param>
        /// <returns>A <see cref="XmlFluentNodeList"/> object.</returns>
        public XmlFluentNodeList Nodes (string xpath)
        {
            return new XmlFluentNodeList (Document, xmlNode.SelectNodes (xpath));
        }

        /// <summary>
        /// Returns a <see cref="XmlFluentAttribute"/> object which represents 
        /// an XML attribute specified by its name.
        /// </summary>
        /// <param name="attributeName">The name of the attribute.</param>
        /// <returns>A <see cref="XmlFluentAttribute"/> object.</returns>
        public XmlFluentAttribute Attribute (string attributeName)
        {
            XmlAttribute xmlAttribute = xmlNode.Attributes[attributeName];
            return new XmlFluentAttribute (xmlAttribute, attributeName);
        }

        /// <summary>
        /// Adds the a new child XML node to the XML node represented by this instance.
        /// </summary>
        /// <param name="nodeName">The name of the node to be added.</param>
        /// <returns>A <see cref="XmlFluentNode"/> object which represents a newly added XML node.</returns>
        public XmlFluentNode AddNode (string nodeName)
        {
            XmlElement xmlElement = xmlDocument.CreateElement (nodeName);
            xmlNode.AppendChild (xmlElement);
            return new XmlFluentNode (this, xmlElement);
        }

        /// <summary>
        /// Adds the a new child XML node to the XML node represented by this instance.
        /// </summary>
        /// <param name="nodeName">The name of the node to be added.</param>
        /// <param name="nodeValue">The value of the node to be added.</param>
        /// <returns>A <see cref="XmlFluentNode"/> object which represents a newly added XML node.</returns>
        public XmlFluentNode AddNode (string nodeName, string nodeValue)
        {
            XmlElement xmlElement = xmlDocument.CreateElement (nodeName);
            xmlElement.InnerText = nodeValue;
            xmlNode.AppendChild (xmlElement);

            return new XmlFluentNode (this, xmlElement);
        }

        public XmlFluentNode AddNodeIfNotNull (string nodeName, object nodeValue)
        {
            if (nodeValue != null)
                AddNode (nodeName, nodeValue.ToString ());

            return this;
        }

        /// <summary>
        /// Adds a new attribute to the XML node represented by this instance.
        /// </summary>
        /// <param name="attributeName">The name of the attribute to be added.</param>
        /// <param name="attributeValue">The string value of the attribute to be added.</param>
        /// <returns>This instance.</returns>
        public XmlFluentNode AddAttribute (string attributeName, string attributeValue)
        {
            if (lastCheckValue == true)
            {
                XmlAttribute xmlAttrib = xmlDocument.CreateAttribute (attributeName);
                xmlAttrib.Value = attributeValue;
                xmlNode.Attributes.Append (xmlAttrib);
            }

            lastCheckValue = true;
            return this;
        }

        /// <summary>
        /// Adds a new attribute to the XML node represented by this instance.
        /// </summary>
        /// <param name="attributeName">The name of the attribute to be added.</param>
        /// <param name="attributeValue">The integer value of the attribute to be added.</param>
        /// <returns>This instance.</returns>
        public XmlFluentNode AddAttribute (string attributeName, int attributeValue)
        {
            return AddAttribute (attributeName, attributeValue.ToString (System.Globalization.CultureInfo.InvariantCulture));
        }

        /// <summary>
        /// Adds a new attribute to the XML node represented by this instance.
        /// </summary>
        /// <param name="attributeName">The name of the attribute to be added.</param>
        /// <param name="attributeValue">The <see cref="Double"/> value of the attribute to be added.</param>
        /// <returns>This instance.</returns>
        public XmlFluentNode AddAttribute (string attributeName, double attributeValue)
        {
            return AddAttribute (attributeName, attributeValue.ToString (System.Globalization.CultureInfo.InvariantCulture));
        }

        /// <summary>
        /// Adds a new attribute to the XML node represented by this instance.
        /// </summary>
        /// <param name="attributeName">The name of the attribute to be added.</param>
        /// <param name="attributeValue">The <see cref="bool"/> value of the attribute to be added.</param>
        /// <returns>This instance.</returns>
        public XmlFluentNode AddAttribute (string attributeName, bool attributeValue)
        {
            return AddAttribute (attributeName, attributeValue.ToString ());
        }

        /// <summary>
        /// If the specified argument has a value of <c>true</c>, the next <c>AddAttribute</c> operation
        /// will be performed; otherwise, it will be ignored.
        /// </summary>
        /// <param name="check">An argument which determines the behavior of the object.</param>
        /// <returns>This instance.</returns>
        public XmlFluentNode If (bool check)
        {
            lastCheckValue = check;
            return this;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override string ToString ()
        {
            return xmlNode.OuterXml;
        }

        private XmlDocument xmlDocument;
        private XmlNode xmlNode;
        private bool lastCheckValue = true;
    }
}
