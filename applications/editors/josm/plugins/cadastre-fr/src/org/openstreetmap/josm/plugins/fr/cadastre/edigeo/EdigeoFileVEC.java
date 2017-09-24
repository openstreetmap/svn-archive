// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdAttributeDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdConstructionRelationDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdConstructionRelationDef.RelationKind;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdObjectDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdPrimitiveDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdRelationDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.McdSemanticRelationDef;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileSCD.ScdBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.ChildBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF.Lot;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC.VecBlock;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoRecord.Nature;
import org.openstreetmap.josm.tools.Logging;

/**
 * Edigeo VEC file.
 */
public class EdigeoFileVEC extends EdigeoLotFile<VecBlock<?>> {

    abstract static class VecBlock<T extends ScdBlock> extends ChildBlock {
        private final Class<T> klass;
        private final List<RelationBlock> parentRelations = new ArrayList<>();

        /** SCP */ T scdRef;
        /** ATC */ int nAttributes;
        /** ATP */ final List<McdAttributeDef> attributeDefs = new ArrayList<>();
        /** TEX */ EdigeoCharset charset;
        /** ATV */ final List<EdigeoRecord> lAttributeValues = new ArrayList<>();
        /** QAC */ int nQualities;
        /** QAP */ final List<String> qualityIndics = new ArrayList<>();

        final List<String> attributeValues = new ArrayList<>();

