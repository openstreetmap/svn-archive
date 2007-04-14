/* Additional general-purpose GPSBabel funcs required by the GPSBabel
 * version of jeeps. */

#include "defs.h"
#include <stdlib.h>


/* 

    Copyright (C) 2002, 2003, 2004, 2005  Robert Lipe, robertlipe@usa.net,
	or Nick Whitelegg (fatal() function)

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
void fatal(const char * msg)
{
	fprintf(stderr,"%s\n",msg);
	exit(1);
}

signed int le_read16(void *addr)
{
	unsigned char *p = addr;
	return p[0] | (p[1] << 8);
}

signed int le_read32(void *addr)
{
	unsigned char *p = addr;
	return p[0] | (p[1] << 8) | (p[2] << 16) | (p[3] << 24);
}
void le_write16(void *addr, unsigned value)
{
	unsigned char *p = addr;
	p[0] = value;
	p[1] = value >> 8;
	
}

void le_write32(void *addr, unsigned value)
{
	unsigned char *p = addr;
	p[0] = value;
	p[1] = value >> 8;
	p[2] = value >> 16;
	p[3] = value >> 24;
}
