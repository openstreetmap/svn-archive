CXX=g++
CC=gcc
BISON=bison
FLEX=flex
SRCDIR:=$(shell pwd)
#INC=-I$(SRCDIR)/include  \
#	-I$(SRCDIR)/include/Packets/Input  \
#	-I$(SRCDIR)/include/Packets/Output
INC=-Iinclude/ -Iinclude/Packets/Output/
OPTFLAGS?=-g -ggdb
CFLAGS?=$(OPTFLAGS)
CXXFLAGS?=$(OPTFLAGS)
LDFLAGS?=$(OPTFLAGS)
#PKT_FILES:=$(wildcard data/Packets/Input/*.pkt data/Packets/Output/*.pkt)
PKT_FILES:=$(wildcard data/Packets/Output/*.pkt)
SOURCES:= $(wildcard src/*.cpp) $(wildcard src/*.c) \
	$(patsubst data/%.pkt,src/%.cpp,$(PKT_FILES))
OBJECTS:=$(subst .c,.o,$(SOURCES:.cpp=.o))

all: sirfdump

clean:
	rm -f sirfdump Makefile.dep src/*.o \
	 $(patsubst data/%.pkt,include/%.hpp,$(PKT_FILES)) \
	 $(patsubst data/%.pkt,src/%.cpp,$(PKT_FILES))

.PHONY: all clean

# include dependancy makefile
ifneq ($(NODEP),yes)
include Makefile.dep
endif

sirfdump: $(OBJECTS)
	$(CXX) $(LDFLAGS) -o $@ $^

utils/mk_packet: utils/packet.o utils/mk_packet.tab.o utils/lex.yy.o
	$(CC) $(LDFLAGS) -o $@ $^

%.tab.c: %.y
	$(BISON) -dv $<

lex.yy.c: mk_packet.l
	$(FLEX) $<

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(INC) -o $@ -c $<

%.o: %.c
	$(CC) $(CFLAGS) $(INC) -o $@ -c $<

# rule to make packet headers from packet files
include/%.hpp: data/%.pkt utils/mk_packet
	utils/mk_packet -h $@ $<

src/%.cpp: data/%.pkt include/%.hpp utils/mk_packet
	utils/mk_packet -c $@ $<

Makefile.dep: $(SOURCES)
	$(CXX) -MM $(CXXFLAGS) $(INC) -E $^ > $@

