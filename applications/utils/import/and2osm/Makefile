#LINKOPT	=	/usr/local/lib/libdbmalloc.a
LINKOPT = -lshp -lm
CFLAGS	= -g -O2 -Wall

all:	2AND mknodedb

# Use linking of pthread is that TCmalloc can be used, which makes it runnable on machines <1GB
2AND:	2AND.o osm.o tags.o rb.o segments.o nodes.o ways.o
	$(CC) $(CFLAGS) 2AND.c osm.o tags.o rb.o segments.o nodes.o ways.o $(LINKOPT) -lpthread -o 2AND

clean:
	rm -f *.o 2AND

mknodedb: mknodedb.o rb.o
	$(CC) $(CFLAGS) mknodedb.o rb.o $(LINKOPT) -lpthread -o $@
