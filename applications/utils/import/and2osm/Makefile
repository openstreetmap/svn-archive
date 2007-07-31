CFLAGS	= -g -O2 -Wall

all:	2AND

shpopen.o:	shpopen.c shapefil.h
	$(CC) $(CFLAGS) -c shpopen.c

shptree.o:	shptree.c shapefil.h
	$(CC) $(CFLAGS) -c shptree.c

dbfopen.o:	dbfopen.c shapefil.h
	$(CC) $(CFLAGS) -c dbfopen.c

osm.o:	osm.c osm.h 2AND.h
	$(CC) $(CFLAGS) -c osm.c 

2AND.o:	2AND.c osm.h shapefil.h
	$(CC) $(CFLAGS) -c 2AND.c

2AND:	2AND.o osm.o shpopen.o dbfopen.o shptree.o
	$(CC) $(CFLAGS) 2AND.o osm.o dbfopen.o shpopen.o shptree.o -o 2AND

clean:
	rm -f 2AND *.o
