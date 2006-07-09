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
#ifndef SRTMCONGEN_H
#define SRTMCONGEN_H

#include "SRTMDataGrid.h"

namespace OpenStreetMap
{

class SRTMConGen
{
private:
	SRTMDataGrid *sampledata;
	int f;

	LATLON_TILE ** get_latlon_tiles(Map& map,int *w,int *h);
	LATLON_TILE** getrects
		(const EarthPoint& bottomleft,const EarthPoint& topright,int *w,int *h);
	void do_contours (DrawSurface *ds,int row,int col, 
				int interval, std::map<int,vector<int> >&last_pt );

public:
	SRTMConGen(Map& map, int f);
	~SRTMConGen() { delete sampledata; }
	void generate(DrawSurface *ds);
	void generateShading(DrawSurface *ds,double shadingres);
};

}
#endif
