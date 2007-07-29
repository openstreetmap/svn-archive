CFLAGS	= -g -O2 -Wall

all:	2AND

shpopen.o:	shpopen.c shapefil.h
	$(CC) $(CFLAGS) -c shpopen.c

dbfopen.o:	dbfopen.c shapefil.h
	$(CC) $(CFLAGS) -c dbfopen.c

osm.o:	osm.h 2AND.h
	$(CC) $(CFLAGS) -c osm.c 

2AND.o:	osm.h shapefil.h
	$(CC) $(CFLAGS) -c osm.c 

2AND:	2AND.o osm.o shpopen.o dbfopen.o
	$(CC) $(CFLAGS) 2AND.c osm.o dbfopen.o shpopen.o -o 2AND

clean:
	rm -f 2AND *.o
