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
#ifndef SRTMDATAGRID_H
#define SRTMDATAGRID_H

#include "SRTMGeneral.h"

namespace OpenStreetMap
{

struct SRTM_SAMPLE_POINT
{
	double hgt;
	ScreenPos screenPos;
};

class SRTMDataGrid
{
private:
	int samplewidth, sampleheight;
	SRTM_SAMPLE_POINT *points;

	int pt;
	int edges[4][2];
	int f;
	static void addContour
		(LINE *lines,LINE line,int edge1,int edge2,int *prevedges,int *go,
 		int *n_lines);
	static void getHgtFilename(char *filename,EarthPoint&);
	Colour doGetHeightShading(double ht,double shadingres);

public:
	SRTMDataGrid(LATLON_TILE **rects,int w, int h, Map& map, int f);
	~SRTMDataGrid();
	void doLoad(LATLON_TILE *rect,Map& map,int index);
	void setPoint (int row,int col);
	int getPoint (){return pt; }
	double startHeight(int interval);
	double endHeight(int interval);
	void getLine(LINE *lines,int *n_lines, int ht);
	int hgtptDistance(vector<int>& prevs);
	int getHeight() { return sampleheight; }
	int getWidth() { return samplewidth; }
	Colour getHeightShading (double shadingres);
	ScreenPos getTopLeft() { return points[pt].screenPos; }
	ScreenPos getTopRight() { return points[pt+f].screenPos; }
	ScreenPos getBottomLeft() { return points[pt+samplewidth*f].screenPos; }
	ScreenPos getBottomRight() { return points[pt+samplewidth*f+f].screenPos; }
};

}

#endif
