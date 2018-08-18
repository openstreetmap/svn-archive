/**
 * @author Stefan Kaintoch
 * @version $Id: GpsRec.java 6 2007-11-28 20:55:14Z ramack $
 */
package org.kaintoch.gps.globalsat.dg100;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author skaintoch
 *
 */
public class GpsRec
{
//  aa aa aa aa: dg100Latitude:
//  bb bb bb bb: dg100Longitude:
//  cc cc cc cc: zulu-time: hh * 10000 + mm * 100 + ss
//  dd dd dd dd: dg100Date: DD * 10000 + MM * 100 + (YY - 2000)
//  ee ee ee ee: dg100Speed (km/h): dg100Speed * 100
//  ff ff ff ff: dg100Altitude (m): alt * 10000

    private int dg100Latitude = -1;
    private int dg100Longitude = -1;
    private int dg100TimeZ = -1;
    private int dg100Date = -1;
    private int dg100Speed = -1;
    private int dg100Altitude = -1;
    private int dg100Unk1 = -1;
    private int dg100TypeOfCurRec = -1;
    private int dg100TypeOfNextRec = -1;
    // calculated data
    private Calendar dateTime = null;

    public GpsRec()
    {
        dg100TypeOfNextRec = 2;
        dg100TypeOfCurRec = 2;
        dg100Latitude = 0;
        dg100Longitude = 0;
        dg100TimeZ = 0;
        dg100Date = 0;
        dg100Speed = 0;
        dg100Altitude = 0;
        dg100Unk1 = -1;
        dateTime = null;
    }

    public void copy(GpsRec init)
    {
        dg100TypeOfNextRec = init.dg100TypeOfNextRec;
        dg100TypeOfCurRec = init.dg100TypeOfCurRec;
        dg100Latitude = init.dg100Latitude;
        dg100Longitude = init.dg100Longitude;
        dg100TimeZ = init.dg100TimeZ;
        dg100Date = init.dg100Date;
        dg100Speed = init.dg100Speed;
        dg100Altitude = init.dg100Altitude;
        dg100Unk1 = init.dg100Unk1;
        dateTime = init.dateTime;
    }

    public GpsRec(GpsRec init)
    {
        copy(init);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dg100Altitude;
        result = prime * result + dg100Date;
        result = prime * result + dg100Latitude;
        result = prime * result + dg100Longitude;
        result = prime * result + dg100Speed;
        result = prime * result + dg100TimeZ;
        result = prime * result + dg100TypeOfCurRec;
        result = prime * result + dg100TypeOfNextRec;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        GpsRec other = (GpsRec) obj;
        return dg100Altitude == other.dg100Altitude
            && dg100Date == other.dg100Date
            && dg100Latitude == other.dg100Latitude
            && dg100Longitude == other.dg100Longitude
            && dg100Speed == other.dg100Speed
            && dg100TimeZ == other.dg100TimeZ
            && dg100TypeOfCurRec == other.dg100TypeOfCurRec
            && dg100TypeOfNextRec == other.dg100TypeOfNextRec;
    }

    public GpsRec(ByteBuffer buf, int recType)
    {
        dg100Latitude =  buf.getInt();
        dg100Longitude =  buf.getInt();
        dg100TypeOfNextRec = recType;
        dg100TypeOfCurRec = recType;
        dateTime = null;
        if (dg100TypeOfNextRec >= 1)
        {
            dg100TimeZ = buf.getInt();
            dg100Date = buf.getInt();
            calcDateTime();
            dg100Speed =  buf.getInt();
            if (dg100TypeOfNextRec >= 1)
            {
                dg100Altitude =  buf.getInt();
                dg100Unk1 =  buf.getInt();
                dg100TypeOfNextRec =  buf.getInt();
            }
        }
    }

    /**
     * Shows wether this is a valid GPS record.
     * @return <code>true</code> if GPS record is valid; otherwise <code>false</code>.
     */
    public boolean isValid()
    {
        return
            dg100Latitude <= 360000000
            && dg100Latitude >= 0
            && dg100Longitude <= 360000000
            && dg100Longitude >= 0
            && dg100TimeZ >= 0
            && dg100TimeZ <= 240000
            && dg100Unk1 >= 0
            && dg100Unk1 <= 1
            ;
    }

    public int getDg100TypeOfNextRec()
    {
        return dg100TypeOfNextRec;
    }

    public int getDg100TypeOfCurRec()
    {
        return dg100TypeOfCurRec;
    }

