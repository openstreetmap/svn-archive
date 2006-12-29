CXXFLAGS = -I/home/nick/expat 
LDFLAGS = -L/home/nick/expat/lib -lexpat -lcurl 
OBJ = Object.o Way.o Parser.o Components.o functions.o
NETOBJ = Client.o
TESTOBJ = test.o
CXX = g++

libosm.a: $(OBJ)
	ar rv libosm.a $(OBJ) 

libosmnet.a: $(NETOBJ)
	ar rv libosmnet.a $(NETOBJ) 

test: $(TESTOBJ) libosm.a libosmnet.a
	$(CXX) -o test $(TESTOBJ) libosm.a libosmnet.a $(LDFLAGS)

Object.o : Object.h
Way.o: Way.h Object.h
Parser.o : Parser.h Way.h Object.h Node.h Segment.h
Client.o : Client.h
