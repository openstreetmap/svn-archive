/* @source jeeps application
**
** Demonstration utility for libjeeps
** @author: Copyright (C) Alan Bleasby (ableasby@hgmp.mrc.ac.uk)
** @@
**
** This program is not meant to be pretty. It is provided as an example
** of how to use the library. It is known to work with GPS II+ and
** GPS III models and should work with others, that being the point
** of the library.
**
** This program is free software; you can redistribute it and/or
** modify it under the terms of the GNU General Public License
** as published by the Free Software Foundation; either version 2
** of the License, or (at your option) any later version.
** 
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
******************************************************************************/

#include <stdio.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>

#include "../lib/gps.h"
#include "jeeps.h"


static void Init_Jeeps(char *port);
static int32  Jeeps_Menu(void);
static void Print_Error(const int32 err);
static void Press_Return(void);


static void Do_Almrec(const char *port);
static void Do_Almtra(const char *port);
static void Do_Wayrec(const char *port);
static void Do_Waytra(const char *port);
static void Do_Rourec(const char *port);
static void Do_Routra(const char *port);
static void Do_Trkrec(const char *port);
static void Do_Trktra(const char *port);
static void Do_Prxrec(const char *port);
static void Do_Prxtra(const char *port);
static void Do_Timrec(const char *port);
static void Do_Timtra(const char *port);
static void Do_Psnrec(const char *port);
static void Do_Psntra(const char *port);
static void Do_Pvtrec(const char *port);
static void GPS_Info(void);
static void Do_Off(const char *port);


FILE *GetOutFileName(char *s, char *prompt);
FILE *GetInFileName(char *s, char *prompt);



static void Init_Jeeps(char *port)
{
    char home[MAXPATHLEN];
    struct stat buf;
    FILE *inf;
    char line[MAXPATHLEN];
    
    char *p;

    (void) strcpy(stdalm,DEF_ALM_FILE);
    (void) strcpy(stdway,DEF_WAY_FILE);
    (void) strcpy(stdrou,DEF_ROU_FILE);
    (void) strcpy(stdtrk,DEF_TRK_FILE);
    (void) strcpy(stdprx,DEF_PRX_FILE);

    
    p = getenv("HOME");
    if(p)
    {
	(void) strcpy(home,p);
	(void) strcat(home,"/");
    }
    else
	(void) strcpy(home,"");
    (void) strcat(home,".jeepsrc");

    if(!stat(home,&buf))
    {
	if(buf.st_mode | S_IRUSR)
	{
	    if((inf=fopen(home,"r")))
	    {
		while(fgets(line,MAXPATHLEN,inf))
		{
		    if(*line=='#' || *line=='\n') continue;
		    line[strlen(line)-1]='\0';
		    
		    if(!strncmp(line,"PORT",4))
			sscanf(&line[4],"%s",port);
		    if(!strncmp(line,"ALMANAC",7))
			sscanf(&line[7],"%s",stdalm);
		    if(!strncmp(line,"ROUTE",5))
			sscanf(&line[5],"%s",stdrou);
		    if(!strncmp(line,"WAYPOINT",8))
			sscanf(&line[8],"%s",stdway);
		    if(!strncmp(line,"PROXIMITY",9))
			sscanf(&line[9],"%s",stdprx);
		    if(!strncmp(line,"TRACK",5))
			sscanf(&line[5],"%s",stdtrk);
		}
	    }
	}
    }
    else
    {
	p = getenv("JEEPSPORT");
	if(p)
	    (void) strcpy(port,p);
	else
	{
	    fprintf(stdout,"Enter serial port [%s]: ",DEF_PORT);
	    fgets(line,MAXPATHLEN,stdin);
	    if(line[0]==0x0a) (void) strcpy(line,DEF_PORT);
	    if(line[strlen(line)-1]=='\n') line[strlen(line)-1]='\0';
	    (void) strcpy(port,line);
	}
    }

    return;
}