    @Override
    public String toString()
    {
        return "[GpsRec: "
            + " Lat = " + dg100Latitude
            + ", Long = " + dg100Longitude
            + ", TimeZ = " + dg100TimeZ
            + ", Date = " + dg100Date
            + ", Speed = " + dg100Speed
            + ", Alt = " + dg100Altitude
            + ", Unk1 = " + dg100Unk1
            + ", TypeOfCurRec = " + dg100TypeOfCurRec
            + ", TypeOfNextRec = " + dg100TypeOfNextRec
            + "]";
    }

    /**
     * Get latitude value
     * @return Returns the dg100Latitude value.
     */
    public int getDg100Latitude()
    {
        return dg100Latitude;
    }

    /**
     * Converts this object to its GPX representation.
     * @return this object's GPX representation as a String.
     */
    public String toGpxTrkpt()
    {
//      <trkpt lat="47.6972383333" lon="11.4178650000">
//      <ele>662.0000000000</ele>
//      <time>2007-04-21T13:56:05Z</time>
//      <dg100Speed>1.0833333333</dg100Speed>
//      </trkpt>
        StringBuffer buf = new StringBuffer(500);
        buf.append("<trkpt");
        buf.append(" lat=\"").append(getLatitude()).append("\"");
        buf.append(" lon=\"").append(getLongitude()).append("\"");
        buf.append(">");
        if (dg100TypeOfCurRec > 0)
        {
            if (dg100TypeOfCurRec > 1)
            {
                buf.append("<ele>").append(getAltitude()).append("</ele>");
            }
            buf.append("<time>").append(getStringZuluTime()).append("</time>");
            buf.append("<speed>").append(getSpeed()).append("</speed>");
        }
        buf.append("</trkpt>");
        return buf.toString();
    }

    /**
     * Converts this object to its GPX waypoint representation.
     * @return this object's GPX representation as a String.
     */
    public String toGpxWpt()
    {
//      <wpt lat="47.6972383333" lon="11.4178650000">
//      <ele>662.0000000000</ele>
//      <time>2007-04-21T13:56:05Z</time>
//      </wpt>
        StringBuffer buf = new StringBuffer(500);
        buf.append("<wpt");
        buf.append(" lat=\"").append(getLatitude()).append("\"");
        buf.append(" lon=\"").append(getLongitude()).append("\"");
        buf.append(">");
        if (dg100TypeOfCurRec > 0)
        {
            if (dg100TypeOfCurRec > 1)
            {
                buf.append("<ele>").append(getAltitude()).append("</ele>");
            }
            buf.append("<time>").append(getStringZuluTime()).append("</time>");
        }
        buf.append("</wpt>");
        return buf.toString();
    }

    /**
     * Converts GlobalSat dg100Latitude and dg100Longitude internal format to degrees.
     * @param gsLatOrLon
     * @return nodeg in degrees
     */
    private double toDegree(int gsLatOrLon)
    {
        int scale = 1000000;
        double deg = 9999.9999;
        double degScaled = gsLatOrLon / scale;
        double minScaled = (gsLatOrLon - degScaled * scale) / 600000.0;
        deg = degScaled + minScaled;
        return deg;
    }

