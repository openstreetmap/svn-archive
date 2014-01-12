/**
 * @author Stefan Kaintoch
 * @version $Id: Dg100Config.java 3 2007-10-30 19:40:04Z ramack $
 */
package org.kaintoch.gps.globalsat.dg100;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * @author skaintoch
 *
 */
public class Dg100Config
{
//  aa aa aa aa: time in ms for switch a
//  bb bb bb bb: time in ms for switch b
//  cc cc cc cc: time in ms for switch c
//  dd dd dd dd: distance in m for switch a
//  ee ee ee ee: distance in m for switch b
//  ff ff ff ff: distance in m for switch c
//  gg: 00=time, 01=distance for switch a
//  hh: 00=time, 01=distance for switch b
//  ii: 00=time, 01=distance for switch c
//  jj: 00=position; 01=position,date,time,speed; 02=position,date,time,speed,altitude
//  kk: 01=disable logging if speed < ll ll ll ll
//  ll ll ll ll: speed in km/h * 100
//  mm: 01=disable logging if distance < nn nn nn nn;
//  nn nn nn nn: distance in m
//  xx xx: checksum
//  A0 A2 00 35 B7 jj kk ll ll ll ll mm nn nn nn nn
//  aa aa aa aa bb bb bb bb cc cc cc cc 00 00 gg hh
//  ii dd dd dd dd ee ee ee ee ff ff ff ff 01 61 01
//  01 0C D5 0D 00 04 CC B0 B3

    private byte logFormat = -1;
    private byte disableLogSpeed = -1;
    private int speedThres = -1;
    private byte disableLogDist = -1;
    private int distThres = -1;
    private int swATime = -1;
    private int swBTime = -1;
    private int swCTime = -1;
    private short unk1 = -1;
    private byte swATimeOrDist = -1;
    private byte swBTimeOrDist = -1;
    private byte swCTimeOrDist = -1;
    private int swADist = -1;
    private int swBDist = -1;
    private int swCDist = -1;
    private byte unk2 = -1;
    private int remainder = -1;
    private int unk3 = -1;
    private int unk4 = -1;

    private static String propLogFormat = "logFormat";
    private static String propDisableLogSpeed = "disableLogSpeed";
    private static String propSpeedThres = "speedThres";
    private static String propDisableLogDist = "disableLogDist";
    private static String propDistThres = "distThres";
    private static String propSwATime = "swATime";
    private static String propSwBTime = "swBTime";
    private static String propSwCTime = "swCTime";
    private static String propUnk1 = "unk1";
    private static String propSwATimeOrDist = "swATimeOrDist";
    private static String propSwBTimeOrDist = "swBTimeOrDist";
    private static String propSwCTimeOrDist = "swCTimeOrDist";
    private static String propSwADist = "swADist";
    private static String propSwBDist = "swBDist";
    private static String propSwCDist = "swCDist";
    private static String propUnk2 = "unk2";
    private static String propRemainder = "remainder";
    private static String propUnk3 = "unk3";
    private static String propUnk4 = "unk4";

    public Dg100Config(ByteBuffer buf)
    {
        logFormat = buf.get();
        disableLogSpeed = buf.get();
        speedThres = buf.getInt();
        disableLogDist = buf.get();
        distThres = buf.getInt();
        swATime = buf.getInt();
        swBTime = buf.getInt();
        swCTime = buf.getInt();
        unk1 = buf.getShort();
        swATimeOrDist = buf.get();
        swBTimeOrDist = buf.get();
        swCTimeOrDist = buf.get();
        swADist = buf.getInt();
        swBDist = buf.getInt();
        swCDist = buf.getInt();
        unk2 = buf.get();
        remainder = buf.get();
        unk3 = buf.get();
        unk4 = buf.get();
    }

    public Dg100Config(String fName)
        throws Exception
    {
        readProps(fName);
    }

    public String toString()
    {
        return
            "[Dg100Config: logFormat = " + logFormat
            + ",disableLogSpeed = " + disableLogSpeed
            + ",speedThres = " + speedThres
            + ",disableLogDist = " + disableLogDist
            + ",distThres = " + distThres
            + ",swATime = " + swATime
            + ",swBTime = " + swBTime
            + ",swCTime = " + swCTime
            + ",unk1 = " + unk1
            + ",swATimeOrDist = " + swATimeOrDist
            + ",swBTimeOrDist = " + swBTimeOrDist
            + ",swCTimeOrDist = " + swCTimeOrDist
            + ",swADist = " + swADist
            + ",swBDist = " + swBDist
            + ",swCDist = " + swCDist
            + ",unk2 = " + unk2
            + ",remainder = " + remainder
            + ",unk3 = " + unk3
            + ",unk4 = " + unk4
            ;
    }

