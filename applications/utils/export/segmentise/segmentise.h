#ifndef SEGMENTISER_H
#define SEGMENTISER_H

#include <map>
#include <string>
#include <vector>

struct Node
{
	double lat, lon;
	int count;

	Node(double lat,double lon)
	{
		this->lat=lat;
		this->lon=lon;
		count=0;
	}
};

struct Way
{
	std::map<std::string,std::string> tags;
	std::vector<int> nds;
};

#endif
