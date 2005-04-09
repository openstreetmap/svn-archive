/********************************************************************
** @source JEEPS NMEA functions
**
** @author Copyright (C) 2000 Alan Bleasby
** @version 1.0 
** @modified June 29th 2000 Alan Bleasby. First version
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
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>

#ifndef BADSIG
#define BADSIG (void(*)(int))-1
#endif

#define APB 0
#define BOD 1
#define BWC 2
#define BWR 3
#define DBT 4
#define GGA 5
#define GLL 6
#define GSA 7
#define GSV 8
#define HDM 9
#define HSC 10
#define MTW 11
#define R00 12
#define RMB 13
#define RMC 14
#define RTE 15
#define VHW 16
#define VWR 17
#define VTG 18
#define WPL 19
#define XTE 20
#define XTR 21
#define RME 22
#define RMZ 23
#define RMM 24
#define LIB 25



static int GPS_NMEA_Buffer(UC *buf, int32 cnt);
static void GPS_NMEA_Get_Sentence(UC *sentence);
static int32 GPS_NMEA_Buffer_Shift(void);
static void GPS_NMEA_Alarm_Read(void);
static int32 GPS_NMEA_Get_Code(const char *s);


static int32 gps_nmea_data_valid=gpsFalse;
static UC gps_nmea_buf[BUFSIZ];
static int32 gps_nmea_count;

int32 gps_fd;
GPS_PNmea gps_nmea;




/* @func GPS_NMEA_Line_Check ***********************************************
**
** Validates an NMEA line
**
** @param [r] s [const char *] line to check
**
** @return [int32] false upon error
************************************************************************/

int32 GPS_NMEA_Line_Check(const char *s)
{
    char  *p;
    int32 c;
    int32 ck;
    
    p=(char *)s;
    if(*p!='$')
	return gpsFalse;
    
    ck=0;
    ++p;
    while(*p && (c=*p++)!='*')
	ck ^= c;

    if(*(p-1)!='*')
	return gpsFalse;

    sscanf(p,"%x",&c);
    if(c!=ck)
	return gpsFalse;

    return gpsTrue;
}







/* @func GPS_NMEA_Load *************************************************
**
** Receive new NMEA data. Parse complete sentences.
**
** @param [r] fd [int32] file descriptor
**
** @return [int32] True=data processed False=no data processed
************************************************************************/

