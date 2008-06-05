using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.Common.Xml
{
    /// <summary>
    /// Represents an XML attribute with the fluent interface.
    /// </summary>
    [SuppressMessage ("Microsoft.Naming", "CA1711:IdentifiersShouldNotHaveIncorrectSuffix")]
    public class XmlFluentAttribute
    {
        /// <summary>
        /// Gets the value of the attribute as string.
        /// </summary>
        /// <value>The value of the attribute.</value>
        /// <exception cref="ArgumentException">The attribute does not exist.</exception>
        public string String
        {
            get
            {
                AssertExists ();
                return xmlAttribute.Value;
            }
        }

        /// <summary>
        /// Gets the value of the attribute as <see cref="Int32"/>.
        /// </summary>
        /// <value>The value of the attribute.</value>
        /// <exception cref="ArgumentException">The attribute does not exist.</exception>
        [SuppressMessage ("Microsoft.Naming", "CA1720:IdentifiersShouldNotContainTypeNames", MessageId = "int")]
        public Int32 Int
        {
            get
            {
                AssertExists ();
                return Int32.Parse (xmlAttribute.Value,
                     System.Globalization.CultureInfo.InvariantCulture);
            }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentAttribute"/> class using a specified 
        /// <see cref="XmlAttribute"/> and the attribute's name.
        /// </summary>
        /// <param name="xmlAttribute">The XML attribute object. Can be <c>null</c>.</param>
        /// <param name="attributeName">Name of the attribute.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentAttribute (XmlAttribute xmlAttribute, string attributeName)
        {
            this.xmlAttribute = xmlAttribute;
            this.attributeName = attributeName;
        }

        /// <summary>
        /// Returns the value of the attribute cast as <see cref="bool"/> or the specified default value
        /// if the attribute does not exist.
        /// </summary>
        /// <param name="defaultValue">The default value to be returned by this method if the attribute does not exist.</param>
        /// <returns>The value.</returns>
        [SuppressMessage ("Microsoft.Naming", "CA1720:IdentifiersShouldNotContainTypeNames", MessageId = "bool")]
        public bool BoolWithDefault (bool defaultValue)
        {
            if (xmlAttribute == null)
                return defaultValue;

            return bool.Parse (xmlAttribute.Value);
        }

        /// <summary>
        /// Returns the value of the attribute cast as <see cref="Int32"/> or the specified default value
        /// if the attribute does not exist.
        /// </summary>
        /// <param name="defaultValue">The default value to be returned by this method if the attribute does not exist.</param>
        /// <returns>The value.</returns>
        [SuppressMessage ("Microsoft.Naming", "CA1720:IdentifiersShouldNotContainTypeNames", MessageId = "int")]
        public Int32 IntWithDefault (Int32 defaultValue)
        {
            if (xmlAttribute == null)
                return defaultValue;

            return Int32.Parse (xmlAttribute.Value,
                System.Globalization.CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Returns the value of the attribute cast as <see cref="Double"/> or the specified default value
        /// if the attribute does not exist.
        /// </summary>
        /// <param name="defaultValue">The default value to be returned by this method if the attribute does not exist.</param>
        /// <returns>The value.</returns>
        public double DoubleWithDefault (double defaultValue)
        {
            if (xmlAttribute == null)
                return defaultValue;

            return double.Parse (xmlAttribute.Value,
                System.Globalization.CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Returns the value of the attribute or the specified default value
        /// if the attribute does not exist.
        /// </summary>
        /// <typeparam name="T">The type of the value.</typeparam>
        /// <param name="defaultValue">The default value to be returned by this method if the attribute does not exist.</param>
        /// <returns>The value.</returns>
        public T ValueWithDefault<T> (T defaultValue)
        {
            if (xmlAttribute == null)
                return defaultValue;

            return (T)Convert.ChangeType (xmlAttribute.Value, typeof (T),
                System.Globalization.CultureInfo.InvariantCulture);
        }

        private void AssertExists ()
        {
            if (xmlAttribute == null)
                throw new ArgumentException (String.Format (System.Globalization.CultureInfo.InvariantCulture,
                    "Attribute '{0}' is missing", attributeName));
        }

        private XmlAttribute xmlAttribute;
        private string attributeName;
    }
}
