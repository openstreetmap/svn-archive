# Simple makefile for jeeps, avoiding all that autoconf/automake stuff.
# I know autoconf/automake is the "correct" way to do it, for portability
# to other versions of Unix, but there are only so many hours in the week,
# I just don't have time to learn autoconf/automake right now! :-)
#
# Nick 09/04/05

OBJ = gpsapp.o gpscom.o gpsfmt.o gpsinput.o gpsmath.o gpsmem.o gpsnmea.o \
	  gpsnmeafmt.o gpsnmeaget.o gpsproj.o gpsprot.o gpsread.o gpsrqst.o \
	  gpssend.o gpsserial.o gpsutil.o 

LIB = libjeeps.a 

INSTROOT = /usr/local

$(LIB) : $(OBJ)
	ar rv $(LIB)  $(OBJ)

install:
	cp $(LIB) $(INSTROOT)/lib
	cp *.h $(INSTROOT)/include

clean:
	rm -f *.o $(LIB) *~
