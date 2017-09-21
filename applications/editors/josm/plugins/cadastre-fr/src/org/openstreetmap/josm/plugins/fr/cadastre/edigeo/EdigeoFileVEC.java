// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;

/**
 * Edigeo VEC file.
 */
public class EdigeoFileVEC extends EdigeoFile {

    /**
     * Node descriptor block.
     */
    public static class NodeBlock extends Block {

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

        /** SCP */ String scdRef = "";
        /** TYP */ NodeType nodeType;
        /** COR */ EastNorth coordinate;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        NodeBlock(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": safeGet(r, s -> scdRef += s); break;
            case "TYP": nodeType = NodeType.of(safeGetInt(r)); break;
            case "COR": coordinate = safeGetEastNorth(r); break;
            case "ATC": nAttributes = safeGetInt(r); break;
            case "QAC": nQualities = safeGetInt(r); break;
            default:
                super.processRecord(r);
            }
        }
    }

    /**
     * Arc descriptor block.
     */
    public static class ArcBlock extends Block {
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

        /** SCP */ String scdRef = "";
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** TYP */ ArcType arcType;
        /** PTC */ int nPoints;
        /** COR */ EastNorth initialPoint;
        /** COR */ EastNorth finalPoint;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        ArcBlock(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": safeGet(r, s -> scdRef += s); break;
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
    public static class FaceBlock extends Block {
        /** SCP */ String scdRef = "";
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        FaceBlock(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": safeGet(r, s -> scdRef += s); break;
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
    public static class ObjectBlock extends Block {
        /** SCP */ String scdRef = "";
        /** CM1 */ EastNorth minCoordinate;
        /** CM2 */ EastNorth maxCoordinate;
        /** REF */ String pointRef = "";
        /** ATC */ int nAttributes;
        /** ATP */ final List<String> attributeDefs = new ArrayList<>();
        /** TEX */ EdigeoCharset charset;
        /** ATV */ final List<String> attributeValues = new ArrayList<>();
        /** QAC */ int nQualities;
        /** QAP */ final List<String> qualityIndics = new ArrayList<>();

        ObjectBlock(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": safeGet(r, s -> scdRef += s); break;
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
    public static class RelationBlock extends Block {

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

        /** SCP */ String scdRef = "";
        /** FTC */ int nElements;
        /** FTP */ final List<String> elements = new ArrayList<>();
        /** SNS */ final Map<String, Composition> compositions = new HashMap<>();
        /** ATC */ int nAttributes;
        /** QAC */ int nQualities;

        RelationBlock(String type) {
            super(type);
        }

        @Override
        void processRecord(EdigeoRecord r) {
            switch (r.name) {
            case "SCP": safeGet(r, s -> scdRef += s); break;
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
     * @param path path to VEC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileVEC(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        switch (type) {
        case "PNO":
            return new NodeBlock(type);
        case "PAR":
            return new ArcBlock(type);
        case "PFE":
            return new FaceBlock(type);
        case "FEA":
            return new ObjectBlock(type);
        case "LNK":
            return new RelationBlock(type);
        default:
            throw new IllegalArgumentException(type);
        }
    }
}
