			G S H H S

Global Self-consistant Hierarchical High-resolution Shorelines

Version 1.2 May 18, 1999

Made programs POSIX.1 compliant and added binary open for DOS.

Version 1.1, April 30, 1996

Paul Wessel, G&G, SOEST, U of Hawaii (wessel@soest.hawaii.edu)
Walter H. F. Smith, NOAA Geosciences Lab (walter.hf.smith@noaa.gov)

Ref: Wessel, P., and W. H. F. Smith, 1996, A global self-consistent,
        hierarchical, high-resolution shoreline database, J. Geophys.
        Res., 101, 8741-8743.

For details on data processing etc. we refer you to that reference.

--------------------------------------------------------------------
This README file explains the usage of the gshhs data sets.  The
archive consists of the following files (after you unzip the compressed
files using gzip -d):

Name		Content
--------------------------------------------------------------------
README		This file
gshhs.h		Header file for programs
gshhs.c		Program to extract ASCII data
gshhs_dp.c	Program to decimate polygons
gshhs_f.b	Full resolution data
gshhs_h.b	High resolution data
gshhs_i.b	Intermediate resolution data
gshhs_l.b	Low resolution data
gshhs_c.b	Crude resolution data

In addition, the following program was supplied by Simon Cox (simon@ned.dem.csiro.au)
and can be used to import the *.b files into a GRASS database:

gshhstograss.c	Import *.b into GRASS GIS database

All the *.b file share the same file structure; thus the gshhs program can
read and extract data from any of the files.  The program's purpose is
simply to demonstrate how a programmer may access the data.  Presumably,
the user wants to access the data from within his/her own programs.
If plotting the data is the only purpose, we strongly recommend you
instead use the GMT package which comes with the same data and tools for
plotting filled landmasses, coastlines, political borders, and rivers.
	The file(s) contain several successive logical blocks of the form

<polygon header>
<polygon points>

Each header consist of the following variables:

int id;				/* Unique polygon id number, starting at 0 */
int n;				/* Number of points in this polygon */
int level;			/* 1 land, 2 lake, 3 island_in_lake, 4 pond_in_island_in_lake */
int west, east, south, north;	/* min/max extent in micro-degrees */
int area;			/* Area of polygon in 1/10 km^2 */
short int greenwich;		/* Greenwich is 1 if Greenwich is crossed */
short int source;		/* 0 = CIA WDBII, 1 = WVS */

Here, int is 4-byte integers and short means 2-byte integers.

The polygon points are stored as n successive records of the form

int	x;	/* longitude of a point in micro-degrees */
int	y;	/* latitude of a point in micro-degrees */

On some systems, the byte order is swapped relative to the order used on
a Sun workstation (on which the current data were processed).  To
determine if you need to swap the byte pairs, do the following test:

1. Compile gshhs
2. Run gshhs gshhs_c.b | head -1	# This shows the 1st line of output
3. If the output does not look exactly like the next line:

P      0    1240 1 W 79793839.900 -17.53378 190.32600 -34.83044  77.71625

   you most likely need to swap the byte-pairs.  Simply recompile gsggs with
  the switch -DFLIP and see if that did the trick.
4. If all fails you may email one of the authors for advice.

Compile the two programs as follows (with or without the -DFLIP switch):

cc -O gshhs.c -o gshhs [-DFLIP]
cc -O gshhs_dp.c -o gshhs_dp -lm [-DFLIP]

[and optionally cc -O -o gshhstograss gshhstograss.c -lm [-DFLIP]]

We have provided 5 different resolution of the data which should
satisfy just about any user.  The [h,i,l,c]-versions were
derived from the gshhs_f.b full resolution file using the
Douglas-Peucker algorithm as implemented in gshhs_dp.c  The
tolerances used were:

File		Content			Tolerance
-------------------------------------------------
gshhs_h.c	High resolution		0.2 km
gshhs_i.c	Interm. resolution	1.0 km
gshhs_l.c	Low resolution		5.0 km
gshhs_c.c	Crude resolution	25  km

However, should you need to decimate the full data set using a
different tolerance you can use the program gshhs_dp to do so:

gshhs_dp gshhs_f.b your_tolerance_in_km newfile.b

gshhs.c can then read the resulting newfile.b
[Note that output from gshhs_dp WILL NOT need byte swapping since
it is created on your machine].

The Douglas-Peucker routine implemented in gshhs_dp was kindly
provided by Dr. Gary J. Robinson, Environmental Systems Science Centre,
University of Reading, Reading, UK (gazza@mail.nerc-nutis.ac.uk).

Good Luck,
Paul Wessel and Walter. H. F. Smith

GMT URL:   http://gmt.soest.hawaii.edu/
