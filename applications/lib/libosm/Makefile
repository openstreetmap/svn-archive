CXXFLAGS = -g -I/usr/local/include -I../
LDFLAGS = -L/usr/local/lib -lexpat -lcurl -lshp -lccoord -L../ccoord/
OBJ = Object.o Way.o Parser.o Components.o functions.o llgr.o FeaturesParser.o FeatureClassification.o Node.o
NETOBJ = Client.o
TESTOBJ = test1.o
RULESTESTOBJ = rulestest.o
CXX = g++

all: libosm.a libosm.so libosmnet.a libosmnet.so

libosm.a: $(OBJ)
	ar rv libosm.a $(OBJ) 

libosm.so: $(OBJ)
	$(CXX) -shared -o libosm.so $(OBJ)

libosmnet.a: $(NETOBJ)
	ar rv libosmnet.a $(NETOBJ) 

libosmnet.so: $(NETOBJ)
	$(CXX) -shared -o libosmnet.so $(NETOBJ)

test: test1 test2 test3 rulestest

test1: $(TESTOBJ) libosm.a libosmnet.a
	$(CXX) -o test1 $(TESTOBJ) libosm.a libosmnet.a $(LDFLAGS)

rulestest: $(RULESTESTOBJ) libosm.a 
	$(CXX) -o rulestest $(RULESTESTOBJ) libosm.a $(LDFLAGS)

test2: test2.o libosm.a libosmnet.a 
	$(CXX) -o test2 test2.o libosm.a libosmnet.a $(LDFLAGS)

test3: test3.o libosm.a libosmnet.a 
	$(CXX) -o test3 test3.o libosm.a libosmnet.a $(LDFLAGS)

clean:
	rm -f *.{o,a,so}

test1.o test2.o test3.o rulestest.o: Parser.h Way.h Object.h Client.h 

install:
	cp *.h /usr/local/include
	cp *.a /usr/local/lib
	cp *.so /usr/local/lib