    /**
     * @param buf
     */
    public void write(ByteBuffer buf)
    {
        buf.position(5);
        buf.put(logFormat);
        buf.put(disableLogSpeed);
        buf.putInt(speedThres);
        buf.put(disableLogDist);
        buf.putInt(distThres);
        buf.putInt(swATime);
        buf.putInt(swBTime);
        buf.putInt(swCTime);
        buf.putShort(unk1);
        buf.put(swATimeOrDist);
        buf.put(swBTimeOrDist);
        buf.put(swCTimeOrDist);
        buf.putInt(swADist);
        buf.putInt(swBDist);
        buf.putInt(swCDist);
        buf.put(unk2);
    }

    /**
     * @return Returns the disableLogDist.
     */
    public boolean getDisableLogDist()
    {
        return disableLogDist != 0;
    }

    /**
     * @param disableLogDist The disableLogDist to set.
     */
    public void setDisableLogDist(boolean disableLogDist)
    {
            this.disableLogDist = (byte)(disableLogDist ? 1 : 0);
    }

    /**
     * @return Returns the disableLogSpeed.
     */
    public boolean getDisableLogSpeed()
    {
        return disableLogSpeed != 0;
    }

    /**
     * @param disableLogSpeed The disableLogSpeed to set.
     */
    public void setDisableLogSpeed(boolean disableLogSpeed)
    {
            this.disableLogSpeed = (byte)(disableLogSpeed ? 1 : 0);
    }

    /**
     * @return Returns the distThres.
     */
    public int getDistThres()
    {
        return distThres;
    }

    /**
     * @param distThres The distThres to set.
     */
    public void setDistThres(int distThres)
    {
        this.distThres = distThres;
    }

    /**
     * @return Returns the logFormat.
     */
    public byte getLogFormat()
    {
        return logFormat;
    }

    /**
     * @param logFormat The logFormat to set.
     */
    public void setLogFormat(byte logFormat)
    {
        this.logFormat = logFormat;
    }

    /**
     * @return Returns the speedThres.
     */
    public int getSpeedThres()
    {
        return speedThres;
    }

    /**
     * @param speedThres The speedThres to set.
     */
    public void setSpeedThres(int speedThres)
    {
        this.speedThres = speedThres;
    }

    /**
     * @return Returns the swADist.
     */
    public int getSwADist()
    {
        return swADist;
    }

    /**
     * @param swADist The swADist to set.
     */
    public void setSwADist(int swADist)
    {
        this.swADist = swADist;
    }

    /**
     * @return Returns the swATime.
     */
    public int getSwATime()
    {
        return swATime;
    }

    /**
     * @param swATime The swATime to set.
     */
    public void setSwATime(int swATime)
    {
        this.swATime = swATime;
    }

    /**
     * @return Returns the swATimeOrDist.
     */
    public byte getSwATimeOrDist()
    {
        return swATimeOrDist;
    }

    /**
     * @param swATimeOrDist The swATimeOrDist to set.
     */
    public void setSwATimeOrDist(byte swATimeOrDist)
    {
        this.swATimeOrDist = swATimeOrDist;
    }

    /**
     * @return Returns the swBDist.
     */
    public int getSwBDist()
    {
        return swBDist;
    }

    /**
     * @param swBDist The swBDist to set.
     */
    public void setSwBDist(int swBDist)
    {
        this.swBDist = swBDist;
    }

    /**
     * @return Returns the swBTime.
     */
    public int getSwBTime()
    {
        return swBTime;
    }

    /**
     * @param swBTime The swBTime to set.
     */
    public void setSwBTime(int swBTime)
    {
        this.swBTime = swBTime;
    }

    /**
     * @return Returns the swBTimeOrDist.
     */
    public byte getSwBTimeOrDist()
    {
        return swBTimeOrDist;
    }

    /**
     * @param swBTimeOrDist The swBTimeOrDist to set.
     */
    public void setSwBTimeOrDist(byte swBTimeOrDist)
    {
        this.swBTimeOrDist = swBTimeOrDist;
    }

    /**
     * @return Returns the swCDist.
     */
    public int getSwCDist()
    {
        return swCDist;
    }

    /**
     * @param swCDist The swCDist to set.
     */
    public void setSwCDist(int swCDist)
    {
        this.swCDist = swCDist;
    }

    /**
     * @return Returns the swCTime.
     */
    public int getSwCTime()
    {
        return swCTime;
    }

