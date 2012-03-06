/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources;

import java.lang.reflect.Constructor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * Bridges to optional dependencies (especially {@code widget-swing} module).
 *
 * @todo Most methods of this class need to move as a {@code Trees} class in a {@code util} module.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/resources/OptionalDependencies.java $
 * @version $Id: OptionalDependencies.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class OptionalDependencies {
    /**
     * Constructor for {@link org.geotools.gui.swing.tree.NamedTreeNode}.
     */
    private static Constructor treeNodeConstructor;

    /**
     * Set to {@code true} if {@link #treeNodeConstructor} can't be obtained.
     */
    private static boolean noNamedTreeNode = false;

    /**
     * Interdit la création d'objets de cette classe.
     */
    private OptionalDependencies() {
    }

    /**
     * Creates an initially empty tree node.
     *
     * @param name   The value to be returned by {@link TreeNode#toString}.
     * @param object The user object to be returned by the tree node. May
     *               or may not be the same than {@code name}.
     * @param allowsChildren if children are allowed.
     */
    public static DefaultMutableTreeNode createTreeNode(final String name,
                                                        final Object object,
                                                        final boolean allowsChildren)
    {
        /*
         * If the "modules/extension/swing-widgets" JAR is in the classpath,  then create an
         * instance of NamedTreeNode (see org.geotools.swing.tree javadoc for an explanation
         * about why the NamedTreeNode workaround is needed).  We use reflection because the
         * swing-widgets module is optional,  so we fallback on the standard Swing object if
         * we can't create an instance of NamedTreeNode.   We will attempt to use reflection
         * only once in order to avoid a overhead if the swing-widgets module is not available.
         *
         * The swing-widgets module contains a "NamedTreeNodeTest" for making sure that the
         * NamedTreeNode instances are properly created.
         *
         * Note: No need to sychronize; this is not a big deal if we make the attempt twice.
         */
        if (!noNamedTreeNode) try {
            if (treeNodeConstructor == null) {
                treeNodeConstructor = Class.forName("org.geotools.gui.swing.tree.NamedTreeNode").
                        getConstructor(new Class[] {String.class, Object.class, Boolean.TYPE});
            }
            return (DefaultMutableTreeNode) treeNodeConstructor.newInstance(
                    new Object[] {name, object, Boolean.valueOf(allowsChildren)});
        } catch (Exception e) {
            /*
             * There is a large amount of checked and unchecked exceptions that the above code
             * may thrown. We catch all of them because a reasonable fallback exists (creation
             * of the default Swing object below).  Note that none of the unchecked exceptions
             * (IllegalArgumentException, NullPointerException...) should occurs, except maybe
             * SecurityException. Maybe we could let the unchecked exceptions propagate...
             */
            noNamedTreeNode = true;
        }
        return new DefaultMutableTreeNode(name, allowsChildren);
    }
}
