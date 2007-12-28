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
#include "SRTMDataGrid.h"
//#include "functions.h"
#include <cmath>
#include "tomerc.h"


// DataGrid constructor 
// Loads heights from one or more .hgt files.
// Each rectangle in the input array "rects" defines the rectangle (in sampling
// point indices) of a grid square. There's normally only one rectangle but
// may be up to 4 if two latitude/longitude lines pass through the visible area.
//
// sampling_pts is a definition of the area in terms of nationally-indexed 
// sampling points; this function fills it in.
//
// Returns an array of all the heights indexed nationally.

SRTMDataGrid::SRTMDataGrid(const std::string& srtmlocation,
							LATLON_TILE **rects,int w, int h, int f,
						const std::string& outCoord, bool feet)
{
	// Do each input rectangle
	int index_w=0, index_h=0,pts_w,pts_h;
	samplewidth=0; 
	sampleheight=0;

	this->f=f;
	this->outCoord=outCoord;
	this->feet=feet;
	this->srtmlocation=srtmlocation;

	for(int hcount=0;hcount<h; hcount++)
	{
		sampleheight+=
				(rects[hcount][0].bottom-rects[hcount][0].top)+1;
	}

	for(int wcount=0; wcount<w; wcount++)
	{
		samplewidth+=
				(rects[0][wcount].right-rects[0][wcount].left)+1;
	}

	points=new SRTM_SAMPLE_POINT [sampleheight*samplewidth];

	for(int hcount=0; hcount<h; hcount++)
	{
		index_w = index_h;
		pts_h = (rects[hcount][0].bottom-rects[hcount][0].top)+1;
		for(int wcount=0; wcount<w; wcount++)
		{
			pts_w = (rects[hcount][wcount].right-rects[hcount][wcount].left)+1;
			doLoad(&rects[hcount][wcount],index_w);
			index_w += pts_w;
		}
		index_h += pts_h*samplewidth; 
	}
}



void SRTMDataGrid::doLoad(LATLON_TILE *rect,int index)
{
	// Get the .hgt file for the current rectangle
	char hgtfile[1024];
	SRTMDataGrid::getHgtFilename(hgtfile,rect->origin);
	FILE *fp=fopen(hgtfile,"rb");	

	if(fp)
	{

		int width = (rect->right-rect->left)+1;
		unsigned char *data = new unsigned char[width*2];

		int datacount; 
		double h;
		EarthPoint curLatLon;

		double frac = 0.0008333333333*f;
		curLatLon.x=rect->origin.x+(((double)rect->left)/1200);
		curLatLon.y=(rect->origin.y+1)-(((double)rect->top)/1200);
		double origlong=curLatLon.x;
		int i;

		for(int row=rect->top; row<=rect->bottom; row++)
		{
			curLatLon.x = origlong; 

			// 20/02/05 Only do every 'f' rows
			if((row-rect->top)%f == 0)
			{
				fseek(fp,(row*1201+rect->left)*2, SEEK_SET);
				fread (data,1,width*2,fp);
				datacount=0;
				i=0;
				for(int pt=row*1201+rect->left;pt<=row*1201+rect->right; pt++)
				{
					// 20/02/05 Only do every 'f' columns
					if( (pt-(row*1201+rect->left) )%f == 0) 
					{
						h=
		 				( ((double)data[datacount])*256+
						  ((double)data[datacount+1]) ) * 
						  (feet ? 3.28084: 1.0);
						points[index+i].hgt = 
							(h>=1 && h<30000) ? h: 1;
						points[index+i].earthPos=
								(outCoord=="Mercator" ? lltomerc(curLatLon):
									curLatLon);

						i++;
					}
					datacount+=2;
					curLatLon.x +=  frac;
				}
				index+=samplewidth;
			}
			curLatLon.y -= frac;
		}
		delete[] data;
		fclose(fp);
	}
}
	
// 211207 moved out of above method into its own as it is not always required,
// e.g. for shapefiles
void SRTMDataGrid::getScreenPoints(Map& map)
{
	for(int i=0; i<sampleheight*samplewidth; i++)
		points[i].screenPos= map.getScreenPos(points[i].earthPos);
}

