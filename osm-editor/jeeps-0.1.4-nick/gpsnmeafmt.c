/********************************************************************
** @source JEEPS NMEA format functions
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
#include <time.h>


static int32  gpsempty(char *s);
static void   nmeacpy(char *s,const char *t);
static double nmeaconv(char *s,char c);

static int32 gps_gsvlast=0;
static int32 gps_rtelast=0;



/* @func GPS_NMEA_Apb_Scan ***********************************************
**
** Translate an apb sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PApb *] apb object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Apb_Scan(const char *s, GPS_PApb *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	(*thys)->blink = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->warn = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->edist);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->steer = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->unit = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->alarmc = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->alarmp = *(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->od);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->wpt,p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->pd);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->hdg);


    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Bod_Scan ***********************************************
**
** Translate a bod sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PBod *] bod object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Bod_Scan(const char *s, GPS_PBod *thys)
{
    char *p;
    
    p=strchr(s,(int)',');

    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);
    
    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->mag);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->dest,p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->start,p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Bwc_Scan ***********************************************
**
** Translate a bwc sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PBwc *] bwc object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Bwc_Scan(const char *s, GPS_PBwc *thys)
{
    char *p;
    struct tm *t;
    char u[83];
    int32 h;
    int32 m;
    int32 se;
    time_t a;
    
    p=strchr(s,(int)',');

    if(!gpsempty(p+1))
    {
	time(&a);
	t = localtime(&a);
	sscanf(p+1,"%2d%2d%2d",&h,&m,&se);
	t->tm_sec=se;
	t->tm_min=m;
	t->tm_hour=h;
	(*thys)->time = mktime(t);
    }
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->mag);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->dist);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->wpt,p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Bwr_Scan ***********************************************
**
** Translate a bwr sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PBwr *] bwc object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Bwr_Scan(const char *s, GPS_PBwr *thys)
{
    char *p;
    struct tm *t;
    char u[83];
    int32 h;
    int32 m;
    int32 se;
    time_t a;
    
    p=strchr(s,(int)',');

    if(!gpsempty(p+1))
    {
	time(&a);
	t = localtime(&a);
	sscanf(p+1,"%2d%2d%2d",&h,&m,&se);
	t->tm_sec=se;
	t->tm_min=m;
	t->tm_hour=h;
	(*thys)->time = mktime(t);
    }
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->mag);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->dist);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->wpt,p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Dbt_Scan ***********************************************
**
** Translate a dbt sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PDbt *] bwc object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Dbt_Scan(const char *s, GPS_PDbt *thys)
{
    char *p;
    
    p=strchr(s,(int)',');

    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->f);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->m);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Gga_Scan ***********************************************
**
** Translate a gga sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PGga *] gga object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Gga_Scan(const char *s, GPS_PGga *thys)
{
    char *p;
    struct tm *t;
    char u[83];
    int32 h;
    int32 m;
    int32 se;
    time_t a;
    
    p=strchr(s,(int)',');

    if(!gpsempty(p+1))
    {
	time(&a);
	t = localtime(&a);
	sscanf(p+1,"%2d%2d%2d",&h,&m,&se);
	t->tm_sec=se;
	t->tm_min=m;
	t->tm_hour=h;
	(*thys)->time = mktime(t);
    }

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->qual);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->nsat);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->hdil);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->alt);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->galt);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->last);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->dgpsid);
    
    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Gll_Scan ***********************************************
**
** Translate a gll sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PGll *] gll object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Gll_Scan(const char *s, GPS_PGll *thys)
{
    char *p;
    struct tm *t;
    char u[83];
    int32 h;
    int32 m;
    int32 se;
    time_t a;
    

    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
    {
	time(&a);
	t = localtime(&a);
	sscanf(p+1,"%2d%2d%2d",&h,&m,&se);
	t->tm_sec=se;
	t->tm_min=m;
	t->tm_hour=h;
	(*thys)->time = mktime(t);
    }

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->dv=*(p+1);

    (*thys)->valid=1;

    return gpsTrue;
}




/* @func GPS_NMEA_Gsa_Scan ***********************************************
**
** Translate a gsa sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PGsa *] gsa object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Gsa_Scan(const char *s, GPS_PGsa *thys)
{
    char *p;
    int32  i;
    int32  c;
    int32  v;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	(*thys)->type=*(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->fix);
    
    p=strchr(p+1,(int)',');
    for(i=0,c=0;i<12;++i)
    {
	if(!gpsempty(p+1))
	{
	    sscanf(p+1,"%d",&v);
	    (*thys)->prn[c++]=v;
	}
	p=strchr(p+1,(int)',');
    }
    (*thys)->nsat = c;
    
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->pdop);

    p=strchr(p+1,(int)',');	    
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->hdop);

    p=strchr(p+1,(int)',');	    
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->vdop);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Gsv_Scan ***********************************************
**
** Translate a gsv sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PGsv *] gsv object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Gsv_Scan(const char *s, GPS_PGsv *thys)
{
    char *p;
    int32  ns;
    int32  n;
    static int32 c;
    int32  i;
    int32  v;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&ns);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&n);

    if(n!=gps_gsvlast+1)
    {
	gps_gsvlast=0;
	return gpsFalse;
    }
    if(!gps_gsvlast)
	c=0;
    ++gps_gsvlast;


    p=strchr(p+1,(int)',');
    if(gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->inview);
    
    p=strchr(p+1,(int)',');
    for(i=0;i<4;++i)
    {
	if(gpsempty(p+1))
	    break;
	sscanf(p+1,"%d",&(*thys)->prn[c]);
	p=strchr(p+1,(int)',');
	sscanf(p+1,"%d",&(*thys)->elevation[c]);
	p=strchr(p+1,(int)',');
	sscanf(p+1,"%d",&(*thys)->azimuth[c]);
	p=strchr(p+1,(int)',');
	sscanf(p+1,"%d",&(*thys)->strength[c++]);
	v=strcspn(p+1,"*,");
	p+=v+2;
    }
    
    if(n==ns)
    {
	(*thys)->inview=c;
	gps_gsvlast=0;
	(*thys)->valid=gpsTrue;
	return gpsTrue;
    }
    
    (*thys)->valid=gpsFalse;
    return gpsFalse;
}




/* @func GPS_NMEA_Hdm_Scan ***********************************************
**
** Translate an hdm sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PHdm *] hdm object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Hdm_Scan(const char *s, GPS_PHdm *thys)
{
    char *p;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->hdg);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Hsc_Scan ***********************************************
**
** Translate an hsc sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PHsc *] hsc object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Hsc_Scan(const char *s, GPS_PHsc *thys)
{
    char *p;
    char u[83];
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->tru = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->mag = nmeaconv(u,*(p+1));

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Mtw_Scan ***********************************************
**
** Translate an mtw sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PMtw *] mtw object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Mtw_Scan(const char *s, GPS_PMtw *thys)
{
    char *p;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->T);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_R00_Scan ***********************************************
**
** Translate an r00 sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PR00 *] r00 object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_R00_Scan(const char *s, GPS_PR00 *thys)
{
    char *p;
    char *q;
    int32 len;
    
    
    p=strchr(s,(int)',');
    ++p;
    q=p;
    while(*q!='*')
    {
	if(*q==',' && *(q+1)==',')
	    break;
	++q;
    }
    len=q-p;
    strncpy((*thys)->wpts,p,len);
    (*thys)->wpts[len]='\0';

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Rmb_Scan ***********************************************
**
** Translate an rmb sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRmb *] rmb object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rmb_Scan(const char *s, GPS_PRmb *thys)
{
    char *p;
    char u[83];
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	(*thys)->warn=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->cross);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->correct=*(p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->owpt,p+1);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->dwpt,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->range);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->velocity);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->alarm=*(p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Rmc_Scan ***********************************************
**
** Translate an rmc sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRmc *] rmc object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rmc_Scan(const char *s, GPS_PRmc *thys)
{
    char *p;
    char u[83];
    struct tm *t;
    struct tm z;
    int32 h;
    int32 m;
    int32 se;
    int32 y;
    int32 d;
    time_t a;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
    {
	time(&a);
	t = localtime(&a);
	sscanf(p+1,"%2d%2d%2d",&h,&m,&se);
	t->tm_sec=se;
	t->tm_min=m;
	t->tm_hour=h;
	(*thys)->time = mktime(t);
    }


    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->warn=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->speed);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->cmg);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
    {
	sscanf(p+1,"%2d%2d%2d",&d,&m,&y);
	z.tm_mday=d;
	z.tm_mon=m-1;
	if(y<70) y+=100;
	z.tm_year=y;
	z.tm_hour=0;
	z.tm_min=0;
	z.tm_sec=0;
	
	a = mktime(&z);
	strcpy((*thys)->date,ctime(&a));
    }

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->magvar);

    p=strchr(p+1,(int)',');
    if(*(p+1)=='W')
	(*thys)->magvar *= (double)-1.;
    
    (*thys)->valid = gpsTrue;
    return gpsTrue;
}




/* @func GPS_NMEA_Rte_Scan ***********************************************
**
** Translate an rte sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRte *] rte object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rte_Scan(const char *s, GPS_PRte *thys)
{
    char *p;
    char *q;
    char u[83];
    int32  ns;
    int32  n;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&ns);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&n);

    if(n!=gps_rtelast+1)
    {
	gps_rtelast=0;
	return gpsFalse;
    }

    if(!gps_rtelast)
    {
	if((*thys)->wpts)
	    free((void *)(*thys)->wpts);
	if(!((*thys)->wpts=(char *)malloc(ns*83)))
	    return gpsFalse;
	*(*thys)->wpts='\0';
    }

    ++gps_rtelast;
    if(gps_rtelast!=1)
	strcat((*thys)->wpts,",");
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->type=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->rte);

    if(!(p=strchr(p+1,(int)',')))
	return gpsFalse;
    
    ++p;
    q=u;
    while( (*q++=*p++)!='*');
    *(q-1)='\0';
    strcat((*thys)->wpts,u);
    
    if(n==ns)
    {
	gps_rtelast=0;
	(*thys)->valid=gpsTrue;
	return gpsTrue;
    }
    
    (*thys)->valid=gpsFalse;
    return gpsFalse;
}




/* @func GPS_NMEA_Vhw_Scan ***********************************************
**
** Translate an vhw sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PVhw *] vhw object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Vhw_Scan(const char *s, GPS_PVhw *thys)
{
    char *p;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->mag);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->wspeed);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->speed);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Vwr_Scan ***********************************************
**
** Translate an vwr sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PVwr *] vwr object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Vwr_Scan(const char *s, GPS_PVwr *thys)
{
    char *p;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->wind);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->wdir=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->knots);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->ms);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->khr);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Vtg_Scan ***********************************************
**
** Translate an vtg sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PVtg *] vtg object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Vtg_Scan(const char *s, GPS_PVtg *thys)
{
    char *p;

    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->tru);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->mag);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->knots);

    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->khr);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Wpl_Scan ***********************************************
**
** Translate an wpl sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PWpl *] wpl object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Wpl_Scan(const char *s, GPS_PWpl *thys)
{
    char *p;
    char u[83];
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lat = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy(u,p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->lon = nmeaconv(u,*(p+1));

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->wpt,p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Xte_Scan ***********************************************
**
** Translate an xte sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PXte *] xte object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Xte_Scan(const char *s, GPS_PXte *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	(*thys)->warn=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->cycle=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->dist);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->steer=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->unit=*(p+1);
    
    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Xtr_Scan ***********************************************
**
** Translate an xtr sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PXtr *] xtr object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Xtr_Scan(const char *s, GPS_PXtr *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->dist);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->steer=*(p+1);
    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->unit=*(p+1);
    
    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Rme_Scan ***********************************************
**
** Translate an rme sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRme *] rme object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rme_Scan(const char *s, GPS_PRme *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->hpe);
    
    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->vpe);
    
    p=strchr(p+1,(int)',');
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->spe);
    
    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Rmz_Scan ***********************************************
**
** Translate an rmz sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRmz *] rmz object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rmz_Scan(const char *s, GPS_PRmz *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->height);

    p=strchr(p+1,(int)',');    
    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%d",&(*thys)->dim);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Rmm_Scan ***********************************************
**
** Translate an rmm sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PRmm *] rmm object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Rmm_Scan(const char *s, GPS_PRmm *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	nmeacpy((*thys)->datum,p+1);

    (*thys)->valid = gpsTrue;

    return gpsTrue;
}




/* @func GPS_NMEA_Lib_Scan ***********************************************
**
** Translate an lib sentence
**
** @param [r] s [const char *] sentence
** @param [w] thys [GPS_PLib *] lib object
**
** @return [int32] false upon error
************************************************************************/
int32 GPS_NMEA_Lib_Scan(const char *s, GPS_PLib *thys)
{
    char *p;
    
    p=strchr(s,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->freq);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	sscanf(p+1,"%lf",&(*thys)->baud);

    p=strchr(p+1,(int)',');
    if(!gpsempty(p+1))
	(*thys)->rqst=*(p+1);
    
    (*thys)->valid = gpsTrue;

    return gpsTrue;
}

    


    






