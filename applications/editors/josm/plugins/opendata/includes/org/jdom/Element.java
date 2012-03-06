/*--

 $Id: Element.java,v 1.159 2007/11/14 05:02:08 jhunter Exp $

 Copyright (C) 2000-2007 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

package org.jdom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jdom.filter.ElementFilter;

/**
 * An XML element. Methods allow the user to get and manipulate its child
 * elements and content, directly access the element's textual content,
 * manipulate its attributes, and manage namespaces.
 *
 * @version $Revision: 1.159 $, $Date: 2007/11/14 05:02:08 $
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Lucas Gonze
 * @author  Kevin Regan
 * @author  Dan Schaffer
 * @author  Yusuf Goolamabbas
 * @author  Kent C. Johnson
 * @author  Jools Enticknap
 * @author  Alex Rosen
 * @author  Bradley S. Huffman
 * @author  Victor Toni
 */
public class Element extends Content implements Parent {

    private static final int INITIAL_ARRAY_SIZE = 5;

    /** The local name of the element */
    protected String name;

    /** The namespace of the element */
    protected transient Namespace namespace;

    /** Additional namespace declarations to store on this element; useful
     * during output */
    protected transient List additionalNamespaces;

    // See http://lists.denveronline.net/lists/jdom-interest/2000-September/003030.html
    // for a possible memory optimization here (using a RootElement subclass)

    /**
     *  The attributes of the element.  Subclassers have to
     * track attributes using their own mechanism.
     */
    AttributeList attributes = new AttributeList(this);

    /**
     * The content of the element.  Subclassers have to
     * track content using their own mechanism.
     */
    ContentList content = new ContentList(this);

    /**
     * This protected constructor is provided in order to support an Element
     * subclass that wants full control over variable initialization. It
     * intentionally leaves all instance variables null, allowing a lightweight
     * subclass implementation. The subclass is responsible for ensuring all the
     * get and set methods on Element behave as documented.
     * <p>
     * When implementing an Element subclass which doesn't require full control
     * over variable initialization, be aware that simply calling super() (or
     * letting the compiler add the implicit super() call) will not initialize
     * the instance variables which will cause many of the methods to throw a
     * NullPointerException. Therefore, the constructor for these subclasses
     * should call one of the public constructors so variable initialization is
     * handled automatically.
     */
    protected Element() { }

    /**
     * Creates a new element with the supplied (local) name and namespace. If
     * the provided namespace is null, the element will have no namespace.
     *
     * @param  name                 local name of the element
     * @param  namespace            namespace for the element
     * @throws IllegalNameException if the given name is illegal as an element
     *                              name
     */
    public Element(final String name, final Namespace namespace) {
        setName(name);
        setNamespace(namespace);
    }

    /**
     * Returns the (local) name of the element (without any namespace prefix).
     *
     * @return                     local element name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the (local) name of the element.
     *
     * @param  name                 the new (local) name of the element
     * @return                      the target element
     * @throws IllegalNameException if the given name is illegal as an Element
     *                              name
     */
    public Element setName(final String name) {
        final String reason = Verifier.checkElementName(name);
        if (reason != null) {
            throw new IllegalNameException(name, "element", reason);
        }
        this.name = name;
        return this;
    }

    /**
     * Returns the element's {@link Namespace}.
     *
     * @return                     the element's namespace
     */
    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Sets the element's {@link Namespace}. If the provided namespace is null,
     * the element will have no namespace.
     *
     * @param  namespace           the new namespace
     * @return                     the target element
     */
    public Element setNamespace(Namespace namespace) {
        if (namespace == null) {
            namespace = Namespace.NO_NAMESPACE;
        }
		String reason = Verifier.checkNamespaceCollision(namespace, 
				getAdditionalNamespaces());
		if (reason != null) {
			throw new IllegalAddException(this, namespace, reason);
		}
		for (Iterator it = getAttributes().iterator(); it.hasNext();) {
			Attribute a = (Attribute)it.next();
			reason = Verifier.checkNamespaceCollision(namespace, a);
			if (reason != null) {
				throw new IllegalAddException(this, namespace, reason);
			}
		}

        this.namespace = namespace;
        return this;
    }