int32 GPS_NMEA_Load(int32 fd)
{
    int32 cnt;
    static UC buf[BUFSIZ];
    static UC sentence[83];
    int32 n;
    int32 i;
    int32 ret;
    
    cnt = read(fd,(void *)buf,sizeof(buf));
    if(cnt==-1)
	return SERIAL_ERROR;

    if(!(n=GPS_NMEA_Buffer(buf,cnt)))
	return gpsFalse;

    for(i=0;i<n;++i)
    {
	GPS_NMEA_Get_Sentence(sentence);

	if(GPS_NMEA_Line_Check(sentence))
	{
	    if(gps_show_bytes)
	    {
		fprintf(stdout,"%s\n",sentence);
		fflush(stdout);
	    }
	
	    switch(GPS_NMEA_Get_Code(sentence))
	    {
	    case APB:
		GPS_NMEA_Apb_Scan(sentence,&gps_nmea->apb);
		break;
	    case BOD:
		GPS_NMEA_Bod_Scan(sentence,&gps_nmea->bod);
		break;
	    case BWC:
		GPS_NMEA_Bwc_Scan(sentence,&gps_nmea->bwc);
		break;
	    case BWR:
		GPS_NMEA_Bwr_Scan(sentence,&gps_nmea->bwr);
		break;
	    case DBT:
		GPS_NMEA_Dbt_Scan(sentence,&gps_nmea->dbt);
		break;
	    case GGA:
		GPS_NMEA_Gga_Scan(sentence,&gps_nmea->gga);
		break;
	    case GLL:
		GPS_NMEA_Gll_Scan(sentence,&gps_nmea->gll);
		break;
	    case GSA:
		GPS_NMEA_Gsa_Scan(sentence,&gps_nmea->gsa);
		break;
	    case GSV:
		GPS_NMEA_Gsv_Scan(sentence,&gps_nmea->gsv);
		break;
	    case HDM:
		GPS_NMEA_Hdm_Scan(sentence,&gps_nmea->hdm);
		break;
	    case HSC:
		GPS_NMEA_Hsc_Scan(sentence,&gps_nmea->hsc);
		break;
	    case MTW:
		GPS_NMEA_Mtw_Scan(sentence,&gps_nmea->mtw);
		break;
	    case R00:
		GPS_NMEA_R00_Scan(sentence,&gps_nmea->r00);
		break;
	    case RMB:
		GPS_NMEA_Rmb_Scan(sentence,&gps_nmea->rmb);
		break;
	    case RMC:
		GPS_NMEA_Rmc_Scan(sentence,&gps_nmea->rmc);
		break;
	    case RTE:
		GPS_NMEA_Rte_Scan(sentence,&gps_nmea->rte);
		break;
	    case VHW:
		GPS_NMEA_Vhw_Scan(sentence,&gps_nmea->vhw);
		break;
	    case VWR:
		GPS_NMEA_Vwr_Scan(sentence,&gps_nmea->vwr);
		break;
	    case VTG:
		GPS_NMEA_Vtg_Scan(sentence,&gps_nmea->vtg);
		break;
	    case WPL:
		GPS_NMEA_Wpl_Scan(sentence,&gps_nmea->wpl);
		break;
	    case XTE:
		GPS_NMEA_Xte_Scan(sentence,&gps_nmea->xte);
		break;
	    case XTR:
		GPS_NMEA_Xtr_Scan(sentence,&gps_nmea->xtr);
		break;
	    case RME:
		GPS_NMEA_Rme_Scan(sentence,&gps_nmea->rme);
		break;
	    case RMZ:
		GPS_NMEA_Rmz_Scan(sentence,&gps_nmea->rmz);
		break;
	    case RMM:
		GPS_NMEA_Rmm_Scan(sentence,&gps_nmea->rmm);
		break;
	    case LIB:
		GPS_NMEA_Lib_Scan(sentence,&gps_nmea->lib);
		break;
	    default:
		return PROTOCOL_ERROR;
	    }
	}

	if((ret=GPS_NMEA_Buffer_Shift())<0)
	    return ret;	/* Set data invalid instead? */
    }

    return gpsTrue;
}






/* @funcstatic GPS_NMEA_Buffer *****************************************
**
** Buffer serial data in gps_nmea_buf. Return the number of complete
** sentences
**
** @param [r] buf [UC *] latest data from serial port
** @param [r] cnt [int32] number of bytes in buf
**
** @return [int32] number of complete sentences
************************************************************************/

static int GPS_NMEA_Buffer(UC *buf, int32 cnt)
{
    int32 i;
    UC *p;
    int nc;
    
    p=buf;
    
    if(!gps_nmea_data_valid)
    {
	for(i=0;i<cnt;++i)
	    if(*p=='$')
	    {
		memmove(gps_nmea_buf,p,(gps_nmea_count=cnt-i));
		gps_nmea_data_valid=gpsTrue;
		break;
	    }
	if(i==cnt)
	    return 0;
    }
    else
    {
	memcpy(&gps_nmea_buf[gps_nmea_count],p,cnt);
	gps_nmea_count += cnt;
    }
    
    for(i=0,nc=0,p=gps_nmea_buf;i<gps_nmea_count;++i)
	if(*p++ == 0xa)
	    ++nc;

    return nc;
}





/* @funcstatic GPS_NMEA_Buffer_Shift ***********************************
**
** Shift buffer down removing first sentence
**
** @return [int32] True=success <0 if buffer doesn't start with $
************************************************************************/
static int32 GPS_NMEA_Buffer_Shift(void)
{
    int32 idx;
    UC *p;
    
    if(!gps_nmea_data_valid)
	return gpsFalse;

    if(*gps_nmea_buf!='$')
    {
	fprintf(stderr,"NO DOLLAR!\n");
	return FRAMING_ERROR;
    }
    

    p=gps_nmea_buf;
    while(*p != 0x0a) ++p;
    idx = p-gps_nmea_buf+1;
    memmove(gps_nmea_buf,gps_nmea_buf+idx,gps_nmea_count-idx);
    gps_nmea_count -= idx;

    return gpsTrue;
}






