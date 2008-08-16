PACKAGE:=coast2shp
VERSION:=0.12
SVN:=$(shell date +%Y%m%d)

CC=gcc
#PROFILE=-pg

CFLAGS += -g -O2 -Wall -Wextra -std=c99 $(PROFILE)
CFLAGS += $(shell xml2-config --cflags)
CFLAGS += '-DVERSION="$(VERSION)-$(SVN)"'

LDFLAGS-coast2shp += -g $(shell xml2-config --libs) 
LDFLAGS-coast2shp += -lbz2 -lproj 
LDFLAGS-osm2coast += $(shell xml2-config --libs) -lbz2
LDFLAGS += -lshp -lpthread -lm $(PROFILE)

ifneq ($(wildcard /usr/local/lib/libtcmalloc.so),)
  LDFLAGS-coast2shp += -ltcmalloc
endif

SRCS:=$(wildcard *.c)
OBJS:=$(SRCS:.c=.o)
DEPS:=$(SRCS:.c=.d)

APPS:=coast2shp closeshp osm2coast

.PHONY: all clean $(PACKAGE).spec

all: $(APPS)

clean: 
	rm -f $(APPS) $(OBJS) $(DEPS)
	rm -f $(PACKAGE)-*.tar.bz2

%.d: %.c
	@set -e; rm -f $@; \
	$(CC) -MM $(CFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$

-include $(DEPS)

coast2shp: coast2shp.o input.o keyvals.o rb.o
	$(CC) -o $@ $^ $(LDFLAGS-coast2shp) $(LDFLAGS)

closeshp: closeshp.o 
	$(CC) -o $@ $^ $(LDFLAGS)
	
osm2coast: osm2coast.o input.o
	$(CC) -o $@ $^ $(LDFLAGS-osm2coast) $(LDFLAGS)

%.o: %.c
	$(CC) -c $(CFLAGS) -o $@ $<

$(PACKAGE).spec: $(PACKAGE).spec.in
	sed -e "s/@PACKAGE@/$(PACKAGE)/g; s/@VERSION@/$(VERSION)/g; s/@SVN@/$(SVN)/g;" $^ > $@

$(PACKAGE)-$(VERSION)-$(SVN).tar.bz2: $(PACKAGE).spec
	rm -fR tmp
	mkdir -p tmp/coast2shp
	cp -p Makefile *.[ch] *.cpp readme.txt coast2shp.spec* coast2shp-svn.sh tmp/coast2shp
	cp -p coast2shp.spec tmp/
	tar cjf $@ -C tmp .
	rm -fR tmp

rpm: $(PACKAGE)-$(VERSION)-$(SVN).tar.bz2
	rpmbuild -ta $^

closeshp.o: closeshp.c
coast2shp.o: coast2shp.c osmtypes.h keyvals.h input.h rb.h
input.o: input.c input.h
keyvals.o: keyvals.c keyvals.h
osm2coast.o: osm2coast.c input.h
rb.o: rb.c rb.h
