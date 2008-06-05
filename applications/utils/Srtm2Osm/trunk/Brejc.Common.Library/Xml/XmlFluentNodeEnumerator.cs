using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Collections;
using System.Diagnostics.CodeAnalysis;

namespace Brejc.Common.Xml
{
    /// <summary>
    /// Fluent interface enumerator for enumerating XML nodes.
    /// </summary>
    public class XmlFluentNodeEnumerator : IEnumerator<XmlFluentNode>
    {
        /// <summary>
        /// Gets the element in the collection at the current position of the enumerator.
        /// </summary>
        /// <value></value>
        /// <returns>The element in the collection at the current position of the enumerator.</returns>
        public XmlFluentNode Current
        {
            get { return new XmlFluentNode (xmlDocument, (XmlNode)(enumerator.Current)); }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="XmlFluentNodeEnumerator"/> class
        /// using a specified XML document and a <see cref="XmlNodeList"/> which should be enumerated.
        /// </summary>
        /// <param name="xmlDocument">The XML document.</param>
        /// <param name="xmlNodeList">The XML node list to be enumerated.</param>
        [SuppressMessage ("Microsoft.Design", "CA1059:MembersShouldNotExposeCertainConcreteTypes", MessageId = "System.Xml.XmlNode")]
        public XmlFluentNodeEnumerator (XmlDocument xmlDocument, XmlNodeList xmlNodeList)
        {
            this.xmlDocument = xmlDocument;
            enumerator = xmlNodeList.GetEnumerator ();
        }

        object System.Collections.IEnumerator.Current
        {
            get { return new XmlFluentNode (xmlDocument, (XmlNode)(enumerator.Current)); }
        }

        /// <summary>
        /// Advances the enumerator to the next element of the collection.
        /// </summary>
        /// <returns>
        /// true if the enumerator was successfully advanced to the next element; false if the enumerator has passed the end of the collection.
        /// </returns>
        /// <exception cref="T:System.InvalidOperationException">The collection was modified after the enumerator was created. </exception>
        public bool MoveNext ()
        {
            return enumerator.MoveNext ();
        }

        /// <summary>
        /// Sets the enumerator to its initial position, which is before the first element in the collection.
        /// </summary>
        /// <exception cref="T:System.InvalidOperationException">The collection was modified after the enumerator was created. </exception>
        public void Reset ()
        {
            enumerator.Reset ();
        }

        #region IDisposable Members

        /// <summary>
        /// Performs application-defined tasks associated with freeing, releasing, or
        /// resetting unmanaged resources.
        /// </summary>
        public void Dispose ()
        {
            Dispose (true);
            GC.SuppressFinalize (this);
        }

        /// <summary>
        /// Disposes the object.
        /// </summary>
        /// <param name="disposing">If <code>false</code>, cleans up native resources. 
        /// If <code>true</code> cleans up both managed and native resources</param>
        protected virtual void Dispose (bool disposing)
        {
            if (false == disposed)
            {
                if (disposing)
                {
                }

                disposed = true;
            }
        }

        private bool disposed;

        #endregion

        private XmlDocument xmlDocument;
        private IEnumerator enumerator;
    }
}
