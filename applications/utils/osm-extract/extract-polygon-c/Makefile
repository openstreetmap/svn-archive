PROG=extract-polygon
CC=gcc
CFLAGS=-Wall -std=c99 -pedantic -g -I /usr/include/libxml2
# Disable debuging
CFLAGS+=-D NDEBUG
CCVERS=$(shell $(CC) -dumpversion)
ifeq ($(word 1, $(sort 4.1.3 $(CCVERS))),4.1.3)
    CFLAGS+=-fgnu89-inline
endif

LDLIBS=-lxml2 -lbz2

.PHONY: all clean

all: $(PROG)

$(PROG): $(patsubst %.c,%.o,$(wildcard *.c))
	$(CC) $^ $(LOADLIBES) $(LDLIBS) -o $@

clean:
	rm -f *.o $(PROG)
