# gpx-import/src/Makefile
#
# GPX Import tool for OpenStreetMap
#
# Copyright 2008 Daniel Silverstone <dsilvers@digital-scurf.org>
#

all: gpx-import

DB := mysql

OPT := -O0
CFLAGS := $(OPT) -g -Wall -std=c99 -D_POSIX_SOURCE -D_BSD_SOURCE -D_XOPEN_SOURCE -include log.h

# Comment this out to get the DEBUG log lines
CFLAGS += -DNDEBUG

# Get dep files
CFLAGS += -MD

CFLAGS += $(shell gdlib-config --cflags)
LDFLAGS += $(shell gdlib-config --libs) -lgd 

ifeq ($(DB),mysql)
CFLAGS += $(shell mysql_config --cflags)
LDFLAGS += $(shell mysql_config --libs)
endif

ifeq ($(DB),postgres)
CFLAGS += -I$(shell pg_config --includedir)
LDFLAGS += -L$(shell pg_config --libdir) -lpq
endif

MAINOBJS := main.o gpx.o mercator.o image.o log.o db.o filename.o interpolate.o quadtile.o

ALLOBJS := $(DB).o $(MAINOBJS)

gpx-import: $(ALLOBJS)
	gcc $(LDFLAGS) -o $@ $^ -lexpat -larchive -lz -lbz2

clean:
	$(RM) *.o gpx-import *.d

distclean: clean
	$(RM) *~

-include $(patsubst %.o,%.d,$(ALLOBJS))
