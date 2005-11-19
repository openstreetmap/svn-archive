
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
#ifndef LANDSAT_H
#define LANDSAT_H

/*
#ifdef __cplusplus
extern "C"
{
#endif
*/

#include <stdio.h>
#include <curl/curl.h>

typedef struct
{
	char *data;
	int nbytes;
} CURL_LOAD_DATA;

CURL_LOAD_DATA *grab_landsat(
					double  west,double south,double east,double north,
					int width_px, int height_px);
CURL_LOAD_DATA  *grab_gpx(const char *urlbase,
					double west,double south,double east,double north);
CURL_LOAD_DATA  *grab_osm(const char *urlbase,
					double west,double south,double east,double north,
					const char* username,const char* password);
CURL_LOAD_DATA *grab_http_response(const char *url,const char* = NULL, const char* = NULL);
size_t response_callback(void *ptr,size_t size,size_t nmemb, void *data);
CURL_LOAD_DATA *Do(CURL *curl,const char *url,const char* = NULL, const char* = NULL);
char* put_data(char* idata,char* url,char* username,char* password);
size_t infunc (void *bufptr,size_t size, size_t nitems, void *userp);

/*
#ifdef __cplusplus
}
#endif
*/

#endif
