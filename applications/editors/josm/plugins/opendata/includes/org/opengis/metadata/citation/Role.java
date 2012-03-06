/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.metadata.citation;

import java.util.List;
import java.util.ArrayList;

import org.opengis.util.CodeList;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Function performed by the responsible party.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/metadata/citation/Role.java $
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author  Martin Desruisseaux (IRD)
 * @author  Cory Horner (Refractions Research)
 * @since   GeoAPI 2.0
 */
@UML(identifier="CI_RoleCode", specification=ISO_19115)
public final class Role extends CodeList<Role> {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -7763516018565534103L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<Role> VALUES = new ArrayList<Role>(11);

    /**
     * Party that supplies the resource.
     */
    @UML(identifier="resourceProvider", obligation=CONDITIONAL, specification=ISO_19115)
    public static final Role RESOURCE_PROVIDER = new Role("RESOURCE_PROVIDER");

    /**
     * Party that owns the resource.
     */
    @UML(identifier="owner", obligation=CONDITIONAL, specification=ISO_19115)
    public static final Role OWNER = new Role("OWNER");

    /**
     * Key party responsible for gathering information and conducting research.
     */
    @UML(identifier="principalInvestigator", obligation=CONDITIONAL, specification=ISO_19115)
    public static final Role PRINCIPAL_INVESTIGATOR = new Role("PRINCIPAL_INVESTIGATOR");

    /**
     * Party who published the resource.
     */
    @UML(identifier="publisher", obligation=CONDITIONAL, specification=ISO_19115)
    public static final Role PUBLISHER = new Role("PUBLISHER");

    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    private Role(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of {@code Role}s.
     *
     * @return The list of codes declared in the current JVM.
     */
    public static Role[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new Role[VALUES.size()]);
        }
    }

    /**
     * Returns the list of enumerations of the same kind than this enum.
     */
    public Role[] family() {
        return values();
    }

    /**
     * Returns the role that matches the given string, or returns a
     * new one if none match it.
     *
     * @param code The name of the code to fetch or to create.
     * @return A code matching the given name.
     */
    public static Role valueOf(String code) {
        return valueOf(Role.class, code);
    }
}