    /**
     * @param swCTime The swCTime to set.
     */
    public void setSwCTime(int swCTime)
    {
        this.swCTime = swCTime;
    }

    /**
     * @return Returns the swCTimeOrDist.
     */
    public byte getSwCTimeOrDist()
    {
        return swCTimeOrDist;
    }

    /**
     * @param swCTimeOrDist The swCTimeOrDist to set.
     */
    public void setSwCTimeOrDist(byte swCTimeOrDist)
    {
        this.swCTimeOrDist = swCTimeOrDist;
    }

    /**
     * @return Returns the unk1.
     */
    public short getUnk1()
    {
        return unk1;
    }

    /**
     * @param unk1 The unk1 to set.
     */
    public void setUnk1(short unk1)
    {
        this.unk1 = unk1;
    }

    /**
     * @return Returns the unk2.
     */
    public byte getUnk2()
    {
        return unk2;
    }

    /**
     * @param unk2 The unk2 to set.
     */
    public void setUnk2(byte unk2)
    {
        this.unk2 = unk2;
    }

    /**
     * @return Returns the remainder.
     */
    public int getRemainder()
    {
        return remainder;
    }

    /**
     * @return Returns the unk3.
     */
    public int getUnk3()
    {
        return unk3;
    }

    /**
     * @return Returns the unk4.
     */
    public int getUnk4()
    {
        return unk4;
    }

    public void writeProps(String fName)
        throws Exception
    {
        Properties props = new Properties();
        props.setProperty(propLogFormat, "" + logFormat);
        props.setProperty(propDisableLogSpeed, "" + disableLogSpeed);
        props.setProperty(propSpeedThres, "" + speedThres);
        props.setProperty(propDisableLogDist, "" + disableLogDist);
        props.setProperty(propDistThres, "" + distThres);
        props.setProperty(propSwATime, "" + swATime);
        props.setProperty(propSwBTime, "" + swBTime);
        props.setProperty(propSwCTime, "" + swCTime);
        props.setProperty(propUnk1, "" + unk1);
        props.setProperty(propSwATimeOrDist, "" + swATimeOrDist);
        props.setProperty(propSwBTimeOrDist, "" + swBTimeOrDist);
        props.setProperty(propSwCTimeOrDist, "" + swCTimeOrDist);
        props.setProperty(propSwADist, "" + swADist);
        props.setProperty(propSwBDist, "" + swBDist);
        props.setProperty(propSwCDist, "" + swCDist);
        props.setProperty(propUnk2, "" + unk2);
        props.setProperty(propRemainder, "" + remainder);
        props.setProperty(propUnk3, "" + unk3);
        props.setProperty(propUnk4, "" + unk4);
        OutputStream os = null;
        try
        {
            os = new FileOutputStream(fName);
            props.store(os, "dg100 config");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            if (os != null) {os.close();}
        }
    }

    public void readProps(String fName)
        throws Exception
    {
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            is = new FileInputStream(fName);
            props.load(is);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            if (is != null) {is.close();}
        }
        logFormat = Byte.parseByte(props.getProperty(propLogFormat, "2"));
        disableLogSpeed = Byte.parseByte(props.getProperty(propDisableLogSpeed, "0"));
        speedThres = Integer.parseInt(props.getProperty(propSpeedThres, "0"));
        disableLogDist = Byte.parseByte(props.getProperty(propDisableLogDist, "0"));
        distThres = Integer.parseInt(props.getProperty(propDistThres, "0"));
        swATime = Integer.parseInt(props.getProperty(propSwATime, "1000"));
        swBTime = Integer.parseInt(props.getProperty(propSwBTime, "1000"));
        swCTime = Integer.parseInt(props.getProperty(propSwCTime, "1000"));
        swATimeOrDist = Byte.parseByte(props.getProperty(propSwATimeOrDist, "0"));
        swBTimeOrDist = Byte.parseByte(props.getProperty(propSwBTimeOrDist, "0"));
        swCTimeOrDist = Byte.parseByte(props.getProperty(propSwCTimeOrDist, "0"));
        swADist = Integer.parseInt(props.getProperty(propSwADist, "0"));
        swBDist = Integer.parseInt(props.getProperty(propSwBDist, "0"));
        swCDist = Integer.parseInt(props.getProperty(propSwCDist, "0"));
        unk1 = Short.parseShort(props.getProperty(propUnk1, "0"));
        unk2 = Byte.parseByte(props.getProperty(propUnk2, "0"));
        unk3 = Integer.parseInt(props.getProperty(propUnk3, "0"));
        unk4 = Integer.parseInt(props.getProperty(propUnk4, "0"));
    }

}
