/**
 * Communicate with a GlobalSat DG-100 GPS mouse and data logger.
 * @author Stefan Kaintoch, Raphael Mack
 * license: GPLv3 or any later version
 */
package org.openstreetmap.josm.plugins.globalsat;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kaintoch.gps.globalsat.dg100.ByteHelper;
import org.kaintoch.gps.globalsat.dg100.Dg100Config;
import org.kaintoch.gps.globalsat.dg100.FileInfoRec;
import org.kaintoch.gps.globalsat.dg100.GpsRec;
import org.kaintoch.gps.globalsat.dg100.Response;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

/**
 * @author ramack
 *
 */
public class GlobalsatDg100
{
    public class ConnectionException extends Exception{
        ConnectionException(Exception cause){
            super(cause);
        }
        ConnectionException(String msg){
            super(msg);
        }
    }

    public static final int TIMEOUT = 2000;
    public static final int TRACK_TYPE = 1;

    /** delete file: A0 A2 00 02 BC 01 00 BD B0 B3 */
    private static byte dg100CmdSwitch2Nmea[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x18, (byte) 0x81,
      (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01,
      (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x01, (byte) 0x01,
      (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
      (byte) 0x00, (byte) 0x25, (byte) 0x80, (byte) 0x00, (byte) 0x00,
      (byte) 0xB0, (byte) 0xB3
    };
    /** delete file: A0 A2 00 02 BC 01 00 BD B0 B3 */
    private static byte dg100CmdEnterGMouse[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x02, (byte) 0xBC
      , (byte) 0x01, (byte) 0x00, (byte) 0xBD, (byte) 0xB0, (byte) 0xB3
    };
    /** delete file: A0 A2 00 03 BA FF FF 02 B8 B0 B3 */
    private static byte dg100CmdDelFile[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x03, (byte) 0xBA
      , (byte) 0xFF, (byte) 0xFF, (byte) 0x02, (byte) 0xB8, (byte) 0xB0, (byte) 0xB3
    };
    /** get file info: A0 A2 00 03 BB 00 00 00 BB B0 B3 */
    private static byte dg100CmdGetFileInfo[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x03, (byte) 0xBB
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xBB, (byte) 0xB0, (byte) 0xB3
    };
    /** get gps recs: A0 A2 00 03 B5 00 02 00 B7 B0 B3 */
    private static byte dg100CmdGetGpsRecs[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x03, (byte) 0xB5
      , (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0xB7, (byte) 0xB0, (byte) 0xB3
    };
    /** read config: A0 A2 00 01 B7 00 B7 B0 B3 */
    private static byte dg100CmdGetConfig[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x01, (byte) 0xB7
      , (byte) 0x00, (byte) 0xB7, (byte) 0xB0, (byte) 0xB3 };
    /** set config: A0 A2 00 2A B8 jj kk ll ll ll ll mm nn nn nn nn
        aa aa aa aa bb bb bb bb cc cc cc cc 00 00 gg hh
        ii dd dd dd dd ee ee ee ee ff ff ff ff 01 xx xx
        B0 B3 */
    private static byte dg100CmdSetConfig[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x2A, (byte) 0xB8
      , (byte) 0x02
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8
      , (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8
      , (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0xE8
      , (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x01
      , (byte) 0x00, (byte) 0x00, (byte) 0xB0, (byte) 0xB3
    };
    /** read config: A0 A2 00 01 BF 00 BF B0 B3 */
    private static byte dg100CmdGetId[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x01, (byte) 0xBF
      , (byte) 0x00, (byte) 0xBF, (byte) 0xB0, (byte) 0xB3 };
    /** read config: A0 A2 00 01 BF 00 BF B0 B3 */
    private static byte dg100CmdSetId[] =
    { (byte) 0xA0, (byte) 0xA2, (byte) 0x00, (byte) 0x09, (byte) 0xC0
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
      , (byte) 0x00, (byte) 0xC0, (byte) 0xB0, (byte) 0xB3};

    private byte[] response = new byte[65536];

    private CommPortIdentifier portIdentifier;
    private SerialPort port = null;

    private boolean cancelled = false;

    public GlobalsatDg100(CommPortIdentifier portId){
        this.portIdentifier = portId;
    }

    public void cancel(){
        cancelled = true;
        disconnect();
    }

    /**
     * Export DG-100's complete data to a GPX file.
     * @param port DG-100 is connected to port.
     */
    public GpxData readData(ProgressMonitor progressMonitor) throws ConnectionException {
    	progressMonitor.beginTask(null);
    	try {
    		GpxData result = null;
    		cancelled = false;
    		if(port == null){
    			connect();
    		}

    		List<FileInfoRec> fileInfoList = readFileInfoList();
    		List<GpsRec> gpsRecList = readGpsRecList(fileInfoList);

    		progressMonitor.setTicksCount(gpsRecList.size());
    		if(gpsRecList.size() > 0){
    			GpsRec last = null;
    			GpxTrack trk = new GpxTrack();
    			Collection<WayPoint> seg = new ArrayList<WayPoint>(100);
    			result = new GpxData();
    			result.tracks.add(trk);
    			trk.trackSegs.add(seg);
    			for(GpsRec r:gpsRecList){
    				if(cancelled){
    					return result;
    				}
    				WayPoint p = wayPointFrom(r);
    				if(r.equals(last)){
    					result.waypoints.add(p);
    				}else{
    					seg.add(p);
    				}
    				last = r;
    				progressMonitor.worked(1);
    			}
    		}
    		return result;
    	} finally {
    		progressMonitor.finishTask();
    	}
    }

    private WayPoint wayPointFrom(GpsRec r){
        LatLon l = new LatLon(r.getLatitude(), r.getLongitude());
        WayPoint result = new WayPoint(l);
        result.attr.put("time", "" + r.getStringZuluTime());
        result.attr.put("speed", "" + r.getSpeed());
        if(r.getDg100TypeOfCurRec() > 1)
            result.attr.put("ele", "" + r.getAltitude());
        return result;
    }

    public void deleteData() throws ConnectionException{
        if(port == null){
            connect();
        }
        try{
            Response response = sendCmdDelFiles();
        }catch(Exception e){
            throw new ConnectionException(e);
        }
    }

    public void disconnect(){
        if(port != null){
            port.close();
            port = null;
        }
    }

    private void connect() throws ConnectionException{
        try{
            port = (SerialPort)portIdentifier.open("DG100", TIMEOUT);
            port.setSerialPortParams(115200, SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
            port.notifyOnOutputEmpty(false);

        }catch(PortInUseException e){
            throw new ConnectionException(e);
        }catch(UnsupportedCommOperationException e){
            throw new ConnectionException(e);
        }
    }

    private List<FileInfoRec> readFileInfoList() throws ConnectionException
    {
        int nextIdx = 0;
        List<FileInfoRec> result = new ArrayList<FileInfoRec>(64);
        try{
            do{
                Response response = sendCmdGetFileInfo(nextIdx);
                nextIdx = response.getNextIdx();
                result.addAll(response.getRecs());
            } while (nextIdx > 0);
            return result;
        }catch(Exception e){
            throw new ConnectionException(e);
        }
    }

    public List<GpsRec> readGpsRecList(List<FileInfoRec> fileInfoList) throws ConnectionException
    {
        int cnt = 0;
        List<GpsRec> result = new ArrayList<GpsRec>(200);

        try{
            for(FileInfoRec fileInfoRec:fileInfoList){
                cnt++;
                Response response = sendCmdGetGpsRecs(fileInfoRec.getIdx());
                result.addAll(response.getRecs());
            }
            return result;
        }catch(Exception e){
            throw new ConnectionException(e);
        }
    }

    private Response sendCmdDelFiles() throws IOException, UnsupportedCommOperationException
    {
        System.out.println("deleting data...");
        int len = sendCmd(dg100CmdDelFile, response, -1);
        return Response.parseResponse(response, len);
    }

    private Response sendCmdGetFileInfo(int idx) throws IOException, UnsupportedCommOperationException
    {
        byte[] src = dg100CmdGetFileInfo;
        ByteBuffer buf = ByteBuffer.wrap(src);
        buf.position(5);
        buf.putShort((short)idx); // index of first file info rec to be read
        updateCheckSum(buf);
        int len = sendCmd(src, response, -1);
        return Response.parseResponse(response, len);
    }

    private Response sendCmdGetConfig() throws IOException, UnsupportedCommOperationException
    {
        byte[] src = dg100CmdGetConfig;
        int len = sendCmd(src, response, -1);
        return Response.parseResponse(response, len);
    }

    public Dg100Config getConfig() throws ConnectionException{
        try{
            if(port == null){
                connect();
            }
            Response response = sendCmdGetConfig();
            return response.getConfig();
        }catch(Exception e){
            throw new ConnectionException(e);
        }
    }


    private void sendCmdSetConfig(Dg100Config config) throws IOException, UnsupportedCommOperationException
    {
        byte[] src = dg100CmdSetConfig;
        ByteBuffer buf = ByteBuffer.wrap(src);
        if (config != null){
            config.write(buf);
        }
        updateCheckSum(buf);
        int len = sendCmd(src, response, -1);

        Response.parseResponse(response, len);
    }

    public void setConfig(Dg100Config conf) throws ConnectionException{
        try{
            sendCmdSetConfig(conf);
        }catch(Exception e){
            throw new ConnectionException(e);
        }
    }

    public boolean isCancelled(){
        return cancelled;
    }

    private Response sendCmdGetGpsRecs(int idx) throws IOException, UnsupportedCommOperationException
    {
        byte[] src = dg100CmdGetGpsRecs;
        ByteBuffer buf = ByteBuffer.wrap(src);
        buf.position(5);
        buf.putShort((short)idx); // index of first chunk of gps recs to be read
        updateCheckSum(buf);
        int len = sendCmd(src, response, 2074);
        return Response.parseResponse(response, len);
    }

    /**
     *
     * @param buf
     */
    private void updateCheckSum(ByteBuffer buf)
    {
        buf.position(2);
        short len = buf.getShort();
        int sum = 0;
        for (int ii = 0 ; ii < len ; ++ii){
            sum += ByteHelper.byte2IntUnsigned(buf.get());
        }
        sum = sum & 0x7FFF;
        buf.putShort((short)sum);
    }

    private int sendCmd(byte cmdAndArgs[], byte response[], int bytesToRead) throws IOException, UnsupportedCommOperationException
    {

        int cntBytTot = 0;

        OutputStream outputStream = null;
        outputStream = port.getOutputStream();
        outputStream.write(cmdAndArgs);
        outputStream.flush();

        cntBytTot = readResponse(response, bytesToRead);
        return cntBytTot;
    }


    private int readResponse(byte[] response, int bytesToRead) throws IOException, UnsupportedCommOperationException
    {
        byte[] readBuffer = new byte[200];
        int responsePos = 0;
        boolean headerReceived = false;

        port.enableReceiveTimeout(TIMEOUT);
        int cntBytTot = 0;
        InputStream inputStream = port.getInputStream();
        if (inputStream != null){
            ByteBuffer buf = ByteBuffer.wrap(response);
            port.enableReceiveThreshold(2); // read at least 2 byte
            boolean a0A2Received = false;
            while (! a0A2Received){
                response[0] = response[1];
                int rcvd = inputStream.read(response, 1, 1);
                if (rcvd == 0){
                    return 0;
                }
                a0A2Received = (response[0] == (byte)0xa0 && response[1] == (byte)0xa2);
            }
            // we already have read 2 bytes
            responsePos = 2;
            cntBytTot = 2;
            // read interesting data
            port.enableReceiveThreshold(2); // read at least 2 bytes
            while (bytesToRead < 0 || (bytesToRead >= 0 && cntBytTot < bytesToRead)){
                int cntBytAct = 0;
                cntBytAct = inputStream.read(readBuffer);
                cntBytTot += cntBytAct;
                // copy readBuffer to response
                responsePos = ByteHelper.copyByteArr2ByteArr(readBuffer, 0, cntBytAct, response, responsePos);
                if (!headerReceived && cntBytTot >= 4){
                    // do we have enough bytes to extract bytesToRead
                    if (bytesToRead == -1){
                        // get bytesToRead from response
                        buf.position(2); // here starts length of payload
                        bytesToRead = buf.getShort() + 4;
                    }
                    port.enableReceiveThreshold(1); // read at least 1 byte
                    headerReceived = true;
                }
                if (cntBytAct == 0){
                    break;
                }
            }
            return ((cntBytTot > bytesToRead) ? bytesToRead : cntBytTot);
    }
        return -1;
    }
} // class GlobalsatDg100