void SRTMDataGrid::getHgtFilename(char *hgtfile,EarthPoint& latlon)
{
	sprintf ( hgtfile,"%s/N%02d%s%03d.hgt",srtmlocation.c_str(),
						int(latlon.y),(latlon.x<0 ? "W":"E"),
						abs(int(latlon.x)));
}


SRTMDataGrid::~SRTMDataGrid()
{
	delete[] points;
}


void SRTMDataGrid::setPoint (int row,int col)
{
	pt = row*samplewidth+col;
	edges[0][0] = pt;
	edges[0][1] = pt+f;
	edges[1][0] = pt;
	edges[1][1] = pt+samplewidth*f;
	edges[2][0] = pt+f;
	edges[2][1] = pt+samplewidth*f+f;
	edges[3][0] = pt+samplewidth*f;
	edges[3][1] = pt+samplewidth*f+f;
}

double SRTMDataGrid::startHeight(int interval)
{
	double ht = min (
					min(points[pt].hgt, points[pt+f].hgt),
					min(points[pt+samplewidth*f].hgt, 
						 points[pt+samplewidth*f+f].hgt )
					);
	return ceil(ht/interval) * interval;
}

double SRTMDataGrid::endHeight(int interval)
{

	double ht = max (
					max(points[pt].hgt, points[pt+f].hgt),
					max(points[pt+samplewidth*f].hgt, 
						 points[pt+samplewidth*f+f].hgt )
					);
	return floor(ht/interval) * interval;
}

// checked : the correct edges are being considered each time.
void SRTMDataGrid::getLine(LINE *lines,int *n_lines, int ht,bool screen)
{
	int go = 0;
	int prevedges[2];
	LINE line;
	double eAh0, eAh1, eBh0, eBh1; 
	double eAp0x, eAp0y, eAp1x, eAp1y, eBp0x, eBp0y, eBp1x, eBp1y;

	// See addContour() 
	bool two_contours = (
			(points[pt].hgt<ht && points[pt+f].hgt>ht && 
			 points[pt+samplewidth*f+f].hgt<ht && 
			 points[pt+samplewidth*f].hgt > ht) 
			 ||
			(points[pt].hgt>ht && points[pt+f].hgt<ht && 
			 points[pt+samplewidth*f+f].hgt>ht && 
			 points[pt+samplewidth*f].hgt < ht)
				) ; 

	for(int edge=0; edge<3; edge++)
	{
		if(between(ht,points[edges[edge][0]].hgt,points[edges[edge][1]].hgt))
		{
			for(int edge2=edge+1; edge2<4; edge2++)
			{
				if(between
					(ht,points[edges[edge2][0]].hgt,
						 points[edges[edge2][1]].hgt))
				{
					eAh0 = points[edges[edge][0]].hgt;
					eAh1 = points[edges[edge][1]].hgt;
					eBh0 = points[edges[edge2][0]].hgt;
					eBh1 = points[edges[edge2][1]].hgt;

					eAp0x = screen ? points[edges[edge][0]].screenPos.x:
									points[edges[edge][0]].earthPos.x;
					eAp0y = screen ? points[edges[edge][0]].screenPos.y:
									points[edges[edge][0]].earthPos.y;
					eAp1x = screen ? points[edges[edge][1]].screenPos.x:
									points[edges[edge][1]].earthPos.x;
					eAp1y = screen ? points[edges[edge][1]].screenPos.y:
									points[edges[edge][1]].earthPos.y;
					eBp0x = screen ? points[edges[edge2][0]].screenPos.x:
									points[edges[edge2][0]].earthPos.x;
					eBp0y = screen ? points[edges[edge2][0]].screenPos.y:
									points[edges[edge2][0]].earthPos.y;
					eBp1x = screen ? points[edges[edge2][1]].screenPos.x:
									points[edges[edge2][1]].earthPos.x;
					eBp1y = screen ? points[edges[edge2][1]].screenPos.y:
									points[edges[edge2][1]].earthPos.y;


					// We draw a line. 
					line.p[0].x =
					   		eAp0x + ( ((ht-eAh0) / (eAh1-eAh0))	
					 		 * (eAp1x-eAp0x) );

					line.p[0].y =
					   		eAp0y + ( ((ht-eAh0) / (eAh1-eAh0))	
					 		 * (eAp1y-eAp0y) );

					line.p[1].x =
					   		eBp0x + ( ((ht-eBh0) / (eBh1-eBh0))	
					 		 * (eBp1x-eBp0x) );

					line.p[1].y =
					   		eBp0y + ( ((ht-eBh0) / (eBh1-eBh0))	
					 		 * (eBp1y-eBp0y) );

					if(two_contours==false) 
					{
						lines[0] = line;
						*n_lines=1;
					}
					else
					{
						SRTMDataGrid::addContour 
								(lines,line,edge,edge2,prevedges,&go,
								 n_lines);
					}
				}
			}
		}
	}
}

