// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

class WMSException extends Exception {
    private String message;
    private static final long serialVersionUID = 1L;
    public WMSException(String message) {
        super();
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
