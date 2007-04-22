#include <mapnik/map.hpp>
#include <mapnik/layer.hpp>
#include <mapnik/envelope.hpp>
#include <mapnik/agg_renderer.hpp>
#include <mapnik/image_util.hpp>
#include <mapnik/load_map.hpp>
#include <mapnik/datasource_cache.hpp>
#include <mapnik/font_engine_freetype.hpp>
#include <mapnik/projection.hpp>

using namespace mapnik;

#include <iostream>
#include <cmath>
using namespace std;

#include <gd.h>
#include <boost/filesystem/operations.hpp>

#include "Tile.h"

#define SECS_IN_30_DAYS 2592000
#define SECS_IN_7_DAYS 604800 

int main()
{
	// read CGI vars
	char *qs = getenv("QUERY_STRING");
	if(qs)
	//std::string qs="bbox=-85000,6595000,-80000,6600000&width=500&height=500";
	//if(true)
	{
		tile t;

	
		t.parse_query_string(qs);

		if(t.chk_input_sensible() )
		{

			//printf("Tile was sensible\n");
			datasource_cache::instance()->register_datasources
				("/usr/local/lib/mapnik/input");
			freetype_engine::instance()->register_font
				("/usr/local/lib/mapnik/fonts/Vera.ttf");

			Map m (t.width,t.height);
			load_map(m,"/home/nick/render/osmmerc.xml");

			std::string tileImg = t.get_filename("/var/www/images/tiles");
			boost::filesystem::path p = tileImg;
			//printf("Doing tileImg: %s\n", tileImg.c_str());
			if(! (boost::filesystem::exists(p) ) || 
				boost::filesystem::last_write_time(p) < 
				time(NULL)-SECS_IN_7_DAYS)
			if(true)
			{
				Envelope<double> bbox (t.w,t.s,t.e,t.n);
				m.zoomToBox(bbox);

				Image32 buf (m.getWidth(), m.getHeight());
				agg_renderer<Image32> r(m,buf);
				r.apply();

				// can you write to stdout?
				//printf("Saving tileImg: %s\n", tileImg.c_str());
				save_to_file<ImageData32>(tileImg,"png",buf.data());
			}
			// read file and echo to stdout (use gd)
			FILE *in=fopen(tileImg.c_str(),"r"); 
			if(in)
			{
				printf("Content-type: image/png\n\n");
				gdImagePtr image;
				image=gdImageCreateFromPng(in);
				fclose(in);
				gdImagePng(image,stdout);
				gdImageDestroy(image);
			}
			else
			{
				printf("Content-type: text/html\n\n");
				cerr<<"Unable to open file: " << tileImg << endl;
			}
		}
		else
		{
			printf("Content-type: text/html\n\n");
			printf(t.get_error().c_str());
		}
	}

	return 0;
}