void Save_Values(char *port)
{
    char *p;
    char home[MAXPATHLEN];
    FILE *outf;
    
    p = getenv("HOME");
    if(p)
    {
	(void) strcpy(home,p);
	(void) strcat(home,"/");
    }
    else
	(void) strcpy(home,"");
    (void) strcat(home,".jeepsrc");
    if(!(outf=fopen(home,"w")))
	return;

    fprintf(outf,"PORT %s\n",port);
    fprintf(outf,"ALMANAC %s\n",stdalm);
    fprintf(outf,"ROUTE %s\n",stdrou);
    fprintf(outf,"WAYPOINT %s\n",stdway);
    fprintf(outf,"PROXIMITY %s\n",stdprx);
    fprintf(outf,"TRACK %s\n",stdtrk);

    (void) fclose(outf);
    
    return;
}




static int32 Jeeps_Menu(void)
{
    char line[MAXPATHLEN];
    int32  n;
    
    /* Add a system specific screen clear here if you wish */
    (void) fprintf(stdout,"\t\t\t\t\tJEEPS\n\n\t\tSelect:\n\n");
    (void) fprintf(stdout,"\t\t 1\tAlmanac   receive\n");
    (void) fprintf(stdout,"\t\t 2\tAlmanac   transmit\n");
    (void) fprintf(stdout,"\t\t 3\tWaypoint  receive\n");
    (void) fprintf(stdout,"\t\t 4\tWaypoint  transmit\n");
    (void) fprintf(stdout,"\t\t 5\tRoute     receive\n");
    (void) fprintf(stdout,"\t\t 6\tRoute     transmit\n");
    (void) fprintf(stdout,"\t\t 7\tTrack     receive\n");
    (void) fprintf(stdout,"\t\t 8\tTrack     transmit\n");
    (void) fprintf(stdout,"\t\t 9\tProximity receive\n");
    (void) fprintf(stdout,"\t\t10\tProximity transmit\n");
    (void) fprintf(stdout,"\t\t11\tTime      receive\n");
    (void) fprintf(stdout,"\t\t12\tTime      transmit\n");
    (void) fprintf(stdout,"\t\t13\tPosition  receive\n");
    (void) fprintf(stdout,"\t\t14\tPosition  transmit\n");
    (void) fprintf(stdout,"\t\t15\tPVT       receive\n\n");
    (void) fprintf(stdout,"\t\t16\tGPS  Information\n");
    (void) fprintf(stdout,"\t\t17\tSave Preferences\n");
    (void) fprintf(stdout,"\t\t 0\tGPS  Off and Exit\n\n");

    n = 11;
    (void) fprintf(stdout,"\t\tEnter number [11]: ");
    fgets(line,MAXPATHLEN,stdin);
    if(line[0]==10) return n;

    sscanf(line,"%d",(int *)&n);
    if(n<0 || n>17) return -1;

    return n;
}



static void Print_Error(const int32 err)
{
    switch(err)
    {
    case FRAMING_ERROR:
	fprintf(stderr,"Framing error (high level serial communication)\n");
	break;
    case PROTOCOL_ERROR:
	fprintf(stderr,"Unknown protocol. Maybe undocumented by Garmin\n");
	break;
    case HARDWARE_ERROR:
	fprintf(stderr,"Unix system call error\n");
	break;
    case SERIAL_ERROR:
	fprintf(stderr,"Error reading/writing to serial port\n");
	break;
    case MEMORY_ERROR:
	fprintf(stderr,"Ran out of memory or other memory error\n");
	break;
    case GPS_UNSUPPORTED:
	fprintf(stderr,"Your GPS doesn't support this protocol\n");
	break;
    case INPUT_ERROR:
	fprintf(stderr,"Corrupt or wrong format input file\n");
	break;
    default:
	fprintf(stderr,"Unknown library error\n");
	break;
    }

    return;
}





static void Do_Off(const char *port)
{
    int32 ret;
    
    if((ret=GPS_Command_Off(port))<0)
    {
	fprintf(stderr,"Error turning off GPS\n");
	Print_Error(ret);
    }

    return;
}



static void Press_Return(void)
{
    char line[MAXPATHLEN];

    fprintf(stdout,"\n\nPress RETURN to continue: ");
    fgets(line,MAXPATHLEN,stdin);
    fprintf(stdout,"\n\n\n");
    return;
}




