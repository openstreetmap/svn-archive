
#include "curlstuff.h"

#include <cstdio>
#include <curl/curl.h>
#include <cstdlib>
#include <cstring>


/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */


/* CREDIT: The approach taken (of a struct storing the read-in-data and its
 * size) is inspired by the callback example on the cURL web site. */


/* Grabs a landsat image. 
 * Returns the data as an array of bytes in JPEG format for further
 * processing. */

CURL_LOAD_DATA *grab_landsat(
					double  west,double south,double east,double north,
					int width_px, int height_px)
{
	char url[1024];
	sprintf(url,"http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=%d&height=%d&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=%lf,%lf,%lf,%lf",width_px,height_px,west,south,east,north);

//	fprintf(stderr,"URL = %s\n", url);
	CURL_LOAD_DATA *resp =  grab_http_response(url);
	//fprintf(stderr,"%s", resp->data);
	return resp;
}

CURL_LOAD_DATA  *grab_gpx(const char *urlbase,
					double west,double south,double east,double north)
{
	char url[1024];
	sprintf(url,"%s?w=%lf&s=%lf&e=%lf&n=%lf",
					urlbase,west,south,east,north);
	return grab_http_response(url);
}

CURL_LOAD_DATA  *grab_osm(const char *urlbase,
					double west,double south,double east,double north,
					const char* username,const char* password)
{
	char url[1024];
	sprintf(url,"%s?bbox=%lf,%lf,%lf,%lf",
					urlbase,west,south,east,north);
	return grab_http_response(url,username,password);
}

CURL_LOAD_DATA *grab_http_response(const char *url,
									const char *username,const char *password)
{
	CURL_LOAD_DATA *data;

	fprintf(stderr, "grab_http_response(): URL=%s\n",url);
	CURL *curl =  curl_easy_init(); 


	if(curl)
	{
		data = Do(curl,url,username,password);
		curl_easy_cleanup(curl);
		return data;
	}
	return NULL;
}

CURL_LOAD_DATA *Do(CURL *curl,const char *url,
					const char* username,const char* password)
{
	char uname_pwd[1024];
	CURLcode res;
	CURL_LOAD_DATA *data = (CURL_LOAD_DATA *)malloc(sizeof(CURL_LOAD_DATA));
	data->data = NULL;
	data->nbytes = 0;
	
	if(username && password)
	{
		sprintf(uname_pwd,"%s:%s",username,password);
		curl_easy_setopt(curl,CURLOPT_USERPWD,uname_pwd);
	}

	curl_easy_setopt(curl,CURLOPT_URL,url);
	curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,response_callback);
	curl_easy_setopt(curl,CURLOPT_WRITEDATA,data);

	res=curl_easy_perform(curl);

	fprintf(stderr,"Got data.\n");
	return data;
}

size_t response_callback(void *ptr,size_t size,size_t nmemb, void *d)
{
	size_t rsize=size*nmemb;
	CURL_LOAD_DATA *data=(CURL_LOAD_DATA *)d;
//	fprintf(stderr,"rsize is %d\n", rsize);
	data->data=(char *)realloc(data->data,(data->nbytes+rsize)
										*sizeof(char));
	memcpy(&(data->data[data->nbytes]),ptr,rsize);
	data->nbytes += rsize;
//	fprintf(stderr,"data->nbytes is %d\n", data->nbytes);
	return rsize;
}

size_t infunc (void *bufptr,size_t size, size_t nitems, void *userp)
{
	char *p1= (char*)bufptr, *p2=(char*)userp;
	size_t retcode;
//	fprintf(stderr,"callback: userp=%s\n",(char *)userp);
	strcpy(p1,p2);
	return strlen(p1)+1;
}

char* put_data(char* idata,char* url,char* username,char* password)
{
	CURL *curl;
	CURLcode res;

	char *odata2 = NULL;
	char uname_pwd[1024];
	sprintf(uname_pwd,"%s:%s",username,password);

	CURL_LOAD_DATA *odata = (CURL_LOAD_DATA *)malloc(sizeof(CURL_LOAD_DATA));
	odata->data = NULL;
	odata->nbytes = 0;

	curl_global_init(CURL_GLOBAL_ALL);
	curl = curl_easy_init();
	
	if(curl)
	{
		curl_easy_setopt(curl,CURLOPT_READFUNCTION,infunc);
		curl_easy_setopt(curl,CURLOPT_UPLOAD, true);
		curl_easy_setopt(curl,CURLOPT_PUT, true);
		curl_easy_setopt(curl,CURLOPT_URL, url);
//					http://www.openstreetmap.org/api/0.2/newnode
		curl_easy_setopt(curl,CURLOPT_USERPWD,uname_pwd);
		curl_easy_setopt(curl,CURLOPT_READDATA,idata);
		curl_easy_setopt(curl,CURLOPT_INFILESIZE,(long)(strlen(idata)+1));
		curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,response_callback);
		curl_easy_setopt(curl,CURLOPT_WRITEDATA,odata);

		curl_easy_perform(curl);
		curl_easy_cleanup(curl);
		odata2 = new char[odata->nbytes+1];
		memcpy(odata2,odata->data,odata->nbytes);
		odata2[odata->nbytes]='\0';
		free(odata->data);
		free(odata);
	
		fprintf(stderr,"Response: %s\n", odata2);
	}

	curl_global_cleanup();
	return odata2;
}
