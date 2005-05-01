#ifdef __cplusplus
extern "C"
{
#endif

#include "landsat.h"

#include <stdio.h>
#include <curl/curl.h>
#include <stdlib.h>


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

LS_LOAD_DATA *grab_landsat(double  west,double south,double east,double north,
					int width_px, int height_px)
{
	CURL *curl;
	CURLcode res;
	char url[1024];
	LS_LOAD_DATA  * lsdata = (LS_LOAD_DATA *)malloc(sizeof(LS_LOAD_DATA));
   	lsdata->data = NULL;
	lsdata->nbytes = 0;
	sprintf(url,"http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=%d&height=%d&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg&bbox=%lf,%lf,%lf,%lf",width_px,height_px,west,south,east,north);

	fprintf(stderr,"URL = %s\n", url);
	curl=curl_easy_init();
	if(curl)
	{
		curl_easy_setopt(curl,CURLOPT_URL,url);
		curl_easy_setopt(curl,CURLOPT_WRITEFUNCTION,ls_read_callback);
		curl_easy_setopt(curl,CURLOPT_WRITEDATA,lsdata);

		res=curl_easy_perform(curl);

		curl_easy_cleanup(curl);

		return lsdata;
	}
	return NULL;
}

size_t ls_read_callback(void *ptr,size_t size,size_t nmemb, void *data)
{
	size_t rsize=size*nmemb;
	LS_LOAD_DATA *lsdata=(LS_LOAD_DATA *)data;
	fprintf(stderr,"rsize is %d\n", rsize);
	lsdata->data=(char *)realloc(lsdata->data,(lsdata->nbytes+rsize)
										*sizeof(char));
	memcpy(&(lsdata->data[lsdata->nbytes]),ptr,rsize);
	lsdata->nbytes += rsize;
	fprintf(stderr,"lsdata->nbytes is %d\n", lsdata->nbytes);
	return rsize;
}

#ifdef __cplusplus
}
#endif