    /**
     * Gets dg100Date and time as a String in GPX dg100Date-time-format (aka zulu time).
     * @return a dg100Date-time-string in GPX dg100Date-time-format (aka zulu time).
     */
    public String getStringZuluTime()
    {
        return getStringDateTime("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    /**
     * Gets dg100Date and time as a String in format "yyyyMMddHHmmss".
     * @return a dg100Date-time-string in format "yyyyMMddHHmmss".
     */
    public String getStringDateTime()
    {
        return getStringDateTime("yyyyMMddHHmmss");
    }

    /**
     * Gets dg100Date and time as a String in given format.
     * @param dateTimeFormat format string for date and time
     * @return formated date and time
     */
    private String getStringDateTime(String dateTimeFormat)
    {
        String dateTimeString = "???";
        if (dateTime != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
            //logger.info(gsTime + " " + gsDate);
            dateTimeString = sdf.format(dateTime.getTime());
        }
        return dateTimeString;
    }

    /**
     * Get the altitude value
     * @return Returns the dg100Altitude.
     */
    public int getDg100Altitude()
    {
        return dg100Altitude;
    }

    /**
     * Set the altitude value
     * @param altitude The dg100Altitude to set.
     */
    private void setDg100Altitude(int altitude)
    {
        this.dg100Altitude = altitude;
    }

    /**
     * Get date information
     * @return Returns the dg100Date.
     */
    public int getDg100Date()
    {
        return dg100Date;
    }

    /**
     * Set date information
     * @param date The dg100Date to set.
     */
    private void setDg100Date(int date)
    {
        this.dg100Date = date;
        calcDateTime();
    }

    /**
     * calculate date and time from individual components
     */
    private void calcDateTime()
    {
        int hh = dg100TimeZ / 10000;
        int mm = (dg100TimeZ - hh * 10000) / 100;
        int ss = dg100TimeZ - hh * 10000 - mm * 100;
        int DD = dg100Date / 10000;
        int MM = (dg100Date - DD * 10000) / 100;
        int YY = dg100Date - DD * 10000 - MM * 100;
        dateTime = GregorianCalendar.getInstance();
        dateTime.set(2000 + YY, MM - 1, DD, hh, mm, ss); // this is somehow strange: (MM - 1) seems correct now, but last year I did not notice wrong behaviour without the "- 1"
    }

    /**
     * Get longitude value
     * @return Returns the dg100Longitude.
     */
    public int getDg100Longitude()
    {
        return dg100Longitude;
    }

    /**
     * Set longitude value
     * @param longitude The dg100Longitude to set.
     */
    private void setDg100Longitude(int longitude)
    {
        this.dg100Longitude = longitude;
    }

    /**
     * Get speed value
     * @return Returns the dg100Speed.
     */
    public int getDg100Speed()
    {
        return dg100Speed;
    }

    /**
     * Set speed value
     * @param speed The dg100Speed to set.
     */
    private void setDg100Speed(int speed)
    {
        this.dg100Speed = speed;
    }

    /**
     * Get timezone value
     * @return Returns the dg100TimeZ.
     */
    public int getDg100TimeZ()
    {
        return dg100TimeZ;
    }

    /**
     * Set timezone value
     * @param timeZ The dg100TimeZ to set.
     */
    private void setDg100TimeZ(int timeZ)
    {
        this.dg100TimeZ = timeZ;
        calcDateTime();
    }

    /**
     * Set latitude value
     * @param latitude The dg100Latitude to set.
     */
    private void setDg100Latitude(int latitude)
    {
        this.dg100Latitude = latitude;
    }

    public void updateMin(GpsRec next)
    {
        if (next != null)
        {
            if (next.getDg100Latitude() < getDg100Latitude()) {setDg100Latitude(next.getDg100Latitude());}
            if (next.getDg100Longitude() < getDg100Longitude()) {setDg100Longitude(next.getDg100Longitude());}
            if (next.getDg100Altitude() < getDg100Altitude()) {setDg100Altitude(next.getDg100Altitude());}
            if (next.getDg100Date() < getDg100Date()
                || (next.getDg100Date() == getDg100Date() && next.getDg100TimeZ() < getDg100TimeZ())
                )
            {
                setDg100Date(next.getDg100Date());
                setDg100TimeZ(next.getDg100TimeZ());
            }
            if (next.getDg100Speed() < getDg100Speed()) {setDg100Speed(next.getDg100Speed());}
        }
    }

    public void updateMax(GpsRec next)
    {
        if (next != null)
        {
            if (next.getDg100Latitude() > getDg100Latitude()) {setDg100Latitude(next.getDg100Latitude());}
            if (next.getDg100Longitude() > getDg100Longitude()) {setDg100Longitude(next.getDg100Longitude());}
            if (next.getDg100Altitude() > getDg100Altitude()) {setDg100Altitude(next.getDg100Altitude());}
            if (next.getDg100Date() > getDg100Date()
                || (next.getDg100Date() == getDg100Date() && next.getDg100TimeZ() > getDg100TimeZ())
                )
            {
                setDg100Date(next.getDg100Date());
                setDg100TimeZ(next.getDg100TimeZ());
            }
            if (next.getDg100Speed() > getDg100Speed()) {setDg100Speed(next.getDg100Speed());}
        }
    }

    /**
     * @return Returns the dateTime.
     */
    public Calendar getDateTime()
    {
        return dateTime;
    }

    public double getLatitude()
    {
        return toDegree(dg100Latitude);
    }

    public double getLongitude()
    {
        return toDegree(dg100Longitude);
    }

    public double getAltitude()
    {
        return dg100Altitude / 10000.0;
    }

    public double getSpeed()
    {
        return dg100Speed / 360.0;
    }

}
