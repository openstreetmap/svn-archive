// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileDIC.DicBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;

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
        /** CAT */ String category = "";

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
    public static class ObjectDef extends DicBlock {
        ObjectDef(Lot lot, String type) {
            super(lot, type);
        }
    }

    /**
     * Attribute definition.
     */
    public static class AttributeDef extends DicBlock {

        /** TYP */ String type = "";
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
            case "TYP": safeGet(r, s -> type += s); break;
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
    public static class RelationDef extends DicBlock {
        RelationDef(Lot lot, String type) {
            super(lot, type);
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

    @Override
    public EdigeoFileDIC read(DataSet ds) throws IOException, ReflectiveOperationException {
        super.read(ds);
        return this;
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