FILE *GetOutFileName(char *s, char *prompt)
{
    char line[MAXPATHLEN];
    int32  len;
    FILE *fp;
    
    fprintf(stdout,"%s [%s]: ",prompt,s);
    fgets(line,MAXPATHLEN,stdin);
    if(line[0]==10)
    {
	if(!strcmp(s,"stdout")) return stdout;
	if(!(fp=fopen(s,"w")))
	    fprintf(stderr,"Error opening file: %s\n",s);
	return fp;
    }

    len = strlen(line)-1;
    if(line[len]=='\n') line[len]='\0';

    if(!strcmp(line,"stdout"))
    {
	(void) strcpy(s,line);
	return stdout;
    }
    
    
    if(!(fp=fopen(line,"w")))
	fprintf(stderr,"Error opening file: %s\n",s);
    else
	(void) strcpy(s,line);

    return fp;
}



FILE *GetInFileName(char *s, char *prompt)
{
    char line[MAXPATHLEN];
    int32  len;
    FILE *fp;
    
    fprintf(stdout,"%s [%s]: ",prompt,s);
    fgets(line,MAXPATHLEN,stdin);
    if(line[0]==10)
    {
	if(!strcmp(s,"stdin")) return stdin;
	if(!(fp=fopen(s,"r")))
	    fprintf(stderr,"Error opening file: %s\n",s);
	return fp;
    }

    len = strlen(line)-1;
    if(line[len]=='\n') line[len]='\0';

    if(!strcmp(line,"stdin"))
    {
	(void) strcpy(s,line);
	return stdin;
    }

    
    if(!(fp=fopen(line,"r")))
	fprintf(stderr,"Error opening file: %s\n",s);
    else
	(void) strcpy(s,line);

    return fp;
}




static void Do_Almrec(const char *port)
{
    FILE *outf;
    GPS_PAlmanac *alm;
    int32 n;
    int32 i;
    
    if(!(outf = GetOutFileName(stdalm,"Almanac Output File")))
	return;

    fprintf(stdout,"\nReading almanac... ");
    fflush(stdout);
    
    if((n=GPS_Command_Get_Almanac(port, &alm))<0)
    {
	Print_Error(n);
	return;
    }
    fprintf(stdout," Done\n");

    GPS_Fmt_Print_Almanac(alm, n, outf);

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Almanac_Del(&alm[i]);
	free((void *)alm);
    }

    if(outf != stdout)
	(void) fclose(outf);

    Press_Return();

    return;
}



static void Do_Almtra(const char *port)
{
    FILE *inf;
    GPS_PAlmanac *alm;
    int32 n;
    int32 i;
    int32 ret;
    
    if(!(inf = GetInFileName(stdalm,"Almanac Input File")))
	return;

    if((n=GPS_Input_Get_Almanac(&alm,inf))<0)
    {
	Print_Error(n);
	return;
    }

    fprintf(stdout,"\nSending almanac... ");
    fflush(stdout);
    
    if(n)
	if((ret=GPS_Command_Send_Almanac(port, alm, n))<0)
	{
	    Print_Error(n);
	    return;
	}

    fprintf(stdout," Done\n");

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Almanac_Del(&alm[i]);
	free((void *)alm);
    }

    if(inf != stdin)
	(void) fclose(inf);

    Press_Return();

    return;
}



static void Do_Wayrec(const char *port)
{
    FILE *outf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    
    if(!(outf = GetOutFileName(stdway,"Waypoint Output File")))
	return;

    fprintf(stdout,"\nReading waypoints... ");
    fflush(stdout);
    
    if((n=GPS_Command_Get_Waypoint(port, &way))<0)
    {
	Print_Error(n);
	return;
    }
    fprintf(stdout," Done\n");

    GPS_Fmt_Print_Waypoint(way, n, outf);

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(outf != stdout)
	(void) fclose(outf);

    Press_Return();

    return;
}



