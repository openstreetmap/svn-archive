#LINKOPT	=	/usr/local/lib/libdbmalloc.a
LINKOPT = -lshp
CFLAGS	= -g -O2 -Wall

all:	2AND

2AND:	2AND.c osm.o
	$(CC) $(CFLAGS) 2AND.c osm.o $(LINKOPT) -o 2AND

clean:
	rm -f *.o 2AND
