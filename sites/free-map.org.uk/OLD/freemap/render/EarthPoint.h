#ifndef EARTHPOINT_H
#define EARTHPOINT_H

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

struct Point
{
public:
	double x, y;
};

struct EarthPoint : public Point
{
	EarthPoint() { x=y=0; }
	EarthPoint(double x1,double y1) { x=x1; y=y1; }
};

#endif
