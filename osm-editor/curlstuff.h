#ifndef LANDSAT_H
#define LANDSAT_H

/*
#ifdef __cplusplus
extern "C"
{
#endif
*/

#include <stdio.h>

typedef struct
{
	char *data;
	int nbytes;
} CURL_LOAD_DATA;

CURL_LOAD_DATA *grab_landsat(double  west,double south,double east,double north,
					int width_px, int height_px);
CURL_LOAD_DATA  *grab_gpx(const char *urlbase,
					double west,double south,double east,double north);
CURL_LOAD_DATA *grab_http_response(const char *url);
size_t response_callback(void *ptr,size_t size,size_t nmemb, void *data);
bool post_gpx(const char *url, char* gpx);

/*
#ifdef __cplusplus
}
#endif
*/

#endif
