#ifndef LANDSAT_H
#define LANDSAT_H

#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>

typedef struct
{
	char *data;
	int nbytes;
} LS_LOAD_DATA;

LS_LOAD_DATA *grab_landsat(double  west,double south,double east,double north,
					int width_px, int height_px);
size_t ls_read_callback(void *ptr,size_t size,size_t nmemb, void *data);


#ifdef __cplusplus
}
#endif

#endif
