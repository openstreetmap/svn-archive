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
SIRF_LIB:=../sirf_driver/libSiRF.so
SIRF_LIB_DIR:=$(shell pwd)/../sirf_driver

all: sirf2gpx

clean:
	rm -f sirf2gpx Makefile.dep *.o \

veryclean: clean
	find `pwd` -name "*.o" -exec rm -f \{\} \;

.PHONY: all clean veryclean sirf_lib

# include dependancy makefile
ifneq ($(NODEP),yes)
include Makefile.dep
endif

sirf2gpx: $(OBJECTS) $(SIRF_LIB)
	$(CXX) $(LDFLAGS) -o $@ $(OBJECTS) -L../sirf_driver -Wl,-rpath=$(SIRF_LIB_DIR) -lSiRF -lncurses -lpthread

$(SIRF_LIB):
	$(MAKE) -C ../sirf_driver

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(INC) -o $@ -c $<

Makefile.dep: $(SOURCES) $(SIRF_LIB)
	$(CXX) -MM -MG $(CXXFLAGS) $(INC) -E $(SOURCES) > $@

