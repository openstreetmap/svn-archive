CXX=g++
CC=gcc
BISON=bison
FLEX=flex
SRCDIR:=$(shell pwd)
INC=-I../sirf_driver/include/ \
	-I../sirf_driver/include/Packets/Output/ \
	-I../sirf_driver/include/Packets/Input/
OPTFLAGS?=-g -ggdb
CFLAGS?=$(OPTFLAGS)
CXXFLAGS?=$(OPTFLAGS)
LDFLAGS?=$(OPTFLAGS)
SOURCES= $(wildcard *.cpp)
OBJECTS:=$(SOURCES:.cpp=.o)

all: sirf2gpx

clean:
	rm -f sirf2gpx Makefile.dep *.o \

veryclean: clean
	find `pwd` -name "*.o" -exec rm -f \{\} \;

.PHONY: all clean veryclean

# include dependancy makefile
ifneq ($(NODEP),yes)
include Makefile.dep
endif

sirf2gpx: $(OBJECTS)
	$(CXX) $(LDFLAGS) -o $@ $^ -L../sirf_driver -lSiRF -lncurses -lpthread

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(INC) -o $@ -c $<

Makefile.dep: $(SOURCES)
	$(CXX) -MM -MG $(CXXFLAGS) $(INC) -E $^ > $@

