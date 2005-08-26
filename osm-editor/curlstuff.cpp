
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

CURL_LOAD_DATA *grab_landsat(double  west,double south,double east,double north,
					int width_px, int height_px)
{
	char url[1024];
	sprintf(url,"http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=%d&height=%d&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=%lf,%lf,%lf,%lf",width_px,height_px,west,south,east,north);

	fprintf(stderr,"URL = %s\n", url);
	return grab_http_response(url);
}

CURL_LOAD_DATA  *grab_gpx(const char *urlbase,
					double west,double south,double east,double north)
{
	char url[1024];
	sprintf(url,"%s?input=latlon&output=gpx&w=%lf&s=%lf&e=%lf&n=%lf",
					urlbase,west,south,east,north);
	return grab_http_response(url);
}

CURL_LOAD_DATA *grab_http_response(const char *url)
{
	CURL *curl;
	CURLcode res;
	CURL_LOAD_DATA  * data = (CURL_LOAD_DATA *)malloc(sizeof(CURL_LOAD_DATA));
   	data->data = NULL;
	data->nbytes = 0;

	curl=curl_easy_init();
	if(curl)
	{
		curl_easy_setopt(curl,CURLOPT_URL,url);
		curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,response_callback);
		curl_easy_setopt(curl,CURLOPT_WRITEDATA,data);

		res=curl_easy_perform(curl);

		curl_easy_cleanup(curl);

		fprintf(stderr,"Got data.\n");
		return data;
	}
	free(data);
	return NULL;
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

bool post_gpx(const char *url, char* gpx)
{
	CURL *curl;
	CURLcode res;

	curl=curl_easy_init();
	if(curl)
	{
		char *urlencoded=curl_escape(gpx,strlen(gpx));
		char *data = new char[strlen(urlencoded)+5];
		sprintf(data,"gpx=%s",urlencoded);
		curl_easy_setopt(curl,CURLOPT_POSTFIELDS,data);
		curl_easy_setopt(curl,CURLOPT_URL,url);

		res=curl_easy_perform(curl);

		delete[] data;
		curl_free(urlencoded);
		curl_easy_cleanup(curl);
		return true; 
	}
	return false; 
}

