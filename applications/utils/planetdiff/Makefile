CFLAGS += -O2 -g -Wall -D_LARGEFILE64_SOURCE
CFLAGS += $(shell xml2-config --cflags)
LDFLAGS += $(shell xml2-config --libs) 
LDFLAGS += -lbz2

.PHONY: all clean

all: planetdiff
	ln -f planetdiff planetpatch 

planetdiff: planetdiff.o keyvals.o UTF8sanitizer.o input.o 

clean:
	rm -f planetdiff planetpatch planetdiff.o keyvals.o UTF8sanitizer.o input.o 
