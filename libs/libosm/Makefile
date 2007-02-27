CXXFLAGS = -I/usr/local/include -I../ccoord
LDFLAGS = -L/usr/local/lib -lexpat -lcurl -lshp -lccoord
OBJ = Object.o Way.o Parser.o Components.o functions.o llgr.o FeaturesParser.o
NETOBJ = Client.o
TESTOBJ = test.o
RULESTESTOBJ = rulestest.o
CXX = g++

libosm.a: $(OBJ)
	ar rv libosm.a $(OBJ) 

libosmnet.a: $(NETOBJ)
	ar rv libosmnet.a $(NETOBJ) 

test: $(TESTOBJ) libosm.a libosmnet.a
	$(CXX) -o test $(TESTOBJ) libosm.a libosmnet.a $(LDFLAGS)

rulestest: $(RULESTESTOBJ) libosm.a 
	$(CXX) -o rulestest $(RULESTESTOBJ) libosm.a $(LDFLAGS)

test2: test2.o libosm.a 
	$(CXX) -o test2 test2.o libosm.a libosmnet.a $(LDFLAGS)

clean:
	rm -f *.{o,a}

Object.o : Object.h
Way.o: Way.h Object.h
Parser.o : Parser.h Way.h Object.h Node.h Segment.h
Client.o : Client.h
llgr.o: llgr.h
FeaturesParser.o : FeaturesParser.h FeatureClassification.h 

install:
	cp *.h /usr/local/include
	cp *.a /usr/local/lib
