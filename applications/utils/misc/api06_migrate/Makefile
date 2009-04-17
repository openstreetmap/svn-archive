# -*- Makefile -*-

CXX=g++
CXXFLAGS=-g -ggdb
#CXXFLAGS=-O2 -march=native -mtune=native
INCLUDES=-I$(HOME)/include -I$(HOME)/include/mysql++ -I/usr/include/mysql 
LIBS=-L$(HOME)/lib -liconv -lmysqlclient -lmysqlpp -lpqxx -lboost_thread -lboost_program_options -lboost_regex -lboost_date_time
MAKEDEPEND=$(CXX) -M $(CXXFLAGS) $(INCLUDES) -o $*.d $<

SRCS=\
	copy_table.cpp \
	split_tags.cpp \
	dumpfile.cpp \
	dump_row.cpp \
	dump_add_version.cpp \
	dump_add_version_tags.cpp \
	dump_uniq_tag.cpp \
	dump_add_version_wr.cpp \
	dump_add_sequence.cpp
OBJS=$(SRCS:.cpp=.o)
DEPS=$(SRCS:.cpp=.d)

copy_table: $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LIBS)

test_tags: split_tags.o test_tags.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LIBS)

changeset_synth: changeset_synth.o
	$(CXX) $(CXXFLAGS) -o $@ $^ $(LIBS)

%.o: %.cpp
	$(MAKEDEPEND)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $@ -c $< 

clean:
	rm -f $(OBJS) $(DEPS) test_tags copy_table

-include $(DEPS)