/* @funcstatic GPS_NMEA_Get_Sentence ***********************************
**
** Retrieve a sentence from the buffer
**
** @return [int32] True=success <0 if buffer doesn't start with $
************************************************************************/

static void GPS_NMEA_Get_Sentence(UC *sentence)
{
    UC *p;
    UC *q;
    int32 ischk;
    UC c;
    
    p=gps_nmea_buf;
    q=sentence;
    ischk=0;
    
    while((c=(*q++=*p++))!=0x0a)
	if(c=='*' && *(p+2)<0x0e)
	    ischk=1;
    *(q-2)='\0';
    
    if(!ischk)
	GPS_NMEA_Add_Checksum(sentence);

    return;
}




/* @funcstatic GPS_NMEA_Add_Checksum ***********************************
**
** Add a checksum to a sentence
**
** @param [w] s [char *] sentence to add  checksum to
**
** @return [void]
************************************************************************/
void GPS_NMEA_Add_Checksum(char *s)
{
    int sum;
    UC *p;

    p = s+1;
    sum=0;
    while(*p)
	sum ^= *p++;
	
    sprintf(p,"*%02X",sum);
    return;
}





/* @func GPS_NMEA_Init ***** *******************************************
**
** Sets up signal based NMEA reading
**
** @param [r] s [const char *] port e.g. /dev/ttyS0
**
** @return [int32] -ve upon error
************************************************************************/

int32 GPS_NMEA_Init(const char *s)
{

    GPS_Serial_On_NMEA(s,&gps_fd);
    gps_nmea = GPS_Nmea_New();
    
    if(signal(SIGALRM,(void(*)(int))GPS_NMEA_Alarm_Read)==BADSIG)
	return HARDWARE_ERROR;
    alarm(1);

    return gpsFalse;
}



/* @func GPS_NMEA_Alarm_Read *******************************************
**
** Sets up signal based NMEA reading
**
** @param [r] s [const char *] port e.g. /dev/ttyS0
**
** @return [int32] -ve upon error
************************************************************************/

static void GPS_NMEA_Alarm_Read(void)
{
    if(GPS_Serial_Chars_Ready(gps_fd))
	GPS_NMEA_Load(gps_fd);

    signal(SIGALRM,(void(*)(int))GPS_NMEA_Alarm_Read);
    alarm(1);

    return;
}




/* @funcstatic GPS_NMEA_Get_Code ***************************************
**
** Get sentence code index
**
** @param [r] s [const char *] sentence
**
** @return [int32] index or -1
************************************************************************/

static int32 GPS_NMEA_Get_Code(const char *s)
{
    static char *gps_code[]=
    {
    "APB","BOD","BWC","BWR","DBT","GGA","GLL","GSA","GSV","HDM","HSC",
    "MTW","R00","RMB","RMC","RTE","VHW","VWR","VTG","WPL","XTE","XTR",
    "RME","RMZ","RMM","LIB",NULL
    };
    int i;
    char *p;
    
    
    i=0;
    p=(char *)s+3;
    while(gps_code[i])
    {
	if(!strncmp(p,gps_code[i],3))
	    break;
	++i;
    }

    if(!gps_code[i])
	return -1;

    return i;
}




/* @func GPS_NMEA_Exit ***********************************************
**
** Close the NMEA interface
**
** @return [void]
************************************************************************/

void GPS_NMEA_Exit(void)
{
    signal(SIGALRM,SIG_DFL);
    close(gps_fd);
    GPS_Nmea_Del(&gps_nmea);
    return;
}




/* @func GPS_NMEA_Sendk ***********************************************
**
** Send an NMEA sentence to the connected device
**
** @param [r] s [const char *] sentence
** @param [r] flag [int32] add checksum if true
**
** @return [int32] -ve if error
************************************************************************/

int32 GPS_NMEA_Send(const char *s, int32 flag)
{
    char t[83];
    int len;
    
    strcpy(t,s);
    if(flag)
	GPS_NMEA_Add_Checksum(t);
    strcat(t,"\r\n");
    
    len=strlen(t);
    if(write(gps_fd,(const void *)t,(size_t)len)==-1)
	return SERIAL_ERROR;

    return 0;
}
