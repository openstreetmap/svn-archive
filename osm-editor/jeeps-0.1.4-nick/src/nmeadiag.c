/* @source nmeadiag application
**
** Simple diagnosis function to report NMEA
** sentences being transmitted. Use as:  nmeadiag port > filename
**
** @author: Copyright (C) Alan Bleasby (ableasby@hgmp.mrc.ac.uk)
** @@
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
    int i;

    if(argc<2)
    {
	fprintf(stderr,"Usage: nmeadiag port\n");
	exit(0);
    }

    GPS_Enable_Diagnose();
    
    GPS_NMEA_Init(argv[1]);

    for(i=0;i<60;++i)
	sleep(1);

    GPS_NMEA_Exit();
    
    return 0;
}
