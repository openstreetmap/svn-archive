package org.openstreetmap.josm.plugins.conflation;

import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Map;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.jts.JTSUtils;

public class OsmFeature extends AbstractBasicFeature {
    private Object[] attributes;
    private OsmPrimitive primitive;
    
    /**
     * Create a copy of the OSM geometry
     * TODO: update from underlying primitive
     * @param prim 
     */
    public OsmFeature(OsmPrimitive prim) {
        super(new FeatureSchema());
        primitive = prim;
        Map<String, String> keys = prim.getKeys();
        attributes = new Object[keys.size() + 1];
        getSchema().addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        for (String key : keys.keySet()) {
            getSchema().addAttribute(key, AttributeType.STRING);
            setAttribute(key, keys.get(key));
        }
        JTSUtils conversion = new JTSUtils();
        setGeometry(conversion.convert(prim));
    }

    @Override
    public void setAttributes(Object[] attributes) {
        this.attributes = attributes;
    }

    @Override
    public void setAttribute(int attributeIndex, Object newAttribute) {
        attributes[attributeIndex] = newAttribute;
    }

    @Override
    public Object getAttribute(int i) {
        return attributes[i];
    }

    @Override
    public Object[] getAttributes() {
        return attributes;
    }
    
    public OsmPrimitive getPrimitive() {
        return primitive;
    }
    
    @Override
    public int getID() {
        // FIXME: should work most of the time, GeoAPI more robust, need to
        // consider the dataset (e.g. two non-uploaded layers can have different
        // objects with the same id
        return (int) primitive.getId();
    }
}
