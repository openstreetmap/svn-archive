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
#include "SRTMConGen.h"
#include "tomerc.h"


SRTMConGen::SRTMConGen(const std::string& srtmlocation,
						EarthPoint& bottomleft,EarthPoint& topright,
						bool feet,int f)
{
	makeGrid(srtmlocation,bottomleft,topright,feet,f);
	inCoord=outCoord="latlon";
}

void SRTMConGen::makeGrid(const std::string& srtmlocation,
							EarthPoint& bottomleft,
							EarthPoint& topright,bool feet,int f)
{
	int w, h;

	// Get the bounding rectangles for all lat/long squares 
	LATLON_TILE **tiles = get_latlon_tiles (bottomleft,topright,&w,&h);

	// Get the sampled heights from the .hgt file, and the screen coordinates
	sampledata = new SRTMDataGrid(srtmlocation,tiles,w,h,f,outCoord,feet);

	for(int hcount=0; hcount<h; hcount++)
		delete[] tiles[hcount];
	delete[] tiles;
}

// Given the latitide and longitude of the bottom left and top right of the
// visible area, this function returns an array of rectangles definining the
// SRTM point indices for all latitude/longitude squares in the visible area.
// This will normally be just one, but if, e.g., 51N and 1W both crossed the
// visible area, it could be up to 4.
LATLON_TILE ** SRTMConGen::get_latlon_tiles(EarthPoint& bottomleft,
				EarthPoint& topright,int *w,int *h)
{

	if (inCoord=="Mercator")
	{
		bottomleft = merctoll(bottomleft);
		topright = merctoll(topright);
	}

	// Get the latitude/longitude square of each rectangle
	LATLON_TILE **rect=getrects(bottomleft,topright,w,h);
	EarthPoint llsq;

	// Fill in the actual bounds
	for(int hcount=0; hcount<*h; hcount++)
	{
		for(int wcount=0; wcount<*w; wcount++)
		{
			llsq = rect[hcount][wcount].origin;
			rect[hcount][wcount].left =  bottomleft.x> llsq.x  ?
					floor((bottomleft.x - llsq.x)*1200): 0;
			rect[hcount][wcount].right = (topright.x < llsq.x+1) ?
					1+floor((topright.x - llsq.x)*1200) :  
							1200;
	
			rect[hcount][wcount].top =(topright.y < llsq.y+1 ) ?
					floor(((llsq.y+1)-topright.y)*1200) :  0;
			rect[hcount][wcount].bottom = bottomleft.y > llsq.y  ?
					1+floor(((llsq.y+1)-bottomleft.y)*1200) :
				  		1200;
		}
	}

	return rect;
}

// Given the latitude and longitude of the bottom left and top right of the
// visible map area, this function returns the appropriate number of
// rectangles specific to a given grid square. For example, if both 51N and 1W
// passed through the visible area, four rectangles would be generated, one
// for the 51N/1W square, one for the 50N/1W square etc. Only the base latitude
// and longitude are filled in at this stage. The dimensions of each rectangle
// within the visible area are filled in by the calling function.
LATLON_TILE** SRTMConGen::getrects
	(const EarthPoint& bottomleft,const EarthPoint& topright, int *w,int *h)
{
	LATLON_TILE** rects;
	*h=0;
	rects=new LATLON_TILE* [int(floor(topright.y)-floor(bottomleft.y))+1];

	for(int lat=floor(topright.y); lat>=floor(bottomleft.y); lat--)
	{
		*w=0;
		rects[*h]=new LATLON_TILE
				[int(floor(topright.x)-floor(bottomleft.x))+1];
		for(int lon=floor(bottomleft.x); lon<=floor(topright.x); lon++)
		{
			rects[*h][*w].origin.y=lat;
			rects[*h][*w].origin.x=lon;
			(*w)++;
		}
		(*h)++;
	}
	return rects;
}


void SRTMConGen::generate(DrawSurface *ds, Map& map)
{
	sampledata->getScreenPoints(map);
	std::map<int,vector<int> > last_pt;

	for(int row=0; row<sampledata->getHeight()-1; row++)
	{
		// Do each point of the current row
		for(int col=0; col<sampledata->getWidth()-1; col++)
		{
			do_contours(ds,row,col,50, last_pt);
		}
	}
}

void SRTMConGen::do_contours (DrawSurface *ds,int row,int col, 
				int interval, std::map<int,vector<int> >&last_pt )
{
	sampledata->setPoint (row,col);
	int start_ht = sampledata->startHeight(interval),
		end_ht = sampledata->endHeight(interval);
			

	LINE lines[2];
	int n_line_pts;
	char htstr[1024];

	Colour colour, contour_colour(192,192,0), mint(0,192,64);

	for(int ht=start_ht; ht<=end_ht; ht+=interval)
	{
		n_line_pts=0;
		sampledata->getLine(lines,&n_line_pts,ht);


		// draw line
		if(n_line_pts!=0)
		{
			for(int count=0; count<n_line_pts; count++)
			{
				colour = (ht%(interval*5)) ?  contour_colour : mint;
				if( (last_pt[ht].size()==0) || 
						(sampledata->hgtptDistance(last_pt[ht])>20))  
				{
					// 08/02/05 changed parameters for slope_angle()
					// 12/02/05 put all the text drawing code in angle_text()
					double angle=slope_angle(lines[count].p[0].x, 
									lines[count].p[0].y,
									lines[count].p[1].x,
									lines[count].p[1].y);
					int i = (lines[count].p[1].x > lines[count].p[0].x) ? 0:1;
					sprintf(htstr,"%d",ht);
					ds->drawAngleText(8, angle, 
						lines[count].p[i].x, lines[count].p[i].y, colour.r,
						colour.g, colour.b, htstr);
					last_pt[ht].push_back(sampledata->getPoint());
				}
					
				ds->drawContour(lines[count].p[0].x,
						lines[count].p[0].y,
						lines[count].p[1].x,
						lines[count].p[1].y, 
						colour.r, colour.g, colour.b); 
			}	
		}
	}
}


