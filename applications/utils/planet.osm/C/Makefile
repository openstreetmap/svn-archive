MYSQL_CFLAGS += -g -O2 -Wall
MYSQL_CFLAGS += $(shell mysql_config --cflags)

MYSQL_LDFLAGS += $(shell mysql_config --libs)

PGSQL_CFLAGS += $(shell pkg-config --cflags libpqxx)
PGSQL_LDFLAGS += $(shell pkg-config --libs libpqxx)

.PHONY: all clean

all: planet05 planet06 UTF8Sanitizer planet06_pg

clean:
	rm -f UTF8Sanitizer planet05 planet06 planet06_pg *.o

planet05: planet05.c keyvals.c
	$(CC) $(MYSQL_CFLAGS) $(MYSQL_LDFLAGS) -o $@ $^

planet06: planet06.c keyvals.c
	$(CC) $(MYSQL_CFLAGS) $(MYSQL_LDFLAGS) -o $@ $^

UTF8Sanitizer: UTF8sanitizer.c
	$(CC) UTF8sanitizer.c -o UTF8Sanitizer

planet06_pg: planet06_pg.o users.o keyvals.o output_osm.o
	$(CXX) -o $@ $^ $(PGSQL_LDFLAGS)

%.o: %.cpp
	$(CXX) $(PGSQL_CFLAGS) -o $@ -c $<