    /**
     * Returns the namespace prefix of the element or an empty string if none
     * exists.
     *
     * @return                     the namespace prefix
     */
    public String getNamespacePrefix() {
        return namespace.getPrefix();
    }

    /**
     * Returns the namespace URI mapped to this element's prefix (or the
     * in-scope default namespace URI if no prefix). If no mapping is found, an
     * empty string is returned.
     *
     * @return                     the namespace URI for this element
     */
    public String getNamespaceURI() {
        return namespace.getURI();
    }

    /**
     * Returns the {@link Namespace} corresponding to the given prefix in scope
     * for this element. This involves searching up the tree, so the results
     * depend on the current location of the element. Returns null if there is
     * no namespace in scope with the given prefix at this point in the
     * document.
     *
     * @param  prefix              namespace prefix to look up
     * @return                     the Namespace for this prefix at this
     *                             location, or null if none
     */
    public Namespace getNamespace(final String prefix) {
        if (prefix == null) {
            return null;
        }

        if ("xml".equals(prefix)) {
            // Namespace "xml" is always bound.
            return Namespace.XML_NAMESPACE;
        }

        // Check if the prefix is the prefix for this element
        if (prefix.equals(getNamespacePrefix())) {
            return getNamespace();
        }

        // Scan the additional namespaces
        if (additionalNamespaces != null) {
            for (int i = 0; i < additionalNamespaces.size(); i++) {
                final Namespace ns = (Namespace) additionalNamespaces.get(i);
                if (prefix.equals(ns.getPrefix())) {
                    return ns;
                }
            }
        }
        
        if (attributes != null) {
        	for (Iterator it = attributes.iterator(); it.hasNext();) {
        		Attribute a = (Attribute)it.next();
        		if (prefix.equals(a.getNamespacePrefix())) {
        			return a.getNamespace();
        		}
        	}
        }

        // If we still don't have a match, ask the parent
        if (parent instanceof Element) {
            return ((Element)parent).getNamespace(prefix);
        }

        return null;
    }

    /**
     * Returns the full name of the element, in the form
     * [namespacePrefix]:[localName]. If the element does not have a namespace
     * prefix, then the local name is returned.
     *
     * @return                     qualified name of the element (including
     *                             namespace prefix)
     */
    public String getQualifiedName() {
        // Note: Any changes here should be reflected in
        // XMLOutputter.printQualifiedName()
        if ("".equals(namespace.getPrefix())) {
            return getName();
        }

        return new StringBuffer(namespace.getPrefix())
            .append(':')
            .append(name)
            .toString();
    }

    /**
     * Adds a namespace declarations to this element. This should <i>not</i> be
     * used to add the declaration for this element itself; that should be
     * assigned in the construction of the element. Instead, this is for adding
     * namespace declarations on the element not relating directly to itself.
     * It's used during output to for stylistic reasons move namespace
     * declarations higher in the tree than they would have to be.
     *
     * @param  additionalNamespace namespace to add
     * @throws IllegalAddException if the namespace prefix collides with another
     *                             namespace prefix on the element
     */
    public void addNamespaceDeclaration(final Namespace additionalNamespace) {

        // Verify the new namespace prefix doesn't collide with another
        // declared namespace, an attribute prefix, or this element's prefix
        final String reason = Verifier.checkNamespaceCollision(additionalNamespace, this);
        if (reason != null) {
            throw new IllegalAddException(this, additionalNamespace, reason);
        }

        if (additionalNamespaces == null) {
            additionalNamespaces = new ArrayList(INITIAL_ARRAY_SIZE);
        }

        additionalNamespaces.add(additionalNamespace);
    }

