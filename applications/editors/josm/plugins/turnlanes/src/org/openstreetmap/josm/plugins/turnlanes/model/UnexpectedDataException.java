package org.openstreetmap.josm.plugins.turnlanes.model;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;

public final class UnexpectedDataException extends RuntimeException {
    private static final long serialVersionUID = 7430280313889494242L;
    
    public enum Kind {
        NO_MEMBER("No member with role \"{0}\".", 1),
        MULTIPLE_MEMBERS("More than one member with role \"{0}\".", 1),
        WRONG_MEMBER_TYPE("A member with role \"{0}\" is a {1} and not a {2} as expected.", 3),
        INVALID_TAG_FORMAT("The tag \"{0}\" has an invalid format: {1}", 2),
        MISSING_TAG("The tag \"{0}\" is missing.", 1);
        
        private final String message;
        private final int params;
        
        private Kind(String message, int params) {
            this.message = message;
            this.params = params;
        }
        
        public UnexpectedDataException chuck(Object... args) {
            throw new UnexpectedDataException(this, format(args));
        }
        
        public String format(Object... args) {
            if (args.length != params) {
                throw new IllegalArgumentException("Wrong argument count for " + this + ": " + Arrays.toString(args));
            }
            
            return tr(message, args);
        }
    }
    
    private final Kind kind;
    
    public UnexpectedDataException(Kind kind, String message) {
        super(message);
        
        this.kind = kind;
    }
    
    public Kind getKind() {
        return kind;
    }
}
