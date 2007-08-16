#LINKOPT	=	/usr/local/lib/libdbmalloc.a
LINKOPT = -lshp
CFLAGS	= -g -O2 -Wall

all:	2AND

2AND:	2AND.c osm.o tags.o rb.o segments.o nodes.o ways.o
	$(CC) $(CFLAGS) 2AND.c osm.o tags.o rb.o segments.o nodes.o ways.o $(LINKOPT) -o 2AND

clean:
	rm -f *.o 2AND
