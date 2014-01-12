// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

@SuppressWarnings("serial")
public class SdsTransferException extends Exception {

    private String url = SdsApi.getSdsApi().getBaseUrl();

    public SdsTransferException() {
    }

    public SdsTransferException(String message) {
        super(message);
    }

    public SdsTransferException(Throwable cause) {
        super(cause);
    }

    public SdsTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return Api base url or url set using setUrl method
     */
    public String getUrl() {
        return url;
    }

}