/* @funcstatic gpsempty ***********************************************
**
** Retrns true if an nmea field is empy, false otherwise
**
** @param [r] s [const char *] filed 
**
** @return [int32] false if field contains data
************************************************************************/

static int32 gpsempty(char *s)
{
    if(*s==',' || *s=='*')
	return gpsTrue;

    return gpsFalse;
}




/* @funcstatic nmeacpy ***********************************************
**
** Copy a string up to a , or * delimiters
**
** @param [w] s [char *] dest
** @param [r] t [const char *] src
**
** @return [void]
************************************************************************/

static void nmeacpy(char *s,const char *t)
{
    char c;
    
    while( (c=(*s++=*t++))!=',' && c!='*');
    *(s-1)='\0';
    return;
}




/* @funcstatic nmeaconv ***********************************************
**
** Convert lat/lon in (x)xx.yyy N/S/W/E format to a real
**
** @param [w] s [char *] deg+min+sec
** @param [r] c [char] compass value
**
** @return [double] lat/lon
************************************************************************/

static double nmeaconv(char *s,char c)
{
    int32 deg;
    double min;
    double v;
    
    if(c=='N' || c=='S')
    {
	sscanf(s,"%2d%lf",&deg,&min);
	GPS_Math_DegMin_To_Deg(deg,min,&v);
	if(c=='S')
	    v = -v;
    }
    else
    {
	sscanf(s,"%3d%lf",&deg,&min);
	GPS_Math_DegMin_To_Deg(deg,min,&v);
	if(c=='W')
	    v = -v;
    }

    return v;
}