static void Do_Waytra(const char *port)
{
    FILE *inf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    int32 ret;
    
    if(!(inf = GetInFileName(stdway,"Waypoint Input File")))
	return;

    if((n=GPS_Input_Get_Waypoint(&way,inf))<0)
    {
	Print_Error(n);
	return;
    }

    fprintf(stdout,"\nSending waypoints... ");
    fflush(stdout);
    
    if(n)
	if((ret=GPS_Command_Send_Waypoint(port, way, n))<0)
	{
	    Print_Error(n);
	    return;
	}

    fprintf(stdout," Done\n");

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(inf != stdin)
	(void) fclose(inf);

    Press_Return();

    return;
}



static void Do_Rourec(const char *port)
{
    FILE *outf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    
    if(!(outf = GetOutFileName(stdrou,"Route Output File")))
	return;

    fprintf(stdout,"\nReading routes... ");
    fflush(stdout);
    
    if((n=GPS_Command_Get_Route(port, &way))<0)
    {
	Print_Error(n);
	return;
    }
    fprintf(stdout," Done\n");

    GPS_Fmt_Print_Route(way, n, outf);

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(outf != stdout)
	(void) fclose(outf);

    Press_Return();

    return;
}



static void Do_Routra(const char *port)
{
    FILE *inf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    int32 ret;
    
    if(!(inf = GetInFileName(stdway,"Route Input File")))
	return;

    if((n=GPS_Input_Get_Route(&way,inf))<0)
    {
	Print_Error(n);
	return;
    }

    fprintf(stdout,"\nSending routes... ");
    fflush(stdout);
    
    if(n)
	if((ret=GPS_Command_Send_Route(port, way, n))<0)
	{
	    Print_Error(n);
	    return;
	}

    fprintf(stdout," Done\n");

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(inf != stdin)
	(void) fclose(inf);

    Press_Return();

    return;
}



static void Do_Trkrec(const char *port)
{
    FILE *outf;
    GPS_PTrack *trk;
    int32 n;
    int32 i;
    
    if(!(outf = GetOutFileName(stdtrk,"Track Output File")))
	return;

    fprintf(stdout,"\nReading tracks... ");
    fflush(stdout);
    
    if((n=GPS_Command_Get_Track(port, &trk))<0)
    {
	Print_Error(n);
	return;
    }
    fprintf(stdout," Done\n");
    
    GPS_Fmt_Print_Track(trk, n, outf);

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Track_Del(&trk[i]);
	free((void *)trk);
    }

    if(outf != stdout)
	(void) fclose(outf);

    Press_Return();

    return;
}



static void Do_Trktra(const char *port)
{
    FILE *inf;
    GPS_PTrack *trk;
    int32 n;
    int32 i;
    int32 ret;
    
    if(!(inf = GetInFileName(stdtrk,"Track Input File")))
	return;

    if((n=GPS_Input_Get_Track(&trk,inf))<0)
    {
	Print_Error(n);
	return;
    }

    fprintf(stdout,"\nSending tracks... ");
    fflush(stdout);
    
    if(n)
	if((ret=GPS_Command_Send_Track(port, trk, n))<0)
	{
	    Print_Error(n);
	    return;
	}

    fprintf(stdout," Done\n");

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Track_Del(&trk[i]);
	free((void *)trk);
    }

    if(inf != stdin)
	(void) fclose(inf);

    Press_Return();

    return;
}



static void Do_Prxrec(const char *port)
{
    FILE *outf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    
    if(!(outf = GetOutFileName(stdprx,"Proximity Output File")))
	return;

    fprintf(stdout,"\nReading proximity waypoints... ");
    fflush(stdout);
    
    if((n=GPS_Command_Get_Proximity(port, &way))<0)
    {
	fprintf(stdout,"\n");
	fflush(stdout);
	Print_Error(n);
	Press_Return();
	return;
    }
    fprintf(stdout," Done\n");

    GPS_Fmt_Print_Proximity(way, n, outf);

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(outf != stdout)
	(void) fclose(outf);

    Press_Return();

    return;
}



