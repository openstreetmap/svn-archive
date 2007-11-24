#LINKOPT	=	/usr/local/lib/libdbmalloc.a
LINKOPT = -lshp -lm
CFLAGS	= -g -O2 -Wall

all:	2AND mknodedb

# Use linking of pthread is that TCmalloc can be used, which makes it runnable on machines <1GB
2AND:	2AND.o osm.o tags.o rb.o nodes.o ways.o relations.o
	$(CC) $(CFLAGS) 2AND.o osm.o tags.o rb.o nodes.o ways.o relations.o $(LINKOPT) -lpthread -o 2AND

clean:
	rm -f *.o 2AND

mknodedb: mknodedb.o rb.o
	$(CC) $(CFLAGS) mknodedb.o rb.o $(LINKOPT) -lpthread -o $@

# Added by running "gcc -MM *.c" and putting the result here
2AND.o: 2AND.c osm.h shapefil.h ways.h relations.h nodes.h tags.h
dbfopen.o: dbfopen.c shapefil.h
mknodedb.o: mknodedb.c rb.h shapefil.h
nodes.o: nodes.c rb.h osm.h shapefil.h tags.h nodes.h
osm.o: osm.c osm.h shapefil.h ways.h relations.h nodes.h tags.h 2AND.h
rb.o: rb.c rb.h
relations.o: relations.c ways.h osm.h shapefil.h rb.h relations.h
shpopen.o: shpopen.c shapefil.h
shptree.o: shptree.c shapefil.h
tags.o: tags.c rb.h osm.h shapefil.h tags.h
ways.o: ways.c ways.h osm.h shapefil.h relations.h tags.h rb.h
