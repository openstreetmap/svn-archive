// License: GPL. For details, see LICENSE file.
package cadastre_fr;

class WMSException extends Exception {
    private String message;
    private static final long serialVersionUID = 1L;

    WMSException(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
