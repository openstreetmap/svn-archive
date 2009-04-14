CXXFLAGS = -g -fPIC
OBJ = OSRef.o LatLng.o util.o
TESTOBJ = test.o
CXX = g++

all: libccoord.a libccoord.so

libccoord.a: $(OBJ)
	ar rv libccoord.a $(OBJ) 

libccoord.so: $(OBJ)
	$(CXX) -shared -o libccoord.so $(OBJ)

test: $(TESTOBJ) libccoord.a
	g++ -o test $(TESTOBJ) libccoord.a

clean:
	rm -f *.o *.a

OSRef.o : OSRef.h LatLng.h util.h RefEll.h
LatLng.o : OSRef.h LatLng.h util.h RefEll.h
util.o : util.h

