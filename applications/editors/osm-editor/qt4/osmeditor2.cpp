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
#include <qapplication.h>
#include "MainWindow2.h"

int main(int argc, char* argv[])
{
	double lat = 50.9, lon = -1.5, scale = 4000, w = 800, h = 600;

	while(argc>2 && strlen(argv[1])>1 && argv[1][0] == '-')
	{
		switch(argv[1][1])
		{
			case 'l': lat = atof(argv[2]);
					  break;

			case 'o': lon = atof(argv[2]);
					  break;

			case 's': scale = atof(argv[2]);
					  break;

			case 'w': w = atof(argv[2]);
					  break;

			case 'h': h = atof(argv[2]);
					  break;

			default : std::cerr << argv[0] << ": invalid option -- " 
					  			<< argv[1][1] << std::endl;
					  exit(1);
		}
		
		argc -= 2;
		argv += 2;
	}

	QApplication app (argc, argv);
	OpenStreetMap::MainWindow2 mainwin ( lat, lon, scale, w, h );
	//app.setMainWidget (&mainwin);
	mainwin.show();
	return app.exec();
}
