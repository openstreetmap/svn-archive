// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileDIC.DicBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoRecord.Format;

/**
 * Edigeo DIC file.
 */
public class EdigeoFileDIC extends EdigeoLotFile<DicBlock> {

    /**
     * Abstract definition.
     */
    abstract static class DicBlock extends ChildBlock {

        /** LAB */ String code = "";
        /** TEX */ EdigeoCharset charset;
        /** DEF */ String definition = "";
        /** ORI */ String origin = "";

        DicBlock(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "LAB": safeGet(r, s -> code += s); break;
            case "TEX": safeGet(r, s -> charset = EdigeoCharset.of(s)); break;
            case "DEF": safeGet(r, s -> definition += s); break;
            case "ORI": safeGet(r, s -> origin += s); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotEmpty(code);
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
    }

    /**
     * Categorized definition.
     */
    abstract static class CategorizedBlock extends DicBlock {

        enum Category {
            GENERAL('G'),
            SPECIFIC('P');

            final char code;
            Category(char code) {
                this.code = code;
            }

            public static Category of(char code) {
                for (Category s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Character.toString(code));
            }
        }

        /** CAT */ Category category;

        CategorizedBlock(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CAT": category = Category.of(safeGetChar(r)); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(category);
        }

        /**
         * Returns category.
         * @return category
         */
        public final Category getCategory() {
            return category;
        }
    }

    /**
     * Object definition.
     */
    public static class ObjectDef extends DicBlock {
        ObjectDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        public String toString() {
            return "ObjectDef [code=" + code + ", charset=" + charset + ", definition=" + definition + ", origin="
                    + origin + ", type=" + type + ", identifier=" + identifier + ']';
        }
    }

    /**
     * Attribute definition.
     */
    public static class AttributeDef extends CategorizedBlock {

        /** TYP */ Format type;
        /** UNI */ String unit = "";
        /** AVC */ int nValues;
        /** AVL */ final List<String> values = new ArrayList<>();
        /** AVD */ final List<String> descrs = new ArrayList<>();

        AttributeDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "TYP": type = Format.of(safeGetChar(r)); break;
            case "UNI": safeGet(r, s -> unit += s); break;
            case "AVC": nValues = safeGetInt(r); break;
            case "AVL": safeGet(r, values); break;
            case "AVD": safeGet(r, descrs); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns attribute type.
         * @return attribute type
         */
        public final Format getType() {
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

        @Override
        public String toString() {
            return "AttributeDef [type=" + type + ", unit=" + unit + ", values=" + values + ", descrs=" + descrs
                    + ", category=" + category + ", code=" + code + ", definition=" + definition + ", origin=" + origin
                    + ", identifier=" + identifier + ']';
        }
    }

    /**
     * Relation definition.
     */
    public static class RelationDef extends CategorizedBlock {
        RelationDef(Lot lot, String type) {
            super(lot, type);
        }

        @Override
        public String toString() {
            return "RelationDef [category=" + category + ", code=" + code + ", charset=" + charset + ", definition="
                    + definition + ", origin=" + origin + ", type=" + type + ", identifier=" + identifier + "]";
        }
    }

    /**
     * Constructs a new {@code EdigeoFileDIC}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to DIC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileDIC(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("DID", ObjectDef.class);
        register("DIA", AttributeDef.class);
        register("DIR", RelationDef.class);
        lot.dic = this;
    }

    /**
     * Returns list of object definitions.
     * @return list of object definitions
     */
    public final List<ObjectDef> getObjects() {
        return Collections.unmodifiableList(blocks.getInstances(ObjectDef.class));
    }

    /**
     * Returns list of attribute definitions.
     * @return list of attribute definitions
     */
    public final List<AttributeDef> getAttributes() {
        return Collections.unmodifiableList(blocks.getInstances(AttributeDef.class));
    }

    /**
     * Returns list of relation definitions.
     * @return list of relation definitions
     */
    public final List<RelationDef> getRelations() {
        return Collections.unmodifiableList(blocks.getInstances(RelationDef.class));
    }
}
