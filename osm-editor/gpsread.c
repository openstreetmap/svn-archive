/*
    Copyright (C) 2004 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */

#include "gpsread.h"


int main(int argc,char *argv[])
{
	GPS_PWay *waypoints;
	int n, gridrefs=0, gpx=0;

	if(argc>1 && argv[1][0]=='-' && strlen(argv[1])>1)
	{
		switch(argv[1][1])
		{
			case 'g': gridrefs=1;
					  break;

			case 'x': gpx=1;
					  break;

			default : printf("%d: invalid option -- %c; ignoring\n",
							argv[0],argv[1][1]);
		}
	}

	if (GPS_Init(PORT) < 0)
	{
		printf("Error contacting GPS on serial port\n");
		exit(1);
	}

	n = GPS_Command_Get_Waypoint(PORT,&waypoints);

	if(gpx)
	{
		toGPX(waypoints,n);
	}
	else
	{
		print_waypoints (waypoints,n,gridrefs);
	}

	free_waypoints (waypoints,n);

	return 0;
}

void print_waypoints(GPS_PWay *waypoints, int n, int gridrefs)
{
	int count;
	double ea,no;

	for(count=0; count<n; count++)
	{

		if(gridrefs)
		{
			// Think this is the right one to use!
			GPS_Math_Airy1830LatLonToNGEN
				(waypoints[count]->lat,
				 waypoints[count]->lon,
				 &ea, &no);
	
			// 22/09/04 Eastings and northings now as 0.001km (6 fig)
			printf("%15s,%06.0lf,%06.0lf;\n", 
						waypoints[count]->ident, ea, no);
		}
		else
		{
			printf("%15s,%10.6lf,%10.6lf;\n", 
				waypoints[count]->ident,
				waypoints[count]->lat,
				waypoints[count]->lon);
		}
	}
}

void free_waypoints(GPS_PWay *waypoints,int n)
{
	int count;
	for(count=0; count<n; count++)
	{
		GPS_Way_Del(&waypoints[count]);
	}
	free(waypoints);
}


int toGPX(GPS_PWay *waypoints,int n)
{
	GPS_PWay * features;
	ROUTES routes;
	ROUTEPOINT rtept;
	int count, ridx, feat=0, routeid;
	char type[1024];
	
	if((features = (GPS_PWay*)malloc(n*sizeof(GPS_PWay)))==NULL ) 
	{
		printf("Out of memory!\n");
		return -1;
	}

	if(alloc_routes(&routes,n,n)<0)
		return -1;

	for(count=0; count<n; count++)
	{
		if(is_route(waypoints[count]->ident))
		{
			routeid=atoi(waypoints[count]->ident+1);
			rtept.id=atoi(waypoints[count]->ident+8);
			rtept.lat = waypoints[count]->lat ;
			rtept.lng = waypoints[count]->lon ;
			ridx=find_route(&routes,routeid);
			get_type_with_road_adj(waypoints[count]->ident[0],type,routeid);
			add_point_to_route(&routes,routeid,type,ridx,&rtept);
		}
		else
		{
			features[feat++] = waypoints[count];
		}
	}

	writeGPX(&routes,features,feat);
}

void writeGPX(ROUTES * routes, GPS_PWay * features, int nfeatures)
{
	startGPX();
	writeWPs(features,nfeatures);
	write_routes(routes);
	endGPX();	
}

void startGPX()
{
	printf ("<gpx version=\"1.0\" creator=\"Hogweed Software gpsread\" ");
	printf ("xmlns=\"http://www.topografix.com/GPX/1/0\">\n");
}

void writeWPs(GPS_PWay * features, int nfeatures)
{
	int count;
	char type[1024];

	for(count=0; count<nfeatures; count++)
	{

		printf("<wpt lat=\"%lf\" lon=\"%lf\">\n",
				features[count]->lat,features[count]->lon);
		get_type (features[count]->ident[0],type);
		printf("<name>%s</name>\n",features[count]->ident+1);
		printf("<type>%s</type>\n",type);
		printf("</wpt>\n");
	}
}

void write_routes(ROUTES * routes)
{
	int count;
	for(count=0; count<routes->nroutes; count++)
	{
		write_route(&routes->routes[count]);
	}
}

void write_route(ROUTE * route)
{
	int count;

	printf("<rte>\n");
	printf("<number>%d</number>\n",route->id);
	printf("<type>%s</type>\n",route->type);

	for(count=0; count<route->npoints; count++)
	{
		printf("<rtept lat=\"%lf\" lon=\"%lf\">\n",
				route->points[count].lat,route->points[count].lng);
		printf("<name>%d</name>\n",route->points[count].id);
		printf("</rtept>\n");
	}
	printf("</rte>\n");
}
	

void endGPX()
{
	printf("</gpx>\n");
}

int is_route(char* ident)
{
	return (strchr("FBYRGCD",ident[0])==NULL) ? 0: 1; 
}

int alloc_routes (ROUTES * routes, int routecap,int pointcap)
{
	int count, count2;

	if ((routes->routes = (ROUTE *) malloc(routecap*sizeof(ROUTE)))==NULL)
		return -1;

	routes->nroutes = 0;
	routes->routecap = routecap;
	routes->pointcap = pointcap;

	for(count=0; count<routecap; count++)
	{
		if ((routes->routes[count].points =
			(ROUTEPOINT *)malloc(pointcap*sizeof(ROUTEPOINT)))==NULL)
		{
			return -1;
		}

		routes->routes[count].npoints = 0;
		routes->routes[count].id = 0;
	}

	return 0; 
}

void free_routes (ROUTES * routes)
{
	int count;

	for(count=0; count<routes->routecap; count++)
		free(routes->routes[count].points);

	free(routes->routes);
}

int find_route(ROUTES *routes,int id)
{
	int count;
	for(count=0; count<routes->nroutes; count++)
	{
		if(routes->routes[count].id==id)
		{
			return count;
		}
	}
	// not found: return the number of routes
	// This helps us integrate with add_point_to_route() to start a new route
	return routes->nroutes; 
}
		
// Add a point to a route
// If "idx" is the number of routes, a new route will begin
void add_point_to_route(ROUTES *routes,int routeid,char *type,
							int idx,ROUTEPOINT *point)
{
	routes->routes[idx].points[routes->routes[idx].npoints++] = *point;
	if(idx==routes->nroutes)
	{
		routes->nroutes++;
		routes->routes[idx].id = routeid;
		strcpy(routes->routes[idx].type,type); 
	}
}

void get_type_with_road_adj(char code, char *type,int id)
{
	get_type(code,type);
	if(!strcmp(type,"road"))
		get_road_type(type,id);
}

void get_type(char code,char * type)
{
	char codes[] = "ABCDFGHIJKLMNOPQRSTUVWXYZ";

	char * types[] = { "farm","bridleway","permissive bridleway","fwpbr",
						"footpath", "permissive footpath","hamlet", 
						"point of interest", "suburb","car park", "locality",
						"mast", "caution!", "railway crossing","pub", 
						"amenity","road", "hill", "small town",
						"large town","village", "viewpoint","church","byway",
						"railway station" };

	char *p;

	strcpy(type,(p=strchr(codes,code))?types[p-codes]:"?");
}

void get_road_type(char *type,int routeid)
{
	if(routeid>990000)
		strcpy(type,"A road");
	else if(routeid>980000)
		strcpy(type,"B road");
	else
		strcpy(type,"minor road");
}