static void Do_Prxtra(const char *port)
{
    FILE *inf;
    GPS_PWay *way;
    int32 n;
    int32 i;
    int32 ret;
    
    if(!(inf = GetInFileName(stdprx,"Proximity Input File")))
	return;

    if((n=GPS_Input_Get_Proximity(&way,inf))<0)
    {
	Print_Error(n);
	return;
    }

    fprintf(stdout,"\nSending proximity waypoints... ");
    fflush(stdout);
    
    if(n)
	if((ret=GPS_Command_Send_Proximity(port, way, n))<0)
	{
	    Print_Error(n);
	    return;
	}

    fprintf(stdout," Done\n");

    if(n)
    {
	for(i=0;i<n;++i)
	    GPS_Way_Del(&way[i]);
	free((void *)way);
    }

    if(inf != stdin)
	(void) fclose(inf);

    Press_Return();

    return;
}



static void Do_Timrec(const char *port)
{
    time_t Time;
    char   line[MAXPATHLEN];
    
    if((Time=GPS_Command_Get_Time(port))<0)
    {
	fprintf(stderr,"Error reading time\n");
	Print_Error((int32)Time);
	return;
    }

    (void) strftime(line,MAXPATHLEN,"%b %e %Y %H:%M:%S",localtime(&Time));
    fprintf(stdout,"\n\nGPS time = %s\n\n",line);

    Press_Return();
    return;
}



static void Do_Timtra(const char *port)
{
    time_t Time;
    char line[MAXPATHLEN];
    struct tm ts;
    struct tm *tp;
    
    int32 ret;
    
    
    Time = time(NULL);
    tp = localtime(&Time);

    (void) strftime(line,MAXPATHLEN,"%b %e %Y %H:%M:%S",tp);

    fprintf(stdout,"\t\tEnter time [%s]: ",line);
    (void) fgets(line,MAXPATHLEN,stdin);
    if(line[0]!=10)
    {
	(void) strptime(line,"%b %e %Y %H:%M:%S",&ts);
	Time = mktime(&ts);

    }

    if((ret=GPS_Command_Send_Time(port,Time))<0)
    {
	fprintf(stderr,"Error setting time\n");
	Print_Error(ret);
    }

    Press_Return();
    return;
}



static void Do_Psnrec(const char *port)
{
    double lat;
    double lon;
    int32 ret;

    if((ret=GPS_Command_Get_Position(port,&lat,&lon))<0)
    {
	fprintf(stderr,"Error getting position\n");
	Print_Error(ret);
	return;
    }
    
    fprintf(stdout,"\n\nGPS Latitude  = %-11.6f degrees\n",lat);
    fprintf(stdout,"GPS Longitude = %-11.6f degrees\n\n",lon);

    Press_Return();
    return;
}



static void Do_Psntra(const char *port)
{
    char line[MAXPATHLEN];
    int32 ret;
    double lat;
    double lon;
    
    fprintf(stdout,"\t\tEnter latitude  [%f]: ",gps_save_lat);
    (void) fgets(line,MAXPATHLEN,stdin);
    if(line[0]==10)
	lat = gps_save_lat;
    else
	sscanf(line,"%lf",&lat);
    
    fprintf(stdout,"\t\tEnter longitude [%f]: ",gps_save_lon);
    (void) fgets(line,MAXPATHLEN,stdin);
    if(line[0]==10)
	lon = gps_save_lon;
    else
	sscanf(line,"%lf",&lon);


    if((ret=GPS_Command_Send_Position(port,lat,lon))<0)
    {
	fprintf(stderr,"Error setting position\n");
	Print_Error(ret);
    }

    gps_save_lat = lat;
    gps_save_lon = lon;

    Press_Return();
    return;
}


static void Do_Pvtrec(const char *port)
{
    GPS_PPvt_Data pvt;
    int32 fd;
    char  line[MAXPATHLEN];
    int32 ret;
    
    if(!(pvt = GPS_Pvt_New()))
	fprintf(stderr,"Memory error\n");
    
    if((ret=GPS_Command_Pvt_On(port,&fd)) > 0)
    {
	GPS_Util_Canon(1);
	GPS_Util_Block(0,0);
	while(read(0,line,1)!=-1);  /* Make sure input buffer is empty */
	while(read(0,line,1)==-1)
	{
	    if(GPS_Serial_Chars_Ready(fd))
	    {
		/* Add a system specific screen clear here if you wish */
		GPS_Command_Pvt_Get(&fd,&pvt);
		GPS_Fmt_Print_Pvt(pvt,stdout);
	    }
	}
	GPS_Command_Pvt_Off(port,&fd);
	GPS_Util_Block(0,1);
	GPS_Util_Canon(0);
    }
    else
	Print_Error(ret);
    
    GPS_Pvt_Del(&pvt);

    Press_Return();
    
    return;
}