// This function will be called when the special case of two opposite corners 
// having a height above a contour, and the other two below, occurs. In this
// case - and this case only - two contours of a given height will be drawn
// through a quadrangle. This special case confuses the standard algorithm
// no end :-) Thus, we need to ensure that the two contours are drawn
// on the opposite side of the quadrangle (any other combination wouldn't 
// make sense), and this function does that. Which two opposite sides 
// doesn't actually matter; in fact, it's impossible to tell which two would
// be correct.
void SRTMDataGrid::addContour
	(LINE *lines,LINE line,int edge1,int edge2,int *prevedges,int *go,
 	int *n_lines)
{
	/* Edge numbering assumed to be:

	     0 
	   +---+
	  1|   |2
	   +---+
	     3

	Don't draw contours through opposite edges. These don't apply in this
	special case. 

	*/	

	if(!((edge1==0 && edge2==3) || (edge1==1 && edge2==2)))
	{
		if(*go==0)
		{
			prevedges[0] = edge1;
			prevedges[1] = edge2;
			*go = 1;
			*n_lines = 1;
			lines[0] = line;
		}
		else if( (edge1==2 && edge2==3 && prevedges[0]==0 && prevedges[1]==1)
			 || (edge1==1 && edge2==3 && prevedges[0]==0 && prevedges[1]==2)
			  && *go==1)
		{
			lines[1] = line;
			*n_lines = 2;
		}
	}
}	

// finds the minimum distance between a point and the list of previous points
int SRTMDataGrid::hgtptDistance(vector<int>& prevs)
{
	int dist=sqrt(samplewidth*sampleheight), result; 
	int dx, dy;
	for(int count=0; count<prevs.size(); count++)
	{
		dx = prevs[count]%samplewidth - pt%samplewidth;
		dy = prevs[count]/sampleheight - pt/sampleheight;
		result= sqrt(dx*dx + dy*dy);
		dist = (result<dist) ? result:dist;
	}
	return dist;
}

Colour SRTMDataGrid::getHeightShading (double shadingres)
{
	double est_ht = floor (((points[pt].hgt+points[pt+samplewidth*f+f].hgt)/2+
			 (points[pt+f].hgt+points[pt+samplewidth*f].hgt)/2 
		   ) / 2);
	return doGetHeightShading(est_ht,shadingres);
}

Colour SRTMDataGrid::doGetHeightShading(double ht,double shadingres)
{
	Colour colour;
	ht=round(ht/shadingres);
	double a=1250/shadingres,
	b=250/shadingres,
	c=2000/shadingres,
	d=500/shadingres,
	e=1750/shadingres;
	
	colour.r = (ht<a) ? 191+(ht*(64/a)) : 
	((ht<c) ? 255-((ht-(1250/shadingres))*(16/b)) : 
				  255-((ht-d)*(16/d)) );

	colour.g=(ht<c) ? 255-(ht*(16/b)) : 255-((ht+c)*(16/d));

	colour.b = (ht<e) ? 191-(ht*(16/b)) :
	((ht<c) ? 95+((ht-e)*(16/b)) :
				  95+((ht-(1500/shadingres))*(16/d)) );
	return colour;
}

