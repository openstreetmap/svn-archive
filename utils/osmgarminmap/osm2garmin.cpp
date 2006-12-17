#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <libxml/xmlmemory.h>
#include <libxml/debugXML.h>
#include <libxml/HTMLtree.h>
#include <libxml/xmlIO.h>
#include <libxml/DOCBparser.h>
#include <libxml/xinclude.h>
#include <libxml/catalog.h>
#include <libxslt/xslt.h>
#include <libxslt/xsltInternals.h>
#include <libxslt/transform.h>
#include <libxslt/xsltutils.h>
#include "curlstuff.h"

int applyXSLT(const char* infile,const char *xsltfile,const char *outfile);
int createGPSmap(const char* file);
int sendmap(const char*, const char files[1024][16], int nfiles);
int makeMap(double w,double s,double e,double n, const char* username,
					const char* password, int*);

int main(int argc, char *argv[])
{
	char username[1024], password[1024], serialport[1024];
	strcpy(serialport,"/dev/ttyS0");
	int send=1, tiled=0;
	double tilesize;


	if(argc>1 && !strcmp(argv[1],"--nosend"))
	{
		send=0;
		argc--;
		argv++;
	}
	if(argc>2 && !strcmp(argv[1],"--tilesize"))
	{
		tilesize=atof(argv[2]);
		argc-=2;
		argv+=2;
		tiled=1;
	}


	if(argc>2 && !strcmp(argv[1],"--serialport"))
	{
		strcpy(serialport,argv[2]);
		argc-=2;
		argv+=2;
	}
		
	if (argc < 7)
	{
		printf("Usage: osm2garmin [--nosend] [--tilesize tilesize] ");
		printf("[--serialport serialport] w s e n uname pwd\n");
		exit(1);
	}
	
	double w = atof(argv[1]), 
		   s = atof(argv[2]),
		   e = atof(argv[3]),
		   n = atof(argv[4]);

	strcpy(username,argv[5]);
	strcpy(password,argv[6]);

	int nmaps=0;

	if(tiled && (e-w>tilesize || n-s>tilesize))
	{
		printf("Large area so doing tiled retrieval\n");
		double lon=w, lat, lon2, lat2;


		while(lon < e)
		{
			lat=s;
			lon2 = lon+tilesize < e ? lon+tilesize : e;

			while(lat < n)
			{
				lat2 = lat+tilesize < n ? lat+tilesize : n;

				printf("Doing tile %lf,%lf,%lf,%lf\n",lon,lat,lon2,lat2);
				makeMap(lon,lat,lon2,lat2,username,password,&nmaps);
				lat += tilesize;
			}
			lon += tilesize;
		}
	}
	else
	{
		printf("%lf %lf %lf %lf %s %s\n",w,s,e,n,username,password);
		makeMap(w,s,e,n,username,password,&nmaps);
	}
	
	//int send=1, nmaps=1;

	if(send && nmaps>0 && nmaps<=1024)
	{
		char mapfiles[1024][16]; 
	   	for(int count=0; count<nmaps; count++)
		{
			sprintf(mapfiles[count],"osm%04d.img", count+1);
		}
		sendmap(serialport,mapfiles,nmaps);
	}

	return 0;
}

int makeMap(double w,double s,double e,double n, const char* username,
					const char* password, int *nmaps)
{
	int retval=1;
	char MPfile[1024];
	
	fprintf(stderr,"Grabbing data from OSM...");
	CURL_LOAD_DATA *osm = grab_osm
			("http://www.openstreetmap.org/api/0.3/map", w, s, e, n,
			 		username, password );
	fprintf(stderr,"done.\n");

	FILE *blah = fopen("data.osm", "w");
	fwrite( osm->data, osm->nbytes, 1, blah );
	fclose(blah);

	free(osm->data);
	free(osm);

	fprintf(stderr,"Converting OSM to MPX...");
	if (applyXSLT("osm2mpx.xml", "osm2mpx.xsl", "map.mpx") == 0)
	{
		fprintf(stderr,"done.\nConverting MPX to MP...");
		sprintf(MPfile,"osm%04d.mp", (*nmaps)+1);
		if(applyXSLT("feature-list.xml", "mpx2mp.xsl", MPfile)==0)
		{
			fprintf(stderr,"done.\n");
			remove("map.mpx");
			remove("data.osm");
			if(createGPSmap(MPfile)==0)
			{
				(*nmaps)++;
				retval=0;
			}
		}
	}

	return retval;
}


int applyXSLT(const char* infile,const char *xsltfile,const char *outfile)
{
	int retval=1;

	xsltStylesheetPtr xslt; 
	xmlDocPtr doc; 
	xmlDocPtr res; 

	const char *params[17];
	params[0]=NULL;

	FILE *out = fopen(outfile,"w");
	if(out!=NULL)
	{
		xslt = xsltParseStylesheetFile((const xmlChar*)xsltfile);
		doc = xmlParseFile (infile);
		res = xsltApplyStylesheet(xslt,doc,params);

		xsltSaveResultToFile(out,res,xslt);
		fclose(out);

		xsltFreeStylesheet(xslt);
		xmlFreeDoc(res);
		xmlFreeDoc(doc);

		xsltCleanupGlobals();
		xmlCleanupParser();

		retval = 0;
	}

	return retval;
}

// TODO: on windows we can use the cgpsmapper DLL directly here
int createGPSmap(const char* file)
{
	char cmd[1024];
	sprintf(cmd,"/home/nick/bin/cgpsmapper %s", file);
	return system(cmd);
}

int sendmap(const char* serialport,const char files[1024][16], int nfiles)
{
	char cmd[1024];
	char blah[1024];
	sprintf(cmd,"/home/nick/bin/sendmap %s", serialport);

	for(int count=0; count<nfiles; count++)
	{
		sprintf(blah," %s", files[count]);
		strcat(cmd,blah);
	}
	
	return system(cmd);
}


// xmlParseMemory(buffer,size)
