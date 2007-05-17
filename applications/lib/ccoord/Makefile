OBJ = OSRef.o LatLng.o util.o
TESTOBJ = test.o
CXX = g++

libccoord.a: $(OBJ)
	ar rv libccoord.a $(OBJ) 

test: $(TESTOBJ) libccoord.a
	g++ -o test $(TESTOBJ) libccoord.a

clean:
	rm -f *.{o,a}

OSRef.o : OSRef.h LatLng.h util.h RefEll.h
LatLng.o : OSRef.h LatLng.h util.h RefEll.h
util.o : util.h