void GPS_Info(void)
{
    fprintf(stdout,"\n");
    fprintf(stdout,"GPS Machine ID = %d\n",(int)gps_save_id);
    fprintf(stdout,"Version        = %.2f\n",gps_save_version);
    fprintf(stdout,"Description    = %s\n",gps_save_string);
    fprintf(stdout,"\nPROTOCOLS\n\n");
    fprintf(stdout,"Waypoint\t\tA%d D%d\n",(int)gps_waypt_transfer,
	    (int)gps_waypt_type);
    fprintf(stdout,"Route\t\t\tA%d D%d D%d\n",(int)gps_route_transfer,
	    (int)gps_rte_hdr_type,(int)gps_rte_type);
    fprintf(stdout,"Track\t\t\tA%d D%d\n",(int)gps_trk_transfer,
	    (int)gps_trk_type);
    fprintf(stdout,"Proximity\t\tA%d D%d\n",(int)gps_prx_waypt_transfer,
	    (int)gps_prx_waypt_type);
    fprintf(stdout,"Almanac\t\t\tA%d D%d\n",(int)gps_almanac_transfer,
	    (int)gps_almanac_type);
    fprintf(stdout,"Time\t\t\tA%d D%d\n",(int)gps_date_time_transfer,
	    (int)gps_date_time_type);
    fprintf(stdout,"Position\t\tA%d D%d\n",(int)gps_position_transfer,
	    (int)gps_position_type);
    fprintf(stdout,"PVT Data\t\tA%d D%d\n",(int)gps_pvt_transfer,
	    (int)gps_pvt_type);
    fprintf(stdout,"Route Link\t\tD%d\n",(int)gps_rte_link_type);
    fprintf(stdout,"Track Header\t\tD%d\n",(int)gps_trk_hdr_type);

    fprintf(stdout,"\n");

    GPS_Unknown_Protocol_Print();

    Press_Return();

    return;
}

    



int main(int argc, char **argv)
{
    char port[MAXPATHLEN];
    int32  ret;

    Init_Jeeps(port);
    if(GPS_Init(port)<0)
    {
	(void) fprintf(stderr,
		"Not a Garmin GPS, GPS off/disconnected/supervisor mode\n");
	(void) fprintf(stderr,"Port: %s\n",port);
	exit(0);
    }
    

    while((ret=Jeeps_Menu()))
    {
	if(ret<0) continue;
	switch(ret)
	{
	case ALMREC:
	    Do_Almrec(port);
	    break;
	case ALMTRA:
	    Do_Almtra(port);
	    break;
	case WAYREC:
	    Do_Wayrec(port);
	    break;
	case WAYTRA:
	    Do_Waytra(port);
	    break;
	case ROUREC:
	    Do_Rourec(port);
	    break;
	case ROUTRA:
	    Do_Routra(port);
	    break;
	case TRKREC:
	    Do_Trkrec(port);
	    break;
	case TRKTRA:
	    Do_Trktra(port);
	    break;
	case PRXREC:
	    Do_Prxrec(port);
	    break;
	case PRXTRA:
	    Do_Prxtra(port);
	    break;
	case TIMREC:
	    Do_Timrec(port);
	    break;
	case TIMTRA:
	    Do_Timtra(port);
	    break;
	case PSNREC:
	    Do_Psnrec(port);
	    break;
	case PSNTRA:
	    Do_Psntra(port);
	    break;
	case PVTREC:
	    Do_Pvtrec(port);
	    break;
	case INFO:
	    GPS_Info();
	    break;
	case SAVE:
	    Save_Values(port);
	    break;
	default:
	    fprintf(stderr,"Shouldn't get here\n");
	    break;
	}
	

    }

    Do_Off(port);

    return 0;
}
