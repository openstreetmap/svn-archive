/**
 * @author Stefan Kaintoch
 * @version $Id: Response.java 6 2007-11-28 20:55:14Z ramack $
 */
package org.kaintoch.gps.globalsat.dg100;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author skaintoch
 *
 */
public class Response
{
    final static public byte typeFileInfo = (byte)0xBB;
    final static public byte typeId = (byte)0xBF;
    final static public byte typeGpsRec = (byte)0xB5;
    final static public byte typeConfig = (byte)0xB7;

    private int typeOfResponse = 0;
    private int cntDataCur = 0;
    private int nextIdx = 0;
    private Dg100Config config = null;
    private List data = new ArrayList(100);
    private long id = 0;

    private Response(int typeOfResponse)
    {
        this.typeOfResponse = typeOfResponse;
    }

    /**
     *
     * @param resp
     */
    static public Response parseResponse(byte resp[], int len)
    {
        ByteBuffer buf = ByteBuffer.wrap(resp);
        byte respType = buf.get(4);
        Response response = new Response(respType);
        buf.position(5);
        if (respType == typeFileInfo) // file info
        {
            int cntInfoCur = buf.getShort();
            int nextIdx = buf.getShort();
            response.cntDataCur = cntInfoCur;
            response.nextIdx = nextIdx;
            for (int ii = 0 ; ii < cntInfoCur ; ++ii)
            {
                FileInfoRec fileInfoRec = new FileInfoRec(buf);
                response.addRec(fileInfoRec);
            }
        }
        else if (respType == typeGpsRec) // gps recs
        {
            int recType = 2;
            // read part 1
            while (buf.position() <= len)
            {
                GpsRec gpsRec = new GpsRec(buf, recType);
                if (gpsRec.isValid() && buf.position() <= len)
                {
                    response.addRec(gpsRec);
                }
                else
                {
                    break;
                }
                recType = gpsRec.getDg100TypeOfNextRec();
            }
            // read part 2
            buf.position(1042);
            while (buf.position() <= len)
            {
                GpsRec gpsRec = new GpsRec(buf, recType);
                if (gpsRec.isValid() && buf.position() <= len)
                {
                    response.addRec(gpsRec);
                }
                else
                {
                    break;
                }
                recType = gpsRec.getDg100TypeOfNextRec();
            }
        }
        else if (respType == typeConfig) // config
        {
            Dg100Config config = new Dg100Config(buf);
            response.config = config;
        }
        else if (respType == typeId) // id
        {
            response.id = 0;
            for (int ii = 0 ; ii < 8 ; ++ii)
            {
                int digit = (int)buf.get();
                response.id = response.id * 10 + digit;
            }
        }
        else
        {
        }
        return response;
    }

    private void addRec(Object obj)
    {
        data.add(obj);
    }

    public List getRecs()
    {
        return data;
    }

    /**
     * @return Returns the cntDataCur.
     */
    public int getCntDataCur()
    {
        return cntDataCur;
    }

    /**
     * @return Returns the nextIdx.
     */
    public int getNextIdx()
    {
        return nextIdx;
    }

    /**
     * @return Returns the typeOfResponse.
     */
    public int getTypeOfResponse()
    {
        return typeOfResponse;
    }

    /**
     * @return Returns the config.
     */
    public Dg100Config getConfig()
    {
        return config;
    }

    /**
     * @return Returns the id.
     */
    public long getId()
    {
        return id;
    }

}
