package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.Main;

public class LiveGpsAcquirer implements Runnable {
    Socket gpsdSocket;
    BufferedReader gpsdReader;
    boolean connected = false;
    String gpsdHost = Main.pref.get("livegps.gpsd.host","localhost");
    int gpsdPort = Main.pref.getInteger("livegps.gpsd.port", 2947);
    boolean shutdownFlag = false;
    private List<PropertyChangeListener> propertyChangeListener = new ArrayList<PropertyChangeListener>();
    private PropertyChangeEvent lastStatusEvent;
    private PropertyChangeEvent lastDataEvent;

    /**
     * Adds a property change listener to the acquirer.
     * @param listener the new listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(!propertyChangeListener.contains(listener)) {
            propertyChangeListener.add(listener);
        }
    }

    /**
     * Fire a gps status change event. Fires events with key "gpsstatus" and a {@link LiveGpsStatus}
     * object as value.
     * @param status the status.
     * @param statusMessage the status message.
     */
    public void fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus status, String statusMessage) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "gpsstatus", null, new LiveGpsStatus(status, statusMessage));
        if(!event.equals(lastStatusEvent)) {
            firePropertyChangeEvent(event);
            lastStatusEvent = event;
        }
    }

    /**
     * Fire a gps data change event to all listeners. Fires events with key "gpsdata" and a
     * {@link LiveGpsData} object as values.
     * @param oldData the old gps data.
     * @param newData the new gps data.
     */
    public void fireGpsDataChangeEvent(LiveGpsData oldData, LiveGpsData newData) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "gpsdata", oldData, newData);
        if(!event.equals(lastDataEvent)) {
            firePropertyChangeEvent(event);
            lastDataEvent = event;
        }
    }

    /**
     * Fires the given event to all listeners.
     * @param event the event to fire.
     */
    protected void firePropertyChangeEvent(PropertyChangeEvent event) {
        for (PropertyChangeListener listener : propertyChangeListener) {
            listener.propertyChange(event);
        }
    }

    public void run() {
        LiveGpsData oldGpsData = null;
        LiveGpsData gpsData = null;
        shutdownFlag = false;
        while(!shutdownFlag) {
            double lat = 0;
            double lon = 0;
            float speed = 0;
            float course = 0;
            boolean haveFix = false;

            try
            {
                if (!connected)
                {
                    System.out.println("LiveGps tries to connect to gpsd");
                    fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTING, tr("Connecting"));
                    InetAddress[] addrs = InetAddress.getAllByName(gpsdHost);
                    for (int i=0; i < addrs.length && gpsdSocket == null; i++) {
                        try {
                            gpsdSocket = new Socket(addrs[i], gpsdPort);
                            break;
                        } catch (Exception e) {
                            System.out.println("LiveGps: Could not open connection to gpsd: " + e);
                            gpsdSocket = null;
                        }
                    }

                    if (gpsdSocket != null)
                    {
                        gpsdReader = new BufferedReader(new InputStreamReader(gpsdSocket.getInputStream()));
                        gpsdSocket.getOutputStream().write(new byte[] { 'w', 13, 10 });
                        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTING, tr("Connecting"));
                        connected = true;
                    System.out.println("LiveGps: Connected to gpsd");
                    }
                }


                if(connected) {
                    // <FIXXME date="23.06.2007" author="cdaller">
                    // TODO this read is blocking if gps is connected but has no fix, so gpsd does not send positions
                    String line = gpsdReader.readLine();
                    // </FIXXME>
                    if (line == null) break;
                    String words[] = line.split(",");

                    if ((words.length == 0) || (!words[0].equals("GPSD"))) {
                        // unexpected response.
                        continue;
                    }

                    for (int i = 1; i < words.length; i++) {

                        if ((words[i].length() < 2) || (words[i].charAt(1) != '=')) {
                            // unexpected response.
                            continue;
                        }

                        char what = words[i].charAt(0);
                        String value = words[i].substring(2);
                        oldGpsData = gpsData;
                        gpsData = new LiveGpsData();
                        switch(what) {
                        case 'O':
                            // full report, tab delimited.
                            String[] status = value.split("\\s+");
                            if (status.length >= 5) {
                                lat = Double.parseDouble(status[3]);
                                lon = Double.parseDouble(status[4]);
                                try {
                                    speed = Float.parseFloat(status[9]);
                                    course = Float.parseFloat(status[8]);
                                    //view.setSpeed(speed);
                                    //view.setCourse(course);
                                } catch (NumberFormatException nex) {}
                                haveFix = true;
                            }
                            break;
                        case 'P':
                            // position report, tab delimited.
                            String[] pos = value.split("\\s+");
                            if (pos.length >= 2) {
                                lat = Double.parseDouble(pos[0]);
                                lon = Double.parseDouble(pos[1]);
                                speed = Float.NaN;
                                course = Float.NaN;
                                haveFix = true;
                            }
                        default:
                            // not interested
                        }
                        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTED, tr("Connected"));
                        gpsData.setFix(haveFix);
                        if (haveFix) {
                            //view.setCurrentPosition(lat, lon);
                            gpsData.setLatLon(new LatLon(lat, lon));
                            gpsData.setSpeed(speed);
                            gpsData.setCourse(course);
                            fireGpsDataChangeEvent(oldGpsData, gpsData);
                        }
                    }
                } else {
                    // not connected:
                    fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.DISCONNECTED, tr("Not connected"));
                    try { Thread.sleep(1000); } catch (InterruptedException ignore) {};
                }
            } catch(IOException iox) {
                connected = false;
                if(gpsData != null) {
                    gpsData.setFix(false);
                    fireGpsDataChangeEvent(oldGpsData, gpsData);
                }
                fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.CONNECTION_FAILED, tr("Connection Failed"));
                try { Thread.sleep(1000); } catch (InterruptedException ignore) {};
                // send warning to layer

            }
        }

        fireGpsStatusChangeEvent(LiveGpsStatus.GpsStatus.DISCONNECTED, tr("Not connected"));
        if (gpsdSocket != null) {
            try {
              gpsdSocket.close();
              gpsdSocket = null;
                  System.out.println("LiveGps: Disconnected from gpsd");
            }
            catch (Exception e) {
              System.out.println("LiveGps: Unable to close socket; reconnection may not be possible");
            }
        }
    }

    public void shutdown()
    {
        shutdownFlag = true;
    }
}
