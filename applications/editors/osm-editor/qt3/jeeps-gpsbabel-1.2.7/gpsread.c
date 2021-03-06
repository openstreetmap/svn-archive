/********************************************************************
** @source JEEPS packet reading and acknowledging functions
**
** @author Copyright (C) 1999 Alan Bleasby
** @version 1.0 
** @modified Dec 28 1999 Alan Bleasby. First version
** @@
** 
** This library is free software; you can redistribute it and/or
** modify it under the terms of the GNU Library General Public
** License as published by the Free Software Foundation; either
** version 2 of the License, or (at your option) any later version.
** 
** This library is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Library General Public License for more details.
** 
** You should have received a copy of the GNU Library General Public
** License along with this library; if not, write to the
** Free Software Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA  02111-1307, USA.
********************************************************************/
#include "gps.h"
#include "gpsusbint.h"
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <errno.h>


/* @func GPS_Time_Now ***********************************************
**
** Get current time
**
** @return [time_t] number of bytes read
**********************************************************************/

time_t GPS_Time_Now(void)
{
    time_t secs;

    if(time(&secs)==-1)
    {
	perror("time");
	GPS_Error("GPS_Time_Now: Error reading time");
	gps_errno = HARDWARE_ERROR;
	return 0;
    }

    return secs;
}







/* @func GPS_Packet_Read ***********************************************
**
** Read a packet
**
** @param [r] fd [int32] file descriptor
** @param [w] packet [GPS_PPacket *] packet string
**
** @return [int32] number of bytes read
**********************************************************************/

int32 GPS_Packet_Read(int32 fd, GPS_PPacket *packet)
{
    time_t start;
    int32  n;
    int32  len;
    UC     u;
    int32  isDLE;
    UC     *p;
    int32  i;
    UC     chk=0;
    const char *m1;
    const char *m2;
    
    len = 0;
    isDLE = gpsFalse;
    p = (*packet)->data;

    if (gps_is_usb) {
	    return GPS_Packet_Read_usb(fd, packet);
    }
    
    start = GPS_Time_Now();
    GPS_Diag("Rx Data:");
    while(GPS_Time_Now() < start+GPS_TIME_OUT)
    {
	if((n=GPS_Serial_Chars_Ready(fd)))
	{
	    if(GPS_Serial_Read(fd,&u,1)==-1)
	    {
		perror("read");
		GPS_Error("GPS_Packet_Read: Read error");
		gps_errno = FRAMING_ERROR;
		return 0;
	    }

	    GPS_Diag("%02x ", u);

	    if(!len)
	    {
		(*packet)->dle = u;
		if(u != DLE)
		{
		    (void) fprintf(stderr,"GPS_Packet_Read: No DLE\n");
		    (void) fflush(stderr);
		    return 0;
		}
		++len;
		continue;
	    }

	    if(len==1)
	    {
		(*packet)->type = u;
		++len;
		continue;
	    }
	    
	    if(u == DLE)
	    {
		if(isDLE)
		{
		    isDLE = gpsFalse;
		    continue;
		}
		isDLE = gpsTrue;
	    }

	    if(len == 2)
	    {
		(*packet)->n = u;
		len = -1;
		continue;
	    }

	    if(u == ETX)
		if(isDLE)
		{
		    (*packet)->edle = DLE;
		    (*packet)->etx = ETX;
		    if(p-(*packet)->data-2 != (*packet)->n)
		    {
			GPS_Error("GPS_Packet_Read: Bad count");
			gps_errno = FRAMING_ERROR;
			return 0;
		    }
		    (*packet)->chk = *(p-2);

		    for(i=0,p=(*packet)->data;i<(*packet)->n;++i)
			chk -= *p++;
		    chk -= (*packet)->type;
		    chk -= (*packet)->n;
		    if(chk != (*packet)->chk)
		    {
			GPS_Error("CHECKSUM: Read error\n");
			gps_errno = FRAMING_ERROR;
			return 0;
		    }
		    
		    m1 = Get_Pkt_Type((*packet)->type, (*packet)->data[0], &m2);
		    GPS_Diag("(%-8s%s)\n", m1, m2 ? m2 : "");
		    return (*packet)->n;
		}
		
	    *p++ = u;
	}
    }
    
	    
    GPS_Error("GPS_Packet_Read: Time-out");
    gps_errno = SERIAL_ERROR;

    return 0;
}



/* @func GPS_Get_Ack *************************************************
**
** Check that returned packet is an ack for the packet sent
**
** @param [r] fd [int32] file descriptor
** @param [r] tra [GPS_PPacket *] packet just transmitted
** @param [r] rec [GPS_PPacket *] packet to receive
**
** @return [int32] true if ACK
**********************************************************************/

int32 GPS_Get_Ack(int32 fd, GPS_PPacket *tra, GPS_PPacket *rec)
{
    if (gps_is_usb) {
	    return 1;
    }

    if(!GPS_Packet_Read(fd, rec))
	return 0;

    if(LINK_ID[0].Pid_Ack_Byte != (*rec)->type)
    {
	gps_error = FRAMING_ERROR;
/* rjl	return 0; */
    }
    
    if(*(*rec)->data != (*tra)->type)
    {
	gps_error = FRAMING_ERROR;
	return 0;
    }

    return 1;
}
