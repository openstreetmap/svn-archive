package org.openstreetmap.josm.plugins.turnlanes.model;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;

public class Issue {
    public enum Severity {
        INFO,
        WARN,
        ERROR;
    }
    
    public static abstract class QuickFix {
        public static final QuickFix NONE = new QuickFix(tr("None")) {
            
            @Override
            public boolean perform() {
                throw new UnsupportedOperationException("Don't call perform on Issue.QuickFix.NONE.");
            }
        };
        
        private final String description;
        
        public QuickFix(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public abstract boolean perform();
    }
    
    private final Severity severity;
    private final Relation relation;
    private final List<OsmPrimitive> primitives;
    private final String description;
    private final QuickFix quickFix;
    
    private Issue(Severity severity, Relation relation, List<? extends OsmPrimitive> primitives, String description,
        QuickFix quickFix) {
        this.relation = relation;
        this.primitives = Collections.unmodifiableList(new ArrayList<OsmPrimitive>(primitives));
        this.severity = severity;
        this.description = description;
        this.quickFix = quickFix;
    }
    
    public static Issue newError(Relation relation, List<? extends OsmPrimitive> primitives, String description,
        QuickFix quickFix) {
        return new Issue(Severity.ERROR, relation, primitives, description, quickFix);
    }
    
    public static Issue newError(Relation relation, List<? extends OsmPrimitive> primitives, String description) {
        return newError(relation, primitives, description, QuickFix.NONE);
    }
    
    public static Issue newError(Relation relation, OsmPrimitive primitive, String description) {
        return newError(relation, Arrays.asList(primitive), description, QuickFix.NONE);
    }
    
    public static Issue newError(Relation relation, String description) {
        return newError(relation, Collections.<OsmPrimitive> emptyList(), description, QuickFix.NONE);
    }
    
    public static Issue newWarning(List<OsmPrimitive> primitives, String description) {
        return new Issue(Severity.WARN, null, primitives, description, QuickFix.NONE);
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Relation getRelation() {
        return relation;
    }
    
    public List<OsmPrimitive> getPrimitives() {
        return primitives;
    }
    
    public QuickFix getQuickFix() {
        return quickFix;
    }
}
