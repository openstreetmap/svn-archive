
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

CURL_LOAD_DATA *grab_http_response(const char *url)
{
	CURL_LOAD_DATA *data;

//	printf("grab_http_response(): URL=%s\n",url);
	CURL *curl =  curl_easy_init(); 

	if(curl)
	{
		data = Do(curl,url);
		curl_easy_cleanup(curl);
		return data;
	}
	return NULL;
}

CURL_LOAD_DATA *Do(CURL *curl,const char *url)
{
	CURLcode res;
	CURL_LOAD_DATA *data = (CURL_LOAD_DATA *)malloc(sizeof(CURL_LOAD_DATA));
	data->data = NULL;
	data->nbytes = 0;
	
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

CURL_LOAD_DATA *post_gpx(const char *url, char* gpx,
				const char* username, const char* password)
{
	CURLcode res;
	CURL_LOAD_DATA *resp;
	CURL *curl =curl_easy_init();

	if(curl)
	{
		char *urlencoded=curl_escape(gpx,strlen(gpx));
		char *urlencoded_username=curl_escape(username,strlen(username));
		char *urlencoded_password=curl_escape(password,strlen(password));
		char *data = new char[strlen(urlencoded)+
				strlen(urlencoded_username)+strlen(urlencoded_password)+16];
		sprintf(data,"username=%s&p=%s&gpx=%s",
						urlencoded_username,urlencoded_password,urlencoded);
		//printf("Sending: %s\n", data);
		curl_easy_setopt(curl,CURLOPT_POSTFIELDS,data);

		resp=Do(curl,url);

		delete[] data;
		curl_free(urlencoded);
		curl_free(urlencoded_username);
		curl_free(urlencoded_password);
		curl_easy_cleanup(curl);
		return resp; 
	}
	return NULL; 
}
