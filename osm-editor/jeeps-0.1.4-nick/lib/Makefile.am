## Process this file with automake to produce Makefile.in

lib_LTLIBRARIES = libjeeps.la


GPSSRC = gpsserial.c gpssend.c gpsread.c gpsutil.c gpsapp.c gpsprot.c \
	 gpscom.c gpsfmt.c gpsmath.c gpsmem.c gpsrqst.c gpsinput.c \
	 gpsproj.c gpsnmea.c gpsnmeafmt.c gpsnmeaget.c

noinst_HEADERS = gps.h gpsserial.h gpssend.h gpsread.h gpsutil.h gpsapp.h \
		 gpsprot.h gpscom.h gpsfmt.h gpsmath.h gpsmem.h gpsrqst.h \
		 gpsinput.h gpsdatum.h gpsport.h gpsproj.h gpsnmea.h \
		 gpsnmeafmt.h gpsnmeaget.h

libjeeps_la_SOURCES = $(GPSSRC)
libjeeps_la_LDFLAGS = -version-info 1:0:1