        VecBlock(Lot lot, String type, Class<T> klass) {
            super(lot, type);
            this.klass = Objects.requireNonNull(klass, "klass");
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": scdRef = lot.scd.find(r.values, klass); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "ATP": attributeDefs.add(lot.scd.find(r.values, McdAttributeDef.class)); break;
            case "TEX": safeGet(r, s -> charset = EdigeoCharset.of(s)); break;
            case "ATV": lAttributeValues.add(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            case "QAP": safeGet(r, qualityIndics); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        void resolve() {
            super.resolve();
            for (EdigeoRecord r : lAttributeValues) {
                if (r.nature == Nature.COMPOSED) {
                    //attributeValues.add(lot.scd.find(r.values, McdAttributeDef.class).dictRef);
                    attributeValues.add(r.values.toString()); // FIXME
                } else {
                    attributeValues.add(r.values.get(0));
                }
            }
            lAttributeValues.clear();
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(scdRef)
                    && areSameSize(nAttributes, attributeDefs, attributeValues)
                    && areSameSize(nQualities, qualityIndics);
        }

        final boolean addRelation(RelationBlock relationBlock) {
            return parentRelations.add(Objects.requireNonNull(relationBlock, "relationBlock"));
        }

        public final List<RelationBlock> getConstructionRelations() {
            return parentRelations.stream().filter(r -> r.scdRef instanceof McdConstructionRelationDef).collect(Collectors.toList());
        }

        public final List<RelationBlock> getSemanticRelations() {
            return parentRelations.stream().filter(r -> r.scdRef instanceof McdSemanticRelationDef).collect(Collectors.toList());
        }

        /**
         * Returns the number of attributes.
         * @return the number of attributes
         */
        public final int getNumberOfAttributes() {
            return nAttributes;
        }

        public final List<McdAttributeDef> getAttributeDefinitions() {
            return Collections.unmodifiableList(attributeDefs);
        }

        public final McdAttributeDef getAttributeDefinition(int i) {
            return attributeDefs.get(i);
        }

        public final String getAttributeValue(int i) {
            return attributeValues.get(i);
        }
    }

    abstract static class BoundedBlock<T extends ScdBlock> extends VecBlock<T> {
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;

        BoundedBlock(Lot lot, String type, Class<T> klass) {
            super(lot, type, klass);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "CM1": minCoordinate = safeGetEastNorth(r); break;
            case "CM2": maxCoordinate = safeGetEastNorth(r); break;
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

        NodeBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "TYP": nodeType = NodeType.of(safeGetInt(r)); break;
            case "COR": coordinate = safeGetEastNorth(r); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        public String toString() {
            return "NodeBlock [identifier=" + identifier + ']';
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
    public static class ArcBlock extends BoundedBlock<McdPrimitiveDef> {
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

        /** TYP */ ArcType arcType;
        /** PTC */ int nPoints;
        /** COR */ final List<EastNorth> points = new ArrayList<>();

        ArcBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "TYP": arcType = ArcType.of(safeGetInt(r)); break;
            case "PTC": nPoints = safeGetInt(r); break;
            case "COR": points.add(safeGetEastNorth(r)); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && areNotNull(arcType) && checkNumberOfPoints() && areSameSize(nPoints, points);
        }

        @Override
        public String toString() {
            return "ArcBlock [identifier=" + identifier + ']';
        }

        private boolean checkNumberOfPoints() {
            switch (arcType) {
            case LINE: return nPoints >= 2;
            case CIRCLE_ARC: return nPoints == 3;
            case CURVE: return nPoints >= 3;
            default: throw new IllegalStateException(arcType.toString());
            }
        }
    }

    /**
     * Face descriptor block.
     */
    public static class FaceBlock extends BoundedBlock<McdPrimitiveDef> {

        FaceBlock(Lot lot, String type) {
            super(lot, type, McdPrimitiveDef.class);
        }

        @Override
        public String toString() {
            return "FaceBlock [identifier=" + identifier + ']';
        }
    }

    /**
     * Object descriptor block. 7.5.1.4
     */
    public static class ObjectBlock extends BoundedBlock<McdObjectDef> {
        /** REF */ EastNorth refPoint;

        ObjectBlock(Lot lot, String type) {
            super(lot, type, McdObjectDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "REF": refPoint = safeGetEastNorth(r); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        public String toString() {
            return "ObjectBlock [identifier=" + identifier + ", attributeDefs=" + attributeDefs +", attributeValues=" + attributeValues + ']';
        }
    }

    /**
     * Relation descriptor block. 7.5.1.5
     */
    public static class RelationBlock extends VecBlock<McdRelationDef> {

        enum Composition {
            PLUS('P'),
            MINUS('M');

            final char code;
            Composition(char code) {
                this.code = code;
            }

            public static Composition of(char code) {
                for (Composition s : values()) {
                    if (s.code == code) {
                        return s;
                    }
                }
                throw new IllegalArgumentException(Character.toString(code));
            }
        }

        /** FTC */ int nElements;
        /** FTP */ final List<List<String>> lElements = new ArrayList<>();
        /** SNS */ final Map<List<String>, Composition> mCompositions = new HashMap<>();

        // Resolution of elements must be done when all VEC files are read
        final List<VecBlock<?>> elements = new ArrayList<>();
        final Map<VecBlock<?>, Composition> compositions = new HashMap<>();

        RelationBlock(Lot lot, String type) {
            super(lot, type, McdRelationDef.class);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "FTC": nElements = safeGetInt(r); break;
            case "FTP": lElements.add(r.values); break;
            case "SNS": mCompositions.put(lElements.get(lElements.size()-1), Composition.of(safeGetChar(r))); break;
            default:
                super.processRecord(r);
            }
        }

        @Override
        boolean isValid() {
            return super.isValid() && nElements >= 2 && areSameSize(nElements, elements) && compositions.size() <= nElements;
        }

        @Override
        final void resolve() {
            super.resolve();
            for (List<String> values : lElements) {
                VecBlock<?> b = lot.vec.stream().filter(v -> v.subsetId.equals(values.get(1))).findAny()
                        .orElseThrow(() -> new IllegalArgumentException(values.toString()))
                        .find(values, VecBlock.class);
                b.addRelation(this);
                elements.add(b);
                compositions.put(b, mCompositions.get(values));
            }
            lElements.clear();
            mCompositions.clear();
        }

        @Override
        public String toString() {
            return "RelationBlock [identifier=" + identifier + ']';
        }
    }

    private static final List<Predicate<ObjectBlock>> ignoredObjects = new ArrayList<>();

    /**
     * Adds a predicate to ignore a special type of object.
     * @param predicate defines how to identify the object to ignore
     * @return {@code true}
     */
    public static boolean addIgnoredObject(Predicate<ObjectBlock> predicate) {
        return ignoredObjects.add(Objects.requireNonNull(predicate, "predicate"));
    }

    /**
     * Adds a predicate to ignore a special type of object based on a key/value attribute.
     * @param key attribute key
     * @param value attribute value
     * @return {@code true}
     */
    public static boolean addIgnoredObject(String key, String value) {
        return addIgnoredObject(o -> {
            for (int i = 0; i < o.nAttributes; i++) {
                if (key.equals(o.attributeDefs.get(i).identifier)
                        && value.equals(o.attributeValues.get(i))) {
                    return true;
                }
            }
            return false;
        });
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

    private static BBox around(LatLon ll) {
        final double r = 1e-7;
        return new BBox(ll.getX() - r, ll.getY() - r, ll.getX() + r, ll.getY() + r);
    }

    private static Node getNodeAt(DataSet ds, Projection proj, EastNorth en) {
        LatLon ll = proj.eastNorth2latlon(en);
        List<Node> nodes = ds.searchNodes(around(ll));
        if (nodes.isEmpty()) {
            Node n = new Node(ll);
            ds.addPrimitive(n);
            return n;
        }
        return nodes.get(0);
    }

    @Override
    EdigeoFileVEC fill(DataSet ds) {
        super.fill(ds);
        Projection proj = lot.geo.getCoorReference().getProjection();
        for (ObjectBlock obj : getObjects()) {
            if (!ignoredObjects.stream().anyMatch(p -> p.test(obj))) {
                List<RelationBlock> constructionRelations = obj.getConstructionRelations();
                switch (obj.scdRef.kind) {
                    case POINT: fillPoint(ds, proj, obj, constructionRelations); break;
                    case LINE: fillLine(ds, proj, obj, constructionRelations); break;
                    case AREA: fillArea(ds, proj, obj, constructionRelations); break;
                    case COMPLEX: break; // TODO (not used in PCI)
                    default: throw new IllegalArgumentException(obj.toString());
                }
            }
        }
        return this;
    }

    private static void addPrimitiveAndTags(DataSet ds, ObjectBlock obj, OsmPrimitive osm) {
        for (int i = 0; i < obj.nAttributes; i++) {
            osm.put(new Tag(obj.attributeDefs.get(i).identifier, obj.attributeValues.get(i)));
        }
        ds.addPrimitive(osm);
    }

    private static void fillPoint(DataSet ds, Projection proj, ObjectBlock obj, List<RelationBlock> constructionRelations) {
        assert constructionRelations.size() == 1 : constructionRelations;
        RelationBlock rel = constructionRelations.get(0);
        assert rel.scdRef instanceof McdConstructionRelationDef : rel;
        if (rel.scdRef instanceof McdConstructionRelationDef) {
            McdConstructionRelationDef crd = (McdConstructionRelationDef) rel.scdRef;
            assert crd.kind == RelationKind.IS_MADE_OF;
            assert crd.nAttributes == 0;
        }
        for (VecBlock<?> e : rel.elements) {
            if (e instanceof NodeBlock) {
                NodeBlock nb = (NodeBlock) e;
                assert nb.nAttributes == 0;
                LatLon ll = proj.eastNorth2latlon(nb.getCoordinate());
                //if (ds.searchNodes(around(ll)).isEmpty()) {
                addPrimitiveAndTags(ds, obj, new Node(ll));
                //}
                break;
            }
        }
    }

    private static void fillLine(DataSet ds, Projection proj, ObjectBlock obj, List<RelationBlock> constructionRelations) {
        assert constructionRelations.size() >= 1 : constructionRelations;
        // Retrieve all arcs for the linear object
        final List<ArcBlock> arcs = new ArrayList<>();
        for (RelationBlock rel : constructionRelations) {
            assert rel.scdRef instanceof McdConstructionRelationDef : rel;
            if (rel.scdRef instanceof McdConstructionRelationDef) {
                McdConstructionRelationDef crd = (McdConstructionRelationDef) rel.scdRef;
                assert crd.kind == RelationKind.IS_MADE_OF_ARC;
                assert crd.nAttributes == 0;
            }
            for (VecBlock<?> e : rel.elements) {
                if (e instanceof ArcBlock) {
                    arcs.add((ArcBlock) e);
                }
            }
        }
        final double EPSILON = 1e-2;
        assert arcs.size() >= 1;
        Way w = new Way();
        // Some lines are made of several arcs, but they need to be sorted
        if (arcs.size() > 1) {
            List<ArcBlock> newArcs = arcs.stream().filter(
                    a -> arcs.stream().noneMatch(
                            b -> b.points.get(0).equalsEpsilon(a.points.get(a.nPoints - 1), EPSILON))).collect(Collectors.toList());
            if (newArcs.size() != 1) {
                Logging.warn("Unable to process geometry of: " + obj);
                return;
            }
            while (newArcs.size() < arcs.size()) {
                ArcBlock ab = newArcs.get(newArcs.size() - 1);
                EastNorth en = ab.points.get(ab.nPoints - 1);
                Stream<ArcBlock> stream = arcs.stream().filter(a -> a.points.get(0).equalsEpsilon(en, EPSILON));
                assert stream.count() == 1;
                newArcs.add(stream.findAny().get());
            }
            assert newArcs.size() == arcs.size();
            arcs.clear();
            arcs.addAll(newArcs);
            w.put(new Tag("fixme", "several_arcs")); // FIXME
        }
        // Add first node of first arc
        w.addNode(getNodeAt(ds, proj, arcs.get(0).points.get(0)));
        // For each arc, add all nodes except first one
        for (ArcBlock ab : arcs) {
            assert ab.nAttributes == 0 : ab;
            assert ab.nQualities == 0 : ab;
            for (int i = 1; i < ab.nPoints; i++) {
                w.addNode(getNodeAt(ds, proj, ab.points.get(i)));
            }
        }
        assert w.getNodesCount() >= 2;
        addPrimitiveAndTags(ds, obj, w);
    }

    private static void fillArea(DataSet ds, Projection proj, ObjectBlock obj, List<RelationBlock> constructionRelations) {
        assert constructionRelations.size() >= 1 : constructionRelations;
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
