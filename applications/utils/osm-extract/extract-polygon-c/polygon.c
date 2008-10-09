/*
    This file is part of extract-polygons.

    extract-polygons is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    extract-polygons is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with extract-polygons.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "polygon.h"
#include "extract-polygon.h"

void polygon_create(Tpolygon *polygon) {
    polygon->size = 64;
    polygon->count = 0;
    polygon->points = malloc(sizeof(Tpoint) * polygon->size);
}

void polygon_destroy(Tpolygon *polygon) {
    free(polygon->points);
    polygon->size = 0;
    polygon->count = 0;
}

int polygon_load(void *polygon, char *filename) {
    float lat,lon;
    FILE *f;
    char line[4097];
    
    polygon_create(polygon);

    f = fopen(filename, "r");
    if (f == NULL) {
	die("Unable to open polygon file");
    }
    fgets(line, 4096, f);
    if (line[0] != '1') {
	die("Bad polygon file format");
    }
    while (!feof(f)) {
	fgets(line, 4096, f);
	if (sscanf(line, "%f %f", &lon, &lat)) {
	    polygon_addPoint(polygon, lat, lon);
	} else {
	    if (strncmp(line, "END", 3) != 0) {
		die("Bad polygon file format");
	    }
	}
    }
    fclose(f);
    
    return 0;
}

void polygon_addPoint(Tpolygon *polygon, float lat, float lon) {
    polygon->points[polygon->count].lat = lat;
    polygon->points[polygon->count].lon = lon;
    polygon->count++;
    if (polygon->count == polygon->size) {
	polygon->size *= 2;
	polygon->points = realloc(polygon->points, sizeof(Tpoint)*polygon->size);
    }
}

void polygon_print(Tpolygon *polygon) {
    printf("Size: %d\n", polygon->size);
    printf("Count: %d\n", polygon->count);
    for (int i = 0 ; i < polygon->count ; i++) {
	printf("%f %f\n", polygon->points[i].lat, polygon->points[i].lon);
    }
}

int polygon_pointInside(Tpolygon *polygon, float lat, float lon) {
    int i, j=polygon->count-1;
    int oddNodes=0;
    
    for (i=0 ; i < polygon->count ; i++) {
	if ((polygon->points[i].lon<lon && polygon->points[j].lon>=lon)
		|| (polygon->points[j].lon<lon && polygon->points[i].lon>=lon)) {
	    if (polygon->points[i].lat+(lon-polygon->points[i].lon)/(polygon->points[j].lon-polygon->points[i].lon)*(polygon->points[j].lat-polygon->points[i].lat)<lat) {
		oddNodes=!oddNodes;
	    }
	}
	j=i;
    }

    return oddNodes;
}
