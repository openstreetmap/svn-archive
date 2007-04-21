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

int applyXSLT(const char* infile,const char *xsltfile,const char *outfile,
					const char**);
int createGPSmap(const char*,const char* file);
int sendmap(const char*,const char*, const char files[1024][16], int nfiles);
int makeMap(double w,double s,double e,double n, const char* username,
					const char* password, const char*,int*);
void error(const char* msg);
void usage();

int main(int argc, char *argv[])
{
	char username[1024], password[1024], serialport[1024], 
		 cgpsmapper_loc[1024], sendmap_loc[1024];
	strcpy(serialport,"/dev/ttyS0");
	strcpy(cgpsmapper_loc,"cgpsmapper");
	strcpy(sendmap_loc,"sendmap");
	strcpy(username,"");
	strcpy(password,"");
	int send=1, tiled=0, specified_bbox=0;
	double tilesize=0.1, w, s, e, n;


	int i=1;
	while (i<argc)
	{
		if(!strcmp(argv[i],"-N"))
		{
			send=0;
		}
		else if(!strcmp(argv[i],"-T"))
		{
			tiled=1;
		}

		else if(!strcmp(argv[i],"-t"))
		{
			if(argc<=i+1)
				error("-t needs a tile size specified!");

			tilesize=atof(argv[i+1]);
			i++;
		}


		else if(!strcmp(argv[i],"-o"))
		{
			if(argc<=i+1)
				error("-o needs a serial port specified!");
			strcpy(serialport,argv[i+1]);
			i++;
		}

		else if (!strcmp(argv[i],"-c"))
		{
			if(argc<=i+1)
				error("-c needs a cGPSmapper location specified!");
			strcpy(cgpsmapper_loc,argv[i+1]);
			i++;
		}

		else if (!strcmp(argv[i],"-s"))
		{
			if(argc<=i+1)
				error("-s needs a sendmap location specified!");
			strcpy(sendmap_loc,argv[i+1]);
			i++;
		}

		else if (!strcmp(argv[i],"-u"))
		{
			if(argc<=i+1)
				error("-u needs a username specified!");
			strcpy(username,argv[i+1]);
			i++;
		}
		else if (!strcmp(argv[i],"-p"))
		{
			if(argc<=i+1)
				error("-p needs a password specified!");
			strcpy(password,argv[i+1]);
			i++;
		}
		else if (!strcmp(argv[i],"-b"))
		{
			if(argc<=i+4)
				error("-b needs a bounding box specified!");
			w=atof(argv[i+1]);
			s=atof(argv[i+2]);
			e=atof(argv[i+3]);
			n=atof(argv[i+4]);
			specified_bbox=1;
			i+=4;
		}
		else if (!strcmp(argv[i],"-h"))
		{
			usage();
			exit(0);
		}
		i++;
	}
	
	if(!specified_bbox || !strcmp(username,"") || !strcmp(password,""))
	{
		error("You need to specify a bounding box, username and password!");
		exit(1);
	}	

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
				makeMap(lon,lat,lon2,lat2,username,password,cgpsmapper_loc,
								&nmaps);
				lat += tilesize;
			}
			lon += tilesize;
		}
	}
	else
	{
		makeMap(w,s,e,n,username,password,cgpsmapper_loc,&nmaps);
	}
	
	//int send=1, nmaps=1;

	if(send && nmaps>0 && nmaps<=1024)
	{
		char mapfiles[1024][16]; 
	   	for(int count=0; count<nmaps; count++)
		{
			sprintf(mapfiles[count],"osm%04d.img", count+1);
		}
		sendmap(sendmap_loc,serialport,mapfiles,nmaps);
	}

	return 0;
}

		

int makeMap(double w,double s,double e,double n, const char* username,
					const char* password, const char* cgpsmapper_loc,
					int *nmaps)
{
	int retval=1;
	char MPfile[1024];
	
	fprintf(stderr,"Grabbing data from OSM...");
	CURL_LOAD_DATA *osm = grab_osm
			("http://www.openstreetmap.org/api/0.3/map", w, s, e, n,
			 		username, password );
	fprintf(stderr,"done.\n");

	const char *params[17];
	char mapid[6];
	strcpy(mapid,"mapid");
	char id[10];

	FILE *blah = fopen("data.osm", "w");
	fwrite( osm->data, osm->nbytes, 1, blah );
	fclose(blah);

	free(osm->data);
	free(osm);

	fprintf(stderr,"Converting OSM to MPX...");

	params[0] = mapid; 
	sprintf(id,"%d", 65536+ (*nmaps) );
	params[1] = id;
	params[2] = NULL;

	if (applyXSLT("osm2mpx.xml", "osm2mpx.xsl", "map.mpx", params) == 0)
	{
		fprintf(stderr,"done.\nConverting MPX to MP...");
		sprintf(MPfile,"osm%04d.mp", (*nmaps)+1);
		params[0] = NULL;

		if(applyXSLT("feature-list.xml", "mpx2mp.xsl", MPfile, params)==0)
		{
			fprintf(stderr,"done.\n");
			//remove("map.mpx");
			//remove("data.osm");
			if(createGPSmap(cgpsmapper_loc,MPfile)==0)
			{
				(*nmaps)++;
				retval=0;
			}
		}
	}

	return retval;
}


int applyXSLT(const char* infile,const char *xsltfile,const char *outfile,
				const char **params)
{
	int retval=1;

	xsltStylesheetPtr xslt; 
	xmlDocPtr doc; 
	xmlDocPtr res; 


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
int createGPSmap(const char* gpsmap_cmd, const char* file)
{
	char cmd[1024];
	sprintf(cmd,"%s %s", gpsmap_cmd, file);
	return system(cmd);
}

int sendmap(const char* sendmap_cmd,const char* serialport,
				const char files[1024][16], int nfiles)
{
	char cmd[1024];
	char blah[1024];
	sprintf(cmd,"%s %s", sendmap_cmd, serialport);

	for(int count=0; count<nfiles; count++)
	{
		sprintf(blah," %s", files[count]);
		strcat(cmd,blah);
	}
	
	return system(cmd);
}

void error(const char* msg)
{
	fprintf(stderr,"ERROR: %s\n\n",msg);
	usage();
	exit(1);
}

void usage()
{
	fprintf(stderr,"Usage: osm2garmin [-N] [-T] [-t tilesize] [-o serialport]");
	fprintf(stderr," [-c cgpsmapper_location] [-s sendmap_location] [-h] ");
	fprintf(stderr,"-u username -p password -b west south east north\n");
	fprintf(stderr,"\n-h: Display this message\n-N: do not send .img to GPS\n");
	fprintf(stderr,"-T: tiled retrieval if area larger than tilesize\n");
}

// xmlParseMemory(buffer,size)
