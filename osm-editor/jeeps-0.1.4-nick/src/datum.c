/* @source datum application
**
** Example program given in the documentation
**
** @author: Copyright (C) Alan Bleasby (ableasby@hgmp.mrc.ac.uk)
** @@
**
** This program is not meant to be pretty. It is provided as an example
** of how to use the library. It is known to work with GPS II+ and
** GPS III models and should work with others, that being the point
** of the library.
**
** This program is free software; you can redistribute it and/or
** modify it under the terms of the GNU General Public License
** as published by the Free Software Foundation; either version 2
** of the License, or (at your option) any later version.
** 
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
******************************************************************************/

#include "../lib/gps.h"


int main(int argc, char **argv)
{
    GPS_PRmm obj;
    
    if(argc<2)
    {
	fprintf(stderr,"Usage: datum port\n");
	exit(0);
    }

    obj = GPS_Rmm_New();
    

    GPS_NMEA_Init(argv[1]);

    while(strcmp(obj->datum,"Ord Srvy GB"))
    {
	if(GPS_NMEA_Get_Rmm(&obj))
	    fprintf(stdout,"%s\n",obj->datum);
	sleep(1);
    }

    GPS_NMEA_Exit();
    GPS_Rmm_Del(&obj);
    
    return 0;
}
