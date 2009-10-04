/* reprojection.c
 *
 * Convert OSM lattitude / longitude from degrees to mercator
 * so that Mapnik does not have to project the data again
 *
 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <proj_api.h>

#include "reprojection.h"

static projPJ pj_ll, pj_merc;
static int Proj;
const struct Projection_Info Projection_Info[] = {
  [PROJ_LATLONG] = { 
     descr: "Latlong", 
     proj4text: "(none)", 
     srs:4326, 
     option: "-l" },
  [PROJ_MERC]    = { 
     descr: "WGS84 Mercator", 
     proj4text: "+proj=merc +datum=WGS84  +k=1.0 +units=m +over +no_defs", 
     srs:3395, 
     option: "-M" },
  [PROJ_SPHERE_MERC] = { 
     descr: "Spherical Mercator",  
     proj4text: "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs", 
     srs:900913, 
     option: "-m" }
};
static struct Projection_Info custom_projection;

// Positive numbers refer the to the table above, negative numbers are
// assumed to refer to EPSG codes and it uses the proj4 to find those.
void project_init(int proj)
{
	char buffer[32];
	Proj = proj;
	
	if( proj == PROJ_LATLONG )
		return;
		
	pj_ll   = pj_init_plus("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");
	if( proj >= 0 && proj < PROJ_COUNT )
		pj_merc = pj_init_plus( Projection_Info[proj].proj4text );
	else if( proj < 0 )
	{
	   if( snprintf( buffer, sizeof(buffer), "+init=epsg:%d", -proj ) >= (int)sizeof(buffer) )
		{
			fprintf( stderr, "Buffer overflow computing proj4 initialisation string\n" );
			exit(1);
		}
		pj_merc = pj_init_plus( buffer );
		if( !pj_merc )
		{
			fprintf( stderr, "Couldn't read EPSG definition (do you have /usr/share/proj/epsg?)\n" );
			exit(1);
		}
	}
			
	if (!pj_ll || !pj_merc) {
		fprintf(stderr, "Projection code failed to initialise\n");
		exit(1);
	}
	
	if( proj >= 0 )
		return;
	custom_projection.srs = -proj;
	custom_projection.proj4text = pj_get_def( pj_merc, 0 );
	if( snprintf( buffer, sizeof(buffer), "EPSG:%d", -proj ) >= (int)sizeof(buffer) )
	{
		fprintf( stderr, "Buffer overflow computing projection description\n" );
		exit(1);
	}
	custom_projection.descr = strdup(buffer);
	custom_projection.option = "-E";
	return;
}

void project_exit(void)
{
	if( Proj == PROJ_LATLONG )
		return;
		
	pj_free(pj_ll);
	pj_ll = NULL;
	pj_free(pj_merc);
	pj_merc = NULL;
}

struct Projection_Info const *project_getprojinfo(void)
{
  if( Proj >= 0 )
    return &Projection_Info[Proj];
  else
    return &custom_projection;
}

void reproject(double *lat, double *lon)
{
	double x[1], y[1], z[1];
	
	if( Proj == PROJ_LATLONG )
		return;

	x[0] = *lon * DEG_TO_RAD;
	y[0] = *lat * DEG_TO_RAD;
	z[0] = 0;
	
	pj_transform(pj_ll, pj_merc, 1, 1, x, y, z);
	
	//printf("%.4f\t%.4f -> %.4f\t%.4f\n", *lat, *lon, y[0], x[0]);	
	*lat = y[0];
	*lon = x[0];
}

