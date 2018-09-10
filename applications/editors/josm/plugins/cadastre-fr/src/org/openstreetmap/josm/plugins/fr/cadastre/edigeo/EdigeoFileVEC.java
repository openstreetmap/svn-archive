// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.josm.actions.CreateMultipolygonAction;
import org.openstreetmap.josm.command.PurgeCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadData;
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
import org.openstreetmap.josm.tools.Pair;

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
        void resolvePhase1() {
            super.resolvePhase1();
            for (EdigeoRecord r : lAttributeValues) {
                // Does not work for composed attributes, the value is overriden in phase 2
                attributeValues.add(r.values.get(0));
            }
        }

        @Override
        void resolvePhase2() {
            super.resolvePhase2();
            for (int i = 0; i < nAttributes; i++) {
                EdigeoRecord r = lAttributeValues.get(i);
                if (r.nature == Nature.COMPOSED) {
                    assert !parentRelations.isEmpty();
                    McdAttributeDef def = lot.scd.find(r.values, McdAttributeDef.class);
                    List<RelationBlock> relations = getSemanticRelations().stream().filter(
                            rel -> rel.elements.stream().anyMatch(e -> e.attributeDefs.contains(def))).collect(Collectors.toList());
                    assert relations.size() == 1;
                    List<VecBlock<?>> elements = relations.get(0).elements.stream().filter(
                            e -> e.attributeDefs.contains(def)).collect(Collectors.toList());
                    assert elements.size() == 1;
                    VecBlock<?> e = elements.get(0);
                    attributeValues.set(i, e.attributeValues.get(e.attributeDefs.indexOf(def)));
                    attributeDefs.set(i, def);
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

        public boolean hasScdIdentifier(String id) {
            return scdRef.identifier.equals(id);
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

            final int code;
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

            final int code;
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

        boolean isClosed() {
            return nPoints >= 4 && points.get(0).equals(points.get(nPoints - 1));
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
        final void resolvePhase1() {
            super.resolvePhase1();
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
    private static final List<EdigeoPostProcessor> postProcessors = new ArrayList<>();

    /**
     * Adds a predicate to ignore a special type of object.
     * @param predicate defines how to identify the object to ignore
     */
    public static void addIgnoredObject(Predicate<ObjectBlock> predicate) {
        ignoredObjects.add(Objects.requireNonNull(predicate, "predicate"));
    }

    /**
     * Adds a predicate to ignore a special type of object based on a key/value attribute.
     * @param key attribute key
     * @param values attribute values
     */
    public static void addIgnoredObject(String key, String... values) {
        addIgnoredObject(predicate(key, values));
    }

    /**
     * Adds a predicate to ignore special types of objects based on their SCD identifier.
     * @param types SCD identifiers to ignore
     */
    public static void addIgnoredScdObjects(String... types) {
        addIgnoredObject(o -> Arrays.asList(types).contains(o.scdRef.identifier));
    }

    private static Predicate<ObjectBlock> predicate(String key, String... values) {
        return o -> {
            List<String> vals = Arrays.asList(values);
            for (int i = 0; i < o.nAttributes; i++) {
                if (key.equals(o.attributeDefs.get(i).identifier)
                        && (vals.isEmpty() || vals.contains(o.attributeValues.get(i)))) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Adds a data postprocessor.
     * @param consumer consumer that will update OSM primitive accordingly
     * @param predicate predicate to match
     */
    public static void addObjectPostProcessor(
            BiConsumer<ObjectBlock, OsmPrimitive> consumer,
            Predicate<ObjectBlock> predicate, BiPredicate<CadastreDownloadData, OsmPrimitive> filter) {
        postProcessors.add(new EdigeoPostProcessor(predicate, filter, consumer));
    }

    /**
     * Adds a data postprocessor based on a key/value attribute.
     * @param consumer consumer that will update OSM primitive accordingly
     * @param key attribute key
     * @param values attribute values
     */
    public static void addObjectPostProcessor(
            BiConsumer<ObjectBlock, OsmPrimitive> consumer,
            BiPredicate<CadastreDownloadData, OsmPrimitive> filter, String key, String... values) {
        postProcessors.add(new EdigeoPostProcessor(predicate(key, values), filter, consumer));
    }

    /**
     * Adds a data postprocessor based on a SYM_id specific value.
     * @param symId value for "SYM_id" attribute.
     * @param keyValues OSM attribute key/values (int the form {@code foo=bar;bar=baz})
     */
    public static void addObjectPostProcessor(String symId, BiPredicate<CadastreDownloadData, OsmPrimitive> filter, String keyValues) {
        postProcessors.add(new EdigeoPostProcessor(predicate("SYM_id", symId), filter, (o, p) -> {
            p.remove("SYM_id");
            for (String tag : keyValues.split(";")) {
                String[] kv = tag.split("=");
                p.put(kv[0], kv[1]);
            }
        }));
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
        final double r = 1e-6;
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
    EdigeoFileVEC fill(DataSet ds, CadastreDownloadData data) {
        super.fill(ds, data);
        Projection proj = lot.geo.getCoorReference().getProjection();
        List<OsmPrimitive> toPurge = new ArrayList<>();
        for (ObjectBlock obj : getObjects()) {
            if (!ignoredObjects.stream().anyMatch(p -> p.test(obj))) {
                OsmPrimitive p;
                switch (obj.scdRef.kind) {
                    case POINT: p = fillPoint(ds, proj, obj, obj.getConstructionRelations(), obj.getSemanticRelations()); break;
                    case LINE: p = fillLine(ds, proj, obj, obj.getConstructionRelations(), obj.getSemanticRelations()); break;
                    case AREA: p = fillArea(ds, proj, obj, obj.getConstructionRelations(), obj.getSemanticRelations()); break;
                    case COMPLEX: // TODO (not used in PCI)
                    default: throw new IllegalArgumentException(obj.toString());
                }
                if (p != null) {
                    boolean purged = false;
                    for (EdigeoPostProcessor e : postProcessors) {
                        if (e.predicate.test(obj)) {
                            if (e.filter.test(data, p)) {
                                purged = toPurge.add(p);
                                if (p instanceof Relation) {
                                    toPurge.addAll(((Relation) p).getMemberPrimitivesList());
                                }
                            } else {
                                e.consumer.accept(obj, p);
                            }
                        }
                    }
                    if (!purged && p.isTagged()) {
                        p.put("source", CadastrePlugin.source);
                    }
                }
            }
        }
        if (!toPurge.isEmpty()) {
            PurgeCommand.build(toPurge, null).executeCommand();
        }
        return this;
    }

    private static <T extends OsmPrimitive> T addPrimitiveAndTags(DataSet ds, ObjectBlock obj, T osm) {
        if (osm != null) {
            for (int i = 0; i < obj.nAttributes; i++) {
                String key = obj.attributeDefs.get(i).identifier;
                if (!key.startsWith("ID_S_ATT_")) { // Ignore Z_1_2_2 text attributes
                    osm.put(key, obj.attributeValues.get(i));
                }
            }
            if (osm.getDataSet() == null) {
                ds.addPrimitive(osm);
            }
        }
        return osm;
    }

    @SuppressWarnings("unchecked")
    private static <T extends VecBlock<?>> List<T> extract(Class<T> klass, List<RelationBlock> constructionRelations, RelationKind kind) {
        final List<T> list = new ArrayList<>();
        for (RelationBlock rel : constructionRelations) {
            assert rel.scdRef instanceof McdConstructionRelationDef : rel;
            if (rel.scdRef instanceof McdConstructionRelationDef) {
                McdConstructionRelationDef crd = (McdConstructionRelationDef) rel.scdRef;
                if (crd.kind == kind) {
                    assert crd.nAttributes == 0;
                    for (VecBlock<?> e : rel.elements) {
                        if (klass.isInstance(e)) {
                            list.add((T) e);
                        }
                    }
                }
            }
        }
        return list;
    }

    private static Node fillPoint(DataSet ds, Projection proj, ObjectBlock obj,
            List<RelationBlock> constructionRelations, List<RelationBlock> semanticRelations) {
        assert constructionRelations.size() == 1 : constructionRelations;
        List<NodeBlock> blocks = extract(NodeBlock.class, constructionRelations, RelationKind.IS_MADE_OF);
        assert blocks.size() == 1 : blocks;
        NodeBlock nb = blocks.get(0);
        assert nb.nAttributes == 0;
        LatLon ll = proj.eastNorth2latlon(nb.getCoordinate());
        List<Node> nodes = ds.searchNodes(around(ll));
        assert nodes.size() <= 1;
        Node n = nodes.isEmpty() ? new Node(ll) : nodes.get(0);
        return addPrimitiveAndTags(ds, obj, n);
    }

    private static Way fillLine(DataSet ds, Projection proj, ObjectBlock obj,
            List<RelationBlock> constructionRelations, List<RelationBlock> semanticRelations) {
        assert constructionRelations.size() >= 1 : constructionRelations;
        // Retrieve all arcs for the linear object
        final List<ArcBlock> arcs = extract(ArcBlock.class, constructionRelations, RelationKind.IS_MADE_OF_ARC);
        final double EPSILON = 1e-2;
        assert arcs.size() >= 1;
        // Some lines are made of several arcs, but they need to be sorted
        if (arcs.size() > 1) {
            List<ArcBlock> newArcs = arcs.stream().filter(
                    a -> arcs.stream().noneMatch(
                            b -> b.points.get(0).equalsEpsilon(a.points.get(a.nPoints - 1), EPSILON))).collect(Collectors.toList());
            if (newArcs.size() != 1) {
                Logging.warn("Unable to process geometry of: " + obj);
                return null;
            }
            while (newArcs.size() < arcs.size()) {
                ArcBlock ab = newArcs.get(newArcs.size() - 1);
                EastNorth en = ab.points.get(ab.nPoints - 1);
                Stream<ArcBlock> stream = arcs.stream().filter(a -> a.points.get(0).equalsEpsilon(en, EPSILON));
                Optional<ArcBlock> x = stream.findAny();
                if (!x.isPresent()) {
                    // Problem observed with 31248000AO01. Choose the nearest node
                    Logging.warn("Degraded mode for " + obj + " around " + en);
                    stream = arcs.stream()
                                 .sorted((o1, o2) -> Double.compare(en.distance(o1.points.get(0)), en.distance(o2.points.get(0))));
                    x = stream.findFirst();
                }
                newArcs.add(x.get());
            }
            assert newArcs.size() == arcs.size();
            arcs.clear();
            arcs.addAll(newArcs);
        }
        Way w = new Way();
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
        return addPrimitiveAndTags(ds, obj, w);
    }

    private static OsmPrimitive fillArea(DataSet ds, Projection proj, ObjectBlock obj,
            List<RelationBlock> constructionRelations, List<RelationBlock> semanticRelations) {
        assert constructionRelations.size() >= 1 : constructionRelations;
        List<FaceBlock> faces = extract(FaceBlock.class, constructionRelations, RelationKind.IS_MADE_OF);
        assert faces.size() >= 1;
        if (faces.size() == 1) {
            return addPrimitiveAndTags(ds, obj, faceToOsmPrimitive(ds, proj, faces.get(0)));
        } else {
            Relation r = new Relation();
            r.put("type", "multipolygon");
            for (FaceBlock face : faces) {
                r.addMember(new RelationMember("outer", faceToOsmPrimitive(ds, proj, face)));
            }
            return addPrimitiveAndTags(ds, obj, r);
        }
    }

    private static OsmPrimitive faceToOsmPrimitive(DataSet ds, Projection proj, FaceBlock face) {
        List<ArcBlock> allArcs = new ArrayList<>();
        allArcs.addAll(extract(ArcBlock.class, face.getConstructionRelations(), RelationKind.HAS_FOR_LEFT_FACE));
        allArcs.addAll(extract(ArcBlock.class, face.getConstructionRelations(), RelationKind.HAS_FOR_RIGHT_FACE));
        assert allArcs.size() >= 1;
        if (allArcs.size() == 1) {
            ArcBlock ab = allArcs.get(0);
            assert ab.isClosed();
            return arcToWay(ds, proj, ab);
        } else {
            List<Way> ways = allArcs.stream().map(ab -> arcToWay(ds, proj, ab)).filter(w -> w != null).collect(Collectors.toList());
            Pair<SequenceCommand, Relation> cmd = CreateMultipolygonAction.createMultipolygonCommand(ways, null);
            if (cmd != null) {
                cmd.a.executeCommand();
                return cmd.b;
            } else {
                ways.forEach(w -> ds.removePrimitive(w.getPrimitiveId()));
                return null;
            }
        }
    }

    private static Way arcToWay(DataSet ds, Projection proj, ArcBlock ab) {
        Way way = new Way();
        for (int i = 0; i < ab.nPoints; i++) {
            Node n = getNodeAt(ds, proj, ab.points.get(i));
            if (i == 0 || !n.equals(way.lastNode())) {
                way.addNode(n);
            }
        }
        if (way.getNodesCount() < 2) {
            return null;
        }
        List<Way> existingWays = ds.searchWays(way.getBBox()).stream().filter(
                w -> w.hasEqualSemanticAttributes(way)).collect(Collectors.toList());
        if (existingWays.isEmpty()) {
            ds.addPrimitive(way);
            return way;
        } else {
            return existingWays.get(0);
        }
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
