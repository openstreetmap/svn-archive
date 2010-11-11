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
	//std::string qs=
	//"bbox=-81250%2C6598750%2C-80000%2C6600000&width=500&height=500";
	
	//char *qs = "x=4079&y=2740&z=13";

	boost::filesystem::path p ("aaa/bbb/ccc.png");
	if(qs)
	{

		tile t;

	
		t.parse_query_string(qs);

		if(true)
		{
			datasource_cache::instance()->register_datasources
				("/usr/local/lib/mapnik/input");
			freetype_engine::instance()->register_font
				("/usr/local/lib/mapnik/fonts/Vera.ttf");

			mapnik::Map m ((t.width/8)*10,(t.height/8)*10);
			load_map(m,"/var/www/freemap/data/"+t.get_layer()+".xml");

			std::string tileImg = t.get_filename("/var/www/images/tiles2");
			boost::filesystem::path p = tileImg;

			bool exist=true;
			if(!(boost::filesystem::exists(p)))
			{
				exist=false;
				boost::filesystem::path p2 = t.getXDir
					("/var/www/images/tiles2");
				if(!(boost::filesystem::exists(p2)))
				{
					boost::filesystem::path p3 = 
						t.getZDir("/var/www/images/tiles2");
					if(!(boost::filesystem::exists(p3)))
					{
						boost::filesystem::create_directory(p3);
					}
					boost::filesystem::create_directory(p2);
				}
			}
			
			if(! exist || 
				boost::filesystem::last_write_time(p) < 
				time(NULL)-SECS_IN_7_DAYS)
			{
				double margin_x = (t.topRight.x-t.bottomLeft.x)/8, 
						margin_y = (t.topRight.y-t.bottomLeft.y)/8;
				Envelope<double> bbox (t.bottomLeft.x-margin_x,
										t.bottomLeft.y-margin_y,
										t.topRight.x+margin_x,
										t.topRight.y+margin_y);
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
				gdImagePtr image, image2;
				image=gdImageCreateTrueColor(t.width,t.height);
				image2=gdImageCreateFromPng(in);
				fclose(in);
				gdImageCopy(image,image2,0,0,t.width/8,t.height/8,
								t.width,t.height);
				if(t.layer=="firsted")
					gdImageColorTransparent(image,0);
				gdImagePng(image,stdout);
				gdImageDestroy(image2);
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
