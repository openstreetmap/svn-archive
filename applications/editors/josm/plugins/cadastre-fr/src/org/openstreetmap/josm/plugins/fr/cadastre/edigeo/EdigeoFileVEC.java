// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdObjectDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdPrimitiveDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdRelationDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.ScdBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC.VecBlock;

/**
 * Edigeo VEC file.
 */
public class EdigeoFileVEC extends EdigeoLotFile<VecBlock<?>> {

    abstract static class VecBlock<T extends ScdBlock> extends ChildBlock {
        final Class<T> klass;

        /** SCP */ T scdRef;

        VecBlock(Lot lot, String type, Class<T> klass) {
            super(lot, type);
            this.klass = klass;
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": scdRef = lot.scd.find(r.values, klass); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Node descriptor block.
     */
    public static class NodeBlock extends VecBlock<McdPrimitiveDef> {

        enum NodeType {
            INITIAL_OR_FINAL(1),
            ISOLATED(2);

            int code;
            NodeType(int code) {
                this.code = code;
            }

            public static NodeType of(int code) {
                for (NodeType s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        /** TYP */ NodeType nodeType;
        /** COR */ EastNorth coordinate;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        NodeBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "TYP": nodeType = NodeType.of(safeGetInt(r)); break;
            case "COR": coordinate = safeGetEastNorth(r); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }

        /**
         * Returns the reference to SCD.
         * @return the reference to SCD
         */
        public final McdPrimitiveDef getScdRef() {
            return scdRef;
        }

        /**
         * Returns the node type.
         * @return the node type
         */
        public final NodeType getNodeType() {
            return nodeType;
        }

        /**
         * Returns the node coordinates.
         * @return the node coordinates
         */
        public final EastNorth getCoordinate() {
            return coordinate;
        }

        /**
         * Returns the number of attributes.
         * @return the number of attributes
         */
        public final int getNumberOfAttributes() {
            return nAttributes;
        }

        /**
         * Returns the number of quality indicators.
         * @return the number of quality indicators
         */
        public final int getNumberOfQualityIndicators() {
            return nQualities;
        }
    }

    /**
     * Arc descriptor block.
     */
    public static class ArcBlock extends VecBlock<McdPrimitiveDef> {
        enum ArcType {
            LINE(1),
            CIRCLE_ARC(2),
            CURVE(3);

            int code;
            ArcType(int code) {
                this.code = code;
            }

            public static ArcType of(int code) {
                for (ArcType s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Integer.toString(code));
            }
        }

        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** TYP */ ArcType arcType;
        /** PTC */ int nPoints;
        /** COR */ EastNorth initialPoint;
        /** COR */ EastNorth finalPoint;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        ArcBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": minCoordinate = safeGetEastNorth(r); break;
            case "CM2": maxCoordinate = safeGetEastNorth(r); break;
            case "TYP": arcType = ArcType.of(safeGetInt(r)); break;
            case "PTC": nPoints = safeGetInt(r); break;
            case "COR":
                EastNorth en = safeGetEastNorth(r);
                if (initialPoint == null) {
                    initialPoint = en;
                } else if (finalPoint == null) {
                    finalPoint = en;
                }
                break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Face descriptor block.
     */
    public static class FaceBlock extends VecBlock<McdPrimitiveDef> {
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        FaceBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": minCoordinate = safeGetEastNorth(r); break;
            case "CM2": maxCoordinate = safeGetEastNorth(r); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Object descriptor block.
     */
    public static class ObjectBlock extends VecBlock<McdObjectDef> {
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** REF */ String pointRef = "";
        /** ATC */ int nAttributes;
        /** ATP */ final List<String> attributeDefs = new ArrayList<>();
        /** TEX */ EdigeoCharset charset;
        /** ATV */ final List<String> attributeValues = new ArrayList<>();
        /** QAC */ int nQualities;
        /** QAP */ final List<String> qualityIndics = new ArrayList<>();

        ObjectBlock(Lot lot, String type) {
            super(lot, type, McdObjectDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": minCoordinate = safeGetEastNorth(r); break;
            case "CM2": maxCoordinate = safeGetEastNorth(r); break;
            case "REF": safeGet(r, s -> pointRef += s); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "ATP": safeGet(r, attributeDefs); break;
            case "TEX": safeGet(r, s -> charset = EdigeoCharset.of(s)); break;
            case "ATV": safeGet(r, attributeValues); break;
            case "QAC": nQualities = safeGetInt(r); break;
            case "QAP": safeGet(r, qualityIndics); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Relation descriptor block.
     */
    public static class RelationBlock extends VecBlock<McdRelationDef> {

        enum Composition {
            PLUS("P"),
            MINUS("M");

            String code;
            Composition(String code) {
                this.code = code;
            }

            public static Composition of(String code) {
                for (Composition s : values()) {
                    if (s.code.equals(code)) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(code);
            }
        }

        /** FTC */ int nElements;
        /** FTP */ final List<String> elements = new ArrayList<>();
        /** SNS */ final Map<String, Composition> compositions = new HashMap<>();
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        RelationBlock(Lot lot, String type) {
            super(lot, type, McdRelationDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "FTC": nElements = safeGetInt(r); break;
            case "FTP": safeGet(r, elements); break;
            case "SNS": safeGet(r, s -> compositions.put(elements.get(elements.size()-1), Composition.of(s))); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Constructs a new {@code EdigeoFileVEC}.
     * @param lot parent lot
     * @param seId subset id
     * @param path path to VEC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileVEC(Lot lot, String seId, Path path) throws IOException {
        super(lot, seId, path);
        register("PNO", NodeBlock.class);
        register("PAR", ArcBlock.class);
        register("PFE", FaceBlock.class);
        register("FEA", ObjectBlock.class);
        register("LNK", RelationBlock.class);
        lot.vec.add(this);
    }

    @Override
    public EdigeoFileVEC read(DataSet ds) throws IOException, ReflectiveOperationException {
        super.read(ds);
        Projection proj = lot.geo.getCoorReference().getProjection();
        for (NodeBlock nb : getNodes()) {
            assert nb.getNumberOfAttributes() == 0;
            assert nb.getNumberOfQualityIndicators() == 0;
            lot.geo.toString();
            Node n = new Node(proj.eastNorth2latlon(nb.getCoordinate()));
            ds.addPrimitive(n);
        }
        return this;
    }

    /**
     * Returns the list of node descriptors.
     * @return the list of node descriptors
     */
    public final List<NodeBlock> getNodes() {
        return Collections.unmodifiableList(blocks.getInstances(NodeBlock.class));
    }

    /**
     * Returns the list of arc descriptors.
     * @return the list of arc descriptors
     */
    public final List<ArcBlock> getArcs() {
        return Collections.unmodifiableList(blocks.getInstances(ArcBlock.class));
    }

    /**
     * Returns the list of face descriptors.
     * @return the list of face descriptors
     */
    public final List<FaceBlock> getFaces() {
        return Collections.unmodifiableList(blocks.getInstances(FaceBlock.class));
    }

    /**
     * Returns the list of object descriptors.
     * @return the list of object descriptors
     */
    public final List<ObjectBlock> getObjects() {
        return Collections.unmodifiableList(blocks.getInstances(ObjectBlock.class));
    }

    /**
     * Returns the list of relation descriptors.
     * @return the list of relation descriptors
     */
    public final List<RelationBlock> getRelations() {
        return Collections.unmodifiableList(blocks.getInstances(RelationBlock.class));
    }
}
