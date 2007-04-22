#include "Tile.h"
#include <cmath>

bool tile::chk_input_sensible()
{
	double escale = width/((double)e-(double)w), 
				nscale=height/((double)n-(double)s);

	scale=int(round(escale*1000));
	
	if(width!=500 || height!=500)
	{
		error="Currently, width and height must be 500 pixels";
		return false;
	}

	if(scale!=100 && scale!=200)
	{
		error= "Invalid scale! Needs to be 100 or 200 pixels/km!";
		return false;
	}

	// Get the amount that the coordinates must be divisible by at this scale
	int eDivisibleBy=(width*1000)/scale,
	    nDivisibleBy=(height*1000)/scale;

	if( e%eDivisibleBy !=0 || w%eDivisibleBy !=0 || 
					s%nDivisibleBy != 0 || n%nDivisibleBy !=0)
	{
		error="w,s,e,n not divisible by the correct amount for this scale";
		return false;
	}

	if(e-w<=0 || n-s<=0)
	{
		error="Not a box, you silly person"; 
		return false;
	}

	return true;
}	

std::string tile::get_filename(const std::string& root)
{
	std::ostringstream strm;
	strm << root << "/" << scale <<  "/" << (w<0 ? "W":"E") 
		<< abs(w) << ".N" << s << ".png";
	return strm.str();
}
