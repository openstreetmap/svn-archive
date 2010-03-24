#include <iostream>
#include <boost/tokenizer.hpp>
#include <sstream>
#include <cmath>
#include "GoogleProjection.h"
#include "tomerc.h"

using std::cout;
using std::cerr;
using std::endl;

class tile 
{
public:
	EarthPoint bottomLeft, topRight;
	int zoom, x, y;
	int width, height;
	std::string error;
	std::string layer;

	tile() { layer="freemap"; }

	void parse_query_string(std::string query_string)
	{
		typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
		boost::char_separator<char> ampersand("&"), equals("="), comma(",");
		tokenizer t (query_string,ampersand);
		std::string key, value;

		for(tokenizer::iterator i=t.begin(); i!=t.end(); i++)
		{
			tokenizer t1 (*i, equals);

			int count=0;
			for(tokenizer::iterator j=t1.begin(); j!=t1.end(); j++)
			{
				if(count==0)
					key=*j;
				else if (count==1)
				{
					value=*j;
					if(key=="X" || key=="x")
					{
						this->x=atoi(value.c_str());
					}
					else if(key=="Y" || key=="y")
					{
						this->y=atoi(value.c_str());
					}
					else if(key=="Z" || key=="z")
					{
						zoom=atoi(value.c_str());
					}
					else if(key=="LAYER" || key=="layer")
					{
						layer=value;	
					}
				}
				count++;
    		}
		}

		GoogleProjection proj;
		bottomLeft = lltomerc
			(proj.fromPixelToLL(ScreenPos(x*256,(y+1)*256),zoom));
		topRight = lltomerc
			(proj.fromPixelToLL(ScreenPos((x+1)*256,y*256),zoom));
		width=256;
		height=256;
	}

	std::string get_error() { return error; }
	std::string get_layer() { return layer; }
	std::string get_filename(const std::string& root)
	{
		std::ostringstream strm;
		strm << root << "/" << 
			layer << "/" << zoom <<  "/" << x << "/" << y << ".png";
		return strm.str();
	}
	std::string getZDir(const std::string& root)
	{
		std::ostringstream strm;
		strm << root << "/" << layer << "/" << zoom;
		return strm.str();
	}
	std::string getXDir(const std::string& root)
	{
		std::ostringstream strm;
		strm << root << "/" << layer << "/" << zoom << "/" << x;
		return strm.str();
	}

	void print()
	{
		cout << "w=" << bottomLeft.x << " s=" << bottomLeft.y 
			  << " e=" << topRight.x <<  " n=" << topRight.y 
				<<" width=" << width << " height=" << height << 
				" x=" << x << " y=" << y << " zoom=" << zoom << endl;
	}
};
