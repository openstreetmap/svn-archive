// License: Public Domain. For details, see LICENSE file.
package livegps;

/**
 * @author cdaller
 *
 */
public class LiveGpsStatus {
    public enum GpsStatus {
        CONNECTING, CONNECTED, DISCONNECTED, CONNECTION_FAILED
    }

    private String statusMessage;
    private GpsStatus status;

    public LiveGpsStatus(GpsStatus status, String statusMessage) {
        super();
        this.status = status;
        this.statusMessage = statusMessage;
    }

    /**
     * @return the status
     */
    public GpsStatus getStatus() {
        return this.status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(GpsStatus status) {
        this.status = status;
    }

    /**
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return this.statusMessage;
    }

    /**
     * @param statusMessage the statusMessage to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

}
