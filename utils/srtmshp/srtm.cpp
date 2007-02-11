/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#include <iostream>
#include "Canvas.h"
#include  <cstdlib>
using namespace std;

int main(int argc,char* argv[])
{
	int shadingres=0, e=488000, n=126900, width=320, height=320, inc;
	double scale=0.1;

	while(argc>2 && argv[1][0]=='-' && strlen(argv[1])>1)
	{
		inc=2;
		switch(argv[1][1])
		{
			case 'e': e = atoi(argv[2]);
					  break;

			case 'n': n = atoi(argv[2]);
					  break;

			case 's': scale = atof(argv[2]);
					  break;

			case 'w': width = atoi(argv[2]);
					  break;

			case 'h': height = atoi(argv[2]);
					  break;

			case 'r': shadingres = atoi(argv[2]);
					  break;

			default : cerr<< "Unknown option -- " << argv[1];
					  inc=1;
		}

		argv+=inc;
		argc-=inc;
	}

	cerr<<e <<" ";
	cerr<<n <<" ";
	cerr<<scale <<" ";
	cerr<<width <<" ";
	cerr<<height <<" ";
	Canvas canvas ( e, n, scale, width, height, shadingres );
	canvas.draw();
	return 0;
}
