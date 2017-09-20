// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Edigeo DIC file.
 */
public class EdigeoFileDIC extends EdigeoFile {

    /**
     * Abstract definition.
     */
    abstract static class Def extends Block {

        /** LAB */ String code = "";
        /** TEX */ EdigeoCharset charset;
        /** DEF */ String definition = "";
        /** ORI */ String origin = "";
        /** CAT */ String category = "";

        Def(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "LAB": safeGet(r, s -> code += s); break;
            case "TEX": safeGet(r, s -> charset = EdigeoCharset.of(s)); break;
            case "DEF": safeGet(r, s -> definition += s); break;
            case "ORI": safeGet(r, s -> origin += s); break;
            case "CAT": safeGet(r, s -> category += s); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns code.
         * @return code
         */
        public final String getCode() {
            return code;
        }

        /**
         * Returns definition.
         * @return definition
         */
        public final String getDefinition() {
            return definition;
        }

        /**
         * Returns definition source.
         * @return definition source
         */
        public final String getOrigin() {
            return origin;
        }

        /**
         * Returns category.
         * @return category
         */
        public final String getCategory() {
            return category;
        }
    }

    /**
     * Object definition.
     */
    public static class ObjectDef extends Def {
        ObjectDef(String type) {
            super(type);
        }
    }

    /**
     * Attribute definition.
     */
    public static class AttributeDef extends Def {

        /** TYP */ String type = "";
        /** UNI */ String unit = "";
        /** AVC */ int nValues;
        /** AVL */ final List<String> values = new ArrayList<>();
        /** AVD */ final List<String> descrs = new ArrayList<>();

        AttributeDef(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "TYP": safeGet(r, s -> type += s); break;
            case "UNI": safeGet(r, s -> unit += s); break;
            case "AVC": nValues = safeGetInt(r); break;
            case "AVL": values.add(""); safeGet(r, values); break;
            case "AVD": descrs.add(""); safeGet(r, descrs); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns attribute type.
         * @return attribute type
         */
        public final String getType() {
            return type;
        }

        /**
         * Returns default unit.
         * @return default unit
         */
        public final String getUnit() {
            return unit;
        }

        /**
         * Returns number of values.
         * @return number of values
         */
        public final int getNumberOfValues() {
            return nValues;
        }

        /**
         * Returns pre-coded values.
         * @return pre-coded values
         */
        public final List<String> getValues() {
            return Collections.unmodifiableList(values);
        }

        /**
         * Returns descriptions of pre-coded values.
         * @return descriptions of pre-coded values
         */
        public final List<String> getDescriptions() {
            return Collections.unmodifiableList(descrs);
        }
    }

    /**
     * Relation definition.
     */
    public static class RelationDef extends Def {
        RelationDef(String type) {
            super(type);
        }
    }

    /** DID */ List<ObjectDef> objects;
    /** DIA */ List<AttributeDef> attributes;
    /** DIR */ List<RelationDef> relations;

    /**
     * Constructs a new {@code EdigeoFileDIC}.
     * @param path path to DIC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileDIC(Path path) throws IOException {
        super(path);
    }

    @Override
    protected void init() {
        objects = new ArrayList<>();
        attributes = new ArrayList<>();
        relations = new ArrayList<>();
    }

    @Override
    protected Block createBlock(String type) {
        switch (type) {
        case "DID":
            ObjectDef objDef = new ObjectDef(type);
            objects.add(objDef);
            return objDef;
        case "DIA":
            AttributeDef attDef = new AttributeDef(type);
            attributes.add(attDef);
            return attDef;
        case "DIR":
            RelationDef relDef = new RelationDef(type);
            relations.add(relDef);
            return relDef;
        default:
            throw new IllegalArgumentException(type);
        }
    }

    /**
     * Returns list of object definitions.
     * @return list of object definitions
     */
    public final List<ObjectDef> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    /**
     * Returns list of attribute definitions.
     * @return list of attribute definitions
     */
    public final List<AttributeDef> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Returns list of relation definitions.
     * @return list of relation definitions
     */
    public final List<RelationDef> getRelations() {
        return Collections.unmodifiableList(relations);
    }
}