// This method by Artem

void SRTMConGen::merge_segments 
	(Contour & contour, LineString & line, unsigned maxlength)
{
   bool first = true;
   unsigned max = contour.size()*contour.size();
   unsigned i = 0;
   double start_x;
   double start_y;
   double end_x;
   double end_y;
   
   while (contour.size() && i++ < max)
   {
      LINE * seg = *(contour.end() - 1);
      double x0 = seg->p[0].x;
      double y0 = seg->p[0].y;
      double x1 = seg->p[1].x;
      double y1 = seg->p[1].y;
      contour.pop_back();
      
      if (first)
      {
         first = false;
         start_x = x0;
         start_y = y0;
         end_x = x1;
         end_y = y1;
         line.add_last(x0,y0);
         line.add_last(x1,y1);
      }
      else if (start_x == x0 && start_y == y0)
      {
         start_x = x1;
         start_y = y1;
         line.add_first(x1,y1);
      }
      else if (start_x == x1 && start_y == y1)
      {
         start_x = x0;
         start_y = y0;
         line.add_first(x0,y0);
      }
      else if (end_x == x0 && end_y == y0)
      {
         end_x = x1;
         end_y = y1;
         line.add_last(x1,y1);
      }
      else if (end_x == x1 && end_y == y1)
      {
         end_x = x0;
         end_y = y0;
         line.add_last(x0,y0);
      } 
      else
      {
         contour.push_front(seg);
      }
      if (line.length() > maxlength) break;
   }
} 

void SRTMConGen::generateShp (const char* shpname,int interval)
{
   SHPHandle shp = SHPCreate(shpname,SHPT_ARC);
   DBFHandle dbf = DBFCreate(shpname);
   int htidx = DBFAddField(dbf,"height",FTInteger,255,0);
   int mjridx = DBFAddField(dbf,"major",FTInteger,255,0);
   appendShp(shp,dbf,interval,htidx,mjridx);
   DBFClose(dbf);
   SHPClose(shp);
}

void SRTMConGen::appendShp(SHPHandle shp,DBFHandle dbf,int interval,
								int htidx, int mjridx)
{
   double xs[2], ys[2];
   
   SegmentCache segments;
   Range heights;
   for(int row=0; row<sampledata->getHeight()-1; row++)
   {
      // Do each point of the current row
      for(int col=0; col<sampledata->getWidth()-1; col++)
      {
         sampledata->setPoint (row,col);
         int start_ht = sampledata->startHeight(interval),
            end_ht = sampledata->endHeight(interval);


         LINE lines[2];
         int n_line_pts;
         char htstr[1024];

         for(int ht=start_ht; ht<=end_ht; ht+=interval)
         {
            n_line_pts=0;
            sampledata->getLine(lines,&n_line_pts,ht,false);
            // draw line
            if(n_line_pts!=0)
            {
               for(int count=0; count<n_line_pts; count++)
               {
                  segments.insert(std::make_pair(ht,lines[count]));
                  heights.insert(ht);
               }
            }
         }
      }
   }
   
   shape_writer writer(shp,dbf,htidx,mjridx,interval);
   //std::for_each(segments.begin(),segments.end(),writer);
   
   Range::iterator itr = heights.begin();
   Range::iterator end = heights.end();
   
   while (itr != end)
   {
      int height = *itr;
      Contour contour;
      SegmentCache::iterator pos;
      
//      std::cout << "height = " <<  height << "\n";
      for ( pos = segments.lower_bound(height);
            pos != segments.upper_bound(height);
            ++pos)
      {
         contour.push_back(&(pos->second));
      }
      
      while (contour.size() > 0)
      {
         LineString line(height);
         merge_segments(contour,line,1000);
         writer(line);
      }
      ++itr;
   }
   
}

void SRTMConGen::generateShading(DrawSurface *ds,double shadingres,Map& map)
{
	sampledata->getScreenPoints(map);
	Colour colour;
	for(int row=0; row<sampledata->getHeight()-1; row++)
	{
		// Do each point of the current row
		for(int col=0; col<sampledata->getWidth()-1; col++)
		{
			sampledata->setPoint(row,col);	
			colour = sampledata->getHeightShading(shadingres);
			ds->heightShading(sampledata->getTopLeft().x,
							  sampledata->getTopLeft().y,
							  sampledata->getTopRight().x,
							  sampledata->getTopRight().y,
							  sampledata->getBottomRight().x,
							  sampledata->getBottomRight().y,
							  sampledata->getBottomLeft().x,
							  sampledata->getBottomLeft().y, 
							  colour.r,colour.g,colour.b);
		}
	}
}