    /**
     * Returns a list of the additional namespace declarations on this element.
     * This includes only additional namespace, not the namespace of the element
     * itself, which can be obtained through {@link #getNamespace()}. If there
     * are no additional declarations, this returns an empty list. Note, the
     * returned list is unmodifiable.
     *
     * @return                     a List of the additional namespace
     *                             declarations
     */
    public List getAdditionalNamespaces() {
        // Not having the returned list be live allows us to avoid creating a
        // new list object when XMLOutputter calls this method on an element
        // with an empty list.
        if (additionalNamespaces == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(additionalNamespaces);
    }

    /**
     * Returns the XPath 1.0 string value of this element, which is the
     * complete, ordered content of all text node descendants of this element
     * (i&#46;e&#46; the text that's left after all references are resolved
     * and all other markup is stripped out.)
     *
     * @return a concatentation of all text node descendants
     */
    public String getValue() {
        final StringBuffer buffer = new StringBuffer();

        final Iterator iter = getContent().iterator();
        while (iter.hasNext()) {
            final Content child = (Content) iter.next();
            if (child instanceof Element || child instanceof Text) {
                buffer.append(child.getValue());
            }
        }
        return buffer.toString();
    }

//    private int indexOf(int start, Filter filter) {
//        int size = getContentSize();
//        for (int i = start; i < size; i++) {
//            if (filter.matches(getContent(i))) {
//                return i;
//            }
//        }
//        return -1;
//    }


    /**
     * Returns the textual content directly held under this element as a string.
     * This includes all text within this single element, including whitespace
     * and CDATA sections if they exist. It's essentially the concatenation of
     * all {@link Text} and {@link CDATA} nodes returned by {@link #getContent}.
     * The call does not recurse into child elements. If no textual value exists
     * for the element, an empty string is returned.
     *
     * @return                     text content for this element, or empty
     *                             string if none
     */
    public String getText() {
        if (content.size() == 0) {
            return "";
        }

        // If we hold only a Text or CDATA, return it directly
        if (content.size() == 1) {
            final Object obj = content.get(0);
            if (obj instanceof Text) {
                return ((Text) obj).getText();
            }
            else {
                return "";
            }
        }

        // Else build String up
        final StringBuffer textContent = new StringBuffer();
        boolean hasText = false;

        for (int i = 0; i < content.size(); i++) {
            final Object obj = content.get(i);
            if (obj instanceof Text) {
                textContent.append(((Text) obj).getText());
                hasText = true;
            }
        }

        if (!hasText) {
            return "";
        }
        else {
            return textContent.toString();
        }
    }

    /**
     * Returns the textual content of this element with all surrounding
     * whitespace removed. If no textual value exists for the element, or if
     * only whitespace exists, the empty string is returned.
     *
     * @return                     trimmed text content for this element, or
     *                             empty string if none
     */
    public String getTextTrim() {
        return getText().trim();
    }

    /**
     * Returns the textual content of the named child element, or null if
     * there's no such child. This method is a convenience because calling
     * <code>getChild().getText()</code> can throw a NullPointerException.
     *
     * @param  name                the name of the child
     * @return                     text content for the named child, or null if
     *                             no such child
     */
    public String getChildText(final String name) {
        final Element child = getChild(name);
        if (child == null) {
            return null;
        }
        return child.getText();
    }

    /**
     * Sets the content of the element to be the text given. All existing text
     * content and non-text context is removed. If this element should have both
     * textual content and nested elements, use <code>{@link #setContent}</code>
     * instead. Setting a null text value is equivalent to setting an empty
     * string value.
     *
     * @param  text                 new text content for the element
     * @return                      the target element
     * @throws IllegalDataException if the assigned text contains an illegal
     *                              character such as a vertical tab (as
     *                              determined by {@link
     *                              org.jdom.Verifier#checkCharacterData})
     */
    public Element setText(final String text) {
        content.clear();

        if (text != null) {
            addContent(new Text(text));
        }

        return this;
    }

    /**
     * This returns the full content of the element as a List which
     * may contain objects of type <code>Text</code>, <code>Element</code>,
     * <code>Comment</code>, <code>ProcessingInstruction</code>,
     * <code>CDATA</code>, and <code>EntityRef</code>.
     * The List returned is "live" in document order and modifications
     * to it affect the element's actual contents.  Whitespace content is
     * returned in its entirety.
     *
     * <p>
     * Sequential traversal through the List is best done with an Iterator
     * since the underlying implement of List.size() may require walking the
     * entire list.
     * </p>
     *
     * @return a <code>List</code> containing the mixed content of the
     *         element: may contain <code>Text</code>,
     *         <code>{@link Element}</code>, <code>{@link Comment}</code>,
     *         <code>{@link ProcessingInstruction}</code>,
     *         <code>{@link CDATA}</code>, and
     *         <code>{@link EntityRef}</code> objects.
     */
    public List getContent() {
        return content;
    }

    /**
     * Appends the child to the end of the element's content list.
     *
     * @param child   child to append to end of content list
     * @return        the element on which the method was called
     * @throws IllegalAddException if the given child already has a parent.     */
    public Element addContent(final Content child) {
        content.add(child);
        return this;
    }

//    public Content getChild(Filter filter) {
//        int i = indexOf(0, filter);
//        return (i < 0) ? null : getContent(i);
//    }

    public boolean removeContent(final Content child) {
        return content.remove(child);
    }

    /**
     * Determines if this element is the ancestor of another element.
     *
     * @param element <code>Element</code> to check against
     * @return <code>true</code> if this element is the ancestor of the
     *         supplied element
     */
    public boolean isAncestor(final Element element) {
        Parent p = element.getParent();
        while (p instanceof Element) {
            if (p == this) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * <p>
     * This returns the complete set of attributes for this element, as a
     * <code>List</code> of <code>Attribute</code> objects in no particular
     * order, or an empty list if there are none.
     * The returned list is "live" and changes to it affect the
     * element's actual attributes.
     * </p>
     *
     * @return attributes for the element
     */
    public List getAttributes() {
        return attributes;
    }

    /**
     * <p>
     * This returns the attribute for this element with the given name
     * and within the given Namespace, or null if no such attribute exists.
     * </p>
     *
     * @param name name of the attribute to return
     * @param ns <code>Namespace</code> to search within
     * @return attribute for the element
     */
    public Attribute getAttribute(final String name, final Namespace ns) {
        return (Attribute) attributes.get(name, ns);
    }

    /**
     * <p>
     * This returns the attribute value for the attribute with the given name
     * and within the given Namespace, null if there is no such attribute, and
     * the empty string if the attribute value is empty.
     * </p>
     *
     * @param name name of the attribute whose valud is to be returned
     * @param ns <code>Namespace</code> to search within
     * @return the named attribute's value, or null if no such attribute
     */
    public String getAttributeValue(final String name, final Namespace ns) {
        return getAttributeValue(name, ns, null);
    }

    /**
     * <p>
     * This returns the attribute value for the attribute with the given name
     * and within the given Namespace, or the passed-in default if there is no
     * such attribute.
     * </p>
     *
     * @param name name of the attribute whose valud is to be returned
     * @param ns <code>Namespace</code> to search within
     * @param def a default value to return if the attribute does not exist
     * @return the named attribute's value, or the default if no such attribute
     */
    public String getAttributeValue(final String name, final Namespace ns, final String def) {
        final Attribute attribute = (Attribute) attributes.get(name, ns);
        if (attribute == null) {
            return def;
        }

        return attribute.getValue();
    }

    /**
     * <p>
     * This sets an attribute value for this element.  Any existing attribute
     * with the same name and namespace URI is removed.
     * </p>
     *
     * @param name name of the attribute to set
     * @param value value of the attribute to set
     * @param ns namespace of the attribute to set
     * @return this element modified
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name, or if the namespace is an unprefixed default
     *         namespace
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         {@link org.jdom.Verifier#checkCharacterData}).
     * @throws IllegalAddException if the attribute namespace prefix
     *         collides with another namespace prefix on the element.
     */
    public Element setAttribute(final String name, final String value, final Namespace ns) {
        final Attribute attribute = getAttribute(name, ns);
        if (attribute == null) {
            final Attribute newAttribute = new Attribute(name, value, ns);
            setAttribute(newAttribute);
        } else {
            attribute.setValue(value);
        }

        return this;
    }

    /**
     * <p>
     * This sets an attribute value for this element.  Any existing attribute
     * with the same name and namespace URI is removed.
     * </p>
     *
     * @param attribute <code>Attribute</code> to set
     * @return this element modified
     * @throws IllegalAddException if the attribute being added already has a
     *   parent or if the attribute namespace prefix collides with another
     *   namespace prefix on the element.
     */
    public Element setAttribute(final Attribute attribute) {
        attributes.add(attribute);
        return this;
    }

    /**
     * <p>
     * This removes the attribute with the given name and within the
     * given Namespace.  If no such attribute exists, this method does
     * nothing.
     * </p>
     *
     * @param name name of attribute to remove
     * @param ns namespace URI of attribute to remove
     * @return whether the attribute was removed
     */
    public boolean removeAttribute(final String name, final Namespace ns) {
        return attributes.remove(name, ns);
    }

    /**
     * <p>
     * This removes the supplied Attribute should it exist.
     * </p>
     *
     * @param attribute Reference to the attribute to be removed.
     * @return whether the attribute was removed
     */
    public boolean removeAttribute(final Attribute attribute) {
        return attributes.remove(attribute);
    }

    /**
     * <p>
     *  This returns a <code>String</code> representation of the
     *    <code>Element</code>, suitable for debugging. If the XML
     *    representation of the <code>Element</code> is desired,
     *    {@link org.jdom.output.XMLOutputter#outputString(Element)}
     *    should be used.
     * </p>
     *
     * @return <code>String</code> - information about the
     *         <code>Element</code>
     */
    public String toString() {
        final StringBuffer stringForm = new StringBuffer(64)
            .append("[Element: <")
            .append(getQualifiedName());

        final String nsuri = getNamespaceURI();
        if (!"".equals(nsuri)) {
            stringForm
            .append(" [Namespace: ")
            .append(nsuri)
            .append("]");
        }
        stringForm.append("/>]");

        return stringForm.toString();
    }

    /**
     * <p>
     *  This returns a deep clone of this element.
     *  The new element is detached from its parent, and getParent()
     *  on the clone will return null.
     * </p>
     *
     * @return the clone of this element
     */
   public Object clone() {

       // Ken Rune Helland <kenh@csc.no> is our local clone() guru

       final Element element = (Element) super.clone();

       // name and namespace are references to immutable objects
       // so super.clone() handles them ok

       // Reference to parent is copied by super.clone()
       // (Object.clone()) so we have to remove it
       // Actually, super is a Content, which has already detached in the
       // clone().
       // element.parent = null;

       // Reference to content list and attribute lists are copyed by
       // super.clone() so we set it new lists if the original had lists
       element.content = new ContentList(element);
       element.attributes = new AttributeList(element);

       // Cloning attributes
       if (attributes != null) {
           for(int i = 0; i < attributes.size(); i++) {
               final Attribute attribute = (Attribute) attributes.get(i);
               element.attributes.add(attribute.clone());
           }
       }

       // Cloning additional namespaces
       if (additionalNamespaces != null) {
           element.additionalNamespaces = new ArrayList(additionalNamespaces);
       }

       // Cloning content
       if (content != null) {
           for(int i = 0; i < content.size(); i++) {
               final Content c = (Content) content.get(i);
               element.content.add(c.clone());
           }
       }

       return element;
   }


    // Support a custom Namespace serialization so no two namespace
    // object instances may exist for the same prefix/uri pair
    private void writeObject(final ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        // We use writeObject() and not writeUTF() to minimize space
        // This allows for writing pointers to already written strings
        out.writeObject(namespace.getPrefix());
        out.writeObject(namespace.getURI());

        if (additionalNamespaces == null) {
            out.write(0);
        }
        else {
            final int size = additionalNamespaces.size();
            out.write(size);
            for (int i = 0; i < size; i++) {
                final Namespace additional = (Namespace) additionalNamespaces.get(i);
                out.writeObject(additional.getPrefix());
                out.writeObject(additional.getURI());
            }
        }
    }

    private void readObject(final ObjectInputStream in)
        throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        namespace = Namespace.getNamespace(
            (String)in.readObject(), (String)in.readObject());

        final int size = in.read();

        if (size != 0) {
            additionalNamespaces = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                final Namespace additional = Namespace.getNamespace(
                    (String)in.readObject(), (String)in.readObject());
                additionalNamespaces.add(additional);
            }
        }
    }

    /**
     * Returns an iterator that walks over all descendants in document order.
     *
     * @return an iterator to walk descendants
     */
    public Iterator getDescendants() {
        return new DescendantIterator(this);
    }

    /**
     * This returns a <code>List</code> of all the child elements
     * nested directly (one level deep) within this element, as
     * <code>Element</code> objects.  If this target element has no nested
     * elements, an empty List is returned.  The returned list is "live"
     * in document order and changes to it affect the element's actual
     * contents.
     *
     * <p>
     * Sequential traversal through the List is best done with a Iterator
     * since the underlying implement of List.size() may not be the most
     * efficient.
     * </p>
     *
     * <p>
     * No recursion is performed, so elements nested two levels deep
     * would have to be obtained with:
     * <pre>
     * <code>
     *   Iterator itr = (currentElement.getChildren()).iterator();
     *   while(itr.hasNext()) {
     *     Element oneLevelDeep = (Element)itr.next();
     *     List twoLevelsDeep = oneLevelDeep.getChildren();
     *     // Do something with these children
     *   }
     * </code>
     * </pre>
     * </p>
     *
     * @return list of child <code>Element</code> objects for this element
     */
    public List getChildren() {
        return content.getView(new ElementFilter());
    }

    /**
     * This returns a <code>List</code> of all the child elements
     * nested directly (one level deep) within this element with the given
     * local name and belonging to the given Namespace, returned as
     * <code>Element</code> objects.  If this target element has no nested
     * elements with the given name in the given Namespace, an empty List
     * is returned.  The returned list is "live" in document order
     * and changes to it affect the element's actual contents.
     * <p>
     * Please see the notes for <code>{@link #getChildren}</code>
     * for a code example.
     * </p>
     *
     * @param name local name for the children to match
     * @param ns <code>Namespace</code> to search within
     * @return all matching child elements
     */
    public List getChildren(final String name, final Namespace ns) {
        return content.getView(new ElementFilter(name, ns));
    }

    /**
     * This returns the first child element within this element with the
     * given local name and belonging to the given namespace.
     * If no elements exist for the specified name and namespace, null is
     * returned.
     *
     * @param name local name of child element to match
     * @param ns <code>Namespace</code> to search within
     * @return the first matching child element, or null if not found
     */
    public Element getChild(final String name, final Namespace ns) {
        final List elements = content.getView(new ElementFilter(name, ns));
        final Iterator iter = elements.iterator();
        if (iter.hasNext()) {
            return (Element) iter.next();
        }
        return null;
    }

    /**
     * This returns the first child element within this element with the
     * given local name and belonging to no namespace.
     * If no elements exist for the specified name and namespace, null is
     * returned.
     *
     * @param name local name of child element to match
     * @return the first matching child element, or null if not found
     */
    public Element getChild(final String name) {
        return getChild(name, Namespace.NO_NAMESPACE);
    }
}
