#ifndef GOOGLEPROJECTION_H
#define GOOGLEPROJECTION_H

#include "Map.h"
#include "EarthPoint.h"
#include <vector>
using std::vector;
#include <cmath>

class GoogleProjection
{
   private:

   vector<double> Bc,Cc,zc,Ac;
   int levels;

	double minmax (double a,double b, double c)
	{
    	a = max(a,b);
    	a = min(a,c);
    	return a;
	}

  public:
   GoogleProjection(int levels=18)
   {
   		this->levels=levels;
        double c = 256;
		double e;
        for (int d=0; d<levels; d++) 
		{
            e = c/2;
            Bc.push_back(c/360.0);
            Cc.push_back(c/(2 * M_PI));
            zc.push_back(e);
            Ac.push_back(c);
            c *= 2;
		}
	}
                
	ScreenPos fromLLtoPixel(EarthPoint ll,int zoom)
	{
		double d = zc[zoom];
		double e = round(d + ll.x * Bc[zoom]);
		double f = minmax(sin((M_PI/180.0) * ll.y),-0.9999,0.9999);
		double g = round(d + 0.5*log((1+f)/(1-f))*-Cc[zoom]);
		return ScreenPos(e,g);
	}

	EarthPoint fromPixelToLL(ScreenPos px,int zoom)
	{
		double e = zc[zoom];
		double f = (px.x - e)/Bc[zoom];
		double g = (px.y - e)/-Cc[zoom];
		double h = (180.0/M_PI) * ( 2 * atan(exp(g)) - 0.5 * M_PI);
		return EarthPoint(f,h);
	}
};

#endif // GOOGLEPROJECTION_H
