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
#include "Map.h"
#include <cmath>
#include <libshp/shapefil.h>
#include <deque>
#include <map>
#include <set>


// Added by Artem

typedef std::multimap<int,LINE> SegmentCache;
typedef std::deque<LINE*> Contour;
typedef std::set<int> Range;

struct LineString
 {
      typedef std::vector<double> List;
      
   public:
      LineString(int height)
         : height_(height),
           length_(0)
      {}
      
      ~LineString() {}
      
      void add_first(double x, double y)
      {
         if (xs_.size() > 0)
         {
            double dx = xs_[0]-x;
            double dy = ys_[0]-y;
            length_ += sqrt(dx*dx + dy*dy); 
         }
         xs_.insert(xs_.begin(),x);
         ys_.insert(ys_.begin(),y);
         
      }
      void add_last(double x, double y)
      {
         unsigned size = xs_.size();
         if ( size > 0)
         {
            double dx = xs_[size-1]-x;
            double dy = ys_[size-1]-y;
            length_ += sqrt(dx*dx + dy*dy); 
         }
         xs_.push_back(x);
         ys_.push_back(y);
      }
      
      unsigned size() const
      {
         return xs_.size();
      }
 
      int height() const
      {
         return height_;
      }
 
      unsigned length () const 
      {
         return length_;
      }
 
      double * xs ()
      {
         return &xs_[0];
      }
      double * ys ()
      {
         return &ys_[0];
      }
      int height_;
      unsigned length_;
      List xs_;
      List ys_;
};
 
struct shape_writer 
{
      shape_writer(SHPHandle & shp, DBFHandle & dbf, int htidx, int mjridx, 
	  		int interval)
         : shp_(shp),
           dbf_(dbf),
           htidx_(htidx),
           mjridx_(mjridx),
           interval_(interval)
      {}
      
      template <typename T>
      void operator() (T) const;
      
      void operator() (LINE const& line)
      {
         //xs[0] = line.p[0].x;
         //xs[1] = line.p[1].x;
         //ys[0] = line.p[0].y;
         //ys[1] = line.p[1].y;
         
         //SHPObject *object = SHPCreateSimpleObject(SHPT_ARC,2,xs,ys,0);
         //int objid = SHPWriteObject(shp_,-1,object);
         //SHPDestroyObject(object);
         //DBFWriteIntegerAttribute (dbf_,objid,htidx_,segment.ht);
         //DBFWriteIntegerAttribute (dbf_,objid,mjridx_,
		 // ( segment.ht %(interval_*5) == 0 ? 1:0));
      }
      
      void operator() (LineString & line)
      {
         //std::cout << "writing LINE num points=" << line.size() << 
		 //	" lenghth=" << line.length() << "\n";
         double * xs = line.xs();
         double * ys = line.ys();
         unsigned size = line.size();
         SHPObject *object = SHPCreateSimpleObject(SHPT_ARC,size,xs,ys,0);
         int objid = SHPWriteObject(shp_,-1,object);
         SHPDestroyObject(object);
         DBFWriteIntegerAttribute (dbf_,objid,htidx_,line.height());
         DBFWriteIntegerAttribute (dbf_,objid,mjridx_,
		 		( line.height()  % ( interval_*5) == 0 ? 1:0));
      }
      
      double xs[2];
      double ys[2];
      int htidx_;
      int mjridx_;
      int interval_;
      SHPHandle & shp_;
      DBFHandle & dbf_;
      
};

// end of code added by Artem

class SRTMConGen
{
private:
	SRTMDataGrid *sampledata;
	int f;
	std::string inCoord, outCoord;

	LATLON_TILE ** get_latlon_tiles(EarthPoint&,EarthPoint&,int *w,int *h);
	LATLON_TILE** getrects
		(const EarthPoint& bottomleft,const EarthPoint& topright,int *w,int *h);
	void do_contours (DrawSurface *ds,int row,int col, 
				int interval, std::map<int,vector<int> >&last_pt );

public:
	SRTMConGen() { sampledata = NULL; }
	SRTMConGen(const std::string&,
				EarthPoint&,EarthPoint&, bool feet=false,int f=1);
	~SRTMConGen() { if(sampledata) delete sampledata; }
	void makeGrid(const std::string&,
					EarthPoint&,EarthPoint&,bool feet=false,int f=1);
	void deleteGrid() { delete sampledata; sampledata=NULL; }
	void generate(DrawSurface *ds,Map&);
	void generateShading(DrawSurface *ds,double shadingres,Map&);
	void generateShp (const char*,int interval);
	void appendShp(SHPHandle shp,DBFHandle dbf,int interval,
								int htidx, int mjridx);
	void merge_segments 
		(Contour & contour, LineString & line, unsigned maxlength);

	void setInCoord(const std::string& inCoord)
	{
		this->inCoord=inCoord;
	}

	void setOutCoord(const std::string& outCoord)
	{
		this->outCoord=outCoord;
	}

};

#endif
